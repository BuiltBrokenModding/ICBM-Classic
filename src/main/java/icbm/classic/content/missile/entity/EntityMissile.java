package icbm.classic.content.missile.entity;

import icbm.classic.ICBMClassic;
import icbm.classic.ICBMConstants;
import icbm.classic.api.ICBMClassicAPI;
import icbm.classic.api.caps.IEMPReceiver;
import icbm.classic.api.events.MissileEvent;
import icbm.classic.api.events.MissileRideEvent;
import icbm.classic.client.ICBMSounds;
import icbm.classic.config.missile.ConfigMissile;
import icbm.classic.content.missile.logic.flight.ArcFlightLogic;
import icbm.classic.content.missile.logic.flight.DeadFlightLogic;
import icbm.classic.lib.CalculationHelpers;
import icbm.classic.lib.capability.chicken.CapSpaceChicken;
import icbm.classic.lib.capability.emp.CapabilityEMP;
import icbm.classic.lib.network.IPacket;
import icbm.classic.lib.network.IPacketIDReceiver;
import icbm.classic.lib.network.lambda.PacketCodexReg;
import icbm.classic.lib.network.lambda.entity.PacketCodexEntity;
import icbm.classic.lib.radar.RadarRegistry;
import icbm.classic.lib.saving.NbtSaveHandler;
import icbm.classic.lib.saving.NbtSaveNode;
import icbm.classic.lib.projectile.EntityProjectile;
import io.netty.buffer.ByteBuf;
import lombok.Getter;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.passive.ChickenEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.*;
import net.minecraft.util.Direction;
import net.minecraft.util.math.*;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fml.common.registry.IEntityAdditionalSpawnData;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashSet;
import java.util.Optional;

/**
 * Created by Robin Seifert on 12/12/2021.
 */
public abstract class EntityMissile<E extends EntityMissile<E>> extends EntityProjectile<E> implements IEntityAdditionalSpawnData, IPacketIDReceiver
{
    // Generic shared missile data
    private final HashSet<Entity> collisionIgnoreList = new HashSet<Entity>();

    @Getter
    private final CapabilityMissile missileCapability = new CapabilityMissile(this); //TODO refactor to use interface so parts can be better customized

    @Getter
    private final IEMPReceiver empCapability = new CapabilityEmpMissile(getMissileCapability());

    /** Toggle to note the missile has impacted something and already triggered impact logic */
    protected boolean hasImpacted = false;

    protected boolean syncClient = false;

    public EntityMissile(EntityType<E> type, World world)
    {
        super(type, world);
        this.hasHealth = true;
    }

    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull final Capability<T> cap, final @Nullable Direction side)
    {
        if (cap == CapabilityEMP.EMP)
        {
            return LazyOptional.of(this::getEmpCapability).cast();
        }
        else if (cap == ICBMClassicAPI.MISSILE_CAPABILITY)
        {
            return LazyOptional.of(this::getMissileCapability).cast();
        }
        return super.getCapability(cap, side);
    }

    public EntityMissile<E> ignore(Entity entity)
    {
        collisionIgnoreList.add(entity);
        return this;
    }

    @Override
    public void tick() {
        super.tick();

        if(syncClient) {
            this.syncClient = false;
            PACKET_DESC.sendToAllAround(this);
        }
    }

    @Override
    protected void updateMotion()
    {
        if (getMissileCapability().canRunFlightLogic())
        {
            Optional.ofNullable(getMissileCapability().getFlightLogic()).ifPresent(logic -> {
                logic.onEntityTick(this, missileCapability, ticksInAir);

                if(logic.shouldRunEngineEffects(this)) {
                    //TODO ICBMClassic.proxy.spawnMissileSmoke(this, logic, ticksInAir);
                    ICBMSounds.MISSILE_ENGINE.play(world, posX, posY, posZ, Math.min(1, ticksInAir / 40F), (1.0F + CalculationHelpers.randFloatRange(this.world.rand, 0.2F)) * 0.7F, true);
                }
            });

            //Trigger events
            //ICBMClassicAPI.EX_MISSILE_REGISTRY.triggerFlightUpdate(getMissileCapability());
        }

        super.updateMotion();
    }

    @Override
    protected void decreaseMotion()
    {
        if (getMissileCapability().getFlightLogic() == null || getMissileCapability().getFlightLogic().shouldDecreaseMotion(this))
        {
            super.decreaseMotion();
        }
    }

    @Override
    protected void handleEntityCollision(EntityRayTraceResult hit)
    {
        if(hit.getEntity() instanceof ChickenEntity) { //TODO baby zombie for lolz?
            if(getRidingEntity() == null) {
                ICBMSounds.MEEP.play(hit.getEntity(), 2, 1, true);
                hit.getEntity().startRiding(this, true);

                hit.getEntity().getCapability(CapSpaceChicken.INSTANCE).ifPresent((cap) -> {
                    cap.setSpace(true);
                });
            }
        }
        else
        {
            onImpactEntity(hit.getEntity(), (float) getMotion().length(), hit);
        }
    }

    /**
     * Checks to see if an entity is touching the missile. If so, blow up!
     */
    @Override
    public AxisAlignedBB getCollisionBox(Entity entity)
    {
        if (collisionIgnoreList.contains(entity))
        {
            return null;
        }
        return super.getBoundingBox();
    }

    @Override
    public void remove()
    {
        if (!world.isRemote)
        {
            RadarRegistry.remove(this);
        }

        super.remove();
    }

    @Override
    public boolean canBeCollidedWith()
    {
        return true;
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public AxisAlignedBB getRenderBoundingBox()
    {
        //TODO handle this with pose to reduce size to angle
        return this.getBoundingBox().expand(5, 5, 5);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public boolean isInRangeToRenderDist(double distance)
    {
        double d0 = this.getBoundingBox().getAverageEdgeLength() * 10.0D;

        if (Double.isNaN(d0))
        {
            d0 = 1.0D;
        }

        d0 = d0 * 64.0D * getRenderDistanceWeight();
        return distance < d0 * d0;
    }

    @Override
    public boolean shouldAlignWithMotion() {
        //Clearing default logic to flight controllers can handle motion
        return Optional.ofNullable(missileCapability.getFlightLogic()).map((logic) -> logic.shouldAlignWithMotion(this)).orElse(false);
    }

    @Override
    public boolean processInitialInteract(@Nonnull PlayerEntity player, @Nonnull Hand hand)
    {
        //Handle player riding missile
        if (!this.world.isRemote && (this.getRidingEntity() == null || this.getRidingEntity() == player) && !MinecraftForge.EVENT_BUS.post(new MissileRideEvent.Start(getMissileCapability(), player)))
        {
            player.startRiding(this);
            return true;
        }

        return false;
    }

    @Override
    public double getMountedYOffset()
    {
        if (this.ticksInAir <= 0 && getMissileCapability().getFlightLogic() instanceof ArcFlightLogic) //TODO abstract or find better way to handle seat position
        {
            return getHeight();
        } else if (getMissileCapability().getFlightLogic() instanceof DeadFlightLogic)
        {
            return getHeight() / 10;
        }

        return getHeight() / 2 + getMotion().y;
    }

    @Override
    protected boolean ignoreImpact(RayTraceResult hit)
    {
        return MinecraftForge.EVENT_BUS.post(new MissileEvent.PreImpact(getMissileCapability(), this, hit));
    }

    @Override
    protected void postImpact(RayTraceResult hit)
    {
        MinecraftForge.EVENT_BUS.post(new MissileEvent.PostImpact(getMissileCapability(), this, hit));
    }

    @Override
    protected boolean shouldCollideWith(Entity entityHit)
    {
        return super.shouldCollideWith(entityHit) && !isRider(entityHit) && entityHit != shootingEntity;
    }

    public boolean isRider(Entity entity) {
        return entity != null && (entity.getRidingEntity() == this || isRider(entity.getRidingEntity()));
    }

    public boolean hasPlayerRiding() {
        return hasPlayerRiding(this);
    }

    public static boolean hasPlayerRiding(Entity entity) {
        return entity.getPassengers().stream().anyMatch(e -> e instanceof PlayerEntity || hasPlayerRiding(e));
    }

    @Override
    protected final void onImpact(RayTraceResult impactLocation) {
        if(!hasImpacted) {
            this.hasImpacted = true; //TODO store impact information and move this to projectile
            logImpact(impactLocation.getHitVec());
            actionOnImpact(impactLocation);
        }
    }

    protected void actionOnImpact(RayTraceResult impactLocation) {
        this.destroy();
    }

    protected void logImpact(Vec3d impactLocation)
    {
        // TODO make optional via config
        // TODO log to ICBM file separated from main config
        // TODO offer hook for database logging
        final String formatString = "Missile[%s] (%sx, %sy, %sz, %sd) impacted at (%s, %s, %s)";
        final String formattedMessage = String.format(formatString,
            this.getEntityId(),
            xi(),
            yi(),
            zi(),
            world().getDimension().getType().getId(),
            impactLocation.x,
            impactLocation.y,
            impactLocation.z
        );
        ICBMClassic.logger().info(formattedMessage);
    }

    @Override
    protected float getImpactDamage(Entity entityHit, float velocity, RayTraceResult hit) {
        if(ConfigMissile.DAMAGE_LIMIT >= 0) {
            return Math.min(MathHelper.ceil(velocity * ConfigMissile.DAMAGE_SCALE), ConfigMissile.DAMAGE_LIMIT);
        }
        return MathHelper.ceil(velocity * ConfigMissile.DAMAGE_SCALE);
    }

    @Override
    public boolean read(ByteBuf buf, int id, PlayerEntity player, IPacket type) {
        if(id == 1) {
           //readSpawnData(buf); TODO
            return true;
        }
        return false;
    }

    @Override
    public void writeSpawnData(PacketBuffer additionalMissileData)
    {
        super.writeSpawnData(additionalMissileData);
        final CompoundNBT saveData = SAVE_LOGIC.save(this, new CompoundNBT());
        additionalMissileData.writeCompoundTag(saveData);
    }

    @Override
    public void readSpawnData(PacketBuffer additionalMissileData)
    {
        super.readSpawnData(additionalMissileData);
        final CompoundNBT saveData = additionalMissileData.readCompoundTag();
        SAVE_LOGIC.load(this, saveData);
    }

    @Override
    public void readAdditional(CompoundNBT nbt)
    {
        super.readAdditional(nbt);
        SAVE_LOGIC.load(this, nbt);
    }

    @Override
    public void writeAdditional(CompoundNBT nbt)
    {
        super.writeAdditional(nbt);
        SAVE_LOGIC.save(this, nbt);
    }

    private static final NbtSaveHandler<EntityMissile> SAVE_LOGIC = new NbtSaveHandler<EntityMissile>()
        .mainRoot()
        /* */.node(new NbtSaveNode<EntityMissile, CompoundNBT>("missile",
            (missile) -> missile.getMissileCapability().serializeNBT(),
            (missile, data) -> missile.getMissileCapability().deserializeNBT(data)
        ))
        .base();

    public static final PacketCodexEntity<EntityMissile, EntityMissile> PACKET_DESC = (PacketCodexEntity<EntityMissile, EntityMissile>) new PacketCodexEntity<EntityMissile, EntityMissile>(new ResourceLocation(ICBMConstants.DOMAIN, "missile"), "description")
            .nodeNbtCompound(SAVE_LOGIC::save, SAVE_LOGIC::load);

    static  {
        PacketCodexReg.register(PACKET_DESC);
    }

    public abstract ItemStack toStack();
}

package icbm.classic.world.missile.entity;

import icbm.classic.ICBMClassic;
import icbm.classic.IcbmConstants;
import icbm.classic.api.ICBMClassicAPI;
import icbm.classic.api.caps.IEMPReceiver;
import icbm.classic.api.events.MissileEvent;
import icbm.classic.api.events.MissileRideEvent;
import icbm.classic.client.ICBMSounds;
import icbm.classic.config.missile.ConfigMissile;
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
import icbm.classic.prefab.entity.IcbmProjectile;
import icbm.classic.world.missile.logic.flight.BallisticFlightLogicOld;
import icbm.classic.world.missile.logic.flight.DeadFlightLogic;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.passive.EntityChicken;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.common.MinecraftForge;
import net.neoforged.common.capabilities.Capability;
import net.neoforged.fml.common.network.ByteBufUtils;
import net.neoforged.fml.common.registry.IEntityAdditionalSpawnData;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashSet;
import java.util.Optional;

/**
 * Created by Robin Seifert on 12/12/2021.
 */
public abstract class EntityMissile<E extends EntityMissile<E>> extends IcbmProjectile<E> implements IEntityAdditionalSpawnData, IPacketIDReceiver {
    // Generic shared missile data
    private final HashSet<Entity> collisionIgnoreList = new HashSet<Entity>();

    private CapabilityMissile missileCapability; //TODO refactor to use interface so parts can be better customized
    private final IEMPReceiver empCapability = new CapabilityEmpMissile(getMissileCapability());

    /**
     * Toggle to note the missile has impacted something and already triggered impact logic
     */
    protected boolean hasImpacted = false;

    protected boolean syncClient = false;

    public EntityMissile(EntityType<? extends EntityMissile<?>> type, Level level) {
        super(type, level);
        this.hasHealth = true;
    }

    @Override
    public float getMaxHealth() {
        return ConfigMissile.TIER_1_HEALTH;
    }

    @Override
    public <T> T getCapability(Capability<T> capability, @Nullable Direction facing) {
        if (capability == CapabilityEMP.EMP) {
            return (T) getEmpCapability();
        } else if (capability == ICBMClassicAPI.MISSILE_CAPABILITY) {
            return (T) getMissileCapability();
        }
        return super.getCapability(capability, facing);
    }

    @Override
    public boolean hasCapability(Capability<?> capability, @Nullable Direction facing) {
        return capability == CapabilityEMP.EMP
            || capability == ICBMClassicAPI.MISSILE_CAPABILITY
            || super.hasCapability(capability, facing);
    }

    public CapabilityMissile getMissileCapability() {
        if (missileCapability == null) {
            missileCapability = new CapabilityMissile(this);
        }
        return missileCapability;
    }

    public IEMPReceiver getEmpCapability() {
        return empCapability;
    }

    public EntityMissile ignore(Entity entity) {
        collisionIgnoreList.add(entity);
        return this;
    }

    @Override
    public void onUpdate() {
        super.onUpdate();

        if (syncClient) {
            this.syncClient = false;
            PACKET_DESC.sendToAllAround(this);
        }
    }

    @Override
    protected void updateMotion() {
        if (getMissileCapability().canRunFlightLogic()) {
            Optional.ofNullable(getMissileCapability().getFlightLogic()).ifPresent(logic -> {
                logic.onEntityTick(this, missileCapability, ticksInAir);

                if (logic.shouldRunEngineEffects(this)) {
                    ICBMClassic.proxy.spawnMissileSmoke(this, logic, ticksInAir);
                    ICBMSounds.MISSILE_ENGINE.play(world, posX, posY, posZ, Math.min(1, ticksInAir / 40F), (1.0F + CalculationHelpers.randFloatRange(this.world.rand, 0.2F)) * 0.7F, true);
                }
            });

            //Trigger events
            ICBMClassicAPI.EX_MISSILE_REGISTRY.triggerFlightUpdate(getMissileCapability());
        }

        super.updateMotion();
    }

    @Override
    protected void decreaseMotion() {
        if (getMissileCapability().getFlightLogic() == null || getMissileCapability().getFlightLogic().shouldDecreaseMotion(this)) {
            super.decreaseMotion();
        }
    }

    @Override
    protected void handleEntityCollision(RayTraceResult hit, Entity entityHit) {
        if (entityHit instanceof EntityChicken) { //TODO baby zombie for lolz?
            if (getRidingEntity() == null) {
                ICBMSounds.MEEP.play(entityHit, 2, 1, true);
                entityHit.startRiding(this, true);
                if (entityHit.hasCapability(CapSpaceChicken.INSTANCE, null)) {
                    final CapSpaceChicken cap = entityHit.getCapability(CapSpaceChicken.INSTANCE, null);
                    if (cap != null) {
                        cap.setSpace(true);
                    }
                }
            }
        } else {
            onImpactEntity(entityHit, (float) getVelocity().magnitude(), hit);
        }
    }

    /**
     * Checks to see if an entity is touching the missile. If so, blow up!
     */
    @Override
    public AxisAlignedBB getCollisionBox(Entity entity) {
        if (collisionIgnoreList.contains(entity)) {
            return null;
        }
        return getEntityBoundingBox();
    }

    @Override
    public void setDead() {
        if (!world.isClientSide()) {
            RadarRegistry.remove(this);
        }

        super.setDead();
    }

    @Override
    public boolean canBeCollidedWith() {
        return true;
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public AxisAlignedBB getRenderBoundingBox() {
        return this.getEntityBoundingBox().expand(5, 5, 5);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public boolean isInRangeToRenderDist(double distance) {
        double d0 = this.getEntityBoundingBox().getAverageEdgeLength() * 10.0D;

        if (Double.isNaN(d0)) {
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
    public boolean processInitialInteract(@Nonnull Player player, @Nonnull InteractionHand hand) {
        //Handle player riding missile
        if (!this.world.isClientSide() && (this.getRidingEntity() == null || this.getRidingEntity() == player) && !MinecraftForge.EVENT_BUS.post(new MissileRideEvent.Start(getMissileCapability(), player))) {
            player.startRiding(this);
            return true;
        }

        return false;
    }

    @Override
    public double getMountedYOffset() {
        if (this.ticksInAir <= 0 && getMissileCapability().getFlightLogic() instanceof BallisticFlightLogicOld) //TODO abstract or find better way to handle seat position
        {
            return height;
        } else if (getMissileCapability().getFlightLogic() instanceof DeadFlightLogic) {
            return height / 10;
        }

        return height / 2 + motionY;
    }

    @Override
    protected boolean ignoreImpact(RayTraceResult hit) {
        return MinecraftForge.EVENT_BUS.post(new MissileEvent.PreImpact(getMissileCapability(), this, hit));
    }

    @Override
    protected void postImpact(RayTraceResult hit) {
        MinecraftForge.EVENT_BUS.post(new MissileEvent.PostImpact(getMissileCapability(), this, hit));
    }

    @Override
    protected boolean shouldCollideWith(Entity entityHit) {
        return super.shouldCollideWith(entityHit) && !isRider(entityHit) && entityHit != shootingEntity;
    }

    public boolean isRider(Entity entity) {
        return entity != null && (entity.getRidingEntity() == this || isRider(entity.getRidingEntity()));
    }

    public boolean hasPlayerRiding() {
        return hasPlayerRiding(this);
    }

    public static boolean hasPlayerRiding(Entity entity) {
        return entity.getPassengers().stream().anyMatch(e -> e instanceof Player || hasPlayerRiding(e));
    }

    @Override
    protected void onImpact(Vec3 impactLocation) {
        if (!hasImpacted) {
            this.hasImpacted = true;
            logImpact(impactLocation);
            dismountRidingEntity();
            removePassengers();
            setDead();
        }
    }

    protected void logImpact(Vec3 impactLocation) {
        // TODO make optional via config
        // TODO log to ICBM file separated from main config
        // TODO offer hook for database logging
        final String formatString = "Missile[%s] (%sx, %sy, %sz, %sd) impacted at (%s, %s, %s)";
        final String formattedMessage = String.format(formatString,
            this.getEntityId(),
            xi(),
            yi(),
            zi(),
            world().provider.getDimension(),
            impactLocation.x,
            impactLocation.y,
            impactLocation.z
        );
        ICBMClassic.logger().info(formattedMessage);
    }

    @Override
    public boolean read(ByteBuf buf, int id, Player player, IPacket type) {
        if (id == 1) {
            readSpawnData(buf);
            return true;
        }
        return false;
    }

    @Override
    public void writeSpawnData(ByteBuf additionalMissileData) {
        final CompoundTag saveData = SAVE_LOGIC.save(this, new CompoundTag());
        ByteBufUtils.writeTag(additionalMissileData, saveData);
    }

    @Override
    public void readSpawnData(ByteBuf additionalMissileData) {
        final CompoundTag saveData = ByteBufUtils.readTag(additionalMissileData);
        SAVE_LOGIC.load(this, saveData);
    }

    /**
     * (abstract) Protected helper method to read subclass entity additionalMissileData from NBT.
     */
    @Override
    public void readEntityFromNBT(CompoundTag nbt) {
        super.readEntityFromNBT(nbt);
        SAVE_LOGIC.load(this, nbt);
    }

    /**
     * (abstract) Protected helper method to write subclass entity additionalMissileData to NBT.
     */
    @Override
    public void writeEntityToNBT(CompoundTag nbt) {
        super.writeEntityToNBT(nbt);
        SAVE_LOGIC.save(this, nbt);
    }

    private static final NbtSaveHandler<EntityMissile> SAVE_LOGIC = new NbtSaveHandler<EntityMissile>()
        .mainRoot()
        /* */.node(new NbtSaveNode<EntityMissile, CompoundTag>("missile",
            (missile) -> missile.getMissileCapability().serializeNBT(),
            (missile, data) -> missile.getMissileCapability().deserializeNBT(data)
        ))
        .base();

    public static final PacketCodexEntity<EntityMissile, EntityMissile> PACKET_DESC = (PacketCodexEntity<EntityMissile, EntityMissile>) new PacketCodexEntity<EntityMissile, EntityMissile>(new ResourceLocation(IcbmConstants.MOD_ID, "missile"), "description")
        .nodeNbtCompound(SAVE_LOGIC::save, SAVE_LOGIC::load);

    static {
        PacketCodexReg.register(PACKET_DESC);
    }

    public abstract ItemStack toStack();
}

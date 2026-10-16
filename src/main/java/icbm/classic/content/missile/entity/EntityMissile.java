package icbm.classic.content.missile.entity;

import icbm.classic.ICBMClassic;
import icbm.classic.ICBMConstants;
import icbm.classic.api.ICBMClassicAPI;
import icbm.classic.api.caps.IEMPReceiver;
import icbm.classic.api.events.MissileEvent;
import icbm.classic.api.events.MissileRideEvent;
import icbm.classic.client.ICBMSounds;
import icbm.classic.config.missile.ConfigMissile;
import icbm.classic.content.missile.logic.flight.BallisticFlightLogicOld;
import icbm.classic.content.missile.logic.flight.DeadFlightLogic;
import icbm.classic.lib.CalculationHelpers;
import icbm.classic.lib.RegistryHelper;
import icbm.classic.lib.capability.chicken.CapSpaceChicken;
import icbm.classic.lib.capability.holder.ICapabilityHolderEntity;
import icbm.classic.lib.network.IPacket;
import icbm.classic.lib.network.IPacketIDReceiver;
import icbm.classic.lib.network.lambda.PacketCodexReg;
import icbm.classic.lib.network.lambda.entity.PacketCodexEntity;
import icbm.classic.lib.projectile.EntityProjectile;
import icbm.classic.lib.radar.RadarRegistry;
import icbm.classic.lib.saving.NbtSaveHandler;
import icbm.classic.lib.saving.NbtSaveNode;
import io.netty.buffer.ByteBuf;
import lombok.Getter;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Chicken;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.capabilities.EntityCapability;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.entity.IEntityWithComplexSpawn;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashSet;
import java.util.Optional;

/**
 * Created by Robin Seifert on 12/12/2021.
 */
public abstract class EntityMissile<E extends EntityMissile<E>> extends EntityProjectile<E> implements IEntityWithComplexSpawn, IPacketIDReceiver, ICapabilityHolderEntity
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

    public EntityMissile(EntityType<? extends EntityMissile> type, Level world)
    {
        super(type, world);
        this.hasHealth = true;
    }

    @Nullable
    @Override
    public <T> T getEntityCapability(EntityCapability<T, Void> capability)
    {
        if (capability == ICBMClassicAPI.EMP_ENTITY_CAPABILITY)
        {
            return (T) getEmpCapability();
        } else if (capability == ICBMClassicAPI.MISSILE_CAPABILITY)
        {
            return (T) getMissileCapability();
        }
        return null;
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
            if(!level().isClientSide()) {
                PACKET_DESC.sendToAllAround(this);
            }
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
                    ICBMClassic.proxy.spawnMissileSmoke(this, logic, ticksInAir);
                    // PORT: 1.12 also played this on the server, which sent a sound packet every tick on top of the client's own sound
                    if (level().isClientSide()) {
                        ICBMSounds.MISSILE_ENGINE.play(level(), getX(), getY(), getZ(), Math.min(1, ticksInAir / 40F), (1.0F + CalculationHelpers.randFloatRange(this.level().random, 0.2F)) * 0.7F, true);
                    }
                }
            });

            //Trigger events
            ICBMClassicAPI.EX_MISSILE_REGISTRY.triggerFlightUpdate(getMissileCapability());
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
    protected void handleEntityCollision(@Nonnull HitResult hit, @Nonnull Entity entityHit)
    {
        if(entityHit instanceof Chicken chicken) { //TODO baby zombie for lolz?
            if(getVehicle() == null) {
                ICBMSounds.MEEP.play(entityHit, 2, 1, true);
                entityHit.startRiding(this, true);
                CapSpaceChicken.setSpace(chicken, true);
            }
        }
        else
        {
            onImpactEntity(entityHit, (float) getVelocity().magnitude(), hit);
        }
    }

    /**
     * PORT: 1.12 getCollisionBox(entity) made the missile solid to other entities except those in the ignore list.
     * 1.21 asks the moving entity, so the ignore list only applies to the missile moving into others.
     */
    @Override
    public boolean canBeCollidedWith()
    {
        return true;
    }

    @Override
    public boolean canCollideWith(Entity entity)
    {
        return !collisionIgnoreList.contains(entity) && super.canCollideWith(entity);
    }

    /**
     * PORT: 1.12 only added the missile to the radar map on launch. Missiles in flight that are loaded
     * back from a saved chunk were invisible to radar stations and SAMs until relaunched.
     */
    @Override
    public void onAddedToLevel()
    {
        super.onAddedToLevel();
        if (!level().isClientSide() && getMissileCapability().canRunFlightLogic())
        {
            RadarRegistry.add(this);
        }
    }

    @Override
    public void remove(RemovalReason reason)
    {
        if (!level().isClientSide())
        {
            RadarRegistry.remove(this);
        }

        super.remove(reason);
    }

    /** 1.12 canBeCollidedWith, allows ray traces and attacks to hit the missile */
    @Override
    public boolean isPickable()
    {
        return true;
    }

    @Override
    public AABB getBoundingBoxForCulling()
    {
        // PORT: 1.12 AABB#expand only grew towards positive values
        return this.getBoundingBox().expandTowards(5, 5, 5);
    }

    @Override
    public boolean shouldRenderAtSqrDistance(double distance)
    {
        double d0 = this.getBoundingBox().getSize() * 10.0D;

        if (Double.isNaN(d0))
        {
            d0 = 1.0D;
        }

        d0 = d0 * 64.0D * getViewScale();
        return distance < d0 * d0;
    }

    @Override
    public boolean shouldAlignWithMotion() {
        //Clearing default logic to flight controllers can handle motion
        return Optional.ofNullable(missileCapability.getFlightLogic()).map((logic) -> logic.shouldAlignWithMotion(this)).orElse(false);
    }

    @Override
    public InteractionResult interact(@Nonnull Player player, @Nonnull InteractionHand hand)
    {
        //Handle player riding missile
        if (!this.level().isClientSide() && (this.getVehicle() == null || this.getVehicle() == player) && !NeoForge.EVENT_BUS.post(new MissileRideEvent.Start(getMissileCapability(), player)).isCanceled())
        {
            player.startRiding(this);
            return InteractionResult.SUCCESS;
        }

        return InteractionResult.PASS;
    }

    /** 1.12 getMountedYOffset */
    public double getMountedYOffset()
    {
        if (this.ticksInAir <= 0 && getMissileCapability().getFlightLogic() instanceof BallisticFlightLogicOld) //TODO abstract or find better way to handle seat position
        {
            return getBbHeight();
        } else if (getMissileCapability().getFlightLogic() instanceof DeadFlightLogic)
        {
            return getBbHeight() / 10;
        }

        return getBbHeight() / 2 + getDeltaMovement().y;
    }

    @Override
    protected Vec3 getPassengerAttachmentPoint(Entity passenger, EntityDimensions dimensions, float scale)
    {
        // PORT: replaces getMountedYOffset, rider offsets (1.12 Entity#getYOffset) now come from the rider's vehicle attachment
        return new Vec3(0, getMountedYOffset(), 0);
    }

    @Override
    protected boolean ignoreImpact(HitResult hit)
    {
        return NeoForge.EVENT_BUS.post(new MissileEvent.PreImpact(getMissileCapability(), this, hit)).isCanceled();
    }

    @Override
    protected void postImpact(HitResult hit)
    {
        NeoForge.EVENT_BUS.post(new MissileEvent.PostImpact(getMissileCapability(), this, hit));
    }

    @Override
    protected boolean shouldCollideWith(Entity entityHit)
    {
        return super.shouldCollideWith(entityHit) && !isRider(entityHit) && entityHit != shootingEntity;
    }

    public boolean isRider(Entity entity) {
        return entity != null && (entity.getVehicle() == this || isRider(entity.getVehicle()));
    }

    public boolean hasPlayerRiding() {
        return hasPlayerRiding(this);
    }

    public static boolean hasPlayerRiding(Entity entity) {
        return entity.getPassengers().stream().anyMatch(e -> e instanceof Player || hasPlayerRiding(e));
    }

    @Override
    protected final void onImpact(@Nonnull HitResult impactLocation) {
        if(!hasImpacted) {
            this.hasImpacted = true; //TODO store impact information and move this to projectile
            logImpact(impactLocation.getLocation());
            actionOnImpact(impactLocation);
        }
    }

    protected void actionOnImpact(HitResult impactLocation) {
        this.destroy();
    }

    protected void logImpact(Vec3 impactLocation)
    {
        // TODO make optional via config
        // TODO log to ICBM file separated from main config
        // TODO offer hook for database logging
        final String formatString = "Missile[%s] (%sx, %sy, %sz, %sd) impacted at (%s, %s, %s)";
        final String formattedMessage = String.format(formatString,
            this.getId(),
            xi(),
            yi(),
            zi(),
            level().dimension().location(),
            impactLocation.x,
            impactLocation.y,
            impactLocation.z
        );
        ICBMClassic.logger().info(formattedMessage);
    }

    @Override
    protected float getImpactDamage(Entity entityHit, float velocity, HitResult hit) {
        if(ConfigMissile.DAMAGE_LIMIT >= 0) {
            return Math.min(Mth.ceil(velocity * ConfigMissile.DAMAGE_SCALE), ConfigMissile.DAMAGE_LIMIT);
        }
        return Mth.ceil(velocity * ConfigMissile.DAMAGE_SCALE);
    }

    @Override
    public boolean read(ByteBuf buf, int id, Player player, IPacket type) {
        if(id == 1 && buf instanceof RegistryFriendlyByteBuf registryBuf) {
            readSpawnData(registryBuf);
            return true;
        }
        return false;
    }

    @Override
    public void writeSpawnData(RegistryFriendlyByteBuf additionalMissileData)
    {
        super.writeSpawnData(additionalMissileData);
        final CompoundTag saveData = SAVE_LOGIC.save(this, new CompoundTag());
        additionalMissileData.writeNbt(saveData);
    }

    @Override
    public void readSpawnData(RegistryFriendlyByteBuf additionalMissileData)
    {
        super.readSpawnData(additionalMissileData);
        final CompoundTag saveData = additionalMissileData.readNbt();
        if (saveData != null)
        {
            SAVE_LOGIC.load(this, saveData);
        }
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag nbt)
    {
        super.readAdditionalSaveData(nbt);
        SAVE_LOGIC.load(this, nbt);
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag nbt)
    {
        super.addAdditionalSaveData(nbt);
        SAVE_LOGIC.save(this, nbt);
    }

    private static final NbtSaveHandler<EntityMissile> SAVE_LOGIC = new NbtSaveHandler<EntityMissile>()
        .mainRoot()
        /* */.node(new NbtSaveNode<EntityMissile, CompoundTag>("missile",
            (missile) -> missile.getMissileCapability().serializeNBT(RegistryHelper.access()),
            (missile, data) -> missile.getMissileCapability().deserializeNBT(RegistryHelper.access(), data)
        ))
        .base();

    public static final PacketCodexEntity<EntityMissile, EntityMissile> PACKET_DESC = (PacketCodexEntity<EntityMissile, EntityMissile>) new PacketCodexEntity<EntityMissile, EntityMissile>(ResourceLocation.fromNamespaceAndPath(ICBMConstants.DOMAIN, "missile"), "description")
            .fromServer()
            .nodeNbtCompound(SAVE_LOGIC::save, SAVE_LOGIC::load);

    private static boolean registered = false;

    /** PORT: 1.12 registered in a static block, now called from the mod constructor so codex ids match on both sides */
    public static void register()
    {
        if (!registered)
        {
            registered = true;
            PacketCodexReg.register(PACKET_DESC);
        }
    }

    public abstract ItemStack toStack();
}
// PORTED

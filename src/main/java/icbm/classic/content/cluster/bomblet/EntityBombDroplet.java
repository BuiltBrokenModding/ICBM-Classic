package icbm.classic.content.cluster.bomblet;

import icbm.classic.api.ICBMClassicAPI;
import icbm.classic.api.reg.IExplosiveData;
import icbm.classic.config.missile.ConfigMissile;
import icbm.classic.content.blast.ICBMDamageTypes;
import icbm.classic.content.missile.logic.source.ActionSource;
import icbm.classic.content.missile.logic.source.cause.EntityCause;
import icbm.classic.content.reg.EntityReg;
import icbm.classic.lib.capability.ex.CapabilityExplosiveEntity;
import icbm.classic.lib.capability.holder.ICapabilityHolderEntity;
import icbm.classic.lib.projectile.EntityProjectile;
import icbm.classic.lib.saving.NbtSaveHandler;
import icbm.classic.lib.saving.NbtSaveNode;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.capabilities.EntityCapability;
import net.neoforged.neoforge.entity.IEntityWithComplexSpawn;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class EntityBombDroplet extends EntityProjectile<EntityBombDroplet> implements IEntityWithComplexSpawn, ICapabilityHolderEntity {

    public final CapabilityExplosiveEntity explosive = new CapabilityExplosiveEntity(this);

    public EntityBombDroplet(EntityType<? extends EntityBombDroplet> type, Level world) {
        super(type, world);
        this.hasHealth = false;
    }

    public EntityBombDroplet(Level world) {
        this(EntityReg.BOMB_DROPLET.get(), world);
    }

    @Override
    protected float getImpactDamage(Entity entityHit, float velocity, HitResult hit) {
        return Mth.ceil(velocity * ConfigMissile.bomblet.impactDamage);
    }

    @Override
    protected DamageSource getImpactDamageSource(Entity entityHit, float velocity, HitResult hit) {
        // PORT: 1.12 static DamageSource("icbmclassic:bomblet") without attacker, now a data driven damage type
        return ICBMDamageTypes.source(level(), ICBMDamageTypes.BOMBLET, this, null);
    }

    @Override
    protected void onImpact(@Nonnull HitResult hit) {
       super.onImpact(hit);
       explosive.doExplosion(hit.getLocation().x, hit.getLocation().y, hit.getLocation().z, new ActionSource(level(), new Vec3(getX(), getY(), getZ()), new EntityCause(this))); //TODO include impact cause info
    }

    // PORT: 1.12 overrode Entity#getLook; Entity#getViewVector is final in 1.21, so this is kept under its own name
    public Vec3 getMotionLook(float partialTicks)
    {
        final Vec3 motion = getDeltaMovement();
        double mag = Math.sqrt(motion.x * motion.x + motion.y * motion.y + motion.z * motion.z);
        return new Vec3(motion.x / mag, motion.y / mag, motion.z / mag);
    }

    @Override
    protected boolean shouldCollideWith(Entity entity)
    {
        return super.shouldCollideWith(entity) && !(entity instanceof EntityBombDroplet); //TODO ignore collision only for first few ticks
    }

    @Override
    public float getMaxHealth()
    {
        return Math.max(1, ConfigMissile.bomblet.health);
    }

    @Override
    protected void onDestroyedBy(DamageSource source, float damage) {
        super.onDestroyedBy(source, damage);
        // TODO add config
        // TODO add random chance modifier
        if (source.is(DamageTypeTags.IS_EXPLOSION) || source.is(DamageTypeTags.IS_FIRE)) {
            explosive.doExplosion(getX(), getY(), getZ(), new ActionSource(level(), new Vec3(getX(), getY(), getZ()), new EntityCause(this))); //TODO include source of damage
        }
    }

    @Nullable
    @Override
    public <T> T getEntityCapability(EntityCapability<T, Void> capability)
    {
        if(capability == ICBMClassicAPI.EXPLOSIVE_ENTITY_CAPABILITY) {
            return (T) explosive;
        }
        return null;
    }

    /** PORT: 1.12 getName() used client I18n, translated through the component instead */
    @Override
    protected Component getTypeName()
    {
        final IExplosiveData data = explosive.getExplosiveData();
        // PORT: 1.12 bomb.droplet.<key>.name keys only existed in old .lang files, en_us has bomblet.<key>
        if (data != null)
        {
            return Component.translatable("bomblet." + data.getRegistryKey().toString());
        }
        return super.getTypeName();
    }

    @Override
    public boolean shouldRenderAtSqrDistance(double distance)
    {
        final AABB box = this.getBoundingBox();
        double d0 = box.getSize() * 10.0D;

        if (Double.isNaN(d0))
        {
            d0 = 1.0D;
        }

        d0 = d0 * 64.0D * getViewScale();
        return distance < d0 * d0;
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

    private static final NbtSaveHandler<EntityBombDroplet> SAVE_LOGIC = new NbtSaveHandler<EntityBombDroplet>()
        .mainRoot()
        /* */.node(new NbtSaveNode<EntityBombDroplet, CompoundTag>("explosive",
            (missile) -> missile.explosive.serializeNBT(),
            (missile, data) -> missile.explosive.deserializeNBT(data))
        )
        .base();

    public ItemStack toStack() {
        return explosive.toStack();
    }
}
// PORTED

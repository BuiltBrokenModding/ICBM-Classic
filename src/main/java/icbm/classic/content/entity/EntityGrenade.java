package icbm.classic.content.entity;

import icbm.classic.api.ICBMClassicAPI;
import icbm.classic.api.reg.IExplosiveData;
import icbm.classic.content.missile.logic.source.ActionSource;
import icbm.classic.content.missile.logic.source.cause.EntityCause;
import icbm.classic.content.reg.EntityReg;
import icbm.classic.lib.NBTConstants;
import icbm.classic.lib.capability.ex.CapabilityExplosiveEntity;
import icbm.classic.lib.capability.holder.ICapabilityHolderEntity;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.capabilities.EntityCapability;
import net.neoforged.neoforge.entity.IEntityWithComplexSpawn;

import javax.annotation.Nullable;

public class EntityGrenade extends Entity implements IEntityWithComplexSpawn, ICapabilityHolderEntity
{
    /** Entity that created the grenade and set it into motion */
    private LivingEntity thrower;

    /** Explosive capability */
    public final CapabilityExplosiveEntity explosive = new CapabilityExplosiveEntity(this);

    private ItemStack clientRenderStack;

    public EntityGrenade(EntityType<? extends EntityGrenade> type, Level level)
    {
        super(type, level);
    }

    public EntityGrenade(Level level)
    {
        this(EntityReg.GRENADE.get(), level);
    }

    /**
     * Sets the explosive stack
     *
     * @param stack - explosive stack
     * @return this
     */
    public EntityGrenade setItemStack(ItemStack stack)
    {
        explosive.setStack(stack);
        return this;
    }

    /**
     * Gets the itemStack meant to represent the render
     *
     * @return stack to render
     */
    public ItemStack renderItemStack()
    {
        // Cache to prevent #toStack from bleeding memory creating new instances
        if (clientRenderStack == null)
        {
            clientRenderStack = explosive.toStack();
        }
        return clientRenderStack;
    }

    /**
     * Sets the throwing entity
     *
     * @param thrower - entity that threw the grenade
     * @return this
     */
    public EntityGrenade setThrower(LivingEntity thrower)
    {
        this.thrower = thrower;
        return this;
    }

    public LivingEntity getThrower()
    {
        return thrower;
    }

    /**
     * Sets the aim and position based on the throwing entity
     *
     * @return this
     */
    public EntityGrenade aimFromThrower() //TODO figure out which hand threw the grenade so we can spawn over shoulder
    {
        this.moveTo(thrower.getX(), thrower.getY() + thrower.getEyeHeight(), thrower.getZ(), thrower.getYRot(), thrower.getXRot());

        //Set position
        final float horizontalOffset = 0.16F;
        final double x = getX() - Mth.cos(this.getYRot() / 180.0F * (float) Math.PI) * horizontalOffset;
        final double y = getY() - 0.10000000149011612D;
        final double z = getZ() - Mth.sin(this.getYRot() / 180.0F * (float) Math.PI) * horizontalOffset;
        this.setPos(x, y, z);

        return this;
    }

    /**
     * Spawns the grenade into the game world
     *
     * @return this
     */
    public EntityGrenade spawn()
    {
        level().addFreshEntity(this);
        return this;
    }

    /**
     * Sets the motion of the grenade
     *
     * @param energy - energy to scale the motion
     * @return this
     */
    public EntityGrenade setThrowMotion(float energy)
    {
        //Set velocity
        final float powerScale = 0.4F;
        final double mx = -Mth.sin(this.getYRot() / 180.0F * (float) Math.PI) * Mth.cos(this.getXRot() / 180.0F * (float) Math.PI) * powerScale;
        final double mz = Mth.cos(this.getYRot() / 180.0F * (float) Math.PI) * Mth.cos(this.getXRot() / 180.0F * (float) Math.PI) * powerScale;
        final double my = -Mth.sin((this.getXRot()) / 180.0F * (float) Math.PI) * powerScale;
        this.setDeltaMovement(mx, my, mz);
        this.setThrowableHeading(mx, my, mz, 1.8f * energy, 1.0F); //TODO see what this 1.8 is and change to be 1 * energy
        return this;
    }

    @Override
    public Component getName()
    {
        if (hasCustomName())
        {
            return super.getName();
        }
        final IExplosiveData data = explosive.getExplosiveData();
        // PORT: 1.12 used icbm.grenade.<key> which only existed in old .lang files, en_us has grenade.<key>
        return data != null ? Component.translatable("grenade." + data.getRegistryKey()) : super.getName();
    }

    @Override
    public void writeSpawnData(RegistryFriendlyByteBuf data)
    {
        ItemStack.OPTIONAL_STREAM_CODEC.encode(data, explosive.toStack());
    }

    @Override
    public void readSpawnData(RegistryFriendlyByteBuf data)
    {
        final ItemStack stack = ItemStack.OPTIONAL_STREAM_CODEC.decode(data);
        if (!stack.isEmpty())
        {
            explosive.setStack(stack);
        }
    }

    /**
     * Sets the velocity of the grenade
     *
     * @param vx     - x component velocity vector
     * @param vy     - y component velocity vector
     * @param vz     - z component velocity vector
     * @param scale  - amount to scale the vector by
     * @param random - amount to randomize the vector
     */
    public void setThrowableHeading(double vx, double vy, double vz, float scale, float random)
    {
        //normalize
        final double power = Math.sqrt(vx * vx + vy * vy + vz * vz);
        vx /= power;
        vy /= power;
        vz /= power;

        //Randomize
        vx += this.random.nextGaussian() * 0.007499999832361937D * random;
        vy += this.random.nextGaussian() * 0.007499999832361937D * random;
        vz += this.random.nextGaussian() * 0.007499999832361937D * random;

        //Scale
        vx *= scale;
        vy *= scale;
        vz *= scale;

        //Apply
        setVelocity(vx, vy, vz);
    }

    /** Sets the velocity to the args. Args: x, y, z */
    public void setVelocity(double vx, double vy, double vz)
    {
        this.setDeltaMovement(vx, vy, vz);

        if (this.xRotO == 0.0F && this.yRotO == 0.0F)
        {
            final double horizontal = Math.sqrt(vx * vx + vz * vz);
            this.setYRot((float) (Math.atan2(vx, vz) * 180.0D / Math.PI));
            this.setXRot((float) (Math.atan2(vy, horizontal) * 180.0D / Math.PI));
            this.yRotO = this.getYRot();
            this.xRotO = this.getXRot();
        }
    }

    /** 1.12 setVelocity was also the client handler for velocity packets */
    @Override
    public void lerpMotion(double x, double y, double z)
    {
        setVelocity(x, y, z);
    }

    @Override
    protected Entity.MovementEmission getMovementEmission()
    {
        return Entity.MovementEmission.NONE;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder)
    {
    }

    /** Called to update the entity's position/logic. */
    @Override
    public void tick()
    {
        super.tick();

        this.move(MoverType.SELF, this.getDeltaMovement());

        Vec3 motion = this.getDeltaMovement();
        final double horizontalMag = Math.sqrt(motion.x * motion.x + motion.z * motion.z);
        float yaw = (float) (Math.atan2(motion.x, motion.z) * 180.0D / Math.PI);
        float pitch = (float) (Math.atan2(motion.y, horizontalMag) * 180.0D / Math.PI);

        while (pitch - this.xRotO < -180.0F)
        {
            this.xRotO -= 360.0F;
        }

        while (pitch - this.xRotO >= 180.0F)
        {
            this.xRotO += 360.0F;
        }

        while (yaw - this.yRotO < -180.0F)
        {
            this.yRotO -= 360.0F;
        }

        while (yaw - this.yRotO >= 180.0F)
        {
            this.yRotO += 360.0F;
        }

        this.setXRot(this.xRotO + (pitch - this.xRotO) * 0.2F);
        this.setYRot(this.yRotO + (yaw - this.yRotO) * 0.2F);
        float var17 = 0.98F;
        float gravity = 0.03F;

        if (this.isInWater())
        {
            for (int var7 = 0; var7 < 4; ++var7)
            {
                float var19 = 0.25F;
                this.level().addParticle(ParticleTypes.BUBBLE, this.getX() - motion.x * var19, this.getY() - motion.y * var19, this.getZ() - motion.z * var19, motion.x, motion.y, motion.z);
            }

            var17 = 0.8F;
        }

        motion = motion.scale(var17);

        if (this.onGround())
        {
            motion = motion.scale(0.5);
        }
        else
        {
            motion = motion.add(0, -gravity, 0);
        }
        this.setDeltaMovement(motion);

        tickFuse();
    }

    /** Ticks the fuse */
    protected void tickFuse()
    {
        if (this.tickCount > ICBMClassicAPI.EX_GRENADE_REGISTRY.getFuseTime(this, explosive.getExplosiveData()))
        {
            triggerExplosion();
        }
        else
        {
            ICBMClassicAPI.EX_GRENADE_REGISTRY.tickFuse(this, explosive.getExplosiveData(), tickCount);
            final Vec3 motion = this.getDeltaMovement();
            this.level().addParticle(ParticleTypes.SMOKE,
                this.getX() - motion.x, this.getY() - motion.y + 0.4, this.getZ() - motion.z,
                motion.x, motion.y, motion.z
            );
        }
    }

    /** Triggers the explosion of the grenade */
    protected void triggerExplosion()
    {
        // TODO handle output and record results in event system
        explosive.doExplosion(this.getX(), this.getY() + 0.3f, this.getZ(), new ActionSource(level(), new Vec3(getX(), getY(), getZ()), new EntityCause(this)));

        this.discard();
    }

    @Override
    public boolean isPickable()
    {
        return true;
    }

    @Override
    public boolean isPushable()
    {
        return true;
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag nbt)
    {
        if (nbt.contains(NBTConstants.EXPLOSIVE))
        {
            final ItemStack stack = ItemStack.parseOptional(registryAccess(), nbt.getCompound(NBTConstants.EXPLOSIVE));
            if (!stack.isEmpty())
            {
                explosive.setStack(stack);
            }
        }
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag nbt)
    {
        // PORT: 1.12 stored the stack NBT under the same key, the stack format is the 1.21 one now
        nbt.put(NBTConstants.EXPLOSIVE, explosive.toStack().saveOptional(registryAccess()));
    }

    @Override
    @Nullable
    @SuppressWarnings("unchecked")
    public <T> T getEntityCapability(EntityCapability<T, Void> capability)
    {
        if (capability == ICBMClassicAPI.EXPLOSIVE_ENTITY_CAPABILITY)
        {
            return (T) explosive;
        }
        return null;
    }
}
// PORTED

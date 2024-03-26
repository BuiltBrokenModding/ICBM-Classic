package icbm.classic.world.entity;

import icbm.classic.api.ICBMClassicAPI;
import icbm.classic.lib.NBTConstants;
import icbm.classic.lib.capability.ex.CapabilityExplosiveEntity;
import icbm.classic.lib.explosive.ExplosiveHandler;
import io.netty.buffer.ByteBuf;
import net.minecraft.block.material.Material;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityLivingBase;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.common.capabilities.Capability;
import net.neoforged.fml.common.network.ByteBufUtils;
import net.neoforged.fml.common.registry.IEntityAdditionalSpawnData;

import javax.annotation.Nullable;
import java.util.Optional;

public class GrenadeEntity extends Entity implements IEntityAdditionalSpawnData {
    /**
     * Entity that created the grenade and set it into motion
     */
    private EntityLivingBase thrower;

    /**
     * Explosive capability
     */
    public final CapabilityExplosiveEntity explosive = new CapabilityExplosiveEntity(this);

    public GrenadeEntity(EntityType<?> type, Level level) {
        super(type, level);
        this.setSize(0.25F, 0.25F);
        //this.renderDistanceWeight = 8;
    }

    /**
     * Sets the explosive stack
     *
     * @param stack - explosive stack
     * @return this
     */
    public GrenadeEntity setItemStack(ItemStack stack) {
        explosive.setStack(stack);
        return this;
    }

    /**
     * Sets the throwing entity
     *
     * @param thrower - entity that threw the grenade
     * @return this
     */
    public GrenadeEntity setThrower(EntityLivingBase thrower) {
        this.thrower = thrower;
        return this;
    }

    public EntityLivingBase getThrower() {
        return thrower;
    }

    /**
     * Sets the aim and position based on the throwing entity
     *
     * @return this
     */
    public GrenadeEntity aimFromThrower() //TODO figure out which hand threw the grenade so we can spawn over shoulder
    {
        this.setLocationAndAngles(thrower.getX(), thrower.getY() + thrower.getEyeHeight(), thrower.getZ(), thrower.getYRot(), thrower.getXRot());

        //Set position
        final float horizontalOffset = 0.16F;
        this.getX() -= MathHelper.cos(this.getYRot() / 180.0F * (float) Math.PI) * horizontalOffset;
        this.getY() -= 0.10000000149011612D;
        this.getZ() -= MathHelper.sin(this.getYRot() / 180.0F * (float) Math.PI) * horizontalOffset;
        this.setPosition(this.getX(), this.getY(), this.getZ());

        return this;
    }

    /**
     * Spawns the grenade into the game world
     *
     * @return this
     */
    public GrenadeEntity spawn() {
        world.spawnEntity(this);
        return this;
    }

    /**
     * Sets the motion of the grenade
     *
     * @param energy - energy to scale the motion
     * @return this
     */
    public GrenadeEntity setThrowMotion(float energy) {
        //Set velocity
        final float powerScale = 0.4F;
        this.motionX = -MathHelper.sin(this.getYRot() / 180.0F * (float) Math.PI) * MathHelper.cos(this.getXRot() / 180.0F * (float) Math.PI) * powerScale;
        this.motionZ = MathHelper.cos(this.getYRot() / 180.0F * (float) Math.PI) * MathHelper.cos(this.getXRot() / 180.0F * (float) Math.PI) * powerScale;
        this.motionY = -MathHelper.sin((this.getXRot()) / 180.0F * (float) Math.PI) * powerScale;
        this.setThrowableHeading(this.motionX, this.motionY, this.motionZ, 1.8f * energy, 1.0F); //TODO see what this 1.8 is and change to be 1 * energy
        return this;
    }

    @Override
    public String getName() {
        return "icbm.grenade." + explosive.getExplosiveData().getRegistryName();
    }

    @Override
    public void writeSpawnData(ByteBuf data) {
        ByteBufUtils.writeTag(data, explosive.serializeNBT());
    }

    @Override
    public void readSpawnData(ByteBuf data) {
        explosive.deserializeNBT(Optional.ofNullable(ByteBufUtils.readTag(data)).orElseGet(CompoundTag::new));
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
    public void setThrowableHeading(double vx, double vy, double vz, float scale, float random) {
        //normalize
        float power = MathHelper.sqrt(vx * vx + vy * vy + vz * vz);
        vx /= power;
        vy /= power;
        vz /= power;

        //Randomize
        vx += this.rand.nextGaussian() * 0.007499999832361937D * random;
        vy += this.rand.nextGaussian() * 0.007499999832361937D * random;
        vz += this.rand.nextGaussian() * 0.007499999832361937D * random;

        //Scale
        vx *= scale;
        vy *= scale;
        vz *= scale;

        //Apply
        setVelocity(vx, vy, vz);
    }

    /**
     * Sets the velocity to the args. Args: x, y, z
     */
    @Override
    public void setVelocity(double vx, double vy, double vz) {
        this.motionX = vx;
        this.motionY = vy;
        this.motionZ = vz;

        if (this.prevRotationPitch == 0.0F && this.prevRotationYaw == 0.0F) {
            float var7 = MathHelper.sqrt(vx * vx + vz * vz);
            this.prevRotationYaw = this.getYRot() = (float) (Math.atan2(vx, vz) * 180.0D / Math.PI);
            this.prevRotationPitch = this.getXRot() = (float) (Math.atan2(vy, var7) * 180.0D / Math.PI);
        }
    }

    /**
     * returns if this entity triggers Block.onEntityWalking on the blocks they walk on. used for
     * spiders and wolves to prevent them from trampling crops
     */
    @Override
    protected boolean canTriggerWalking() {
        return false;
    }

    @Override
    protected void entityInit() {
    }

    /**
     * Called to update the entity's position/logic.
     */
    @Override
    public void onUpdate() {
        this.lastTickPosX = this.getX();
        this.lastTickPosY = this.getY();
        this.lastTickPosZ = this.getZ();
        super.onUpdate();

        this.move(MoverType.SELF, this.motionX, this.motionY, this.motionZ);

        final float horizontalMag = MathHelper.sqrt(this.motionX * this.motionX + this.motionZ * this.motionZ);
        this.getYRot() = (float) (Math.atan2(this.motionX, this.motionZ) * 180.0D / Math.PI);

        for (this.getXRot() = (float) (Math.atan2(this.motionY, horizontalMag) * 180.0D / Math.PI); this.getXRot() - this.prevRotationPitch < -180.0F; this.prevRotationPitch -= 360.0F) {
            ;
        }

        while (this.getXRot() - this.prevRotationPitch >= 180.0F) {
            this.prevRotationPitch += 360.0F;
        }

        while (this.getYRot() - this.prevRotationYaw < -180.0F) {
            this.prevRotationYaw -= 360.0F;
        }

        while (this.getYRot() - this.prevRotationYaw >= 180.0F) {
            this.prevRotationYaw += 360.0F;
        }

        this.getXRot() = this.prevRotationPitch + (this.getXRot() - this.prevRotationPitch) * 0.2F;
        this.getYRot() = this.prevRotationYaw + (this.getYRot() - this.prevRotationYaw) * 0.2F;
        float var17 = 0.98F;
        float gravity = 0.03F;

        if (this.isInWater()) {
            for (int var7 = 0; var7 < 4; ++var7) {
                float var19 = 0.25F;
                this.world.spawnParticle(EnumParticleTypes.WATER_BUBBLE, this.getX() - this.motionX * var19, this.getY() - this.motionY * var19, this.getZ() - this.motionZ * var19, this.motionX, this.motionY, this.motionZ);
            }

            var17 = 0.8F;
        }

        this.motionX *= var17;
        this.motionY *= var17;
        this.motionZ *= var17;

        if (this.onGround) {
            this.motionX *= 0.5;
            this.motionZ *= 0.5;
            this.motionY *= 0.5;
        } else {
            this.motionY -= gravity;
            //this.pushOutOfBlocks(this.getX(), (this.boundingBox.minY + this.boundingBox.maxY) / 2.0D, this.getZ());
        }

        tickFuse();
    }

    /**
     * Ticks the fuse
     */
    protected void tickFuse() {
        if (this.ticksExisted > ICBMClassicAPI.EX_GRENADE_REGISTRY.getFuseTime(this, explosive.getExplosiveData().getRegistryID())) {
            triggerExplosion();
        } else {
            ICBMClassicAPI.EX_GRENADE_REGISTRY.tickFuse(this, ticksExisted, explosive.getExplosiveData().getRegistryID());
        }
    }

    /**
     * Triggers the explosion of the grenade
     */
    protected void triggerExplosion() {
        this.world.spawnParticle(EnumParticleTypes.EXPLOSION_HUGE, this.getX(), this.getY(), this.getZ(), 0.0D, 0.0D, 0.0D);
        ExplosiveHandler.createExplosion(this, this.world, this.getX(), this.getY() + 0.3f, this.getZ(), explosive.getExplosiveData().getRegistryID(), 1, explosive.getCustomBlastData());
        this.setDead();
    }

    @Override
    public boolean handleWaterMovement() {
        return this.world.handleMaterialAcceleration(this.getEntityBoundingBox(), Material.WATER, this);
    }

    @Override
    public boolean canBeCollidedWith() {
        return true;
    }

    @Override
    public boolean canBePushed() {
        return true;
    }

    @Override
    protected void readEntityFromNBT(CompoundTag nbt) {
        if (nbt.contains(NBTConstants.EXPLOSIVE)) {
            explosive.deserializeNBT(nbt.getCompound(NBTConstants.EXPLOSIVE));
        }
    }

    @Override
    protected void writeEntityToNBT(CompoundTag nbt) {
        nbt.put(NBTConstants.EXPLOSIVE, explosive.serializeNBT());
    }

    @Override
    public boolean hasCapability(Capability<?> capability, @Nullable Direction facing) {
        return capability == ICBMClassicAPI.EXPLOSIVE_CAPABILITY || super.hasCapability(capability, facing);
    }

    @Override
    @Nullable
    public <T> T getCapability(Capability<T> capability, @Nullable Direction facing) {
        if (capability == ICBMClassicAPI.EXPLOSIVE_CAPABILITY) {
            return ICBMClassicAPI.EXPLOSIVE_CAPABILITY.cast(explosive);
        }
        return super.getCapability(capability, facing);
    }
}
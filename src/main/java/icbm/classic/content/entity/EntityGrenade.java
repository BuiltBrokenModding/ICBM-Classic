package icbm.classic.content.entity;

import icbm.classic.api.actions.IActionData;
import icbm.classic.content.missile.logic.source.cause.EntityCause;
import icbm.classic.lib.actions.PotentialAction;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MoverType;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.IPacket;
import net.minecraft.network.play.server.SSpawnObjectPacket;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.common.util.NonNullSupplier;

@Accessors(chain = true)
public class EntityGrenade extends Entity
{
    private static final int FUSE_TIME = 100; //TODO config

    /** Entity that created the grenade and set it into motion */
    @Getter @Setter
    private LivingEntity thrower;

    /** Explosive capability */
    // TODO public final CapabilityExplosiveEntity explosive = new CapabilityExplosiveEntity(this);

    private final PotentialAction explodeAction = new PotentialAction();
    private final LazyOptional<ItemStack> itemstack;


    public EntityGrenade(EntityType<EntityGrenade> type, World par1World, IActionData actionData, NonNullSupplier<ItemStack> itemstack)
    {
        super(type, par1World);
        this.explodeAction.setActionData(actionData);
        this.itemstack = LazyOptional.of(itemstack);
    }
    /**
     * Gets the itemStack meant to represent the render
     *
     * @return stack to render
     */
    public ItemStack renderItemStack() {
        return itemstack.orElse(ItemStack.EMPTY);
    }

    /**
     * Sets the aim and position based on the throwing entity
     *
     * @return this
     */
    public EntityGrenade aimFromThrower() //TODO figure out which hand threw the grenade so we can spawn over shoulder
    {
        this.setLocationAndAngles(thrower.posX, thrower.posY + thrower.getEyeHeight(), thrower.posZ, thrower.rotationYaw, thrower.rotationPitch);

        //Set position
        final float horizontalOffset = 0.16F;
        this.posX -= MathHelper.cos(this.rotationYaw / 180.0F * (float) Math.PI) * horizontalOffset;
        this.posY -= 0.10000000149011612D;
        this.posZ -= MathHelper.sin(this.rotationYaw / 180.0F * (float) Math.PI) * horizontalOffset;
        this.setPosition(this.posX, this.posY, this.posZ);

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
        this.setMotion(
            -MathHelper.sin(this.rotationYaw / 180.0F * (float) Math.PI) * MathHelper.cos(this.rotationPitch / 180.0F * (float) Math.PI) * powerScale,
            -MathHelper.sin((this.rotationPitch) / 180.0F * (float) Math.PI) * powerScale,
            MathHelper.cos(this.rotationYaw / 180.0F * (float) Math.PI) * MathHelper.cos(this.rotationPitch / 180.0F * (float) Math.PI) * powerScale
            );
        this.setThrowableHeading(this.getMotion().x, this.getMotion().y, this.getMotion().z, 1.8f * energy, 1.0F); //TODO see what this 1.8 is and change to be 1 * energy
        return this;
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

    /** Sets the velocity to the args. Args: x, y, z */
    @Override
    public void setVelocity(double vx, double vy, double vz)
    {
        super.setVelocity(vx, vy, vz);

        if (this.prevRotationPitch == 0.0F && this.prevRotationYaw == 0.0F)
        {
            float var7 = MathHelper.sqrt(vx * vx + vz * vz);
            this.prevRotationYaw = this.rotationYaw = (float) (Math.atan2(vx, vz) * 180.0D / Math.PI);
            this.prevRotationPitch = this.rotationPitch = (float) (Math.atan2(vy, var7) * 180.0D / Math.PI);
        }
    }

    @Override
    public IPacket<?> createSpawnPacket() {
        return new SSpawnObjectPacket(this); //TODO figure out what this is
    }

    /**
     * returns if this entity triggers Block.onEntityWalking on the blocks they walk on. used for
     * spiders and wolves to prevent them from trampling crops
     */
    @Override
    protected boolean canTriggerWalking()
    {
        return false;
    }

    @Override
    protected void registerData()
    {
    }

    /** Called to update the entity's position/logic. */
    @Override
    public void tick()
    {
        this.lastTickPosX = this.posX;
        this.lastTickPosY = this.posY;
        this.lastTickPosZ = this.posZ;
        super.tick();

        this.move(MoverType.SELF, this.getMotion());

        final float horizontalMag = MathHelper.sqrt(this.getMotion().x * this.getMotion().x + this.getMotion().z * this.getMotion().z);
        this.rotationYaw = (float) (Math.atan2(this.getMotion().x, this.getMotion().z) * 180.0D / Math.PI);

        for (this.rotationPitch = (float) (Math.atan2(this.getMotion().y, horizontalMag) * 180.0D / Math.PI); this.rotationPitch - this.prevRotationPitch < -180.0F; this.prevRotationPitch -= 360.0F)
        {
            ;
        }

        while (this.rotationPitch - this.prevRotationPitch >= 180.0F)
        {
            this.prevRotationPitch += 360.0F;
        }

        while (this.rotationYaw - this.prevRotationYaw < -180.0F)
        {
            this.prevRotationYaw -= 360.0F;
        }

        while (this.rotationYaw - this.prevRotationYaw >= 180.0F)
        {
            this.prevRotationYaw += 360.0F;
        }

        this.rotationPitch = this.prevRotationPitch + (this.rotationPitch - this.prevRotationPitch) * 0.2F;
        this.rotationYaw = this.prevRotationYaw + (this.rotationYaw - this.prevRotationYaw) * 0.2F;
        float var17 = 0.98F;
        float gravity = 0.03F;

        if (this.isInWater())
        {
            for (int var7 = 0; var7 < 4; ++var7)
            {
                float var19 = 0.25F;
                this.world.addParticle(ParticleTypes.UNDERWATER,
                    this.posX - this.getMotion().x * var19,
                    this.posY - this.getMotion().y * var19,
                    this.posZ - this.getMotion().z * var19,
                    this.getMotion().x, this.getMotion().y, this.getMotion().z);
            }

            var17 = 0.8F;
        }

        this.setMotion(this.getMotion().scale(var17).add(var17, var17, var17));

        if (this.onGround)
        {
            this.setMotion(this.getMotion().scale(0.5D));
        }
        else
        {
            this.setMotion(this.getMotion().add(0.0D, -gravity, 0.0D));
            //this.pushOutOfBlocks(this.posX, (this.boundingBox.minY + this.boundingBox.maxY) / 2.0D, this.posZ);
        }

        tickFuse();
    }

    /** Ticks the fuse */
    protected void tickFuse()
    {
        if (this.ticksExisted > FUSE_TIME)
        {
            triggerExplosion();
        }
    }

    /** Triggers the explosion of the grenade */
    protected void triggerExplosion()
    {
        this.explodeAction.doAction(world, this.posX, this.posY + 0.3f, this.posZ, new EntityCause(this));
        this.remove();
    }

    @Override
    public boolean canBeCollidedWith()
    {
        return true;
    }

    @Override
    public boolean canBePushed()
    {
        return true;
    }

    @Override
    protected void readAdditional(CompoundNBT compound) {

    }

    @Override
    protected void writeAdditional(CompoundNBT compound) {

    }
}
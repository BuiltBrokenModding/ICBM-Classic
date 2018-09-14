package icbm.classic.content.entity;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import io.netty.buffer.ByteBuf;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.monster.EntityEnderman;
import net.minecraft.init.SoundEvents;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.*;
import net.minecraft.util.math.*;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.IEntityAdditionalSpawnData;

import javax.annotation.Nullable;
import java.util.List;

public class EntityFragments extends Entity implements IEntityAdditionalSpawnData
{
    private static final Predicate<Entity> ARROW_TARGETS = Predicates.and(EntitySelectors.NOT_SPECTATING, EntitySelectors.IS_ALIVE, new Predicate<Entity>()
    {
        public boolean apply(@Nullable Entity p_apply_1_)
        {
            return p_apply_1_.canBeCollidedWith();
        }
    });

    private BlockPos inTilePosition = new BlockPos(0, 0, 0);
    private IBlockState inTile;

    private boolean inGround = false;
    public boolean isExplosive;
    public boolean isAnvil;
    private boolean isExploding = false;

    /** Seems to be some sort of timer for animating an arrow. */
    public int arrowShake = 0;

    /** The owner of this arrow. */
    private int ticksInAir = 0;
    private int damage = 11;

    /** Is this arrow a critical hit? (Controls particles and damage) */
    public boolean arrowCritical = false;
    public float explosionSize = 1.5F;

    public EntityFragments(World par1World)
    {
        super(par1World);
        this.setSize(0.5F, 0.5F);
    }

    public EntityFragments(World par1World, double x, double y, double z, boolean isExplosive, boolean isAnvil)
    {
        super(par1World);
        this.setPosition(x, y, z);
        //this.yOffset = 0.0F;
        this.isExplosive = isExplosive;
        this.isAnvil = isAnvil;

        if (this.isAnvil)
        {
            this.setSize(1, 1);
            this.damage = 30;
        }
        else
        {
            this.setSize(0.5f, 0.5f);
        }
    }

    @Override
    public void writeSpawnData(ByteBuf data)
    {
        data.writeBoolean(this.isExplosive);
        data.writeBoolean(this.isAnvil);
    }

    @Override
    public void readSpawnData(ByteBuf data)
    {
        this.isExplosive = data.readBoolean();
        this.isAnvil = data.readBoolean();
    }

    @Override
    protected void entityInit()
    {
    }

    @Override
    public String getName()
    {
        return "Fragments";
    }

    /**
     * Uses the provided coordinates as a heading and determines the velocity from it with the set
     * force and random variance. Args: x, y, z, force, forceVariation
     */
    public void setArrowHeading(double par1, double par3, double par5, float par7, float par8)
    {
        float var9 = MathHelper.sqrt(par1 * par1 + par3 * par3 + par5 * par5);
        par1 /= var9;
        par3 /= var9;
        par5 /= var9;
        par1 += this.rand.nextGaussian() * 0.007499999832361937D * par8;
        par3 += this.rand.nextGaussian() * 0.007499999832361937D * par8;
        par5 += this.rand.nextGaussian() * 0.007499999832361937D * par8;
        par1 *= par7;
        par3 *= par7;
        par5 *= par7;
        this.motionX = par1;
        this.motionY = par3;
        this.motionZ = par5;
        float var10 = MathHelper.sqrt(par1 * par1 + par5 * par5);
        this.prevRotationYaw = this.rotationYaw = (float) (Math.atan2(par1, par5) * 180.0D / Math.PI);
        this.prevRotationPitch = this.rotationPitch = (float) (Math.atan2(par3, var10) * 180.0D / Math.PI);
    }

    /** Sets the velocity to the args. Args: x, y, z */
    @Override
    public void setVelocity(double par1, double par3, double par5)
    {
        this.motionX = par1;
        this.motionY = par3;
        this.motionZ = par5;

        if (this.prevRotationPitch == 0.0F && this.prevRotationYaw == 0.0F)
        {
            float var7 = MathHelper.sqrt(par1 * par1 + par5 * par5);
            this.prevRotationYaw = this.rotationYaw = (float) (Math.atan2(par1, par5) * 180.0D / Math.PI);
            this.prevRotationPitch = this.rotationPitch = (float) (Math.atan2(par3, var7) * 180.0D / Math.PI);
            this.prevRotationPitch = this.rotationPitch;
            this.prevRotationYaw = this.rotationYaw;
            this.setLocationAndAngles(this.posX, this.posY, this.posZ, this.rotationYaw, this.rotationPitch);
        }
    }

    private void explode()
    {
        if (!this.isExploding && !this.world.isRemote)
        {
            this.isExploding = true;
            this.world.createExplosion(this, posX, posY, posZ, this.explosionSize, true);
            this.setDead();
        }
    }

    /** Called to update the entity's position/logic. */
    //  entity.attackEntityFrom(DamageSource.ANVIL, 15);
    @Override
    public void onUpdate()
    {
        super.onUpdate();

        if (this.prevRotationPitch == 0.0F && this.prevRotationYaw == 0.0F)
        {
            float f = MathHelper.sqrt(this.motionX * this.motionX + this.motionZ * this.motionZ);
            this.rotationYaw = (float)(MathHelper.atan2(this.motionX, this.motionZ) * (180D / Math.PI));
            this.rotationPitch = (float)(MathHelper.atan2(this.motionY, (double)f) * (180D / Math.PI));
            this.prevRotationYaw = this.rotationYaw;
            this.prevRotationPitch = this.rotationPitch;
        }

        IBlockState iblockstate = this.world.getBlockState(inTilePosition);

        if (iblockstate.getMaterial() != Material.AIR)
        {
            AxisAlignedBB axisalignedbb = iblockstate.getCollisionBoundingBox(this.world, inTilePosition);

            if (axisalignedbb != Block.NULL_AABB && axisalignedbb.offset(inTilePosition).contains(new Vec3d(this.posX, this.posY, this.posZ)))
            {
                this.inGround = true;
            }
        }

        if (this.arrowShake > 0)
        {
            --this.arrowShake;
        }

        if (this.inGround)
        {
            if (iblockstate != this.inTile && !this.world.collidesWithAnyBlock(this.getEntityBoundingBox().grow(0.05D)))
            {
                this.inGround = false;
                this.motionX *= (double)(this.rand.nextFloat() * 0.2F);
                this.motionY *= (double)(this.rand.nextFloat() * 0.2F);
                this.motionZ *= (double)(this.rand.nextFloat() * 0.2F);
                this.ticksInAir = 0;
            }
            else
            {
                onImpactGround();
            }
        }
        else
        {
            ++this.ticksInAir;
            Vec3d vec3d1 = new Vec3d(this.posX, this.posY, this.posZ);
            Vec3d vec3d = new Vec3d(this.posX + this.motionX, this.posY + this.motionY, this.posZ + this.motionZ);
            RayTraceResult raytraceresult = this.world.rayTraceBlocks(vec3d1, vec3d, false, true, false);
            vec3d1 = new Vec3d(this.posX, this.posY, this.posZ);
            vec3d = new Vec3d(this.posX + this.motionX, this.posY + this.motionY, this.posZ + this.motionZ);

            if (raytraceresult != null)
            {
                vec3d = new Vec3d(raytraceresult.hitVec.x, raytraceresult.hitVec.y, raytraceresult.hitVec.z);
            }

            Entity entity = this.findEntityOnPath(vec3d1, vec3d);

            if (entity != null)
            {
                raytraceresult = new RayTraceResult(entity);
            }

            if (raytraceresult != null && !net.minecraftforge.event.ForgeEventFactory.onProjectileImpact(this, raytraceresult))
            {
                this.onHit(raytraceresult);
            }

            this.posX += this.motionX;
            this.posY += this.motionY;
            this.posZ += this.motionZ;
            float f4 = MathHelper.sqrt(this.motionX * this.motionX + this.motionZ * this.motionZ);
            this.rotationYaw = (float)(MathHelper.atan2(this.motionX, this.motionZ) * (180D / Math.PI));

            for (this.rotationPitch = (float)(MathHelper.atan2(this.motionY, (double)f4) * (180D / Math.PI)); this.rotationPitch - this.prevRotationPitch < -180.0F; this.prevRotationPitch -= 360.0F)
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
            float f1 = 0.99F;
            float f2 = 0.05F;

            if (this.isInWater())
            {
                for (int i = 0; i < 4; ++i)
                {
                    float f3 = 0.25F;
                    this.world.spawnParticle(EnumParticleTypes.WATER_BUBBLE, this.posX - this.motionX * 0.25D, this.posY - this.motionY * 0.25D, this.posZ - this.motionZ * 0.25D, this.motionX, this.motionY, this.motionZ);
                }

                f1 = 0.6F;
            }

            if (this.isWet())
            {
                this.extinguish();
            }

            this.motionX *= (double)f1;
            this.motionY *= (double)f1;
            this.motionZ *= (double)f1;

            if (!this.hasNoGravity())
            {
                this.motionY -= 0.05000000074505806D;
            }

            this.setPosition(this.posX, this.posY, this.posZ);
            this.doBlockCollisions();
        }
    }

    /**
     * Called when the arrow hits a block or an entity
     */
    protected void onHit(RayTraceResult raytraceResultIn)
    {
        Entity entity = raytraceResultIn.entityHit;

        if (entity != null)
        {
            float speed = MathHelper.sqrt(this.motionX * this.motionX + this.motionY * this.motionY + this.motionZ * this.motionZ);
            int damageScaled = MathHelper.ceil((double)speed * this.damage);

            DamageSource damagesource = new EntityDamageSourceIndirect("arrow", this, null).setProjectile(); //TODO track source, TODO custom damage type

            //Notify that we hit an entity
            this.onImpactEntity(entity);

            if (this.isBurning() && !(entity instanceof EntityEnderman))
            {
                entity.setFire(5);
            }

            if (entity.attackEntityFrom(damagesource, (float)damageScaled))
            {
                if (!(entity instanceof EntityEnderman))
                {
                    this.setDead();
                }
            }
            else
            {
                this.motionX *= -0.10000000149011612D;
                this.motionY *= -0.10000000149011612D;
                this.motionZ *= -0.10000000149011612D;
                this.rotationYaw += 180.0F;
                this.prevRotationYaw += 180.0F;
                this.ticksInAir = 0;

                if (!this.world.isRemote && this.motionX * this.motionX + this.motionY * this.motionY + this.motionZ * this.motionZ < 0.0010000000474974513D)
                {
                    this.setDead();
                }
            }
        }
        else
        {
            this.inTilePosition = raytraceResultIn.getBlockPos();
            this.inTile = this.world.getBlockState(inTilePosition);
            this.motionX = (double)((float)(raytraceResultIn.hitVec.x - this.posX));
            this.motionY = (double)((float)(raytraceResultIn.hitVec.y - this.posY));
            this.motionZ = (double)((float)(raytraceResultIn.hitVec.z - this.posZ));
            float f2 = MathHelper.sqrt(this.motionX * this.motionX + this.motionY * this.motionY + this.motionZ * this.motionZ);
            this.posX -= this.motionX / (double)f2 * 0.05000000074505806D;
            this.posY -= this.motionY / (double)f2 * 0.05000000074505806D;
            this.posZ -= this.motionZ / (double)f2 * 0.05000000074505806D;
            this.playSound(SoundEvents.ENTITY_ARROW_HIT, 1.0F, 1.2F / (this.rand.nextFloat() * 0.2F + 0.9F));
            this.inGround = true;
            this.arrowShake = 7;

            if (this.inTile.getMaterial() != Material.AIR)
            {
                this.inTile.getBlock().onEntityCollision(this.world, inTilePosition, this.inTile, this);
            }
        }
    }

    protected void onImpactEntity(Entity entity)
    {
        if (this.isExplosive)
        {
            explode();
        }
        else
        {
            playImpactAudio();
        }
    }


    protected void onImpactGround()
    {
        if (this.isExplosive)
        {
            explode();
        }
        else
        {
            playImpactAudio();
            this.setDead();
        }
    }

    protected void playImpactAudio()
    {
        if(this.world.rand.nextFloat() > 0.5f)
        {
            if (this.isAnvil)
            {
                this.world.playSound(this.posX, (int) this.posY, (int) this.posZ, SoundEvents.BLOCK_ANVIL_HIT, SoundCategory.BLOCKS, 1, 1, true);
            }
            else
            {
                this.playSound(SoundEvents.ENTITY_ARROW_HIT, 1.0F, 1.2F / (this.rand.nextFloat() * 0.2F + 0.9F));
            }
        }
    }

    @Nullable
    protected Entity findEntityOnPath(Vec3d start, Vec3d end)
    {
        Entity entity = null;
        List<Entity> list = this.world.getEntitiesInAABBexcluding(this, this.getEntityBoundingBox().expand(this.motionX, this.motionY, this.motionZ).grow(1.0D), ARROW_TARGETS);
        double d0 = 0.0D;

        for (int i = 0; i < list.size(); ++i)
        {
            Entity entity1 = list.get(i);

            if (this.ticksInAir >= 5)
            {
                AxisAlignedBB axisalignedbb = entity1.getEntityBoundingBox().grow(0.30000001192092896D);
                RayTraceResult raytraceresult = axisalignedbb.calculateIntercept(start, end);

                if (raytraceresult != null)
                {
                    double d1 = start.squareDistanceTo(raytraceresult.hitVec);

                    if (d1 < d0 || d0 == 0.0D)
                    {
                        entity = entity1;
                        d0 = d1;
                    }
                }
            }
        }

        return entity;
    }

    /** (abstract) Protected helper method to write subclass entity data to NBT. */
    @Override
    public void writeEntityToNBT(NBTTagCompound nbt)
    {
        nbt.setByte("shake", (byte) this.arrowShake);
        nbt.setBoolean("isExplosive", this.isExplosive);
    }

    /** (abstract) Protected helper method to read subclass entity data from NBT. */
    @Override
    public void readEntityFromNBT(NBTTagCompound nbt)
    {
        this.arrowShake = nbt.getByte("shake") & 255;
        this.isExplosive = nbt.getBoolean("isExplosive");
    }

    /** Called by a player entity when they collide with an entity */
    @Override
    public void applyEntityCollision(Entity par1Entity)
    {
        super.applyEntityCollision(par1Entity);

        if (this.isExplosive && this.ticksExisted < 20 * 2)
        {
            this.explode();
        }
    }
}

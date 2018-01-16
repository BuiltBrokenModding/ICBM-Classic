package icbm.classic.content.entity;

import io.netty.buffer.ByteBuf;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.init.Blocks;
import net.minecraft.init.SoundEvents;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EntityDamageSourceIndirect;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.*;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.IEntityAdditionalSpawnData;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class EntityFragments extends Entity implements IEntityAdditionalSpawnData
{
    private BlockPos inTilePosition;
    private IBlockState inTile;
    private boolean inGround = false;
    public boolean isExplosive;
    public boolean isAnvil;
    private boolean isExploding = false;

    /** Seems to be some sort of timer for animating an arrow. */
    public int arrowShake = 0;

    /** The owner of this arrow. */
    private int ticksInAir = 0;
    private final int damage = 11;

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
    @Override
    public void onUpdate()
    {
        super.onUpdate();

        if (this.isAnvil)
        {
            ArrayList entities = new ArrayList(this.world.getEntitiesWithinAABBExcludingEntity(this, this.getEntityBoundingBox()));

            Iterator var5 = entities.iterator();

            while (var5.hasNext())
            {
                Entity entity = (Entity) var5.next();
                entity.attackEntityFrom(DamageSource.ANVIL, 15);
            }
        }

        if (this.prevRotationPitch == 0.0F && this.prevRotationYaw == 0.0F)
        {
            float var1 = MathHelper.sqrt(this.motionX * this.motionX + this.motionZ * this.motionZ);
            this.prevRotationYaw = this.rotationYaw = (float) (Math.atan2(this.motionX, this.motionZ) * 180.0D / Math.PI);
            this.prevRotationPitch = this.rotationPitch = (float) (Math.atan2(this.motionY, var1) * 180.0D / Math.PI);
        }

        IBlockState blockState = this.world.getBlockState(inTilePosition);
        Block block = blockState.getBlock();

        if (block != Blocks.AIR)
        {
            //var15.setBlockBoundsBasedOnState(this.world, this.xTile, this.yTile, this.zTile);
            AxisAlignedBB var2 = block.getCollisionBoundingBox(blockState, this.world, inTilePosition);

            if (var2 != null && var2.contains(new Vec3d(this.posX, this.posY, this.posZ)))
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
            blockState = this.world.getBlockState(inTilePosition);

            if (blockState == inTile)
            {
                if (this.isExplosive)
                {
                    explode();
                }
                else
                {
                    if (this.isAnvil && this.world.rand.nextFloat() > 0.5f)
                    {
                        this.world.playSound(this.posX, (int) this.posY, (int) this.posZ, SoundEvents.BLOCK_ANVIL_HIT, SoundCategory.BLOCKS, 1, 1, true);
                    }

                    this.setDead();
                }
            }
            else
            {
                this.inGround = false;
                this.motionX *= (this.rand.nextFloat() * 0.2F);
                this.motionY *= (this.rand.nextFloat() * 0.2F);
                this.motionZ *= (this.rand.nextFloat() * 0.2F);
                this.ticksInAir = 0;
            }
        }
        else
        {
            ++this.ticksInAir;
            Vec3d var16 = new Vec3d(this.posX, this.posY, this.posZ);
            Vec3d var17 = new Vec3d(this.posX + this.motionX, this.posY + this.motionY, this.posZ + this.motionZ);
            RayTraceResult movingObjPos = this.world.rayTraceBlocks(var16, var17, false);
            var16 = new Vec3d(this.posX, this.posY, this.posZ);
            var17 = new Vec3d(this.posX + this.motionX, this.posY + this.motionY, this.posZ + this.motionZ);

            if (movingObjPos != null)
            {
                var17 = new Vec3d(movingObjPos.hitVec.x, movingObjPos.hitVec.y, movingObjPos.hitVec.z);
            }

            Entity var4 = null;
            List var5 = this.world.getEntitiesWithinAABBExcludingEntity(this, this.getEntityBoundingBox().offset(this.motionX, this.motionY, this.motionZ).expand(1.0D, 1.0D, 1.0D));
            double var6 = 0.0D;
            int var8;
            float var10;

            for (var8 = 0; var8 < var5.size(); ++var8)
            {
                Entity var9 = (Entity) var5.get(var8);

                if (var9.canBeCollidedWith() && (this.ticksInAir >= 5))
                {
                    var10 = 0.3F;
                    AxisAlignedBB var11 = var9.getEntityBoundingBox().expand(var10, var10, var10);
                    RayTraceResult var12 = var11.calculateIntercept(var16, var17);

                    if (var12 != null)
                    {
                        double var13 = var16.distanceTo(var12.hitVec);

                        if (var13 < var6 || var6 == 0.0D)
                        {
                            var4 = var9;
                            var6 = var13;
                        }
                    }
                }
            }

            if (var4 != null)
            {
                movingObjPos = new RayTraceResult(var4);
            }

            float speed;

            if (movingObjPos != null)
            {
                if (movingObjPos.entityHit != null)
                {
                    speed = MathHelper.sqrt(this.motionX * this.motionX + this.motionY * this.motionY + this.motionZ * this.motionZ);
                    int damage = (int) Math.ceil(speed * this.damage);

                    if (this.arrowCritical)
                    {
                        damage += this.rand.nextInt(damage / 2 + 2);
                    }

                    DamageSource damageSource = (new EntityDamageSourceIndirect("arrow", this, this)).setProjectile();

                    if (this.isBurning())
                    {
                        movingObjPos.entityHit.setFire(5);
                    }

                    if (movingObjPos.entityHit.attackEntityFrom(damageSource, damage))
                    {
                        if (movingObjPos.entityHit instanceof EntityLiving)
                        {
                            EntityLiving var24 = (EntityLiving) movingObjPos.entityHit;

                            if (!this.world.isRemote)
                            {
                                var24.setArrowCountInEntity(var24.getArrowCountInEntity() + 1);
                            }
                        }

                        this.world.playSound(posX, posY, posZ, SoundEvents.ENTITY_ARROW_HIT , SoundCategory.BLOCKS, 1.0F, 1.2F / (this.rand.nextFloat() * 0.2F + 0.9F), true);
                        this.setDead();
                    }
                    else
                    {
                        this.motionX *= -0.10000000149011612D;
                        this.motionY *= -0.10000000149011612D;
                        this.motionZ *= -0.10000000149011612D;
                        this.rotationYaw += 180.0F;
                        this.prevRotationYaw += 180.0F;
                        this.ticksInAir = 0;
                    }
                }
                else
                {
                    this.inTilePosition = movingObjPos.getBlockPos();
                    this.inTile = this.world.getBlockState(inTilePosition);
                    this.motionX = ((float) (movingObjPos.hitVec.x - this.posX));
                    this.motionY = ((float) (movingObjPos.hitVec.y - this.posY));
                    this.motionZ = ((float) (movingObjPos.hitVec.z - this.posZ));
                    speed = MathHelper.sqrt(this.motionX * this.motionX + this.motionY * this.motionY + this.motionZ * this.motionZ);
                    this.posX -= this.motionX / speed * 0.05000000074505806D;
                    this.posY -= this.motionY / speed * 0.05000000074505806D;
                    this.posZ -= this.motionZ / speed * 0.05000000074505806D;
                    this.world.playSound(posX, posY, posZ, SoundEvents.ENTITY_ARROW_HIT , SoundCategory.BLOCKS, 1.0F, 1.2F / (this.rand.nextFloat() * 0.2F + 0.9F), true);
                    this.inGround = true;
                    this.arrowShake = 7;
                    this.arrowCritical = false;
                }
            }

            if (this.arrowCritical)
            {
                for (var8 = 0; var8 < 4; ++var8)
                {
                    this.world.spawnParticle(EnumParticleTypes.CRIT, this.posX + this.motionX * var8 / 4.0D, this.posY + this.motionY * var8 / 4.0D, this.posZ + this.motionZ * var8 / 4.0D, -this.motionX, -this.motionY + 0.2D, -this.motionZ);
                }
            }

            this.posX += this.motionX;
            this.posY += this.motionY;
            this.posZ += this.motionZ;
            speed = MathHelper.sqrt(this.motionX * this.motionX + this.motionZ * this.motionZ);
            this.rotationYaw = (float) (Math.atan2(this.motionX, this.motionZ) * 180.0D / Math.PI);

            for (this.rotationPitch = (float) (Math.atan2(this.motionY, speed) * 180.0D / Math.PI); this.rotationPitch - this.prevRotationPitch < -180.0F; this.prevRotationPitch -= 360.0F)
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
            float var23 = 0.99F;
            var10 = 0.05F;

            if (this.isInWater())
            {
                for (int var25 = 0; var25 < 4; ++var25)
                {
                    float var24 = 0.25F;
                    this.world.spawnParticle(EnumParticleTypes.WATER_BUBBLE, this.posX - this.motionX * var24, this.posY - this.motionY * var24, this.posZ - this.motionZ * var24, this.motionX, this.motionY, this.motionZ);
                }

                var23 = 0.8F;
            }

            this.motionX *= var23;
            this.motionY *= var23;
            this.motionZ *= var23;
            this.motionY -= var10;
            this.setPosition(this.posX, this.posY, this.posZ);
        }
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

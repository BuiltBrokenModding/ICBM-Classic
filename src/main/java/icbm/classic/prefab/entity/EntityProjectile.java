package icbm.classic.prefab.entity;

import icbm.classic.lib.NBTConstants;
import icbm.classic.lib.transform.vector.Pos;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IProjectile;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.*;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.List;
import java.util.UUID;

/**
 * Modified version of the arrow projectile to be more abstract
 *
 * @author Darkguardsman
 */
public abstract class EntityProjectile extends EntityICBM implements IProjectile
{
    /**
     * The entity who shot this projectile and can be used for damage calculations
     * As well useful for causing argo on the shooter
     */
    public Entity shootingEntity;
    /**
     * Used to track shooting entity after being loaded from a save
     */
    public UUID shootingEntityUUID;
    /**
     * Location the projectile was fired from, use this over the shooting entity
     * to force argo on the source of the projectile. This way things like
     * zombies don't auto go to players. Instead they can move towards the firing
     * location. Think of it like moving towards the sound of the weapon.
     */
    public Pos sourceOfProjectile;

    //Settings
    protected int inGroundKillTime = 1200;
    protected int inAirKillTime = 1200;
    /**
     * Damage source to do on impact
     */
    //TODO replace with method allowing more complex calculations
    protected DamageSource impact_damageSource = DamageSource.ANVIL;

    //In ground data
    public BlockPos tilePos = new BlockPos(0, 0, 0);
    public EnumFacing sideTile = EnumFacing.UP;
    protected IBlockState blockInside = Blocks.AIR.getDefaultState();
    protected boolean inGround = false;

    //Timers
    public int ticksInGround;
    public int ticksInAir;

    public EntityProjectile(World world)
    {
        super(world);
        //this.renderDistanceWeight = 10.0D;
        this.setSize(0.5F, 0.5F);
    }

    public EntityProjectile(World world, double x, double y, double z)
    {
        super(world);
        this.setPosition(x, y, z);
        this.sourceOfProjectile = new Pos(x, y, z);
    }

    public EntityProjectile(World world, EntityLivingBase shooter, EntityLivingBase target, float p_i1755_4_, float p_i1755_5_)
    {
        this(world);
        this.shootingEntity = shooter;
        this.sourceOfProjectile = new Pos(shooter);

        this.posY = shooter.posY + (double) shooter.getEyeHeight() - 0.10000000149011612D;
        double d0 = target.posX - shooter.posX;
        double d1 = target.getEntityBoundingBox().minY + (double) (target.height / 3.0F) - this.posY;
        double d2 = target.posZ - shooter.posZ;
        double d3 = (double) MathHelper.sqrt(d0 * d0 + d2 * d2);

        if (d3 >= 1.0E-7D)
        {
            float f2 = (float) (Math.atan2(d2, d0) * 180.0D / Math.PI) - 90.0F;
            float f3 = (float) (-(Math.atan2(d1, d3) * 180.0D / Math.PI));
            double d4 = d0 / d3;
            double d5 = d2 / d3;
            this.setLocationAndAngles(shooter.posX + d4, this.posY, shooter.posZ + d5, f2, f3);
            float f4 = (float) d3 * 0.2F;
            this.shoot(d0, d1 + (double) f4, d2, p_i1755_4_, p_i1755_5_);
        }
    }

    public EntityProjectile(World world, EntityLivingBase shooter, float f)
    {
        this(world, shooter, f, 1);
    }

    public EntityProjectile(World world, EntityLivingBase shooter, float f, float distanceScale)
    {
        this(world, shooter.posX, shooter.posY + (double) shooter.getEyeHeight(), shooter.posZ, shooter.rotationYaw, shooter.rotationPitch, f, distanceScale);
    }

    public EntityProjectile(World world, double x, double y, double z, float yaw, float pitch, float speedScale, float distanceScale)
    {
        super(world);
        //this.renderDistanceWeight = 10.0D;
        this.sourceOfProjectile = new Pos(x, y, z);

        this.setSize(0.5F, 0.5F);
        this.setLocationAndAngles(x, y, z, yaw, pitch);
        this.posX -= (double) (MathHelper.cos(this.rotationYaw / 180.0F * (float) Math.PI) * 0.16F * distanceScale);
        this.posY -= 0.10000000149011612D;
        this.posZ -= (double) (MathHelper.sin(this.rotationYaw / 180.0F * (float) Math.PI) * 0.16F * distanceScale);
        this.setPosition(this.posX, this.posY, this.posZ);
        //this.yOffset = 0.0F;
        this.motionX = (double) (-MathHelper.sin(this.rotationYaw / 180.0F * (float) Math.PI) * MathHelper.cos(this.rotationPitch / 180.0F * (float) Math.PI));
        this.motionZ = (double) (MathHelper.cos(this.rotationYaw / 180.0F * (float) Math.PI) * MathHelper.cos(this.rotationPitch / 180.0F * (float) Math.PI));
        this.motionY = (double) (-MathHelper.sin(this.rotationPitch / 180.0F * (float) Math.PI));
        this.shoot(this.motionX, this.motionY, this.motionZ, speedScale, 1.0F);
    }

    @Override
    protected void entityInit()
    {
        super.entityInit();
    }

    @Override
    public void onUpdate()
    {
        super.onUpdate();

        //Update rotation to match motion
        if (this.prevRotationPitch == 0.0F && this.prevRotationYaw == 0.0F)
        {
            float f = MathHelper.sqrt(this.motionX * this.motionX + this.motionZ * this.motionZ);
            this.prevRotationYaw = this.rotationYaw = (float) (Math.atan2(this.motionX, this.motionZ) * 180.0D / Math.PI);
            this.prevRotationPitch = this.rotationPitch = (float) (Math.atan2(this.motionY, (double) f) * 180.0D / Math.PI);
        }


        //Check if we hit the ground
        IBlockState state = this.world.getBlockState(tilePos);
        if (!state.getBlock().isAir(state, world, tilePos))
        {
            //Check if what we hit can be collided with
            AxisAlignedBB axisalignedbb = state.getCollisionBoundingBox(this.world, tilePos);
            if (axisalignedbb != Block.NULL_AABB && axisalignedbb.offset(tilePos).contains(new Vec3d(this.posX, this.posY, this.posZ)))
            {
                this.inGround = true;
            }
        }

        //Handle stuck in ground
        if (this.inGround)
        {
            //TODO allow this to be disabled
            if (state == blockInside)
            {
                ++this.ticksInGround;

                if (this.ticksInGround == inGroundKillTime)
                {
                    this.setDead();
                }
            }
            else
            {
                //TODO change to apply gravity
                this.inGround = false;
                this.motionX *= (double) (this.rand.nextFloat() * 0.2F);
                this.motionY *= (double) (this.rand.nextFloat() * 0.2F);
                this.motionZ *= (double) (this.rand.nextFloat() * 0.2F);
                this.ticksInGround = 0;
                this.ticksInAir = 0;
            }
        }
        else
        {
            //Kills the projectile if it moves forever into space
            ++this.ticksInAir;
            if (ticksInAir >= inAirKillTime)
            {
                this.setDead();
                return;
            }

            //Do raytrace TODO move to prefab entity for reuse
            Vec3d rayStart = new Vec3d(this.posX, this.posY, this.posZ);
            Vec3d rayEnd = new Vec3d(this.posX + this.motionX, this.posY + this.motionY, this.posZ + this.motionZ);
            RayTraceResult rayHit = this.world.rayTraceBlocks(rayStart, rayEnd, false, true, false);

            //Reset data to do entity ray trace
            rayStart = new Vec3d(this.posX, this.posY, this.posZ);
            rayEnd = new Vec3d(this.posX + this.motionX, this.posY + this.motionY, this.posZ + this.motionZ);
            if (rayHit != null)
            {
                rayEnd = new Vec3d(rayHit.hitVec.x, rayHit.hitVec.y, rayHit.hitVec.z);
            }

            //Handle entity collision boxes
            Entity entity = null;
            List list = this.world.getEntitiesWithinAABBExcludingEntity(this, this.getEntityBoundingBox().offset(this.motionX, this.motionY, this.motionZ).expand(1.0D, 1.0D, 1.0D));
            double distanceToHit = 0.0D;
            float hitBoxSizeScale;

            for (int i = 0; i < list.size(); ++i)
            {
                Entity checkEntity = (Entity) list.get(i);

                if (shouldCollideWith(checkEntity) && (checkEntity != this.shootingEntity || this.ticksInAir >= 5))
                {
                    hitBoxSizeScale = 0.3F;
                    AxisAlignedBB hitBox = checkEntity.getEntityBoundingBox().expand((double) hitBoxSizeScale, (double) hitBoxSizeScale, (double) hitBoxSizeScale);
                    RayTraceResult entityRayHit = hitBox.calculateIntercept(rayStart, rayEnd);

                    if (entityRayHit != null)
                    {
                        double distance = rayStart.distanceTo(entityRayHit.hitVec);

                        if (distance < distanceToHit || distanceToHit == 0.0D)
                        {
                            entity = checkEntity;
                            distanceToHit = distance;
                        }
                    }
                }
            }

            //If we collided with an entity, set hit to entity
            if (entity != null)
            {
                rayHit = new RayTraceResult(entity);
            }

            if (rayHit != null && rayHit.typeOfHit != RayTraceResult.Type.MISS && !ignoreImpact(rayHit))
            {
                //Handle entity hit
                if (rayHit.typeOfHit == RayTraceResult.Type.ENTITY)
                {
                    handleEntityCollision(rayHit, rayHit.entityHit);
                }
                else //Handle block hit
                {
                    handleBlockCollision(rayHit);
                }

                postImpact(rayHit);
            }
            updateMotion();
        }
    }

    protected boolean ignoreImpact(RayTraceResult hit)
    {
        return false;
    }

    protected void postImpact(RayTraceResult hit) {}

    /**
     * Called to see if collision checks should be ignored on the
     * entity.
     *
     * @param entity
     * @return true to collide, false to ignore
     */
    protected boolean shouldCollideWith(Entity entity)
    {
        //TODO add listener support
        return entity.canBeCollidedWith();
    }

    protected void handleBlockCollision(RayTraceResult movingobjectposition)
    {
        this.tilePos = movingobjectposition.getBlockPos();
        this.sideTile = movingobjectposition.sideHit;

        this.blockInside = this.world.getBlockState(tilePos);

        this.motionX = (double) ((float) (movingobjectposition.hitVec.x - this.posX));
        this.motionY = (double) ((float) (movingobjectposition.hitVec.y - this.posY));
        this.motionZ = (double) ((float) (movingobjectposition.hitVec.z - this.posZ));

        float velocity = MathHelper.sqrt(this.motionX * this.motionX + this.motionY * this.motionY + this.motionZ * this.motionZ);
        this.posX -= this.motionX / (double) velocity * 0.05000000074505806D;
        this.posY -= this.motionY / (double) velocity * 0.05000000074505806D;
        this.posZ -= this.motionZ / (double) velocity * 0.05000000074505806D;
        //TODO this.playSound("random.bowhit", 1.0F, 1.2F / (this.rand.nextFloat() * 0.2F + 0.9F));
        this.inGround = true;

        if (this.blockInside != null && this.blockInside.getMaterial() != Material.AIR)
        {
            this.blockInside.getBlock().onEntityCollision(this.world, tilePos, blockInside, this);
        }
        onImpactTile(movingobjectposition);
    }

    /**
     * Deprecated due to precision issues with hit position
     */
    @Deprecated
    protected void onImpactTile()
    {
    }

    /**
     * Called when the projectile impacts a tile.
     * Data about the hit is stored in the entity
     * include location, block, and meta
     *
     * @param hit - exact hit position
     */
    protected void onImpactTile(RayTraceResult hit)
    {
        onImpactTile();
    }

    /**
     * Handles entity being impacted by the projectile
     *
     * @param movingobjectposition
     * @param entityHit
     */
    protected void handleEntityCollision(RayTraceResult movingobjectposition, Entity entityHit)
    {
        onImpactEntity(entityHit, (float) getVelocity().magnitude(), movingobjectposition);
    }


    protected void onImpactEntity(Entity entityHit, float velocity, RayTraceResult hit)
    {
        onImpactEntity(entityHit, (float) getVelocity().magnitude());
    }

    protected void onImpactEntity(Entity entityHit, float velocity)
    {
        if (!world.isRemote)
        {
            int damage = MathHelper.ceil((double) velocity * 2);

            //If entity takes damage add velocity to entity
            if (impact_damageSource != null && entityHit.attackEntityFrom(impact_damageSource, (float) damage))
            {
                if (entityHit instanceof EntityLivingBase)
                {
                    float vel_horizontal = MathHelper.sqrt(this.motionX * this.motionX + this.motionZ * this.motionZ);
                    if (vel_horizontal > 0.0F)
                    {
                        entityHit.addVelocity(this.motionX * 0.6000000238418579D / (double) vel_horizontal, 0.1D, this.motionZ * 0.6000000238418579D / (double) vel_horizontal);
                    }
                }

            }
            this.setDead();
        }
    }

    protected void updateMotion()
    {
        //Update motion
        this.posX += this.motionX;
        this.posY += this.motionY;
        this.posZ += this.motionZ;

        //Get rotation from motion
        float speed = MathHelper.sqrt(this.motionX * this.motionX + this.motionZ * this.motionZ);
        this.rotationYaw = (float) (Math.atan2(this.motionX, this.motionZ) * 180.0D / Math.PI);
        this.rotationPitch = (float) (Math.atan2(this.motionY, (double) speed) * 180.0D / Math.PI);

        //-------------------------------------
        //Fix rotation
        while (this.rotationPitch - this.prevRotationPitch < -180.0F)
        {
            this.prevRotationPitch -= 360.0F;
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
        //-------------------------------------
        //Reduce delta in rotation to provide for a smoother animation
        this.rotationPitch = this.prevRotationPitch + (this.rotationPitch - this.prevRotationPitch) * 0.2F;
        this.rotationYaw = this.prevRotationYaw + (this.rotationYaw - this.prevRotationYaw) * 0.2F;
        //-------------------------------------

        //Decrease motion so the projectile stops
        decreaseMotion();

        //Set position
        this.setPosition(this.posX, this.posY, this.posZ);

        //Adjust for collision      TODO check if works, rewrite code to prevent clip through of block
        this.doBlockCollisions();
    }

    protected void decreaseMotion()
    {
        //TODO get friction value
        this.motionX *= 0.99F;
        this.motionY *= 0.99F;
        this.motionZ *= 0.99F;
        //Add gravity so the projectile will fall
        this.motionY -= 0.05F;
    }


    @Override
    public void shoot(double xx, double yy, double zz, float multiplier, float random)
    {
        //Normalize
        float velocity = MathHelper.sqrt(xx * xx + yy * yy + zz * zz);
        xx /= (double) velocity;
        yy /= (double) velocity;
        zz /= (double) velocity;

        //Add randomization to make the arrow miss
        if (random > 0)
        {
            xx += this.rand.nextGaussian() * (double) (this.rand.nextBoolean() ? -1 : 1) * 0.007499999832361937D * (double) random;
            yy += this.rand.nextGaussian() * (double) (this.rand.nextBoolean() ? -1 : 1) * 0.007499999832361937D * (double) random;
            zz += this.rand.nextGaussian() * (double) (this.rand.nextBoolean() ? -1 : 1) * 0.007499999832361937D * (double) random;
        }

        //Add multiplier
        xx *= (double) multiplier;
        yy *= (double) multiplier;
        zz *= (double) multiplier;

        //Set motion
        this.motionX = xx;
        this.motionY = yy;
        this.motionZ = zz;

        //Update rotation
        float f3 = MathHelper.sqrt(xx * xx + zz * zz);
        this.prevRotationYaw = this.rotationYaw = (float) (Math.atan2(xx, zz) * 180.0D / Math.PI);
        this.prevRotationPitch = this.rotationPitch = (float) (Math.atan2(yy, (double) f3) * 180.0D / Math.PI);
        this.ticksInGround = 0;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void setPositionAndRotationDirect(double x, double y, double z, float yaw, float pitch, int posRotationIncrements, boolean teleport)
    {
        this.setPosition(x, y, z);
        this.setRotation(yaw, pitch);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void setVelocity(double xx, double yy, double zz)
    {
        this.motionX = xx;
        this.motionY = yy;
        this.motionZ = zz;

        if (this.prevRotationPitch == 0.0F && this.prevRotationYaw == 0.0F)
        {
            float f = MathHelper.sqrt(xx * xx + zz * zz);
            this.prevRotationYaw = this.rotationYaw = (float) (Math.atan2(xx, zz) * 180.0D / Math.PI);
            this.prevRotationPitch = this.rotationPitch = (float) (Math.atan2(yy, (double) f) * 180.0D / Math.PI);
            this.prevRotationPitch = this.rotationPitch;
            this.prevRotationYaw = this.rotationYaw;
            this.setLocationAndAngles(this.posX, this.posY, this.posZ, this.rotationYaw, this.rotationPitch);
            this.ticksInGround = 0;
        }
    }

    @Override
    public void writeEntityToNBT(NBTTagCompound nbt)
    {
        if (tilePos != null)
        {
            nbt.setInteger(NBTConstants.X_TILE_POS, this.tilePos.getX());
            nbt.setInteger(NBTConstants.Y_TILE_POS, this.tilePos.getY());
            nbt.setInteger(NBTConstants.Z_TILE_POS, this.tilePos.getZ());
        }
        nbt.setByte(NBTConstants.SIDE_TILE_POS, (byte) this.sideTile.ordinal());

        if (blockInside != null)
        {
            nbt.setInteger(NBTConstants.IN_TILE_STATE, Block.getStateId(blockInside));
        }

        nbt.setShort(NBTConstants.LIFE, (short) this.ticksInGround);
        nbt.setByte(NBTConstants.IN_GROUND, (byte) (this.inGround ? 1 : 0));
        if (sourceOfProjectile != null)
        {
            nbt.setTag(NBTConstants.SOURCE_POS, sourceOfProjectile.toNBT());
        }
        if (shootingEntity != null)
        {
            nbt.setString(NBTConstants.SHOOTER_UUID, shootingEntity.getUniqueID().toString());
        }
    }

    @Override
    public void readEntityFromNBT(NBTTagCompound nbt)
    {
        if (nbt.hasKey(NBTConstants.X_TILE))
        {
            //Legacy
            tilePos = new BlockPos(nbt.getShort(NBTConstants.X_TILE), nbt.getShort(NBTConstants.Y_TILE), nbt.getShort(NBTConstants.Z_TILE));
        }
        else if (nbt.hasKey(NBTConstants.X_TILE_POS))
        {
            tilePos = new BlockPos(nbt.getInteger(NBTConstants.X_TILE_POS), nbt.getInteger(NBTConstants.Y_TILE_POS), nbt.getInteger(NBTConstants.Z_TILE_POS));
        }

        if (nbt.hasKey(NBTConstants.SIDE_TILE))
        {
            //Legacy
            this.sideTile = EnumFacing.byIndex(nbt.getShort(NBTConstants.SIDE_TILE));
        }
        else
        {
            this.sideTile = EnumFacing.byIndex(nbt.getByte(NBTConstants.SIDE_TILE_POS));
        }
        this.ticksInGround = nbt.getShort(NBTConstants.LIFE);
        if (nbt.hasKey(NBTConstants.IN_TILE))
        {
            //Legacy
            Block block = Block.getBlockById(nbt.getByte(NBTConstants.IN_TILE));
            if (block != null)
            {
                int meta = nbt.getByte(NBTConstants.IN_DATA);
                this.blockInside = block.getStateFromMeta(meta);
            }
        }
        else if (nbt.hasKey(NBTConstants.IN_TILE_STATE))
        {
            this.blockInside = Block.getStateById(nbt.getInteger(NBTConstants.IN_TILE_STATE));
        }

        this.inGround = nbt.getByte(NBTConstants.IN_GROUND) == 1;
        if (nbt.hasKey(NBTConstants.SOURCE_POS))
        {
            sourceOfProjectile = new Pos(nbt.getCompoundTag(NBTConstants.SOURCE_POS));
        }
        if (nbt.hasKey(NBTConstants.SHOOTER_UUID))
        {
            shootingEntityUUID = UUID.fromString(nbt.getString(NBTConstants.SHOOTER_UUID));
        }
    }

    @Override
    protected boolean canTriggerWalking()
    {
        return false;
    }

    public boolean canBeDestroyed(Object attacker, DamageSource damageSource)
    {
        return hasHealth;
    }
}

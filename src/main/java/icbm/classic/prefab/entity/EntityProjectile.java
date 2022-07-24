package icbm.classic.prefab.entity;

import icbm.classic.api.missiles.IMissileAiming;
import icbm.classic.lib.saving.NbtSaveHandler;
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
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
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
public class EntityProjectile<E extends EntityProjectile<E>> extends EntityICBM implements IProjectile, IMissileAiming
{
    /**
     * The entity who shot this projectile and can be used for damage calculations
     * As well useful for causing argo on the shooter
     */
    public Entity shootingEntity;
    /**
     * Used to track shooting entity after being loaded from a save
     */
    public UUID shootingEntityUUID; //TODO abstract as a shooter object so we can track player vs entity vs tile vs admin command
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
    public IBlockState blockInside = Blocks.AIR.getDefaultState();
    public boolean inGround = false;

    //Timers
    public int ticksInGround;
    public int ticksInAir;


    public EntityProjectile(World world)
    {
        super(world);
        //this.renderDistanceWeight = 10.0D;
        this.setSize(0.5F, 0.5F);
    }

    /**
     * Initialized the projectile to spawn from the shooter and aim at the target
     *
     * @param shooter    - spawn point, used for aiming and position offsets
     * @param target     - aim target
     * @param multiplier - power multiplier, mostly changes speed
     * @param random     - random multiplier
     * @return this
     */
    @Deprecated
    public E init(EntityLivingBase shooter, EntityLivingBase target, float multiplier, float random)
    {
        this.shootingEntity = shooter;
        this.sourceOfProjectile = new Pos(shooter);

        this.posY = shooter.posY + (double) shooter.getEyeHeight() - 0.10000000149011612D;
        double deltaX = target.posX - shooter.posX;
        double deltaY = target.getEntityBoundingBox().minY + (double) (target.height / 3.0F) - this.posY; //TODO why div3
        double deltaZ = target.posZ - shooter.posZ;
        double deltaMag = MathHelper.sqrt(deltaX * deltaX + deltaZ * deltaZ);

        if (deltaMag >= 1.0E-7D) //TODO why the small num? rounding likely but maybe a better solution
        {
            float yaw = (float) (Math.atan2(deltaZ, deltaX) * 180.0D / Math.PI) - 90.0F;
            float pitch = (float) (-(Math.atan2(deltaY, deltaMag) * 180.0D / Math.PI));

            double subX = deltaX / deltaMag;
            double subZ = deltaZ / deltaMag;
            float subY = (float) deltaMag * 0.2F; //TODO why the 0.2F

            this.setLocationAndAngles(shooter.posX + subX, this.posY, shooter.posZ + subZ, yaw, pitch);
            this.shoot(deltaX, deltaY + (double) subY, deltaZ, multiplier, random);
        }
        return (E) this;
    }

    @Deprecated
    public E init(double x, double y, double z, float yaw, float pitch, float multiplier, float distanceScale)
    {
        //this.renderDistanceWeight = 10.0D;
        this.sourceOfProjectile = new Pos(x, y, z);

        this.setSize(0.5F, 0.5F);
        this.setLocationAndAngles(x, y, z, yaw, pitch);

        //TODO figure out why we are updating position by rotation after spawning
        this.posX -= (double) (MathHelper.cos(this.rotationYaw / 180.0F * (float) Math.PI) * 0.16F * distanceScale);
        this.posY -= 0.10000000149011612D;
        this.posZ -= (double) (MathHelper.sin(this.rotationYaw / 180.0F * (float) Math.PI) * 0.16F * distanceScale);
        this.setPosition(this.posX, this.posY, this.posZ);
        //this.yOffset = 0.0F;

        this.motionX = (double) (-MathHelper.sin(this.rotationYaw / 180.0F * (float) Math.PI) * MathHelper.cos(this.rotationPitch / 180.0F * (float) Math.PI));
        this.motionZ = (double) (MathHelper.cos(this.rotationYaw / 180.0F * (float) Math.PI) * MathHelper.cos(this.rotationPitch / 180.0F * (float) Math.PI));
        this.motionY = (double) (-MathHelper.sin(this.rotationPitch / 180.0F * (float) Math.PI));
        this.shoot(this.motionX, this.motionY, this.motionZ, multiplier, 1.0F);

        return (E) this;
    }

    @Override
    public void initAimingPosition(double x, double y, double z, float yaw, float pitch, float offsetMultiplier, float forceMultiplier)
    {
        init(x, y, z, yaw, pitch, forceMultiplier, offsetMultiplier);
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
            } else
            {
                //TODO change to apply gravity
                this.inGround = false;
                this.motionX *= (double) (this.rand.nextFloat() * 0.2F);
                this.motionY *= (double) (this.rand.nextFloat() * 0.2F);
                this.motionZ *= (double) (this.rand.nextFloat() * 0.2F);
                this.ticksInGround = 0;
                this.ticksInAir = 0;
            }
        } else
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
                } else //Handle block hit
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

    protected void postImpact(RayTraceResult hit)
    {
    }

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
            this.blockInside.getBlock().onEntityCollidedWithBlock(this.world, tilePos, blockInside, this);
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

        rotateTowardsMotion();

        //Decrease motion so the projectile stops
        decreaseMotion();

        //Set position
        this.setPosition(this.posX, this.posY, this.posZ);

        //Adjust for collision      TODO check if works, rewrite code to prevent clip through of block
        this.doBlockCollisions();
    }

    protected void rotateTowardsMotion() {
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
    protected boolean canTriggerWalking()
    {
        return false;
    }

    public boolean canBeDestroyed(Object attacker, DamageSource damageSource)
    {
        return hasHealth;
    }

    @Override
    public void writeEntityToNBT(NBTTagCompound nbt)
    {
        SAVE_LOGIC.save(this, nbt);
        super.writeEntityToNBT(nbt);
    }

    @Override
    public void readEntityFromNBT(NBTTagCompound nbt)
    {
        super.readEntityFromNBT(nbt);
        SAVE_LOGIC.load(this, nbt);
    }

    private static final NbtSaveHandler<EntityProjectile> SAVE_LOGIC = new NbtSaveHandler<EntityProjectile>()
        //Stuck in ground data
        .addRoot("ground")
        /* */.nodeBlockPos("pos", (projectile) -> projectile.tilePos, (projectile, pos) -> projectile.tilePos = pos)
        /* */.nodeFacing("side", (projectile) -> projectile.sideTile, (projectile, side) -> projectile.sideTile = side)
        /* */.nodeBlockState("state", (projectile) -> projectile.blockInside, (projectile, blockState) -> projectile.blockInside = blockState)
        .base()
        //Flags
        .addRoot("flags")
        /* */.nodeBoolean("ground", (projectile) -> projectile.inGround, (projectile, flag) -> projectile.inGround = flag)
        .base()
        //Ticks
        .addRoot("ticks")
        /* */.nodeInteger("air", (projectile) -> projectile.ticksInAir, (projectile, flag) -> projectile.ticksInAir = flag)
        /* */.nodeInteger("ground", (projectile) -> projectile.ticksInGround, (projectile, flag) -> projectile.ticksInGround = flag)
        .base()
        //Project source
        .addRoot("source")
        /* */.nodePos("pos", (projectile) -> projectile.sourceOfProjectile, (projectile, pos) -> projectile.sourceOfProjectile = pos)
        /* */.nodeUUID("uuid", (projectile) -> projectile.shootingEntityUUID, (projectile, uuid) -> projectile.shootingEntityUUID = uuid)
        .base();
}

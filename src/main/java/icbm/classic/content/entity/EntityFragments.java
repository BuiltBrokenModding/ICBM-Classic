package icbm.classic.content.entity;

import icbm.classic.content.blast.ICBMDamageTypes;
import icbm.classic.content.reg.EntityReg;
import icbm.classic.lib.NBTConstants;
import icbm.classic.lib.world.IProjectileBlockInteraction;
import icbm.classic.lib.world.ProjectileBlockInteraction;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.entity.IEntityWithComplexSpawn;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;

@Deprecated //TODO recode to use projectile system and have a unique entity per type
public class EntityFragments extends Entity implements IEntityWithComplexSpawn
{
    private BlockPos inTilePosition = new BlockPos(0, 0, 0);
    private BlockState inTile;

    public LivingEntity shootingEntity;

    //Type settings
    @Setter @Accessors(chain = true)
    public boolean isExplosive; //TODO replace with ENUM
    @Setter @Accessors(chain = true)
    public boolean isAnvil; //TODO replace with ENUM

    //Triggers
    private boolean inGround = false;
    private boolean isExploding = false;

    /** Seems to be some sort of timer for animating an arrow. */
    public int arrowShake = 0;

    /** The owner of this arrow. */
    private int ticksInAir = 0;

    @Setter @Accessors(chain = true)
    public int damage = 11;

    public boolean flatDamage = false;

    /** Is this arrow a critical hit? (Controls particles and damage) */
    @Setter @Accessors(chain = true)
    public boolean arrowCritical = false;
    public float explosionSize = 1.5F;

    public EntityFragments(EntityType<? extends EntityFragments> type, Level level)
    {
        super(type, level);
    }

    public EntityFragments(Level level)
    {
        this(EntityReg.BLOCK_FRAGMENT.get(), level);
    }

    @Override
    public void writeSpawnData(RegistryFriendlyByteBuf data)
    {
        data.writeBoolean(this.isExplosive); //TODO replace with ENUM
        data.writeBoolean(this.isAnvil);
    }

    @Override
    public void readSpawnData(RegistryFriendlyByteBuf data)
    {
        this.isExplosive = data.readBoolean();
        this.isAnvil = data.readBoolean();
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder)
    {
    }

    // PORT: 1.12 getName() returned the literal "Fragments", the entity type translation is used now

    /**
     * Uses the provided coordinates as a heading and determines the velocity from it with the set
     * force and random variance. Args: x, y, z, force, forceVariation
     */
    public void setArrowHeading(double vecX, double vecY, double vecZ, float scale, float random)
    {
        //Normalize vector
        double mag = Math.sqrt(vecX * vecX + vecY * vecY + vecZ * vecZ);
        vecX /= mag;
        vecY /= mag;
        vecZ /= mag;

        //Add random
        vecX += this.random.nextGaussian() * 0.007499999832361937D * random;
        vecY += this.random.nextGaussian() * 0.007499999832361937D * random;
        vecZ += this.random.nextGaussian() * 0.007499999832361937D * random;

        //Scale to power
        vecX *= scale;
        vecY *= scale;
        vecZ *= scale;

        //Set motion
        this.setDeltaMovement(vecX, vecY, vecZ);

        //Update rotation
        double var10 = Math.sqrt(vecX * vecX + vecZ * vecZ);
        this.setYRot((float) (Math.atan2(vecX, vecZ) * 180.0D / Math.PI));
        this.setXRot((float) (Math.atan2(vecY, var10) * 180.0D / Math.PI));
        this.yRotO = this.getYRot();
        this.xRotO = this.getXRot();
    }

    /** Sets the velocity to the args (client, from packets). 1.12 setVelocity */
    @Override
    public void lerpMotion(double par1, double par3, double par5)
    {
        this.setDeltaMovement(par1, par3, par5);

        if (this.xRotO == 0.0F && this.yRotO == 0.0F)
        {
            double var7 = Math.sqrt(par1 * par1 + par5 * par5);
            this.setYRot((float) (Math.atan2(par1, par5) * 180.0D / Math.PI));
            this.setXRot((float) (Math.atan2(par3, var7) * 180.0D / Math.PI));
            this.yRotO = this.getYRot();
            this.xRotO = this.getXRot();
            this.moveTo(this.getX(), this.getY(), this.getZ(), this.getYRot(), this.getXRot());
        }
    }

    private void explode()
    {
        if (!this.isExploding && !this.level().isClientSide())
        {
            this.isExploding = true;
            this.level().explode(this, getX(), getY(), getZ(), this.explosionSize, Level.ExplosionInteraction.TNT);
            this.discard();
        }
    }

    /** Called to update the entity's position/logic. */
    @Override
    public void tick()
    {
        super.tick();

        Vec3 motion = this.getDeltaMovement();

        if (this.xRotO == 0.0F && this.yRotO == 0.0F)
        {
            double f = Math.sqrt(motion.x * motion.x + motion.z * motion.z);
            this.setYRot((float) (Mth.atan2(motion.x, motion.z) * (180D / Math.PI)));
            this.setXRot((float) (Mth.atan2(motion.y, f) * (180D / Math.PI)));
            this.yRotO = this.getYRot();
            this.xRotO = this.getXRot();
        }

        BlockState iblockstate = this.level().getBlockState(inTilePosition);

        if (!iblockstate.isAir())
        {
            final VoxelShape shape = iblockstate.getCollisionShape(this.level(), inTilePosition);
            if (!shape.isEmpty())
            {
                final Vec3 pos = this.position();
                for (AABB box : shape.toAabbs())
                {
                    if (box.move(inTilePosition).contains(pos))
                    {
                        this.inGround = true;
                        break;
                    }
                }
            }
        }

        if (this.arrowShake > 0)
        {
            --this.arrowShake;
        }

        if (this.inGround)
        {
            if (iblockstate != this.inTile && this.level().noCollision(this.getBoundingBox().inflate(0.05D)))
            {
                this.inGround = false;
                this.setDeltaMovement(motion.multiply(this.random.nextFloat() * 0.2F, this.random.nextFloat() * 0.2F, this.random.nextFloat() * 0.2F));
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

            //Check for block collision
            Vec3 start = this.position();
            Vec3 end = start.add(motion.x * 2, motion.y * 2, motion.z * 2);
            HitResult raytraceresult = this.level().clip(new ClipContext(start, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this));
            if (raytraceresult.getType() == HitResult.Type.MISS)
            {
                raytraceresult = null;
            }

            //Reset start end
            start = this.position();
            end = start.add(motion);

            //IF hit, change end to last block hit
            if (raytraceresult != null)
            {
                end = raytraceresult.getLocation();
            }

            //Check for entities
            Entity entity = this.findEntityOnPath(start, end);

            //Wrapper entity hit
            if (entity != null)
            {
                raytraceresult = new EntityHitResult(entity);
            }

            //Handle entity hit
            // PORT: 1.12 fired ForgeEventFactory.onProjectileImpact, NeoForge only supports that event for Projectile subclasses
            if (raytraceresult != null)
            {
                this.onHit(raytraceresult);
                if (this.isRemoved())
                {
                    return;
                }
            }

            //onHit may change motion
            motion = this.getDeltaMovement();

            //Update position
            final double nextX = this.getX() + motion.x;
            final double nextY = this.getY() + motion.y;
            final double nextZ = this.getZ() + motion.z;

            //Update rotation
            double flatMag = Math.sqrt(motion.x * motion.x + motion.z * motion.z);
            float yaw = (float) (Mth.atan2(motion.x, motion.z) * (180D / Math.PI));
            float pitch = (float) (Mth.atan2(motion.y, flatMag) * (180D / Math.PI));

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


            float motionModifier = 0.99F;

            //Handle water
            if (this.isInWater())
            {
                float motionMultiplier = 0.25F;

                for (int i = 0; i < 4; ++i)
                {
                    this.level().addParticle(ParticleTypes.BUBBLE, nextX - motion.x * motionMultiplier, nextY - motion.y * motionMultiplier, nextZ - motion.z * motionMultiplier, motion.x, motion.y, motion.z);
                }

                motionModifier = 0.6F;
            }

            if (this.isInWaterRainOrBubble())
            {
                this.clearFire();
            }

            //Air friction
            motion = motion.scale(motionModifier);

            //Gravity
            if (!this.isNoGravity())
            {
                motion = motion.subtract(0, 0.05000000074505806D, 0);
            }

            this.setDeltaMovement(motion);
            this.setPos(nextX, nextY, nextZ);
            this.checkInsideBlocks();
        }
    }

    protected double getDamage(float speed)
    {
        if (flatDamage)
        {
            return damage;
        }
        return (double) speed * this.damage;
    }

    protected boolean canAttack(Entity entity)
    {
        return entity != null && entity.isAlive();
    }

    /**
     * Called when the arrow hits a block or an entity
     */
    protected void onHit(HitResult raytraceResultIn)
    {
        if (raytraceResultIn instanceof EntityHitResult entityHit)
        {
            final Entity entity = entityHit.getEntity();
            if (!canAttack(entity))
            {
                this.discard();
                return;
            }

            final Vec3 motion = this.getDeltaMovement();
            final float speed = (float) motion.length();
            final int damageScaled = Mth.ceil(getDamage(speed));

            //TODO change damage source to match fragment
            final DamageSource damagesource = ICBMDamageTypes.source(level(), ICBMDamageTypes.FRAGMENTS, this, shootingEntity); //TODO track source

            //Notify that we hit an entity
            this.onImpactEntity(entity);

            if (this.isOnFire() && !(entity instanceof EnderMan))
            {
                entity.igniteForSeconds(5);
            }

            if (entity.hurt(damagesource, (float) damageScaled))
            {
                if (!(entity instanceof EnderMan))
                {
                    this.discard();
                }
            }
            else
            {
                this.setDeltaMovement(motion.scale(-0.10000000149011612D));
                this.setYRot(this.getYRot() + 180.0F);
                this.yRotO += 180.0F;
                this.ticksInAir = 0;

                if (!this.level().isClientSide() && this.getDeltaMovement().lengthSqr() < 0.0010000000474974513D)
                {
                    this.discard();
                }
            }
        }
        else if (raytraceResultIn instanceof BlockHitResult blockHit)
        {
            this.inTilePosition = blockHit.getBlockPos();
            this.inTile = this.level().getBlockState(inTilePosition);

            final IProjectileBlockInteraction.EnumHitReactions reaction =
                ProjectileBlockInteraction.handleSpecialInteraction(level(), inTilePosition, blockHit.getLocation(), blockHit.getDirection(), inTile, this);
            if (reaction.stop)
            {
                return;
            }

            final Vec3 hit = blockHit.getLocation();
            final double mx = (float) (hit.x - this.getX());
            final double my = (float) (hit.y - this.getY());
            final double mz = (float) (hit.z - this.getZ());
            this.setDeltaMovement(mx, my, mz);

            double f2 = Math.sqrt(mx * mx + my * my + mz * mz);
            this.setPos(this.getX() - mx / f2 * 0.05000000074505806D,
                this.getY() - my / f2 * 0.05000000074505806D,
                this.getZ() - mz / f2 * 0.05000000074505806D);

            this.playSound(SoundEvents.ARROW_HIT, 1.0F, 1.2F / (this.random.nextFloat() * 0.2F + 0.9F));

            this.inGround = true;
            this.arrowShake = 7;

            if (!this.inTile.isAir())
            {
                this.inTile.entityInside(this.level(), inTilePosition, this);
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
            this.discard();
        }
    }

    protected void playImpactAudio()
    {
        if (this.level().random.nextFloat() > 0.5f)
        {
            if (this.isAnvil)
            {
                // 1.12 World#playSound(double...) was client only as well
                this.level().playLocalSound(this.getX(), (int) this.getY(), (int) this.getZ(), SoundEvents.ANVIL_HIT, SoundSource.BLOCKS, 1, 1, true);
            }
            else
            {
                this.playSound(SoundEvents.ARROW_HIT, 1.0F, 1.2F / (this.random.nextFloat() * 0.2F + 0.9F));
            }
        }
    }

    @Nullable
    protected Entity findEntityOnPath(Vec3 start, Vec3 end)
    {
        Entity resultEntity = null;
        final Vec3 motion = this.getDeltaMovement();
        final List<Entity> entityList = this.level().getEntities(this, this.getBoundingBox().expandTowards(motion.x * 1.2, motion.y * 1.2, motion.z * 1.2).inflate(1.0D), Entity::isPickable);
        double d0 = 0.0D;

        for (final Entity currentEntity : entityList)
        {
            if (!(currentEntity instanceof EntityFragments) && (this.ticksInAir >= 5))
            {
                AABB axisalignedbb = currentEntity.getBoundingBox().inflate(0.30000001192092896D);
                Optional<Vec3> hit = axisalignedbb.clip(start, end);

                if (hit.isPresent())
                {
                    double d1 = start.distanceToSqr(hit.get());

                    if (d1 < d0 || d0 == 0.0D)
                    {
                        resultEntity = currentEntity;
                        d0 = d1;
                    }
                }
            }
        }

        return resultEntity;
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag nbt)
    {
        nbt.putByte(NBTConstants.SHAKE, (byte) this.arrowShake);
        nbt.putBoolean(NBTConstants.IS_EXPLOSIVE, this.isExplosive);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag nbt)
    {
        this.arrowShake = nbt.getByte(NBTConstants.SHAKE) & 255;
        this.isExplosive = nbt.getBoolean(NBTConstants.IS_EXPLOSIVE);
    }

    /** Called when an entity collides with this one, 1.12 applyEntityCollision */
    @Override
    public void push(Entity par1Entity)
    {
        super.push(par1Entity);

        if (this.isExplosive && this.tickCount < 20 * 2)
        {
            this.explode();
        }
    }
}
// PORTED

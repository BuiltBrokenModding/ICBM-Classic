package icbm.classic.prefab.entity;

import icbm.classic.api.missiles.IMissileAiming;
import icbm.classic.lib.saving.NbtSaveHandler;
import icbm.classic.lib.world.IProjectileBlockInteraction;
import icbm.classic.lib.world.ProjectileBlockInteraction;
import icbm.classic.world.entity.PlayerSeatEntity;
import net.minecraft.block.material.Material;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.math.*;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageSources;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityLivingBase;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.common.util.ITeleporter;
import org.joml.Math;

import java.util.List;
import java.util.UUID;

/**
 * Modified version of the arrow projectile to be more abstract
 *
 * @author Darkguardsman
 */
public abstract class IcbmProjectile<E extends IcbmProjectile<E>> extends Projectile implements IMissileAiming {
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
    public Vec3 sourceOfProjectile;

    //Settings
    protected int inGroundKillTime = 1200;
    protected int inAirKillTime = 1200;
    /**
     * Damage source to do on impact
     */
    //TODO replace with method allowing more complex calculations
    protected DamageSource impact_damageSource = DamageSources.ANVIL;

    //In ground data
    /**
     * Block position projectile is stuck inside
     */
    public BlockPos tilePos = new BlockPos(0, 0, 0);
    /**
     * Face of tile we are stuck inside
     */
    public Direction sideTile = Direction.UP;
    /**
     * Block state we are stuck inside
     */
    public BlockState blockInside = Blocks.AIR.defaultBlockState();
    /**
     * Toggle to note we are stuck in a tile on the ground
     */
    public boolean inGround = false;
    /**
     * Entity is being teleported, causes it to ignore block impacts
     */
    public boolean changingDimensions = false;

    //Timers
    public int ticksInGround;
    public int ticksInAir;


    public IcbmProjectile(EntityType<? extends IcbmProjectile<?>> type, Level level) {
        super(type, level);
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
    public E init(LivingEntity shooter, LivingEntity target, float multiplier, float random) {
        this.shootingEntity = shooter;
        this.sourceOfProjectile = shooter.getEyePosition();

        this.setPos(this.position().add(0, shooter.getEyeHeight() - 0.10000000149011612D, 0));
        double deltaX = target.getX() - shooter.getX();
        double deltaY = target.getBoundingBox().minY + (double) (target.getBbHeight() / 3.0F) - this.getY(); //TODO why div3
        double deltaZ = target.getZ() - shooter.getZ();
        double deltaMag = Math.sqrt(deltaX * deltaX + deltaZ * deltaZ);

        if (deltaMag >= 1.0E-7D) //TODO why the small num? rounding likely but maybe a better solution
        {
            float yaw = (float) (Math.atan2(deltaZ, deltaX) * 180.0D / Math.PI) - 90.0F;
            float pitch = (float) (-(Math.atan2(deltaY, deltaMag) * 180.0D / Math.PI));

            double subX = deltaX / deltaMag;
            double subZ = deltaZ / deltaMag;
            float subY = (float) deltaMag * 0.2F; //TODO why the 0.2F

            this.moveTo(shooter.getX() + subX, this.getY(), shooter.getZ() + subZ, yaw, pitch, 1, false);
            this.shoot(deltaX, deltaY + (double) subY, deltaZ, multiplier, random);
        }
        return (E) this;
    }

    @Deprecated
    public E init(double x, double y, double z, float yaw, float pitch, float multiplier, float distanceScale) {
        //this.renderDistanceWeight = 10.0D;
        this.sourceOfProjectile = new Vec3(x, y, z);

        this.setSize(0.5F, 0.5F);
        this.moveTo(x, y, z, yaw, pitch, 1, false);

        //TODO figure out why we are updating position by rotation after spawning
        this.setPos(this.position().subtract(
            Math.cos(this.getYRot() / 180.0F * (float) Math.PI) * 0.16F * distanceScale,
            0.10000000149011612D,
            Math.sin(this.getYRot() / 180.0F * (float) Math.PI) * 0.16F * distanceScale
        ));
        //this.yOffset = 0.0F;

        this.setDeltaMovement(new Vec3(
            -Math.sin(this.getYRot() / 180.0F * (float) Math.PI) * Math.cos(this.getXRot() / 180.0F * (float) Math.PI),
            -Math.sin(this.getXRot() / 180.0F * (float) Math.PI),
            Math.cos(this.getYRot() / 180.0F * (float) Math.PI) * Math.cos(this.getXRot() / 180.0F * (float) Math.PI)
        ));
        Vec3 motion = this.getDeltaMovement();
        this.shoot(motion.x(), motion.y(), motion.z(), multiplier, 1.0F);

        return (E) this;
    }

    @Override
    public void initAimingPosition(double x, double y, double z, float yaw, float pitch, float offsetMultiplier, float forceMultiplier) {
        init(x, y, z, yaw, pitch, forceMultiplier, offsetMultiplier);
    }

    @Override
    public void initAimingPosition(Entity shooter, float offsetMultiplier, float forceMultiplier) {
        this.shootingEntity = shooter;
        initAimingPosition(
            shooter.getX(), shooter.getY() + shooter.getEyeHeight(), shooter.getZ(),
            shooter.getYRot(), shooter.getXRot(),
            offsetMultiplier, forceMultiplier
        );
    }

    @Override
    public void tick() {
        super.tick();

        //Check if we hit the ground
        BlockState state = this.level().getBlockState(tilePos);
        if (!state.getBlock().isEmpty(state)) {
            //Check if what we hit can be collided with
            VoxelShape shape = state.getCollisionShape(this.level(), tilePos);
            if (!shape.isEmpty() && shape.bounds().contains(this.position())) {
                setInGround(true);
            }
        }

        //Handle stuck in ground
        if (this.inGround) {
            //TODO allow this to be disabled
            if (state == blockInside) {
                ++this.ticksInGround;

                if (this.ticksInGround == inGroundKillTime) {
                    this.kill();
                }
            }
            // if was in ground but block changes, fall slightly
            else {
                //TODO change to apply gravity
                this.inGround = false;
                this.setDeltaMovement(new Vec3(
                    this.random.nextFloat() * 0.2F,
                    this.random.nextFloat() * 0.2F,
                    this.random.nextFloat() * 0.2F
                ));
                this.ticksInGround = 0;
                this.ticksInAir = 0;
            }
        } else {
            //Kills the projectile if it moves forever into space
            ++this.ticksInAir;
            if (ticksInAir >= inAirKillTime) {
                this.kill();
                return;
            }

            double rayEndVecX = this.getDeltaMovement().x();
            double rayEndVecY = this.getDeltaMovement().y();
            double rayEndVecZ = this.getDeltaMovement().z();

            // Ensure we always raytrace 1 block ahead to allow early detection of collisions
            double velocity = Math.sqrt(rayEndVecX * rayEndVecX + rayEndVecY * rayEndVecY + rayEndVecZ * rayEndVecZ);

            rayEndVecX /= velocity;
            rayEndVecY /= velocity;
            rayEndVecZ /= velocity;

            rayEndVecX *= (velocity + 1);
            rayEndVecY *= (velocity + 1);
            rayEndVecZ *= (velocity + 1);

            //Do raytrace TODO move to prefab entity for reuse
            // Portal block will always return as collision even though it lacks a bounding box
            Vec3 rayStart = this.position();
            Vec3 rayEnd = new Vec3(this.getX() + rayEndVecX, this.getY() + rayEndVecY, this.getZ() + rayEndVecZ);
            RayTraceResult rayHit = this.level().rayTraceBlocks(rayStart, rayEnd, false, true, false);

            //Reset data to do entity ray trace
            rayStart = this.position();
            rayEnd = new Vec3(this.getX() + this.getDeltaMovement().x(),
                this.getY() + this.getDeltaMovement().y(), this.getZ() + this.getDeltaMovement().z());
            if (rayHit != null) {
                rayEnd = new Vec3(rayHit.hitVec.x, rayHit.hitVec.y, rayHit.hitVec.z);
            }

            //Handle entity collision boxes
            Entity entity = null;
            List<Entity> nearbyEntities = this.level().getEntities(this, this.getBoundingBox().move(this.getDeltaMovement())
                .inflate(1.0D, 1.0D, 1.0D), entity1 -> entity1 != this);
            double distanceToHit = 0.0D;
            float hitBoxSizeScale;

            for (Entity checkEntity : nearbyEntities) {

                if (shouldCollideWith(checkEntity) && (checkEntity != this.shootingEntity || this.ticksInAir >= 5)) {
                    hitBoxSizeScale = 0.3F;
                    AABB hitBox = checkEntity.getBoundingBox().inflate(hitBoxSizeScale, hitBoxSizeScale, hitBoxSizeScale);
                    RayTraceResult entityRayHit = hitBox.calculateIntercept(rayStart, rayEnd);

                    if (entityRayHit != null) {
                        double distance = rayStart.distanceTo(entityRayHit.hitVec);

                        if (distance < distanceToHit || distanceToHit == 0.0D) {
                            entity = checkEntity;
                            distanceToHit = distance;
                        }
                    }
                }
            }

            //If we collided with an entity, set hit to entity
            if (entity != null) {
                rayHit = new RayTraceResult(entity);
            }


            if (rayHit != null && rayHit.typeOfHit != RayTraceResult.Type.MISS && !ignoreImpact(rayHit)) {
                //Handle entity hit
                if (rayHit.typeOfHit == RayTraceResult.Type.ENTITY) {
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

    protected void setInGround(boolean b) {
        if (!this.inGround && b) {
            this.ticksInGround = 0;
        }
        this.inGround = b;
    }

    protected boolean ignoreImpact(RayTraceResult hit) {
        return false;
    }


    protected void postImpact(RayTraceResult hit) {
    }

    /**
     * Called to see if collision checks should be ignored on the
     * entity.
     *
     * @param entity
     * @return true to collide, false to ignore
     */
    protected boolean shouldCollideWith(Entity entity) {
        //TODO add listener support
        return entity.canBeCollidedWith() && !(entity instanceof PlayerSeatEntity);
    }

    protected void handleBlockCollision(RayTraceResult hit) {
        this.tilePos = hit.getBlockPos();
        this.sideTile = hit.sideHit;
        this.blockInside = this.world.getBlockState(tilePos);

        // Special handling for ender gateways TODO move to a registry of Block -> lambda
        final IProjectileBlockInteraction.EnumHitReactions reaction =
            ProjectileBlockInteraction.handleSpecialInteraction(world, tilePos, hit.hitVec, sideTile, blockInside, this);
        if (reaction.stop) {
            return;
        }

        // Move entity to collision location
        moveTowards(hit.hitVec, width / 2f);

        //TODO this.playSound("random.bowhit", 1.0F, 1.2F / (this.rand.nextFloat() * 0.2F + 0.9F));

        if (this.blockInside.getMaterial() != Material.AIR) {
            this.blockInside.getBlock().onEntityCollidedWithBlock(this.world, tilePos, blockInside, this);
        }

        if (!changingDimensions && !isDead && !inPortal && reaction != IProjectileBlockInteraction.EnumHitReactions.CONTINUE_NO_IMPACT) {
            this.motionX = 0;
            this.motionY = 0;
            this.motionZ = 0;
            setInGround(true);
            onImpactTile(hit);
        }
    }

    public void moveTowards(Vec3 hit, double offset) {
        Vec3 delta = hit.subtract(this.position());
        double mag = delta.length();
        Vec3 vec = new Vec3(delta.x / mag, delta.y / mag, delta.z / mag).multiply(offset, offset, offset);

        this.setPos(hit.add(vec));
    }

    @Override
    public Entity changeDimension(ServerLevel level, ITeleporter teleporter) {
        this.changingDimensions = true;
        return super.changeDimension(level, teleporter);
    }

    @Override
    public int getPortalCooldown() {
        return 10;
    }

    @Override
    public int getPortalWaitTime() {
        return -1;
    }

    /**
     * Called when the projectile impacts a tile.
     * Data about the hit is stored in the entity
     * include location, block, and meta
     *
     * @param hit - exact hit position
     */
    protected void onImpactTile(RayTraceResult hit) {
        onImpact(hit.hitVec);
    }

    /**
     * Handles entity being impacted by the projectile
     *
     * @param hit
     * @param entityHit
     */
    protected void handleEntityCollision(RayTraceResult hit, Entity entityHit) {
        onImpactEntity(entityHit, (float) getVelocity().magnitude(), hit);
    }


    protected void onImpactEntity(Entity entityHit, float velocity, RayTraceResult hit) {
        if (!world.isClientSide()) {
            int damage = MathHelper.ceil((double) velocity * 2);

            //If entity takes damage add velocity to entity
            if (impact_damageSource != null && entityHit.attackEntityFrom(impact_damageSource, (float) damage)) {
                if (entityHit instanceof EntityLivingBase) {
                    float vel_horizontal = MathHelper.sqrt(this.motionX * this.motionX + this.motionZ * this.motionZ);
                    if (vel_horizontal > 0.0F) {
                        entityHit.addVelocity(this.motionX * 0.6000000238418579D / (double) vel_horizontal, 0.1D, this.motionZ * 0.6000000238418579D / (double) vel_horizontal);
                    }
                }

            }
            onImpact(hit.hitVec);
        }
    }

    /**
     * Generalized impact callback, used to do cleanup
     * steps regardless of impact reason.
     * <p>
     * Use {@link #onImpactEntity(Entity, float, RayTraceResult)} or {@link #onImpactTile(RayTraceResult)} for
     * better handling of impacts.
     */
    protected void onImpact(Vec3 impactLocation) {
        this.kill();
    }

    protected void updateMotion() {
        //Update motion
        this.setPos(this.position().add(this.getDeltaMovement()));

        if (shouldAlignWithMotion()) {
            rotateTowardsMotion();
        }

        //Decrease motion so the projectile stops
        decreaseMotion();

        //Adjust for collision      TODO check if works, rewrite code to prevent clip through of block
        this.doBlockCollisions();
    }

    public boolean shouldAlignWithMotion() {
        return true;
    }

    public void rotateTowardsMotion() {
        //Get rotation from motion
        double speed = this.getDeltaMovement().horizontalDistance();
        float yaw = (float) (Math.atan2(this.getDeltaMovement().x(), this.getDeltaMovement().z()) * 180.0D / Math.PI);
        float pitch = (float) (Math.atan2(this.getDeltaMovement().y(), speed) * 180.0D / Math.PI);

        //-------------------------------------
        //Fix rotation
        while (pitch - this.getXRot() < -180.0F) {
            this.prevRotationPitch -= 360.0F;
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
        //-------------------------------------
        //Reduce delta in rotation to provide for a smoother animation
        this.getXRot() = this.prevRotationPitch + (this.getXRot() - this.prevRotationPitch) * 0.2F;
        this.getYRot() = this.prevRotationYaw + (this.getYRot() - this.prevRotationYaw) * 0.2F;
        //-------------------------------------
    }

    protected void decreaseMotion() {
        //TODO get friction value
        this.motionX *= 0.99F;
        this.motionY *= 0.99F;
        this.motionZ *= 0.99F;
        //Add gravity so the projectile will fall
        this.motionY -= 0.05F;
    }


    @Override
    public void shoot(double xx, double yy, double zz, float multiplier, float random) {
        //Normalize
        double velocity = Math.sqrt(xx * xx + yy * yy + zz * zz);
        xx /= velocity;
        yy /= velocity;
        zz /= velocity;

        //Add randomization to make the arrow miss
        if (random > 0) {
            xx += this.random.nextGaussian() * (this.random.nextBoolean() ? -1D : 1D) * 0.007499999832361937D * (double) random;
            yy += this.random.nextGaussian() * (this.random.nextBoolean() ? -1D : 1D) * 0.007499999832361937D * (double) random;
            zz += this.random.nextGaussian() * (this.random.nextBoolean() ? -1D : 1D) * 0.007499999832361937D * (double) random;
        }

        //Add multiplier
        xx *= multiplier;
        yy *= multiplier;
        zz *= multiplier;

        //Set motion
        this.setDeltaMovement(new Vec3(xx, yy, zz));

        //Update rotation
        double f3 = Math.sqrt(xx * xx + zz * zz);
        this.setYRot((float) (Math.atan2(xx, zz) * 180.0D / Math.PI));
        this.setXRot((float) (Math.atan2(yy, f3) * 180.0D / Math.PI));
        this.ticksInGround = 0;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void setVelocity(double xx, double yy, double zz) {
        this.motionX = xx;
        this.motionY = yy;
        this.motionZ = zz;
    }

    @Override
    protected boolean canTriggerWalking() {
        return false;
    }

    @Override
    protected boolean canHitEntity(Entity pTarget) {
        return hasHealth;
    }

    @Override
    public boolean canDamage(Entity entity, DamageSource source) {
        return hasHealth;
    }

    @Override
    public void writeEntityToNBT(CompoundTag nbt) {
        SAVE_LOGIC.save(this, nbt);
        super.writeEntityToNBT(nbt);
    }

    @Override
    public void readEntityFromNBT(CompoundTag nbt) {
        super.readEntityFromNBT(nbt);
        SAVE_LOGIC.load(this, nbt);
    }

    private static final NbtSaveHandler<IcbmProjectile> SAVE_LOGIC = new NbtSaveHandler<IcbmProjectile>()
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
        .base();
    //Project source, if needed implement in each projectile directly. or add a boolean toggle to .addRoot. As missile doesn't need source nor shooter due to missile.targetData
        /*
        .addRoot("source")
        .nodePos("pos", (projectile) -> projectile.sourceOfProjectile, (projectile, pos) -> projectile.sourceOfProjectile = pos)
        .nodeUUID("uuid", (projectile) -> projectile.shootingEntityUUID, (projectile, uuid) -> projectile.shootingEntityUUID = uuid)
        .base();
        */
}

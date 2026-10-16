package icbm.classic.lib.projectile;

import icbm.classic.ICBMConstants;
import icbm.classic.api.actions.cause.IActionSource;
import icbm.classic.api.data.D3Consumer;
import icbm.classic.api.missiles.IMissileAiming;
import icbm.classic.api.missiles.projectile.IProjectileThrowable;
import icbm.classic.content.blast.ICBMDamageTypes;
import icbm.classic.content.entity.EntityPlayerSeat;
import icbm.classic.lib.saving.NbtSaveHandler;
import icbm.classic.lib.transform.vector.Pos;
import icbm.classic.lib.world.IProjectileBlockInteraction;
import icbm.classic.lib.world.ProjectileBlockInteraction;
import icbm.classic.prefab.entity.EntityICBM;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.portal.DimensionTransition;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.entity.IEntityWithComplexSpawn;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Modified version of the arrow projectile to be more abstract
 *
 * @author Darkguardsman
 */
public abstract class EntityProjectile<PROJECTILE extends EntityProjectile<PROJECTILE>> extends EntityICBM implements IMissileAiming, IEntityWithComplexSpawn, IProjectileThrowable<PROJECTILE> {

    /**
     * Effectively just render distance but scales with entity size
     */
    public static final double RENDER_DISTANCE_SCALE = 256; //TODO add config

    // Default is motionY *= 0.9800000190734863D
    public static final float DEFAULT_GRAVITY = 0.05F;

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
     * zombies don't auto go to players. Instead, they can move towards the firing
     * location. Think of it like moving towards the sound of the weapon.
     */
    public Pos sourceOfProjectile;

    //Settings
    @Setter
    @Getter
    @Accessors(chain = true)
    protected int inGroundKillTime = ICBMConstants.TICKS_MIN;
    @Setter
    @Getter
    @Accessors(chain = true)
    protected int inAirKillTime = ICBMConstants.TICKS_MIN;

    //In ground data
    @Setter(value = AccessLevel.PROTECTED)
    @Getter
    private InGroundData inGroundData;
    /**
     * Entity is being teleported, causes it to ignore block impacts
     */
    public boolean changingDimensions = false;

    //Timers
    public int ticksInGround;
    public int ticksInAir;

    // Debug
    public boolean freezeMotion = false;

    public EntityProjectile(EntityType<? extends EntityProjectile> type, Level world) {
        super(type, world);
        // PORT: size 0.5x0.5 is set by the EntityType in EntityReg
    }

    @Override
    public void writeSpawnData(RegistryFriendlyByteBuf buffer) {
        buffer.writeBoolean(freezeMotion);
    }

    @Override
    public void readSpawnData(RegistryFriendlyByteBuf additionalData) {
        freezeMotion = additionalData.readBoolean();
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
    public PROJECTILE init(LivingEntity shooter, LivingEntity target, float multiplier, float random) {
        this.shootingEntity = shooter;
        this.sourceOfProjectile = new Pos(shooter);

        final double posY = shooter.getY() + (double) shooter.getEyeHeight() - 0.10000000149011612D;
        this.setPos(getX(), posY, getZ());
        double deltaX = target.getX() - shooter.getX();
        double deltaY = target.getBoundingBox().minY + (double) (target.getBbHeight() / 3.0F) - posY; //TODO why div3
        double deltaZ = target.getZ() - shooter.getZ();
        double deltaMag = Math.sqrt(deltaX * deltaX + deltaZ * deltaZ);

        if (deltaMag >= 1.0E-7D) //TODO why the small num? rounding likely but maybe a better solution
        {
            float yaw = (float) (Math.atan2(deltaZ, deltaX) * 180.0D / Math.PI) - 90.0F;
            float pitch = (float) (-(Math.atan2(deltaY, deltaMag) * 180.0D / Math.PI));

            double subX = deltaX / deltaMag;
            double subZ = deltaZ / deltaMag;
            float subY = (float) deltaMag * 0.2F; //TODO why the 0.2F

            this.moveTo(shooter.getX() + subX, posY, shooter.getZ() + subZ, yaw, pitch);
            this.shoot(deltaX, deltaY + (double) subY, deltaZ, multiplier, random);
        }
        return (PROJECTILE) this;
    }

    @Deprecated
    public PROJECTILE init(double x, double y, double z, float yaw, float pitch, float multiplier, float distanceScale) {
        this.sourceOfProjectile = new Pos(x, y, z);
        this.moveTo(x, y, z, yaw, pitch);

        //TODO figure out why we are updating position by rotation after spawning
        final float yRot = this.getYRot();
        final float xRot = this.getXRot();
        final double posX = getX() - (double) (Mth.cos(yRot / 180.0F * (float) Math.PI) * 0.16F * distanceScale);
        final double posY = getY() - 0.10000000149011612D; //TODO magic number - likely half arrow height
        final double posZ = getZ() - (double) (Mth.sin(yRot / 180.0F * (float) Math.PI) * 0.16F * distanceScale);
        this.setPos(posX, posY, posZ);

        final double motionX = (double) (-Mth.sin(yRot / 180.0F * (float) Math.PI) * Mth.cos(xRot / 180.0F * (float) Math.PI));
        final double motionZ = (double) (Mth.cos(yRot / 180.0F * (float) Math.PI) * Mth.cos(xRot / 180.0F * (float) Math.PI));
        final double motionY = (double) (-Mth.sin(xRot / 180.0F * (float) Math.PI));
        this.setDeltaMovement(motionX, motionY, motionZ);
        this.shoot(motionX, motionY, motionZ, multiplier, 0);

        return (PROJECTILE) this;
    }

    @Override
    public void initAimingPosition(double x, double y, double z, float yaw, float pitch, float offsetMultiplier, float forceMultiplier) {
        init(x, y, z, yaw, pitch, forceMultiplier, offsetMultiplier);
    }

    @Override
    public void initAimingPosition(Entity shooter, float offsetMultiplier, float forceMultiplier) {
        this.shootingEntity = shooter;
        initAimingPosition(
            shooter.getX(), shooter.getY() + (double) shooter.getEyeHeight(), shooter.getZ(),
            shooter.getYRot(), shooter.getXRot(),
            offsetMultiplier, forceMultiplier
        );
    }

    @Override
    public boolean throwProjectile(@Nonnull PROJECTILE entity, @Nullable IActionSource source, double x, double y, double z, float yaw, float pitch, float velocity, float random) {
        initAimingPosition(
            x, y, z,
            yaw, pitch,
            1, velocity
        );
        // TODO implement random
        return true;
    }

    @Override
    public void tick() {
        super.tick();

        this.checkInGround();

        // Handle ground logic
        if (this.inGroundData != null) {

            // Kill projectile if in ground for a while
            ++this.ticksInGround;

            if (shouldExpire()) {
                this.destroy();
            }
        }
        // Handle in air logic
        else {

            //Kills the projectile if it moves forever into space
            ++this.ticksInAir;
            if (shouldExpire()) {
                this.destroy();
                return;
            }

            final Vec3 motion = getDeltaMovement();

            // Ensure we always raytrace 1 block ahead to allow early detection of collisions
            final double velocity = Math.sqrt(motion.x * motion.x + motion.y * motion.y + motion.z * motion.z);

            //Do raytrace TODO move to prefab entity for reuse
            HitResult rayHit = null;
            // PORT: skip the block trace when not moving. 0/0 gives a NaN ray end, 1.12 rayTraceBlocks returned null for it
            // but 1.21 BlockGetter#traverseBlocks floors NaN to 0 and looks up (loading) block 0,0,0 every tick
            if (velocity > 1.0E-7D)
            {
                final double scale = (velocity + 1) / velocity;
                final Vec3 blockRayStart = new Vec3(this.getX(), this.getY(), this.getZ());
                final Vec3 blockRayEnd = new Vec3(this.getX() + motion.x * scale, this.getY() + motion.y * scale, this.getZ() + motion.z * scale);
                // PORT: 1.12 rayTraceBlocks(start, end, false, true, false) -> collider shapes, ignore fluids
                final BlockHitResult blockHit = this.level().clip(new ClipContext(blockRayStart, blockRayEnd, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this));
                if (blockHit.getType() != HitResult.Type.MISS)
                {
                    rayHit = blockHit;
                }
            }

            //Reset data to do entity ray trace
            final Vec3 rayStart = new Vec3(this.getX(), this.getY(), this.getZ());
            Vec3 rayEnd = new Vec3(this.getX() + motion.x, this.getY() + motion.y, this.getZ() + motion.z);
            if (rayHit != null) {
                rayEnd = rayHit.getLocation();
            }

            //Handle entity collision boxes
            Entity entity = null;
            // PORT: 1.12 AABB#expand only grew towards the sign of the arguments, same as expandTowards
            final List<Entity> list = this.level().getEntities(this, this.getBoundingBox().move(motion.x, motion.y, motion.z).expandTowards(1.0D, 1.0D, 1.0D));
            double distanceToHit = 0.0D;

            for (Entity checkEntity : list) {
                if (shouldCollideWith(checkEntity) && (checkEntity != this.shootingEntity || this.ticksInAir >= 5)) { //TODO why 5 ticks specifically? Why not 'has collider left shooter'
                    final AABB hitBox = checkEntity.getBoundingBox().expandTowards(0.3F, 0.3F, 0.3F);
                    final Optional<Vec3> entityRayHit = hitBox.clip(rayStart, rayEnd);

                    if (entityRayHit.isPresent()) {
                        double distance = rayStart.distanceTo(entityRayHit.get());

                        if (distance < distanceToHit || distanceToHit == 0.0D) {
                            entity = checkEntity;
                            distanceToHit = distance;
                        }
                    }
                }
            }

            //If we collided with an entity, set hit to entity
            if (entity != null) {
                rayHit = new EntityHitResult(entity);
            }

            if (rayHit != null && rayHit.getType() != HitResult.Type.MISS && !ignoreImpact(rayHit)) {
                //Handle entity hit
                if (rayHit instanceof EntityHitResult entityHitResult) {
                    final Entity entityHit = entityHitResult.getEntity();
                    //rayHit.hitVec == rayHit.entityHit.pos, So, hitbox points are calculated to prevent the entity from disappearing.
                    final Optional<Vec3> hitPoint = entityHit.getBoundingBox().clip(
                        new Vec3(getX(), getY(), getZ()),
                        new Vec3(entityHit.getX(), entityHit.getY(), entityHit.getZ())
                    );

                    // PORT: NeoForge ProjectileImpactEvent only accepts vanilla Projectile entities, not fired for ICBM projectiles
                    handleEntityCollision(hitPoint.<HitResult>map(vec -> new EntityHitResult(entityHit, vec)).orElse(rayHit), entityHit);
                    postImpact(rayHit);
                }
                //Handle block hit
                else if (rayHit instanceof BlockHitResult blockHitResult)
                {
                    // PORT: NeoForge ProjectileImpactEvent only accepts vanilla Projectile entities, not fired for ICBM projectiles
                    handleBlockCollision(blockHitResult, velocity);
                    postImpact(rayHit);
                }
            }
            updateMotion();
        }
    }

    /**
     * Runs each tick to check what block we are inside. Raytrace this in air movement
     * but once in ground we need to refresh manually to detect block breaking.
     *
     * If block isn't the same or has changed logic will revalidate. If block has no
     * collision then things will fall
     */
    protected void checkInGround() {
        //Check if in ground TODO do we need to run this every tick?
        final BlockPos tilePos = inGroundData != null ? inGroundData.getPos() : this.blockPosition();
        final BlockState state = this.level().getBlockState(tilePos);
        final InGroundData prevInGround = this.inGroundData;

        // Only run logic if we don't have an existing collision or detected block has changed TODO use events to trigger this on block edits
        if (inGroundData == null || state != inGroundData.getState()) {
            this.inGroundData = null;

            if (!state.isAir()) {
                //Check if what we hit can be collided with
                final VoxelShape shape = state.getCollisionShape(this.level(), tilePos);
                if (!shape.isEmpty() && shape.bounds().move(tilePos).contains(this.position())) {
                    final Direction side = Direction.UP; //TODO calculate side based on position
                    this.inGroundData = new InGroundData(tilePos, side, state);
                }
            }
        }

        // We are no longer in the ground
        if (prevInGround != null && this.inGroundData == null) {
            //TODO change to apply gravity instead of random
            final Vec3 motion = getDeltaMovement();
            this.setDeltaMovement(
                motion.x * (double) (this.random.nextFloat() * 0.2F),
                motion.y * (double) (this.random.nextFloat() * 0.2F),
                motion.z * (double) (this.random.nextFloat() * 0.2F));
            this.ticksInGround = 0;
            this.ticksInAir = 0;
        }
    }

    /**
     * Called to check if the projectile
     * should invoke {@link #destroy()}
     *
     * @return true to expire
     */
    protected boolean shouldExpire() {
        // PORT: 1.12 used y < -100, worlds now go down to -64 so the limit is relative to the min build height
        return ticksInAir >= inAirKillTime || ticksInGround >= inGroundKillTime || this.getY() < level().getMinBuildHeight() - 100;
    }


    @Override
    protected void destroy() {
        stopRiding();
        ejectPassengers();
        super.destroy();
    }

    protected boolean ignoreImpact(HitResult hit) {
        return false;
    }

    protected void postImpact(HitResult hit) {
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
        return entity.isPickable() && !(entity instanceof EntityPlayerSeat);
    }

    protected void handleBlockCollision(BlockHitResult hit, double velocity) {
        this.inGroundData = new InGroundData(level(), hit);

        // Special handling for ender gateways TODO move to a registry of Block -> lambda
        final IProjectileBlockInteraction.EnumHitReactions reaction = specialHandleBlock(hit, velocity);
        if (reaction.stop) {
            return;
        }

        // Move entity to collision location
        moveTowards(hit.getLocation(), getBbWidth() / 2f);

        //TODO this.playSound("random.bowhit", 1.0F, 1.2F / (this.rand.nextFloat() * 0.2F + 0.9F));

        if (!this.inGroundData.getState().isAir()) {
            this.inGroundData.getState().entityInside(this.level(), this.inGroundData.getPos(), this);
        }

        if (!changingDimensions && !isRemoved() && !isInPortal() && reaction != IProjectileBlockInteraction.EnumHitReactions.CONTINUE_NO_IMPACT) {
            onImpactTile(hit);
            this.setDeltaMovement(0, 0, 0);
        }
    }

    /** Replaces 1.12 Entity#inPortal */
    protected boolean isInPortal() {
        return this.portalProcess != null && this.portalProcess.isInsidePortalThisTick(); // PORT-CHECK: PortalProcessor#isInsidePortalThisTick name in 1.21.1
    }

    protected IProjectileBlockInteraction.EnumHitReactions specialHandleBlock(BlockHitResult hit, double velocity) {
        return ProjectileBlockInteraction.handleSpecialInteraction(level(), this.inGroundData.getPos(), hit.getLocation(), this.inGroundData.getSide(), this.inGroundData.getState(), this);
    }

    public void moveTowards(Vec3 hit, double offset) {

        final double deltaX = hit.x - this.getX();
        final double deltaY = hit.y - this.getY();
        final double deltaZ = hit.z - this.getZ();

        final float mag = (float) Math.sqrt(deltaX * deltaX + deltaY * deltaY + deltaZ * deltaZ);
        final double vecX = (deltaX / (double) mag) * offset;
        final double vecY = (deltaX / (double) mag) * offset; // PORT: kept 1.12 behaviour (uses deltaX)
        final double vecZ = (deltaZ / (double) mag) * offset;

        setPos(hit.x + vecX, hit.y + vecY, hit.z + vecZ);
    }

    @Nullable
    @Override
    public Entity changeDimension(DimensionTransition transition) {
        this.changingDimensions = true;
        final Entity result = super.changeDimension(transition);
        // PORT: 1.21 returns this same instance for same-level teleports (end gateways) and for failed changes.
        // 1.12 gateways used setPositionAndUpdate and never set the flag, so clear it or block impacts stay disabled forever
        if (result == this || result == null && !this.isRemoved())
        {
            this.changingDimensions = false;
        }
        return result;
    }

    @Override
    public int getDimensionChangingDelay() {
        return 10;
    }
    // PORT: 1.12 getMaxInPortalTime() = -1 (instant), 1.21 non-player entities already transition instantly

    /**
     * Called when the projectile impacts a tile.
     * Data about the hit is stored in the entity
     * include location, block, and meta
     *
     * @param hit - exact hit position
     */
    protected void onImpactTile(BlockHitResult hit) {
        onImpact(hit);
    }

    /**
     * Handles entity being impacted by the projectile
     *
     * @param hit
     * @param entityHit
     */
    protected void handleEntityCollision(@Nonnull HitResult hit, @Nonnull Entity entityHit) {
        onImpactEntity(entityHit, (float) getVelocity().magnitude(), hit);
    }

    /**
     * Called when the projectile impacts an entity. The location of the projectile
     * may not be in the same location as the impacted entity. As impact is calculated
     * by the raytrace with no update on position provided. Meaning impact is often
     * position + velocity.
     *
     * @param entityHit by the projectile
     * @param velocity  during the impact
     * @param hit       trace used to calculate the impact, use this for projectile position
     */

    protected void onImpactEntity(@Nonnull Entity entityHit, float velocity, @Nonnull HitResult hit) {
        if (!level().isClientSide()) {
            final float damage = getImpactDamage(entityHit, velocity, hit);
            final DamageSource damageSource = getImpactDamageSource(entityHit, velocity, hit);

            if (damageSource != null && entityHit.hurt(damageSource, damage)
                && entityHit instanceof LivingEntity) {
                applyKnockBack(entityHit);
            }
            onImpact(hit);
            // TODO add deflection for some projectiles, so when impact entities the projectile might redirect rather than vanish
        }
    }

    /**
     * Called to apply knock back effects to entity during impact
     *
     * @param entity to move
     */
    protected void applyKnockBack(Entity entity) {
        final double vertKnock = 0.6000000238418579D;
        final double hortKnock = 0.1;

        final Vec3 motion = getDeltaMovement();
        final float vel_horizontal = (float) Math.sqrt(motion.x * motion.x + motion.z * motion.z);
        if (vel_horizontal > 0.0F) {
            entity.push(
                motion.x * vertKnock / (double) vel_horizontal,
                hortKnock,
                motion.z * vertKnock / (double) vel_horizontal
            );
        }
    }

    /**
     * Called to get damage to apply during impact
     *
     * @param entityHit by the projectile
     * @param velocity  during the impact
     * @param hit       trace used to calculate the impact, use this for projectile position
     * @return damage to apply
     */
    protected float getImpactDamage(Entity entityHit, float velocity, HitResult hit) {
        return Mth.ceil(velocity * 2f);
    }

    /**
     * Called to get damage source to apply during impact
     *
     * @param entityHit by the projectile
     * @param velocity  during the impact
     * @param hit       trace used to calculate the impact, use this for projectile position
     * @return damage source to use
     */
    protected DamageSource getImpactDamageSource(Entity entityHit, float velocity, HitResult hit) {
        // PORT: 1.12 static DAMAGE_SOURCE "icbmclassic:projectile", now a data driven damage type
        return ICBMDamageTypes.source(level(), ICBMDamageTypes.PROJECTILE, this, shootingEntity);
    }

    /**
     * Generalized impact callback, used to do cleanup
     * steps regardless of impact reason.
     */
    protected void onImpact(@Nonnull HitResult hit) {
        this.discard();
    }

    protected void updateMotion() {
        if (!freezeMotion && isServer()) {
            //Update motion
            final Vec3 motion = getDeltaMovement();
            final double posX = this.getX() + motion.x;
            final double posY = this.getY() + motion.y;
            final double posZ = this.getZ() + motion.z;

            if (shouldAlignWithMotion()) {
                rotateTowardsMotion(0.05f);
            }

            //Decrease motion so the projectile stops
            decreaseMotion();

            //Set position
            this.setPos(posX, posY, posZ);

            //Adjust for collision      TODO check if works, rewrite code to prevent clip through of block
            this.checkInsideBlocks();
        }
    }

    public boolean shouldAlignWithMotion() {
        return true;
    }

    public void rotateTowardsMotion(float delta) {
        //Get rotation from motion
        final Vec3 motion = getDeltaMovement();
        float speed = (float) Math.sqrt(motion.x * motion.x + motion.z * motion.z);
        float yaw = (float) (Math.atan2(motion.x, motion.z) * 180.0D / Math.PI);
        float pitch = (float) (Math.atan2(motion.y, (double) speed) * 180.0D / Math.PI);

        //-------------------------------------
        //Fix rotation
        while (pitch - this.xRotO < -180.0F) {
            this.xRotO -= 360.0F;
        }

        while (pitch - this.xRotO >= 180.0F) {
            this.xRotO += 360.0F;
        }

        while (yaw - this.yRotO < -180.0F) {
            this.yRotO -= 360.0F;
        }

        while (yaw - this.yRotO >= 180.0F) {
            this.yRotO += 360.0F;
        }
        //-------------------------------------
        //Reduce delta in rotation to provide for a smoother animation
        this.setXRot(this.xRotO + (pitch - this.xRotO) * delta);
        this.setYRot(this.yRotO + (yaw - this.yRotO) * delta);
        //-------------------------------------
    }

    /**
     * Called each tick, assuming no collisions, to slow
     * down the projectile. Both via air resistance and
     * gravity constant.
     */
    protected void decreaseMotion() {
        final float airResistance = getAirResistance();
        final Vec3 motion = getDeltaMovement();
        //Add gravity so the projectile will fall
        this.setDeltaMovement(motion.x * airResistance, motion.y * airResistance - (float) getGravity(), motion.z * airResistance);
    }

    /**
     * Multiplier to simulate air resistance.
     *
     * @return multiplier
     */
    protected float getAirResistance() {
        return 0.99f;
    }

    /**
     * Amount to subtract each tick to create
     * a pull towards the ground
     *
     * PORT: 1.12 getGravity(), vanilla Entity#getGravity() is final and wraps this (0 when no-gravity is set)
     *
     * @return gravity acceleration as m/t
     */
    @Override
    protected double getDefaultGravity() {
        return DEFAULT_GRAVITY;
    }

    /** 1.12 IProjectile#shoot */
    public void shoot(double xx, double yy, double zz, float speed, float random) {
        //Normalize
        float magnitude = (float) Math.sqrt(xx * xx + yy * yy + zz * zz);
        xx /= (double) magnitude;
        yy /= (double) magnitude;
        zz /= (double) magnitude;

        //Add randomization to make the arrow miss
        if (random > 0) {
            xx += this.random.nextGaussian() * (double) (this.random.nextBoolean() ? -1 : 1) * 0.007499999832361937D * (double) random;
            yy += this.random.nextGaussian() * (double) (this.random.nextBoolean() ? -1 : 1) * 0.007499999832361937D * (double) random;
            zz += this.random.nextGaussian() * (double) (this.random.nextBoolean() ? -1 : 1) * 0.007499999832361937D * (double) random;
        }

        //Add multiplier
        xx *= (double) speed;
        yy *= (double) speed;
        zz *= (double) speed;

        //Set motion
        this.setDeltaMovement(xx, yy, zz);

        //Update rotation
        float f3 = (float) Math.sqrt(xx * xx + zz * zz);
        final float yaw = (float) (Math.atan2(xx, zz) * 180.0D / Math.PI);
        final float pitch = (float) (Math.atan2(yy, (double) f3) * 180.0D / Math.PI);
        this.setYRot(yaw);
        this.setXRot(pitch);
        this.yRotO = yaw;
        this.xRotO = pitch;
        this.ticksInGround = 0;
    }

    protected static void vectorFromAngles(float yaw, float pitch, D3Consumer callback) {
        callback.apply(
            -Mth.sin(yaw * 0.017453292F) * Mth.cos(pitch * 0.017453292F),
            -Mth.sin(pitch * 0.017453292F),
            Mth.cos(yaw * 0.017453292F) * Mth.cos(pitch * 0.017453292F));
    }

    /** PORT: replaces 1.12 setPositionAndRotationDirect, snaps instead of interpolating */
    @Override
    public void lerpTo(double x, double y, double z, float yaw, float pitch, int posRotationIncrements) {
        this.setPos(x, y, z);
        this.setRot(yaw, pitch);
    }

    @Override
    protected MovementEmission getMovementEmission() {
        return MovementEmission.NONE;
    }

    public boolean canBeDestroyed(Object attacker, DamageSource damageSource) {
        return hasHealth;
    }


    @Override
    public boolean shouldRenderAtSqrDistance(double distanceSq) {
        final double renderDistance = getAverageEdgeLength() * getViewScale() * getRenderDistance();
        return distanceSq < renderDistance * renderDistance;
    }

    protected double getRenderDistance() {
        return RENDER_DISTANCE_SCALE;
    }

    protected double getAverageEdgeLength() {
        final AABB boundingBox = this.getBoundingBoxForCulling();
        if (boundingBox == null) {
            return 1;
        }
        double size = boundingBox.getSize();
        return Double.isNaN(size) || Double.isInfinite(size) ? 1 : size;
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag nbt) {
        SAVE_LOGIC.save(this, nbt);
        super.addAdditionalSaveData(nbt);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag nbt) {
        super.readAdditionalSaveData(nbt);
        SAVE_LOGIC.load(this, nbt);
    }

    private static final NbtSaveHandler<EntityProjectile> SAVE_LOGIC = new NbtSaveHandler<EntityProjectile>()
        //Stuck in ground data
        .mainRoot()
        .nodeINBTSerializable("ground", EntityProjectile::getInGroundData, EntityProjectile::setInGroundData, InGroundData::new)
        .base()
        //Ticks
        .addRoot("ticks")
        /* */.nodeInteger("air", EntityProjectile::getInAirKillTime, EntityProjectile::setInAirKillTime)
        /* */.nodeInteger("ground", EntityProjectile::getInGroundKillTime, EntityProjectile::setInGroundKillTime)
        .base();
}
// PORTED

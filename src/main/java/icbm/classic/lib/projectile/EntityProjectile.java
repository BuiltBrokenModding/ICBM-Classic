package icbm.classic.lib.projectile;

import icbm.classic.ICBMClassic;
import icbm.classic.ICBMConstants;
import icbm.classic.api.actions.cause.IActionSource;
import icbm.classic.api.data.D3Consumer;
import icbm.classic.api.missiles.IMissileAiming;
import icbm.classic.api.missiles.projectile.IProjectileThrowable;
import icbm.classic.content.entity.EntityPlayerSeat;
import icbm.classic.lib.saving.NbtSaveHandler;
import icbm.classic.lib.transform.vector.Pos;
import icbm.classic.lib.world.IProjectileBlockInteraction;
import icbm.classic.lib.world.ProjectileBlockInteraction;
import icbm.classic.prefab.entity.EntityICBM;
import io.netty.buffer.ByteBuf;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IProjectile;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.*;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ITeleporter;
import net.minecraftforge.fml.common.registry.IEntityAdditionalSpawnData;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.UUID;

/**
 * Modified version of the arrow projectile to be more abstract
 *
 * @author Darkguardsman
 */
public abstract class EntityProjectile<PROJECTILE extends EntityProjectile<PROJECTILE>> extends EntityICBM implements IProjectile, IMissileAiming, IEntityAdditionalSpawnData, IProjectileThrowable<PROJECTILE> {

    /**
     * Effectively just render distance but scales with entity size
     */
    public static final double RENDER_DISTANCE_SCALE = 256; //TODO add config
    /**
     * Generic damage source to use on impact
     */
    public static final DamageSource DAMAGE_SOURCE = new DamageSource("icbmclassic:projectile").setProjectile();

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

    public EntityProjectile(World world) {
        super(world);
        this.setSize(0.5F, 0.5F);
    }

    @Override
    public void writeSpawnData(ByteBuf buffer) {
        buffer.writeBoolean(freezeMotion);
    }

    @Override
    public void readSpawnData(ByteBuf additionalData) {
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
    public PROJECTILE init(EntityLivingBase shooter, EntityLivingBase target, float multiplier, float random) {
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
        return (PROJECTILE) this;
    }

    @Deprecated
    public PROJECTILE init(double x, double y, double z, float yaw, float pitch, float multiplier, float distanceScale) {
        this.sourceOfProjectile = new Pos(x, y, z);
        this.setLocationAndAngles(x, y, z, yaw, pitch);

        //TODO figure out why we are updating position by rotation after spawning
        this.posX -= (double) (MathHelper.cos(this.rotationYaw / 180.0F * (float) Math.PI) * 0.16F * distanceScale);
        this.posY -= 0.10000000149011612D; //TODO magic number - likely half arrow height
        this.posZ -= (double) (MathHelper.sin(this.rotationYaw / 180.0F * (float) Math.PI) * 0.16F * distanceScale);
        this.setPosition(this.posX, this.posY, this.posZ);

        this.motionX = (double) (-MathHelper.sin(this.rotationYaw / 180.0F * (float) Math.PI) * MathHelper.cos(this.rotationPitch / 180.0F * (float) Math.PI));
        this.motionZ = (double) (MathHelper.cos(this.rotationYaw / 180.0F * (float) Math.PI) * MathHelper.cos(this.rotationPitch / 180.0F * (float) Math.PI));
        this.motionY = (double) (-MathHelper.sin(this.rotationPitch / 180.0F * (float) Math.PI));
        this.shoot(this.motionX, this.motionY, this.motionZ, multiplier, 0);

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
            shooter.posX, shooter.posY + (double) shooter.getEyeHeight(), shooter.posZ,
            shooter.rotationYaw, shooter.rotationPitch,
            offsetMultiplier, forceMultiplier
        );
    }

    @Override
    public boolean throwProjectile(@Nonnull EntityProjectile entity, @Nullable IActionSource source, double x, double y, double z, float yaw, float pitch, float velocity, float random) {
        initAimingPosition(
            x, y, z,
            yaw, pitch,
            1, velocity
        );
        // TODO implement random
        return true;
    }

    @Override
    public void onUpdate() {
        super.onUpdate();
        //ICBMClassic.logger().info(String.format("Projectile: - Pos(%.5f,%.5f,%.5f), VEL(%.5f,%.5f,%.5f)", posX, posY, posZ, motionX, motionY, motionZ));

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

            double rayEndVecX = motionX;
            double rayEndVecY = motionY;
            double rayEndVecZ = motionZ;

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
            Vec3d rayStart = new Vec3d(this.posX, this.posY, this.posZ);
            Vec3d rayEnd = new Vec3d(this.posX + rayEndVecX, this.posY + rayEndVecY, this.posZ + rayEndVecZ);
            RayTraceResult rayHit = this.world.rayTraceBlocks(rayStart, rayEnd, false, true, false);

            //Reset data to do entity ray trace
            rayStart = new Vec3d(this.posX, this.posY, this.posZ);
            rayEnd = new Vec3d(this.posX + this.motionX, this.posY + this.motionY, this.posZ + this.motionZ);
            if (rayHit != null) {
                rayEnd = new Vec3d(rayHit.hitVec.x, rayHit.hitVec.y, rayHit.hitVec.z);
            }

            //Handle entity collision boxes
            Entity entity = null;
            final List<Entity> list = this.world.getEntitiesWithinAABBExcludingEntity(this, this.getEntityBoundingBox().offset(this.motionX, this.motionY, this.motionZ).expand(1.0D, 1.0D, 1.0D));
            double distanceToHit = 0.0D;

            // TODO see if we can parallel stream this? As it might be thread safe assuming we .map first
            for (Entity checkEntity : list) {
                if (shouldCollideWith(checkEntity) && (checkEntity != this.shootingEntity || this.ticksInAir >= 5)) { //TODO why 5 ticks specifically? Why not 'has collider left shooter'
                    final AxisAlignedBB hitBox = checkEntity.getEntityBoundingBox().expand(0.3F, 0.3F, 0.3F);
                    final RayTraceResult entityRayHit = hitBox.calculateIntercept(rayStart, rayEnd);

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

    /**
     * Runs each tick to check what block we are inside. Raytrace this in air movement
     * but once in ground we need to refresh manually to detect block breaking.
     *
     * If block isn't the same or has changed logic will revalidate. If block has no
     * collision then things will fall
     */
    protected void checkInGround() {
        //Check if in ground TODO do we need to run this every tick?
        final BlockPos tilePos = inGroundData != null ? inGroundData.getPos() : this.getPos();
        final IBlockState state = this.world.getBlockState(tilePos);
        final InGroundData prevInGround = this.inGroundData;

        // Only run logic if we don't have an existing collision or detected block has changed TODO use events to trigger this on block edits
        if (inGroundData == null || state != inGroundData.getState()) {
            this.inGroundData = null;

            if (!state.getBlock().isAir(state, world, tilePos)) {
                //Check if what we hit can be collided with
                final AxisAlignedBB axisalignedbb = state.getCollisionBoundingBox(this.world, tilePos);
                if (axisalignedbb != null && axisalignedbb.offset(tilePos).contains(new Vec3d(this.posX, this.posY, this.posZ))) {
                    final EnumFacing side = EnumFacing.UP; //TODO calculate side based on position
                    this.inGroundData = new InGroundData(tilePos, side, state);
                }
            }
        }

        // We are no longer in the ground
        if (prevInGround != null && this.inGroundData == null) {
            //TODO change to apply gravity instead of random
            this.motionX *= (double) (this.rand.nextFloat() * 0.2F);
            this.motionY *= (double) (this.rand.nextFloat() * 0.2F);
            this.motionZ *= (double) (this.rand.nextFloat() * 0.2F);
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
        return ticksInAir >= inAirKillTime || ticksInGround >= inGroundKillTime || this.posY < -100;
    }


    @Override
    protected void destroy() {
        dismountRidingEntity();
        removePassengers();
        super.destroy();
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
        return entity.canBeCollidedWith() && !(entity instanceof EntityPlayerSeat);
    }

    protected void handleBlockCollision(RayTraceResult hit) {
        this.inGroundData = new InGroundData(world, hit);

        // Special handling for ender gateways TODO move to a registry of Block -> lambda
        final IProjectileBlockInteraction.EnumHitReactions reaction =
            ProjectileBlockInteraction.handleSpecialInteraction(world, this.inGroundData.getPos(), hit.hitVec, this.inGroundData.getSide(), this.inGroundData.getState(), this);
        if (reaction.stop) {
            return;
        }

        // Move entity to collision location
        moveTowards(hit.hitVec, width / 2f);

        //TODO this.playSound("random.bowhit", 1.0F, 1.2F / (this.rand.nextFloat() * 0.2F + 0.9F));

        if (this.inGroundData.getMaterial() != Material.AIR) {
            this.inGroundData.getState().getBlock().onEntityCollidedWithBlock(this.world, this.inGroundData.getPos(), this.inGroundData.getState(), this);
        }

        if (!changingDimensions && !isDead && !inPortal && reaction != IProjectileBlockInteraction.EnumHitReactions.CONTINUE_NO_IMPACT) {
            onImpactTile(hit);
            this.motionX = 0;
            this.motionY = 0;
            this.motionZ = 0;
        }
    }

    public void moveTowards(Vec3d hit, double offset) {

        final double deltaX = hit.x - this.posX;
        final double deltaY = hit.y - this.posY;
        final double deltaZ = hit.z - this.posZ;

        final float mag = MathHelper.sqrt(deltaX * deltaX + deltaY * deltaY + deltaZ * deltaZ);
        final double vecX = (deltaX / (double) mag) * offset;
        final double vecY = (deltaX / (double) mag) * offset;
        final double vecZ = (deltaZ / (double) mag) * offset;

        this.posX = hit.x + vecX;
        this.posY = hit.y + vecY;
        this.posZ = hit.z + vecZ;

        setPosition(posX, posY, posZ);
    }

    @Override
    public Entity changeDimension(int dimensionIn, ITeleporter teleporter) {

        this.changingDimensions = true;
        return super.changeDimension(dimensionIn, teleporter);
    }

    @Override
    public int getPortalCooldown() {
        return 10;
    }

    @Override
    public int getMaxInPortalTime() {
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
        onImpact(hit);
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

    protected void onImpactEntity(Entity entityHit, float velocity, RayTraceResult hit) {
        if (!world.isRemote) {
            final float damage = getImpactDamage(entityHit, velocity, hit);
            final DamageSource damageSource = getImpactDamageSource(entityHit, velocity, hit);

            if (damageSource != null && entityHit.attackEntityFrom(damageSource, damage)
                && entityHit instanceof EntityLivingBase) {
                applyKnockBack(entityHit);
            }
            hit.hitVec = hit.hitVec.add(new Vec3d(0, 0.5, 0)); //Add some offset to prevent the entity from disappearing
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

        // TODO rework to use projectile motionY so knockback is in motion direction, add random offset to mimic deflection
        // TODO see if we can drop sqrt, 1 projectile it is fine... 1000s it is slow
        final float vel_horizontal = MathHelper.sqrt(this.motionX * this.motionX + this.motionZ * this.motionZ);
        if (vel_horizontal > 0.0F) {
            entity.addVelocity(
                this.motionX * vertKnock / (double) vel_horizontal,
                hortKnock,
                this.motionZ * vertKnock / (double) vel_horizontal
            );
        }
    }

    /**
     * Called to get damage to apply during impact
     * <p>
     * Inputs can be used to dynamically change the damage source based on angle and velocity. An example
     * is using 20% damage for side hits and 100% damage for direct impacts. As well reducing damage
     * done to specific entities. Such as 5% damage to slimes and 0% damage to ghosts.
     *
     * @param entityHit by the projectile
     * @param velocity  during the impact
     * @param hit       trace used to calculate the impact, use this for projectile position
     * @return damage to apply
     */
    protected float getImpactDamage(Entity entityHit, float velocity, RayTraceResult hit) {
        return MathHelper.ceil(velocity * 2f);
    }

    /**
     * Called to get damage source to apply during impact
     * <p>
     * Inputs can be used to dynamically change the damage source based on angle and velocity. An example
     * is using blunt impact for side hits and piercing for front on impacts.
     *
     * @param entityHit by the projectile
     * @param velocity  during the impact
     * @param hit       trace used to calculate the impact, use this for projectile position
     * @return damage source to use
     */
    protected DamageSource getImpactDamageSource(Entity entityHit, float velocity, RayTraceResult hit) {
        return DAMAGE_SOURCE;
    }

    /**
     * Generalized impact callback, used to do cleanup
     * steps regardless of impact reason.
     * <p>
     * Use {@link #onImpactEntity(Entity, float, RayTraceResult)} or {@link #onImpactTile(RayTraceResult)} for
     * better handling of impacts.
     */
    protected void onImpact(RayTraceResult hit) {
        this.setDead();
    }

    protected void updateMotion() {
        if (!freezeMotion && isServer()) {
            //Update motion
            this.posX += this.motionX;
            this.posY += this.motionY;
            this.posZ += this.motionZ;

            if (shouldAlignWithMotion()) {
                rotateTowardsMotion(0.05f);
            }

            //Decrease motion so the projectile stops
            decreaseMotion();

            //Set position
            this.setPosition(this.posX, this.posY, this.posZ);

            //Adjust for collision      TODO check if works, rewrite code to prevent clip through of block
            this.doBlockCollisions();
        }
    }

    public boolean shouldAlignWithMotion() {
        return true;
    }

    public void rotateTowardsMotion(float delta) {
        //Get rotation from motion
        float speed = MathHelper.sqrt(this.motionX * this.motionX + this.motionZ * this.motionZ);
        this.rotationYaw = (float) (Math.atan2(this.motionX, this.motionZ) * 180.0D / Math.PI); //TODO update to catch atan for better performance on repeat calls
        this.rotationPitch = (float) (Math.atan2(this.motionY, (double) speed) * 180.0D / Math.PI);

        //-------------------------------------
        //Fix rotation
        while (this.rotationPitch - this.prevRotationPitch < -180.0F) {
            this.prevRotationPitch -= 360.0F;
        }

        while (this.rotationPitch - this.prevRotationPitch >= 180.0F) {
            this.prevRotationPitch += 360.0F;
        }

        while (this.rotationYaw - this.prevRotationYaw < -180.0F) {
            this.prevRotationYaw -= 360.0F;
        }

        while (this.rotationYaw - this.prevRotationYaw >= 180.0F) {
            this.prevRotationYaw += 360.0F;
        }
        //-------------------------------------
        //Reduce delta in rotation to provide for a smoother animation
        this.rotationPitch = this.prevRotationPitch + (this.rotationPitch - this.prevRotationPitch) * delta;
        this.rotationYaw = this.prevRotationYaw + (this.rotationYaw - this.prevRotationYaw) * delta;
        //-------------------------------------
    }

    /**
     * Called each tick, assuming no collisions, to slow
     * down the projectile. Both via air resistance and
     * gravity constant.
     */
    protected void decreaseMotion() {
        final float airResistance = getAirResistance();
        this.motionX *= airResistance;
        this.motionY *= airResistance;
        this.motionZ *= airResistance;
        //Add gravity so the projectile will fall
        this.motionY -= getGravity();
    }

    /**
     * Multiplier to simulate air resistance. Works
     * by multiplying the resistance by the motion. So
     * if the value is 0.99 or 99% then motion will
     * decrease by 1% per tick.
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
     * @return gravity acceleration as m/t
     */
    protected float getGravity() {
        return DEFAULT_GRAVITY;
    }

    @Override
    public void shoot(double xx, double yy, double zz, float speed, float random) {
        //Normalize
        float magnitude = MathHelper.sqrt(xx * xx + yy * yy + zz * zz);
        xx /= (double) magnitude;
        yy /= (double) magnitude;
        zz /= (double) magnitude;

        //Add randomization to make the arrow miss
        if (random > 0) {
            xx += this.rand.nextGaussian() * (double) (this.rand.nextBoolean() ? -1 : 1) * 0.007499999832361937D * (double) random;
            yy += this.rand.nextGaussian() * (double) (this.rand.nextBoolean() ? -1 : 1) * 0.007499999832361937D * (double) random;
            zz += this.rand.nextGaussian() * (double) (this.rand.nextBoolean() ? -1 : 1) * 0.007499999832361937D * (double) random;
        }

        //Add multiplier
        xx *= (double) speed;
        yy *= (double) speed;
        zz *= (double) speed;

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

    protected static void vectorFromAngles(float yaw, float pitch, D3Consumer callback) {
        callback.apply(
            -MathHelper.sin(yaw * 0.017453292F) * MathHelper.cos(pitch * 0.017453292F),
            -MathHelper.sin(pitch * 0.017453292F),
            MathHelper.cos(yaw * 0.017453292F) * MathHelper.cos(pitch * 0.017453292F));
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void setPositionAndRotationDirect(double x, double y, double z, float yaw, float pitch, int posRotationIncrements, boolean teleport) {
        this.setPosition(x, y, z);
        this.setRotation(yaw, pitch);
    }

    @Override
    protected boolean canTriggerWalking() {
        return false;
    }

    public boolean canBeDestroyed(Object attacker, DamageSource damageSource) {
        return hasHealth;
    }


    @Override
    @SideOnly(Side.CLIENT)
    public boolean isInRangeToRenderDist(double distanceSq) {
        final double renderDistance = getAverageEdgeLength() * getRenderDistanceWeight() * getRenderDistance();
        return distanceSq < renderDistance * renderDistance;
    }

    protected double getRenderDistance() {
        return RENDER_DISTANCE_SCALE;
    }

    protected double getAverageEdgeLength() {
        final AxisAlignedBB boundingBox = this.getRenderBoundingBox();
        if (boundingBox == null) {
            return 1;
        }
        double size = boundingBox.getAverageEdgeLength();
        return Double.isNaN(size) || Double.isInfinite(size) ? 1 : size;
    }

    @Override
    public void writeEntityToNBT(NBTTagCompound nbt) {
        SAVE_LOGIC.save(this, nbt);
        super.writeEntityToNBT(nbt);
    }

    @Override
    public void readEntityFromNBT(NBTTagCompound nbt) {
        super.readEntityFromNBT(nbt);
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
    //Project source, if needed implement in each projectile directly. or add a boolean toggle to .addRoot. As missile doesn't need source nor shooter due to missile.targetData
        /*
        .addRoot("source")
        .nodePos("pos", (projectile) -> projectile.sourceOfProjectile, (projectile, pos) -> projectile.sourceOfProjectile = pos)
        .nodeUUID("uuid", (projectile) -> projectile.shootingEntityUUID, (projectile, uuid) -> projectile.shootingEntityUUID = uuid)
        .base();
        */
}

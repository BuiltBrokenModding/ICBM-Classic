package icbm.classic.content.entity;

import icbm.classic.content.blocks.launcher.base.TileLauncherBase;
import icbm.classic.content.reg.EntityReg;
import icbm.classic.lib.transform.rotation.EulerAngle;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.entity.vehicle.Minecart;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.entity.IEntityWithComplexSpawn;

/**
 * Used a placeholder to move riding entities around
 *
 *
 * Created by Dark(DarkGuardsman, Robin) on 10/31/2017.
 */
public class EntityPlayerSeat extends Entity implements IEntityWithComplexSpawn
{
    private TileLauncherBase host; //TODO save host position so we can restore from save
    private BlockPos hostPos;

    public float offsetX = 0;
    public float offsetY = 0;
    public float offsetZ = 0;
    public Direction prevFace;
    public Direction prevRotation;
    public boolean prevRiding;

    /** PORT: 1.12 width/height fields, 1.21 entity size comes from {@link #getDimensions(Pose)} */
    private float width = 0.5f;
    private float height = 2.5f;

    public EntityPlayerSeat(EntityType<? extends EntityPlayerSeat> type, Level world)
    {
        super(type, world);
    }

    public EntityPlayerSeat(Level world)
    {
        this(EntityReg.MISSILE_SEAT.get(), world);
    }

    public void setHost(TileLauncherBase host) {
        this.host = host;
        if(host != null) {
            this.hostPos = host.getBlockPos();
        }
        else {
            this.hostPos = null;
        }
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder)
    {
    }

    /** 1.12 getEyeHeight returned 0 */
    @Override
    public EntityDimensions getDimensions(Pose pose)
    {
        return EntityDimensions.scalable(width, height).withEyeHeight(0);
    }

    /** 1.12 removePassengers */
    @Override
    public void ejectPassengers()
    {
        for (int i = this.getPassengers().size() - 1; i >= 0; --i)
        {
            final Entity entity = this.getPassengers().get(i);
            double prevX = entity.getX();
            double prevY = entity.getY();
            double prevZ = entity.getZ();

            entity.stopRiding();

            // Player will sometimes warp to bottom of map when dismounting
            // PORT: 1.12 compared prevY against posZ (typo), fixed to compare against Y
            if(Math.abs(prevX - entity.getX()) > 2 || Math.abs(prevY - entity.getY()) > 2 || Math.abs(prevZ - entity.getZ()) > 2) {
                entity.setPos(prevX, prevY, prevZ);
            }
        }
    }

    /** 1.12 onEntityUpdate, base entity tick (fire, portals, water) was skipped in 1.12 too */
    @Override
    public void tick()
    {
        if(host == null && hostPos != null) {
            final BlockEntity tile = level().getBlockEntity(hostPos);
            if(tile instanceof TileLauncherBase launcher) {
                // PORT: 1.21 loads entity sections after block entities start ticking, so the launcher may have
                // already spawned a fresh seat before this saved seat arrived. Yield to it instead of duplicating,
                // unless this seat came back with a rider (player rejoined while seated) and the other is empty.
                final EntityPlayerSeat other = launcher.seat;
                if(!level().isClientSide() && other != null && other != this && !other.isRemoved()) {
                    if(this.isVehicle() && !other.isVehicle()) {
                        other.discard();
                    }
                    else {
                        this.ejectPassengers();
                        this.discard();
                        return;
                    }
                }
                host = launcher;
                host.seat = this;
            }
        }

        // PORT: also drop seats the launcher no longer owns (orphans left by older duplicated saves)
        if (!level().isClientSide() && (host == null || host.isRemoved() || host.seat != this || this.getY() < -64.0D))
        {
            this.ejectPassengers();
            this.discard();
            return;
        }

        if(host != null && (prevFace != host.getLaunchDirection() || prevRotation != host.getSeatSide() || prevRiding != this.isVehicle())) {
            prevFace = host.getLaunchDirection();
            prevRotation = host.getSeatSide();
            prevRiding = this.isVehicle();
            updatePosition(host.getLaunchDirection(), host.getSeatSide());
            updateBox(host.getLaunchDirection(), host.getSeatSide());
        }
    }

    protected void updatePosition(Direction face, Direction rotation) {

        // Rotation relative to block face
        offsetX = rotation.getStepX() * 0.2f; //TODO customize to match missile visuals
        offsetY = rotation.getStepY() * 0.2f;
        offsetZ = rotation.getStepZ() * 0.2f;

        // Height relative to block rotation
        offsetX += face.getStepX() * 0.5f;
        offsetY += face.getStepY() * 0.5f;
        offsetZ += face.getStepZ() * 0.5f;

        if(face == Direction.DOWN) {
            offsetY -= height;
        }
        else if(face == Direction.EAST) {
            offsetX += width / 2;
            offsetY -= 0.6f;
        }
        else if(face == Direction.WEST) {
            offsetX -= width / 2;
            offsetY -= 0.6f;
        }
        else if(face == Direction.NORTH) {
            offsetZ -= width / 2;
            offsetY -= 0.6f;
        }
        else if(face == Direction.SOUTH) {
            offsetZ += width / 2;
            offsetY -= 0.6f;
        }

        // Position
        double posX = host.getBlockPos().getX() + 0.5 + face.getStepX() * 0.5;
        double posY = host.getBlockPos().getY() + 0.5 + face.getStepY() * 0.5;
        double posZ = host.getBlockPos().getZ() + 0.5 + face.getStepZ() * 0.5;
        setPos(posX, posY, posZ);

        final EulerAngle angle = new EulerAngle(rotation);
        this.setYRot((float) angle.yaw());
        this.yRotO = this.getYRot();
        this.setXRot((float) angle.pitch());
        this.xRotO = this.getXRot();
    }

    protected void updateBox(Direction face, Direction rotation) {
        // Size
        if(face == Direction.UP || face == Direction.DOWN) {
            setSize(0.5f, this.isVehicle() ? 0.5f : 2.5f);
        }
        else {
            setSize(this.isVehicle() ? 0.5f : 2.5f, 0.5f);
        }

        // bounding box
        this.setBoundingBox(this.makeBoundingBox());
    }

    /**
     * PORT: 1.12 set a custom bounding box once in updateBox, 1.21 rebuilds the box from position on every
     * setPos so the face based box is computed here instead.
     */
    @Override
    protected AABB makeBoundingBox()
    {
        final Direction face = prevFace;
        if (face == null) {
            return super.makeBoundingBox();
        }

        final float dimA;
        final float dimB;
        if(face == Direction.UP || face == Direction.DOWN) {
            dimA = this.width / 2;
            dimB = this.height;
        }
        else {
            dimA = this.height / 2;
            dimB = this.width;
        }

        final double posX = getX();
        final double posY = getY();
        final double posZ = getZ();

        switch (face) {
            case UP:
                return new AABB(posX - dimA, posY, posZ - dimA, posX + dimA, posY + dimB, posZ + dimA);
            case DOWN:
                return new AABB(posX - dimA, posY - dimB, posZ - dimA, posX + dimA, posY, posZ + dimA);
            case EAST:
                return new AABB(posX, posY - dimA, posZ - dimA, posX + dimB, posY + dimA, posZ + dimA);
            case WEST:
                return new AABB(posX - dimB, posY - dimA, posZ - dimA, posX, posY + dimA, posZ + dimA);
            case NORTH:
                return new AABB(posX - dimA, posY - dimA, posZ - dimB, posX + dimA, posY + dimA, posZ);
            case SOUTH:
            default:
                return new AABB(posX - dimA, posY - dimA, posZ, posX + dimA, posY + dimA, posZ + dimB);
        }
    }

    @Override
    public void move(MoverType type, Vec3 motion)
    {
        // Can't move
    }

    /** 1.12 processInitialInteract */
    @Override
    public InteractionResult interact(Player player, InteractionHand hand)
    {
        if (player.isShiftKeyDown())
        {
            return InteractionResult.PASS;
        }
        else if (this.isVehicle())
        {
            return InteractionResult.SUCCESS;
        }
        else
        {
            if (!this.level().isClientSide())
            {
                if(player.isCreative()) {
                    final ItemStack itemStack = player.getItemInHand(hand);
                    if (itemStack.getItem() == Items.MINECART) {
                        final AbstractMinecart cart = new Minecart(level(), getX(), getY(), getZ());
                        level().addFreshEntity(cart);

                        cart.startRiding(this);
                        return InteractionResult.SUCCESS;
                    }
                }
                player.startRiding(this);
            }

            return InteractionResult.SUCCESS;
        }
    }

    /** 1.12 canBeCollidedWith (ray trace / interaction) */
    @Override
    public boolean isPickable()
    {
        return !this.isRemoved();
    }

    /** 1.12 getCollisionBoundingBox, solid only while empty */
    @Override
    public boolean canBeCollidedWith()
    {
        return this.getPassengers().isEmpty();
    }

    public void setSize(float width, float height)
    {
        if (width != this.width || height != this.height) {
            this.width = width;
            this.height = height;
            this.refreshDimensions();
        }
    }

    public double getMountedYOffset()
    {
        return offsetY;
    }

    @Override
    protected void positionRider(Entity passenger, Entity.MoveFunction callback)
    {
        if (this.hasPassenger(passenger) && host != null)
        {
            // PORT-CHECK: 1.12 passenger.getYOffset() (player -0.35) approximated by the negated vehicle attachment point
            final double yOffset = -passenger.getVehicleAttachmentPoint(this).y;
            double x = this.getX() + offsetX + yOffset * (prevFace != null ? prevFace.getStepX() : 0);
            double y = this.getY() + offsetY + yOffset * (prevFace != null ? prevFace.getStepY() : 0);
            double z = this.getZ() + offsetZ + yOffset * (prevFace != null ? prevFace.getStepZ() : 0);
            callback.accept(passenger, x, y, z);
        }
    }

    /** 1.12 applyEntityCollision */
    @Override
    public void push(Entity entity)
    {
        //disable collision
    }

    /** 1.12 addVelocity */
    @Override
    public void push(double x, double y, double z)
    {
        //disable velocity
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag save)
    {
        if(save.contains("pos")) {
            // Same layout as 1.12 NBTUtil.createPosTag
            final CompoundTag pos = save.getCompound("pos");
            this.hostPos = new BlockPos(pos.getInt("X"), pos.getInt("Y"), pos.getInt("Z"));
        }
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag save)
    {
        if(hostPos != null) {
            final CompoundTag pos = new CompoundTag();
            pos.putInt("X", hostPos.getX());
            pos.putInt("Y", hostPos.getY());
            pos.putInt("Z", hostPos.getZ());
            save.put("pos", pos);
        }
    }

    @Override
    public void writeSpawnData(RegistryFriendlyByteBuf buffer)
    {
        buffer.writeBoolean(host != null);
        if(host != null) {
            buffer.writeInt(host.getBlockPos().getX());
            buffer.writeInt(host.getBlockPos().getY());
            buffer.writeInt(host.getBlockPos().getZ());
        }
    }

    @Override
    public void readSpawnData(RegistryFriendlyByteBuf additionalData)
    {
        if(additionalData.readBoolean()) {
            final BlockEntity tile = level().getBlockEntity(new BlockPos(additionalData.readInt(), additionalData.readInt(), additionalData.readInt()));
            if(tile instanceof TileLauncherBase) {
                host = (TileLauncherBase) tile;
            }
        }
    }
}
// PORTED

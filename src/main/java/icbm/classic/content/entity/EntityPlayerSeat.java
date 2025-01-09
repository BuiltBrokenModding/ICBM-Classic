package icbm.classic.content.entity;

import icbm.classic.content.blocks.launcher.base.TileLauncherBase;
import icbm.classic.lib.transform.rotation.EulerAngle;
import net.minecraft.entity.*;
import net.minecraft.entity.item.minecart.AbstractMinecartEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.network.IPacket;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.server.SSpawnObjectPacket;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.fml.common.registry.IEntityAdditionalSpawnData;

import javax.annotation.Nullable;

/**
 * Used a placeholder to move riding entities around
 *
 *
 * Created by Dark(DarkGuardsman, Robin) on 10/31/2017.
 */
public class EntityPlayerSeat extends Entity implements IEntityAdditionalSpawnData
{
    private static final EntitySize VERT_SIZE = new EntitySize(0.5f, 2.5f, true);
    private static final EntitySize HORT_SIZE = new EntitySize(2.5f, 0.5f, true);

    private TileLauncherBase host; //TODO save host position so we can restore from save
    private BlockPos hostPos;

    public float offsetX = 0;
    public float offsetY = 0;
    public float offsetZ = 0;
    public Direction prevFace;
    public Direction prevRotation;

    public EntityPlayerSeat(EntityType<EntityPlayerSeat> type, World world)
    {
        super(type, world);
    }

    @Override
    protected float getEyeHeight(Pose p_213316_1_, EntitySize p_213316_2_) {
        return 0;
    }

    @Override
    public IPacket<?> createSpawnPacket() {
        return new SSpawnObjectPacket(this); //TODO figure out what this is
    }

    public void setHost(TileLauncherBase host) {
        this.host = host;
        if(host != null) {
            this.hostPos = host.getPos();
        }
        else {
            this.hostPos = null;
        }
    }

    @Override
    protected void registerData()
    {
    }

    @Override
    public void onRemovedFromWorld() {
        super.onRemovedFromWorld();
        //removePassengers();
    }

    @Override
    public void removePassengers()
    {
        super.removePassengers();
        for (int i = this.getPassengers().size() - 1; i >= 0; --i)
        {
            final Entity entity = this.getPassengers().get(i);
            double prevX = entity.posX;
            double prevY = entity.posY;
            double prevZ = entity.posZ;

            entity.stopRiding();

            // Player will sometimes warp to bottom of map when dismounting
            if(Math.abs(prevX - entity.posX) > 2 || Math.abs(prevY - entity.posZ) > 2 || Math.abs(prevZ - entity.posZ) > 2) {
                entity.setPosition(prevX, prevY, prevZ);
            }
        }
    }

    @Override
    public void tick()
    {
        if(host == null && hostPos != null) {
            final TileEntity tile = world.getTileEntity(hostPos);
            if(tile instanceof TileLauncherBase) {
                host = (TileLauncherBase) tile;
                host.seat = this;
            }
        }

        if (!world.isRemote && (host == null || host.isRemoved() || this.posY < -64.0D))
        {
            this.removePassengers();
            this.remove();
        }

        if(host != null && (prevFace != host.getLaunchDirection() || prevRotation != host.getSeatSide())) {
            prevFace = host.getLaunchDirection();
            prevRotation = host.getSeatSide();
            updatePosition(host.getLaunchDirection(), host.getSeatSide());
            updateBox(host.getLaunchDirection(), host.getSeatSide());
        }
    }

    protected void updatePosition(Direction face, Direction rotation) {

        // Rotation relative to block face
        offsetX = rotation.getXOffset() * 0.2f; //TODO customize to match missile visuals
        offsetY = rotation.getYOffset() * 0.2f;
        offsetZ = rotation.getZOffset() * 0.2f;

        // Height relative to block rotation
        offsetX += face.getXOffset() * 0.5f;
        offsetY += face.getYOffset() * 0.5f;
        offsetZ += face.getZOffset() * 0.5f;

        if(face == Direction.DOWN) {
            offsetY -= getWidth();
        }
        else if(face == Direction.EAST) {
            offsetX += getWidth() / 2;
            offsetY -= 0.6f;
        }
        else if(face == Direction.WEST) {
            offsetX -= getWidth() / 2;
            offsetY -= 0.6f;
        }
        else if(face == Direction.NORTH) {
            offsetZ -= getWidth() / 2;
            offsetY -= 0.6f;
        }
        else if(face == Direction.SOUTH) {
            offsetZ += getWidth() / 2;
            offsetY -= 0.6f;
        }

        // Position
        double posX = host.getPos().getX() + 0.5 + face.getXOffset() * 0.5;
        double posY = host.getPos().getY() + 0.5 + face.getYOffset() * 0.5;
        double posZ = host.getPos().getZ() + 0.5 + face.getZOffset() * 0.5;
        setPosition(posX, posY, posZ);

        final EulerAngle angle = new EulerAngle(rotation);
        this.rotationYaw = this.prevRotationYaw = (float) angle.yaw();
        this.rotationPitch = this.prevRotationPitch = (float) angle.pitch();
    }



    @Override
    public EntitySize getSize(Pose poseIn) {
        final Direction face = host.getLaunchDirection();
        if(face == Direction.UP || face == Direction.DOWN) {
           return VERT_SIZE;
        }
        return HORT_SIZE;
    }

    protected void updateBox(Direction face, Direction rotation) {

        final float dimA;
        final float dimB;
        // Size
        if(face == Direction.UP || face == Direction.DOWN) {
            dimA = this.getWidth() / 2;
            dimB = this.getHeight();
        }
        else {
            dimA = this.getHeight() / 2;
            dimB = this.getWidth();
        }

        // bounding box
        if(face == Direction.UP) {
            double minX = posX - dimA;
            double minY = posY;
            double minZ = posZ - dimA;

            double maxX = posX + dimA;
            double maxY = posY + dimB;
            double maxZ = posZ + dimA;
            setBoundingBox(new AxisAlignedBB(minX, minY, minZ, maxX, maxY, maxZ));
        }
        else if(face == Direction.DOWN) {
            double minX = posX - dimA;
            double minY = posY - dimB;
            double minZ = posZ - dimA;

            double maxX = posX + dimA;
            double maxY = posY;
            double maxZ = posZ + dimA;
            setBoundingBox(new AxisAlignedBB(minX, minY, minZ, maxX, maxY, maxZ));
        }
        else if(face == Direction.EAST) {
            double minX = posX;
            double minY = posY - dimA;
            double minZ = posZ - dimA;

            double maxX = posX + dimB;
            double maxY = posY + dimA;
            double maxZ = posZ + dimA;
            setBoundingBox(new AxisAlignedBB(minX, minY, minZ, maxX, maxY, maxZ));
        }
        else if(face == Direction.WEST) {
            double minX = posX - dimB;
            double minY = posY - dimA;
            double minZ = posZ - dimA;

            double maxX = posX;
            double maxY = posY + dimA;
            double maxZ = posZ + dimA;
            setBoundingBox(new AxisAlignedBB(minX, minY, minZ, maxX, maxY, maxZ));
        }
        else if(face == Direction.NORTH) {
            double minX = posX - dimA;
            double minY = posY - dimA;
            double minZ = posZ - dimB;

            double maxX = posX + dimA;
            double maxY = posY + dimA;
            double maxZ = posZ;
            setBoundingBox(new AxisAlignedBB(minX, minY, minZ, maxX, maxY, maxZ));
        }

        else if(face == Direction.SOUTH) {
            double minX = posX - dimA;
            double minY = posY - dimA;
            double minZ = posZ;

            double maxX = posX + dimA;
            double maxY = posY + dimA;
            double maxZ = posZ + dimB;
            setBoundingBox(new AxisAlignedBB(minX, minY, minZ, maxX, maxY, maxZ));
        }
    }

    @Override
    public void setPosition(double x, double y, double z)
    {
        this.posX = x;
        this.posY = y;
        this.posZ = z;
        if (this.isAddedToWorld() && !this.world.isRemote && world instanceof ServerWorld) {
            ((ServerWorld)this.world).chunkCheck(this); // Forge - Process chunk registration after moving.
        }
        // Copied from super to remove bounding box changes
    }

    @Override
    public void move(MoverType type, Vec3d pos)
    {
        // Can't move
    }


    @Override
    public boolean processInitialInteract(PlayerEntity player, Hand hand)
    {
        if (player.isSneaking())
        {
            return false;
        }
        else if (this.isBeingRidden())
        {
            return true;
        }
        else
        {
            if (!this.world.isRemote)
            {
                if(player.isCreative()) {
                    final ItemStack itemStack = player.getHeldItem(hand);
                    if (itemStack.getItem() == Items.MINECART) {
                        final AbstractMinecartEntity cart = EntityType.MINECART.create(world);
                        cart.setPosition(posX, posY, posZ);
                        world.addEntity(cart);

                        cart.startRiding(this);
                        return true;
                    }
                }
                player.startRiding(this);
            }

            return true;
        }
    }

    @Override
    public boolean canBeCollidedWith()
    {
        return this.isAlive();
    }

    @Override
    public double getMountedYOffset()
    {
        return offsetY;
    }

    @Override
    @Nullable
    public AxisAlignedBB getCollisionBox(Entity entityIn)
    {
        return super.getBoundingBox(); //TODO might be needed for interaction
    }

    @Override
    public void updatePassenger(Entity passenger)
    {
        if (this.isPassenger(passenger) && host != null)
        {
            double x = this.posX + offsetX + passenger.getYOffset() * (prevFace != null ? prevFace.getXOffset() : 0);
            double y = this.posY + offsetY + passenger.getYOffset() * (prevFace != null ? prevFace.getYOffset() : 0);
            double z = this.posZ + offsetZ + passenger.getYOffset() * (prevFace != null ? prevFace.getZOffset() : 0);
            passenger.setPosition(x, y, z);
        }
    }

    @Override
    public void applyEntityCollision(Entity p_70108_1_)
    {
        //disable collision
    }

    @Override
    public void addVelocity(double p_70024_1_, double p_70024_3_, double p_70024_5_)
    {
        //disable velocity
    }

    @Override
    protected void readAdditional(CompoundNBT save)
    {
        if(save.contains("pos")) {
            this.hostPos = NBTUtil.readBlockPos(save.getCompound("pos"));
        }

    }

    @Override
    protected void writeAdditional(CompoundNBT save)
    {
        if(hostPos != null) {
            save.put("pos", NBTUtil.writeBlockPos(hostPos));
        }
    }

    @Override
    public void writeSpawnData(PacketBuffer buffer)
    {
        buffer.writeBoolean(host != null);
        if(host != null) {
            buffer.writeInt(host.getPos().getX());
            buffer.writeInt(host.getPos().getY());
            buffer.writeInt(host.getPos().getZ());
        }
    }

    @Override
    public void readSpawnData(PacketBuffer additionalData)
    {
        if(additionalData.readBoolean()) {
            final TileEntity tile = world.getTileEntity(new BlockPos(additionalData.readInt(), additionalData.readInt(), additionalData.readInt()));
            if(tile instanceof TileLauncherBase) {
                host = (TileLauncherBase) tile;
            }
        }
    }
}

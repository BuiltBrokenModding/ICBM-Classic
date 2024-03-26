package icbm.classic.world.entity;

import icbm.classic.lib.transform.rotation.EulerAngle;
import icbm.classic.world.block.launcher.base.LauncherBaseBlockEntity;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.init.Items;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.item.EntityMinecart;
import net.minecraft.world.entity.item.EntityMinecartEmpty;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.fml.common.registry.IEntityAdditionalSpawnData;

import javax.annotation.Nullable;

/**
 * Used a placeholder to move riding entities around
 * <p>
 * <p>
 * Created by Dark(DarkGuardsman, Robert) on 10/31/2017.
 */
public class PlayerSeatEntity extends Entity implements IEntityAdditionalSpawnData {
    private LauncherBaseBlockEntity host; //TODO save host position so we can restore from save
    private BlockPos hostPos;

    public float offsetX = 0;
    public float offsetY = 0;
    public float offsetZ = 0;
    public Direction prevFace;
    public Direction prevRotation;

    public PlayerSeatEntity(EntityType<?> type, Level level) {
        super(type, level);
    }

    public void setHost(LauncherBaseBlockEntity host) {
        this.host = host;
        if (host != null) {
            this.hostPos = host.getPos();
        } else {
            this.hostPos = null;
        }
    }

    @Override
    public float getEyeHeight() {
        return 0;
    }

    @Override
    protected void entityInit() {
    }

    @Override
    public void onRemovedFromLevel() {
        super.onRemovedFromLevel();
        //removePassengers();
    }

    @Override
    public void removePassengers() {
        for (int i = this.getPassengers().size() - 1; i >= 0; --i) {
            final Entity entity = this.getPassengers().get(i);
            double prevX = entity.getX();
            double prevY = entity.getY();
            double prevZ = entity.getZ();

            entity.dismountRidingEntity();

            // Player will sometimes warp to bottom of map when dismounting
            if (Math.abs(prevX - entity.getX()) > 2 || Math.abs(prevY - entity.getZ()) > 2 || Math.abs(prevZ - entity.getZ()) > 2) {
                entity.setPos(prevX, prevY, prevZ);
            }
        }
    }

    @Override
    public void onEntityUpdate() {
        if (host == null && hostPos != null) {
            final BlockEntity blockEntity = world.getBlockEntity(hostPos);
            if (tile instanceof LauncherBaseBlockEntity) {
                host = (LauncherBaseBlockEntity) tile;
                host.seat = this;
            }
        }

        if (!world.isClientSide() && (host == null || host.isInvalid() || this.getY() < -64.0D)) {
            this.removePassengers();
            this.setDead();
        }

        if (host != null && (prevFace != host.getLaunchDirection() || prevRotation != host.getSeatSide())) {
            prevFace = host.getLaunchDirection();
            prevRotation = host.getSeatSide();
            updatePosition(host.getLaunchDirection(), host.getSeatSide());
            updateBox(host.getLaunchDirection(), host.getSeatSide());
        }
    }

    protected void updatePosition(Direction face, Direction rotation) {

        // Rotation relative to block face
        offsetX = rotation.getFrontOffsetX() * 0.2f; //TODO customize to match missile visuals
        offsetY = rotation.getFrontOffsetY() * 0.2f;
        offsetZ = rotation.getFrontOffsetZ() * 0.2f;

        // Height relative to block rotation
        offsetX += face.getFrontOffsetX() * 0.5f;
        offsetY += face.getFrontOffsetY() * 0.5f;
        offsetZ += face.getFrontOffsetZ() * 0.5f;

        if (face == Direction.DOWN) {
            offsetY -= height;
        } else if (face == Direction.EAST) {
            offsetX += width / 2;
            offsetY -= 0.6f;
        } else if (face == Direction.WEST) {
            offsetX -= width / 2;
            offsetY -= 0.6f;
        } else if (face == Direction.NORTH) {
            offsetZ -= width / 2;
            offsetY -= 0.6f;
        } else if (face == Direction.SOUTH) {
            offsetZ += width / 2;
            offsetY -= 0.6f;
        }

        // Position
        double posX = host.getPos().getX() + 0.5 + face.getFrontOffsetX() * 0.5;
        double posY = host.getPos().getY() + 0.5 + face.getFrontOffsetY() * 0.5;
        double posZ = host.getPos().getZ() + 0.5 + face.getFrontOffsetZ() * 0.5;
        setPosition(posX, posY, posZ);

        final EulerAngle angle = new EulerAngle(rotation);
        this.getYRot() = this.prevRotationYaw = (float) angle.yaw();
        this.getXRot() = this.prevRotationPitch = (float) angle.pitch();
    }

    protected void updateBox(Direction face, Direction rotation) {

        final float dimA;
        final float dimB;
        // Size
        if (face == Direction.UP || face == Direction.DOWN) {
            setSize(0.5f, 2.5f);
            dimA = this.width / 2;
            dimB = this.height;
        } else {
            setSize(2.5f, 0.5f);
            dimA = this.height / 2;
            dimB = this.width;
        }

        // bounding box
        if (face == Direction.UP) {
            double minX = posX - dimA;
            double minY = posY;
            double minZ = posZ - dimA;

            double maxX = posX + dimA;
            double maxY = posY + dimB;
            double maxZ = posZ + dimA;
            setEntityBoundingBox(new AxisAlignedBB(minX, minY, minZ, maxX, maxY, maxZ));
        } else if (face == Direction.DOWN) {
            double minX = posX - dimA;
            double minY = posY - dimB;
            double minZ = posZ - dimA;

            double maxX = posX + dimA;
            double maxY = posY;
            double maxZ = posZ + dimA;
            setEntityBoundingBox(new AxisAlignedBB(minX, minY, minZ, maxX, maxY, maxZ));
        } else if (face == Direction.EAST) {
            double minX = posX;
            double minY = posY - dimA;
            double minZ = posZ - dimA;

            double maxX = posX + dimB;
            double maxY = posY + dimA;
            double maxZ = posZ + dimA;
            setEntityBoundingBox(new AxisAlignedBB(minX, minY, minZ, maxX, maxY, maxZ));
        } else if (face == Direction.WEST) {
            double minX = posX - dimB;
            double minY = posY - dimA;
            double minZ = posZ - dimA;

            double maxX = posX;
            double maxY = posY + dimA;
            double maxZ = posZ + dimA;
            setEntityBoundingBox(new AxisAlignedBB(minX, minY, minZ, maxX, maxY, maxZ));
        } else if (face == Direction.NORTH) {
            double minX = posX - dimA;
            double minY = posY - dimA;
            double minZ = posZ - dimB;

            double maxX = posX + dimA;
            double maxY = posY + dimA;
            double maxZ = posZ;
            setEntityBoundingBox(new AxisAlignedBB(minX, minY, minZ, maxX, maxY, maxZ));
        } else if (face == Direction.SOUTH) {
            double minX = posX - dimA;
            double minY = posY - dimA;
            double minZ = posZ;

            double maxX = posX + dimA;
            double maxY = posY + dimA;
            double maxZ = posZ + dimB;
            setEntityBoundingBox(new AxisAlignedBB(minX, minY, minZ, maxX, maxY, maxZ));
        }
    }

    @Override
    public void setPosition(double x, double y, double z) {
        this.getX() = x;
        this.getY() = y;
        this.getZ() = z;
        if (this.isAddedToLevel() && !this.world.isClientSide())
            this.world.updateEntityWithOptionalForce(this, false); // Forge - Process chunk registration after moving.
    }

    @Override
    public void move(MoverType type, double x, double y, double z) {
        // Can't move
    }


    @Override
    public boolean processInitialInteract(Player player, InteractionHand hand) {
        if (player.isSneaking()) {
            return false;
        } else if (this.isBeingRidden()) {
            return true;
        } else {
            if (!this.world.isClientSide()) {
                if (player.isCreative()) {
                    final ItemStack itemStack = player.getHeldItem(hand);
                    if (itemStack.getItem() == Items.MINECART) {
                        final EntityMinecart cart = new EntityMinecartEmpty(world);
                        cart.setPosition(posX, posY, posZ);
                        world.spawnEntity(cart);

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
    public boolean canBeCollidedWith() {
        return !this.isDead;
    }

    @Override //make method public
    public void setSize(float width, float height) {
        this.width = width;
        this.height = height;
    }

    @Override
    public double getMountedYOffset() {
        return offsetY;
    }

    @Override
    @Nullable
    public AxisAlignedBB getCollisionBox(Entity entityIn) {
        return super.getEntityBoundingBox(); //TODO might be needed for interaction
    }

    @Override
    public void updatePassenger(Entity passenger) {
        if (this.isPassenger(passenger) && host != null) {
            double x = this.getX() + offsetX + passenger.getYOffset() * (prevFace != null ? prevFace.getFrontOffsetX() : 0);
            double y = this.getY() + offsetY + passenger.getYOffset() * (prevFace != null ? prevFace.getFrontOffsetY() : 0);
            double z = this.getZ() + offsetZ + passenger.getYOffset() * (prevFace != null ? prevFace.getFrontOffsetZ() : 0);
            passenger.setPosition(x, y, z);
        }
    }

    @Override
    public void applyEntityCollision(Entity p_70108_1_) {
        //disable collision
    }

    @Override
    public void addVelocity(double p_70024_1_, double p_70024_3_, double p_70024_5_) {
        //disable velocity
    }

    @Override
    protected void readEntityFromNBT(CompoundTag save) {
        if (save.contains("pos")) {
            this.hostPos = NBTUtil.getPosFromTag(save.getCompound("pos"));
        }

    }

    @Override
    protected void writeEntityToNBT(CompoundTag save) {
        if (hostPos != null) {
            save.put("pos", NBTUtil.createPosTag(hostPos));
        }
    }

    @Override
    public void writeSpawnData(ByteBuf buffer) {
        buffer.writeBoolean(host != null);
        if (host != null) {
            buffer.writeInt(host.getPos().getX());
            buffer.writeInt(host.getPos().getY());
            buffer.writeInt(host.getPos().getZ());
        }
    }

    @Override
    public void readSpawnData(ByteBuf additionalData) {
        if (additionalData.readBoolean()) {
            final BlockEntity blockEntity = world.getBlockEntity(new BlockPos(additionalData.readInt(), additionalData.readInt(), additionalData.readInt()));
            if (tile instanceof LauncherBaseBlockEntity) {
                host = (LauncherBaseBlockEntity) tile;
            }
        }
    }
}

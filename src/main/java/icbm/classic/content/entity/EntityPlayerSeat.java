package icbm.classic.content.entity;

import icbm.classic.content.blocks.launcher.base.TileLauncherBase;
import icbm.classic.lib.transform.rotation.EulerAngle;
import icbm.classic.lib.transform.vector.Pos;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.Entity;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.item.EntityMinecart;
import net.minecraft.entity.item.EntityMinecartEmpty;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.IEntityAdditionalSpawnData;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;

/**
 * Used a placeholder to move riding entities around
 *
 *
 * Created by Dark(DarkGuardsman, Robert) on 10/31/2017.
 */
public class EntityPlayerSeat extends Entity implements IEntityAdditionalSpawnData
{
    public TileLauncherBase host; //TODO save host position so we can restore from save

    public float offsetX = 0;
    public float offsetY = 0;
    public float offsetZ = 0;
    public EnumFacing prevFace;
    public EnumFacing prevRotation;

    public EntityPlayerSeat(World world)
    {
        super(world);
    }

    @Override
    public float getEyeHeight()
    {
        return 0;
    }

    @Override
    protected void entityInit()
    {

    }

    @Override
    public void onEntityUpdate()
    {
        if (!world.isRemote && (host == null || host.isInvalid() || this.posY < -64.0D))
        {
            this.setDead();
        }

        if(host != null && (prevFace != host.getLaunchDirection() || prevRotation != host.getSeatSide())) {
            prevFace = host.getLaunchDirection();
            prevRotation = host.getSeatSide();
            updatePosition(host.getLaunchDirection(), host.getSeatSide());
            updateBox(host.getLaunchDirection(), host.getSeatSide());
        }
    }

    protected void updatePosition(EnumFacing face, EnumFacing rotation) {

        // Rotation relative to block face
        offsetX = rotation.getFrontOffsetX() * 0.2f; //TODO customize to match missile visuals
        offsetY = rotation.getFrontOffsetY() * 0.2f;
        offsetZ = rotation.getFrontOffsetZ() * 0.2f;

        // Height relative to block rotation
        offsetX += face.getFrontOffsetX() * 0.5f;
        offsetY += face.getFrontOffsetY() * 0.5f;
        offsetZ += face.getFrontOffsetZ() * 0.5f;

        if(face == EnumFacing.DOWN) {
            offsetY -= height;
        }
        else if(face == EnumFacing.EAST) {
            offsetX += width / 2;
            offsetY -= 0.6f;
        }
        else if(face == EnumFacing.WEST) {
            offsetX -= width / 2;
            offsetY -= 0.6f;
        }
        else if(face == EnumFacing.NORTH) {
            offsetZ -= width / 2;
            offsetY -= 0.6f;
        }
        else if(face == EnumFacing.SOUTH) {
            offsetZ += width / 2;
            offsetY -= 0.6f;
        }

        // Position
        double posX = host.getPos().getX() + 0.5 + face.getFrontOffsetX() * 0.5;
        double posY = host.getPos().getY() + 0.5 + face.getFrontOffsetY() * 0.5;
        double posZ = host.getPos().getZ() + 0.5 + face.getFrontOffsetZ() * 0.5;
        setPosition(posX, posY, posZ);

        final EulerAngle angle = new EulerAngle(rotation);
        this.rotationYaw = this.prevRotationYaw = (float) angle.yaw();
        this.rotationPitch = this.prevRotationPitch = (float) angle.pitch();
    }

    protected void updateBox(EnumFacing face, EnumFacing rotation) {

        final float dimA;
        final float dimB;
        // Size
        if(face == EnumFacing.UP || face == EnumFacing.DOWN) {
            setSize(0.5f, 2.5f);
            dimA = this.width / 2;
            dimB = this.height;
        }
        else {
            setSize(2.5f, 0.5f);
            dimA = this.height / 2;
            dimB = this.width;
        }

        // bounding box
        if(face == EnumFacing.UP) {
            double minX = posX - dimA;
            double minY = posY;
            double minZ = posZ - dimA;

            double maxX = posX + dimA;
            double maxY = posY + dimB;
            double maxZ = posZ + dimA;
            setEntityBoundingBox(new AxisAlignedBB(minX, minY, minZ, maxX, maxY, maxZ));
        }
        else if(face == EnumFacing.DOWN) {
            double minX = posX - dimA;
            double minY = posY - dimB;
            double minZ = posZ - dimA;

            double maxX = posX + dimA;
            double maxY = posY;
            double maxZ = posZ + dimA;
            setEntityBoundingBox(new AxisAlignedBB(minX, minY, minZ, maxX, maxY, maxZ));
        }
        else if(face == EnumFacing.EAST) {
            double minX = posX;
            double minY = posY - dimA;
            double minZ = posZ - dimA;

            double maxX = posX + dimB;
            double maxY = posY + dimA;
            double maxZ = posZ + dimA;
            setEntityBoundingBox(new AxisAlignedBB(minX, minY, minZ, maxX, maxY, maxZ));
        }
        else if(face == EnumFacing.WEST) {
            double minX = posX - dimB;
            double minY = posY - dimA;
            double minZ = posZ - dimA;

            double maxX = posX;
            double maxY = posY + dimA;
            double maxZ = posZ + dimA;
            setEntityBoundingBox(new AxisAlignedBB(minX, minY, minZ, maxX, maxY, maxZ));
        }
        else if(face == EnumFacing.NORTH) {
            double minX = posX - dimA;
            double minY = posY - dimA;
            double minZ = posZ - dimB;

            double maxX = posX + dimA;
            double maxY = posY + dimA;
            double maxZ = posZ;
            setEntityBoundingBox(new AxisAlignedBB(minX, minY, minZ, maxX, maxY, maxZ));
        }

        else if(face == EnumFacing.SOUTH) {
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
    public void setPosition(double x, double y, double z)
    {
        this.posX = x;
        this.posY = y;
        this.posZ = z;
        if (this.isAddedToWorld() && !this.world.isRemote) this.world.updateEntityWithOptionalForce(this, false); // Forge - Process chunk registration after moving.
    }

    @Override
    public void move(MoverType type, double x, double y, double z)
    {
        // Can't move
    }


    @Override
    public boolean processInitialInteract(EntityPlayer player, EnumHand hand)
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
    public boolean canBeCollidedWith()
    {
        return !this.isDead;
    }

    @Override //make method public
    public void setSize(float width, float height)
    {
        this.width = width;
        this.height = height;
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
        return super.getEntityBoundingBox(); //TODO might be needed for interaction
    }

    @Override
    public void updatePassenger(Entity passenger)
    {
        if (this.isPassenger(passenger))
        {
            double x = this.posX + offsetX + passenger.getYOffset() * (prevFace != null ? prevFace.getFrontOffsetX() : 0);
            double y = this.posY + offsetY + passenger.getYOffset() * (prevFace != null ? prevFace.getFrontOffsetY() : 0);
            double z = this.posZ + offsetZ + passenger.getYOffset() * (prevFace != null ? prevFace.getFrontOffsetZ() : 0);
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
    protected void readEntityFromNBT(NBTTagCompound p_70037_1_)
    {

    }

    @Override
    protected void writeEntityToNBT(NBTTagCompound p_70014_1_)
    {

    }

    @Override
    public void writeSpawnData(ByteBuf buffer)
    {
        buffer.writeBoolean(host != null);
        if(host != null) {
            buffer.writeInt(host.getPos().getX());
            buffer.writeInt(host.getPos().getY());
            buffer.writeInt(host.getPos().getZ());
        }
    }

    @Override
    public void readSpawnData(ByteBuf additionalData)
    {
        if(additionalData.readBoolean()) {
            final TileEntity tile = world.getTileEntity(new BlockPos(additionalData.readInt(), additionalData.readInt(), additionalData.readInt()));
            if(tile instanceof TileLauncherBase) {
                host = (TileLauncherBase) tile;
            }
        }
    }
}

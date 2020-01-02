package icbm.classic.content.entity;

import icbm.classic.lib.transform.vector.Pos;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
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
    public TileEntity host;
    public Pos rideOffset;

    public EntityPlayerSeat(World world)
    {
        super(world);
    }

    @Override
    protected void entityInit()
    {

    }

    @Override
    public void onEntityUpdate()
    {
        if (!world.isRemote && (host == null || host.isInvalid()) || this.posY < -64.0D)
        {
            this.setDead();
        }
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
        super.setSize(width, height);
    }

    @Override
    public double getMountedYOffset()
    {
        if (rideOffset != null)
        {
            return rideOffset.y();
        }
        return super.getMountedYOffset();
    }

    @Override
    @Nullable
    public AxisAlignedBB getCollisionBox(Entity entityIn)
    {
        return null; //TODO might be needed for interaction
    }

    @Override
    public void updatePassenger(Entity passenger)
    {
        if (this.isPassenger(passenger))
        {
            passenger.setPosition(this.posX, this.posY + this.getMountedYOffset() + passenger.getYOffset(), this.posZ); //TODO add rotation and position math
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
        buffer.writeFloat(height);
        buffer.writeFloat(width);
        buffer.writeBoolean(rideOffset != null);
        if (rideOffset != null)
        {
            rideOffset.writeByteBuf(buffer);
        }
    }

    @Override
    public void readSpawnData(ByteBuf additionalData)
    {
        height = additionalData.readFloat();
        width = additionalData.readFloat();
        setSize(width, height);
        if (additionalData.readBoolean())
        {
            rideOffset = new Pos(additionalData);
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public boolean isInRangeToRender3d(double x, double y, double z)
    {
        return false;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public boolean isInRangeToRenderDist(double distance)
    {
        return false;
    }
}

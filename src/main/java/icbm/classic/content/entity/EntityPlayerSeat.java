package icbm.classic.content.entity;

import cpw.mods.fml.common.registry.IEntityAdditionalSpawnData;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.World;

/**
 * Used a placeholder to move riding entities around
 *
 * @see <a href="https://github.com/BuiltBrokenModding/VoltzEngine/blob/development/license.md">License</a> for what you can and can't do with the code.
 * Created by Dark(DarkGuardsman, Robert) on 10/31/2017.
 */
public class EntityPlayerSeat extends Entity implements IEntityAdditionalSpawnData
{
    public TileEntity host;

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
        if (!worldObj.isRemote && host == null || this.posY < -64.0D)
        {
            this.kill();
        }
    }

    @Override
    public boolean interactFirst(EntityPlayer entityPlayer)
    {
        //Handle player riding missile
        if (this.riddenByEntity != null && this.riddenByEntity instanceof EntityPlayer && this.riddenByEntity != entityPlayer)
        {
            return true;
        }
        else if (this.riddenByEntity != null && this.riddenByEntity != entityPlayer)
        {
            return false;
        }
        else
        {
            if (!this.worldObj.isRemote)
            {
                entityPlayer.mountEntity(this);
            }
            return true;
        }
    }

    @Override
    public AxisAlignedBB getBoundingBox()
    {
        return boundingBox;
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
        return super.getMountedYOffset();
    }

    @Override
    public void applyEntityCollision(Entity p_70108_1_)
    {
        //disable collision
    }

    @Override
    protected boolean func_145771_j(double p_145771_1_, double p_145771_3_, double p_145771_5_)
    {
        //Disable collision
        return false;
    }

    @Override
    public void addVelocity(double p_70024_1_, double p_70024_3_, double p_70024_5_)
    {
        //disable velocity
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void setPositionAndRotation2(double p_70056_1_, double p_70056_3_, double p_70056_5_, float p_70056_7_, float p_70056_8_, int p_70056_9_)
    {
        this.setPosition(p_70056_1_, p_70056_3_, p_70056_5_);
        this.setRotation(p_70056_7_, p_70056_8_);
        //Removed collision update
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
    }

    @Override
    public void readSpawnData(ByteBuf additionalData)
    {
        height = additionalData.readFloat();
        width = additionalData.readFloat();
        setSize(width, height);
    }
}

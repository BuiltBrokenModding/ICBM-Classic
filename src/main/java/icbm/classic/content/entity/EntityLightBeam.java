package icbm.classic.content.entity;

import com.builtbroken.jlib.data.vector.IPos3D;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.IEntityAdditionalSpawnData;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class EntityLightBeam extends Entity implements IEntityAdditionalSpawnData
{
    public float red, green, blue;

    public EntityLightBeam(World world)
    {
        super(world);
        this.setSize(1F, 1F);
        this.preventEntitySpawning = true;
        this.ignoreFrustumCheck = true;
        this.height = 1;
        this.width = 1;
        //this.renderDistanceWeight = 3;
    }

    public EntityLightBeam(World world, IPos3D position, float red, float green, float blue)
    {
        super(world);
        this.setPosition(position.x(), position.y(), position.z());
        this.red = red;
        this.green = green;
        this.blue = blue;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public AxisAlignedBB getRenderBoundingBox()
    {
        return new AxisAlignedBB(posX - 5, -10, posZ - 5, posX + 5, Double.POSITIVE_INFINITY, posZ + 5);
    }

    @Override
    public void writeSpawnData(ByteBuf data)
    {
        data.writeFloat(this.red);
        data.writeFloat(this.green);
        data.writeFloat(this.blue);
    }

    @Override
    public void readSpawnData(ByteBuf data)
    {
        this.red = data.readFloat();
        this.green = data.readFloat();
        this.blue = data.readFloat();
    }

    @Override
    protected void entityInit()
    {
    }

    @Override
    public void onUpdate()
    {
        if (ticksExisted > 20 * 60 * 5)
        {
            setDead();
        }
    }

    @Override
    public boolean canBePushed()
    {
        return false;
    }

    @Override
    protected boolean canTriggerWalking()
    {
        return false;
    }

    @Override
    public boolean canBeCollidedWith()
    {
        return false;
    }

    @Override
    protected void readEntityFromNBT(NBTTagCompound var1)
    {

    }

    @Override
    protected void writeEntityToNBT(NBTTagCompound var1)
    {

    }
}
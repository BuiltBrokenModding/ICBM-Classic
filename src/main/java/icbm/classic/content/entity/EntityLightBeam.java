package icbm.classic.content.entity;

import com.builtbroken.jlib.data.vector.IPos3D;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.IEntityAdditionalSpawnData;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;

public class EntityLightBeam extends Entity implements IEntityAdditionalSpawnData
{
    //Render color
    public float red = 1;
    public float green = 0;
    public float blue = 0;

    //Render Size
    public float beamSize = 0.5f;
    public float beamGlowSize = 1f;

    //Client state
    public float clientBeamProgress = 0;

    //State
    public boolean deathCycle = false;
    public float targetBeamProgress = 1;
    public float beamGrowthRate = 0.05f;

    //Data
    private static final DataParameter<Float> BEAM_PROGRESS = EntityDataManager.createKey(EntityLightBeam.class, DataSerializers.FLOAT);

    public EntityLightBeam(World world)
    {
        super(world);
        this.setSize(1F, 1F);
        this.preventEntitySpawning = true;
        this.ignoreFrustumCheck = true;
        this.height = 1;
        this.width = 1;
    }

    @Override
    protected void entityInit()
    {
        this.getDataManager().register(BEAM_PROGRESS, -1f);
    }

    public EntityLightBeam setPosition(IPos3D position)
    {
        this.setPosition(position.x(), position.y(), position.z());
        return this;
    }

    public EntityLightBeam setColor(float red, float green, float blue)
    {
        this.red = red;
        this.green = green;
        this.blue = blue;
        return this;
    }

    public void startDeathCycle()
    {
        deathCycle = true;
    }

    public void setTargetBeamProgress(float value)
    {
        targetBeamProgress = value;
    }

    public void setActualBeamProgress(float value)
    {
        this.getDataManager().set(BEAM_PROGRESS, Math.min(1, Math.max(0, value)));
    }

    public float getBeamProgress()
    {
        return this.getDataManager().get(BEAM_PROGRESS);
    }

    @Override
    @SideOnly(Side.CLIENT)
    @Nonnull
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
        data.writeFloat(this.beamSize);
        data.writeFloat(this.beamGlowSize);
    }

    @Override
    public void readSpawnData(ByteBuf data)
    {
        this.red = data.readFloat();
        this.green = data.readFloat();
        this.blue = data.readFloat();
        this.beamSize = data.readFloat();
        this.beamGlowSize = data.readFloat();
    }

    @Override
    public void onUpdate()
    {
        //Grow beam slowly
        if (getBeamProgress() < targetBeamProgress)
        {
            setActualBeamProgress(Math.min(targetBeamProgress, getBeamProgress() + beamGrowthRate));
        }
        //Decrease size slowly
        else if(getBeamProgress() > targetBeamProgress)
        {
            setActualBeamProgress(Math.max(targetBeamProgress, getBeamProgress() - beamGrowthRate));
        }

        //Kill off beam when animation finishes
        if (deathCycle && Math.abs(getBeamProgress() - targetBeamProgress) <= 0.01)
        {
            setDead();
        }
        //Safety in case the beam is never killed
        else if (ticksExisted > 20 * 60 * 5) //ticks per second * seconds * mins = 5 mins
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
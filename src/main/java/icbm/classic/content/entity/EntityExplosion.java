package icbm.classic.content.entity;

import com.builtbroken.mc.api.IWorldPosition;
import com.builtbroken.mc.lib.transform.vector.Location;
import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.common.registry.IEntityAdditionalSpawnData;
import icbm.classic.ICBMClassic;
import icbm.classic.content.explosive.blast.Blast;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

import java.lang.reflect.Constructor;

/** The Entity handler responsible for entity explosions.
 *
 * @author Calclavia */
public class EntityExplosion extends Entity implements IEntityAdditionalSpawnData, IWorldPosition
{
    public Blast blast;

    private boolean endExplosion = false;

    public EntityExplosion(World world)
    {
        super(world);
        this.preventEntitySpawning = true;
        this.setSize(0.98F, 0.98F);
        this.yOffset = this.height / 2.0F;
        this.renderDistanceWeight = 2f;
        this.ignoreFrustumCheck = true;
        this.ticksExisted = 0;
    }

    public EntityExplosion(Blast blast)
    {
        this(blast.world());
        this.blast = blast;
        this.setPosition(blast.position.x(), blast.position.y(), blast.position.z());
    }

    @Override
    public String getCommandSenderName()
    {
        return "Explosion";
    }

    @Override
    public void writeSpawnData(ByteBuf data)
    {
        try
        {
            NBTTagCompound nbt = new NBTTagCompound();
            this.writeEntityToNBT(nbt);
            ByteBufUtils.writeTag(data, nbt);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public void readSpawnData(ByteBuf data)
    {
        try
        {
            this.readEntityFromNBT(ByteBufUtils.readTag(data));
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    @Override
    protected void entityInit()
    {
    }

    /** returns if this entity triggers Block.onEntityWalking on the blocks they walk on. used for
     * spiders and wolves to prevent them from trampling crops */
    @Override
    protected boolean canTriggerWalking()
    {
        return false;
    }

    /** Returns true if other Entities should be prevented from moving through this Entity. */
    @Override
    public boolean canBeCollidedWith()
    {
        return false;
    }

    /** Called to update the entity's position/logic. */
    @Override
    public void onUpdate()
    {
        if (this.blast == null)
        {
            this.setDead();
            ICBMClassic.INSTANCE.logger().error("Procedural explosion ended due to null! This is a bug!");
            return;
        }

        this.blast.controller = this;
        this.blast.position = new Location((IWorldPosition) this);

        if (this.blast.isMovable() && (this.motionX != 0 || this.motionY != 0 || this.motionZ != 0))
        {
            this.moveEntity(this.motionX, this.motionY, this.motionZ);
        }

        if (this.ticksExisted == 1)
        {
            this.blast.preExplode();
        }
        else if (this.ticksExisted % this.blast.proceduralInterval() == 0)
        {
            if (!this.endExplosion)
            {
                this.blast.onExplode();
            }
            else
            {
                this.blast.postExplode();
                this.setDead();
            }
        }
    }


    public void endExplosion()
    {
        this.endExplosion = true;
    }

    /** (abstract) Protected helper method to read subclass entity data from NBT. */
    @Override
    protected void readEntityFromNBT(NBTTagCompound nbt)
    {
        try
        {
            NBTTagCompound blastSave = nbt.getCompoundTag("blast");

            if (this.blast == null)
            {
                Class clazz = Class.forName(blastSave.getString("class"));
                Constructor constructor = clazz.getConstructor(World.class, Entity.class, double.class, double.class, double.class, float.class);
                this.blast = (Blast) constructor.newInstance(this.worldObj, null, this.posX, this.posY, this.posZ, 0);
            }

            this.blast.readFromNBT(blastSave);
        }
        catch (Exception e)
        {
            ICBMClassic.INSTANCE.logger().error("ICBM error in loading an explosion!");
            e.printStackTrace();
        }
    }

    /** (abstract) Protected helper method to write subclass entity data to NBT. */
    @Override
    protected void writeEntityToNBT(NBTTagCompound nbt)
    {
        NBTTagCompound baoZhaNBT = new NBTTagCompound();
        baoZhaNBT.setString("class", this.blast.getClass().getCanonicalName());
        this.blast.writeToNBT(baoZhaNBT);
        nbt.setTag("blast", baoZhaNBT);
    }

    @Override
    public float getShadowSize()
    {
        return 0F;
    }

    @Override
    public double x()
    {
        return this.posX;
    }

    @Override
    public double y()
    {
        return this.posY;
    }

    @Override
    public double z()
    {
        return this.posZ;
    }

    @Override
    public World world()
    {
        return this.worldObj;
    }
}

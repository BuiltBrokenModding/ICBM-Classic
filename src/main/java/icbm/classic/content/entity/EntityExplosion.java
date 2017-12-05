package icbm.classic.content.entity;

import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.common.registry.IEntityAdditionalSpawnData;
import icbm.classic.ICBMClassic;
import icbm.classic.content.explosive.blast.Blast;
import icbm.classic.content.explosive.blast.BlastRedmatter;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;

import java.lang.reflect.Constructor;

/**
 * The Entity handler responsible for entity explosions.
 *
 * @author Calclavia
 */
public class EntityExplosion extends Entity implements IEntityAdditionalSpawnData
{
    private Blast blast;
    private double blastYOffset = 0;

    private boolean endExplosion = false;

    public EntityExplosion(World world)
    {
        super(world);
        this.preventEntitySpawning = true;
        this.noClip = true;
        this.setSize(0.98F, 0.98F);
        this.yOffset = this.height / 2.0F;
        this.renderDistanceWeight = 2f;
        this.ignoreFrustumCheck = true;
        this.ticksExisted = 0;
    }

    public EntityExplosion(Blast blast)
    {
        this(blast.oldWorld());
        this.setBlast(blast);
    }

    @Override
    public String getCommandSenderName()
    {
        return "Explosion[" + blast + "]";
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

    @Override
    protected boolean func_145771_j(double p_145771_1_, double p_145771_3_, double p_145771_5_)
    {
        if (!(blast instanceof BlastRedmatter))
        {
            return super.func_145771_j(p_145771_1_, p_145771_3_, p_145771_5_);
        }
        return false;
    }

    /**
     * returns if this entity triggers Block.onEntityWalking on the blocks they walk on. used for
     * spiders and wolves to prevent them from trampling crops
     */
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
        if (this.getBlast() == null || this.getBlast().controller != this || !this.getBlast().isAlive)
        {
            this.setDead();
            ICBMClassic.INSTANCE.logger().error("Procedural explosion ended due to null! This is a bug!");
            return;
        }

        if (this.getBlast().isMovable() && (this.motionX != 0 || this.motionY != 0 || this.motionZ != 0))
        {
            //Slow entity down
            this.motionX *= .98;
            this.motionY *= .98;
            this.motionZ *= .98;

            //Normalize
            float speed = MathHelper.sqrt_double(motionX * motionX + motionY * motionY + motionZ * motionZ);
            this.motionX /= (double) speed;
            this.motionY /= (double) speed;
            this.motionZ /= (double) speed;

            //Apply Speed
            speed = Math.min(speed, 0.5f);
            this.motionX *= (double) speed;
            this.motionY *= (double) speed;
            this.motionZ *= (double) speed;

            //Move box
            this.boundingBox.offset(motionX, motionY, motionZ);

            //Reset position based on box
            this.posX = (this.boundingBox.minX + this.boundingBox.maxX) / 2.0D;
            this.posY = this.boundingBox.minY + (double) this.yOffset - (double) this.ySize;
            this.posZ = (this.boundingBox.minZ + this.boundingBox.maxZ) / 2.0D;

            //Update blast
            getBlast().onPositionUpdate(posX, posY + blastYOffset, posZ);
        }

        if (this.ticksExisted == 1)
        {
            this.getBlast().preExplode();
        }
        else if (this.ticksExisted % this.getBlast().proceduralInterval() == 0)
        {
            if (!this.endExplosion)
            {
                this.getBlast().onExplode();
            }
            else
            {
                this.getBlast().postExplode();
                this.setDead();
            }
        }
    }

    @Override
    public void moveEntity(double p_70091_1_, double p_70091_3_, double p_70091_5_)
    {
        //Remove default movement
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
            this.blastYOffset = nbt.getDouble("blastPosY");
            if (this.getBlast() == null)
            {
                Class clazz = Class.forName(blastSave.getString("class"));
                Constructor constructor = clazz.getConstructor(World.class, Entity.class, double.class, double.class, double.class, float.class);
                //TODO save person who triggered the explosion
                this.setBlast((Blast) constructor.newInstance(this.worldObj, null, posX, posY + blastYOffset, posZ, 0));
            }

            this.getBlast().readFromNBT(blastSave);
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
        nbt.setDouble("blastPosY", blastYOffset);

        NBTTagCompound baoZhaNBT = new NBTTagCompound();
        baoZhaNBT.setString("class", this.getBlast().getClass().getCanonicalName());
        this.getBlast().writeToNBT(baoZhaNBT);
        nbt.setTag("blast", baoZhaNBT);
    }

    public Blast getBlast()
    {
        return blast;
    }

    public void setBlast(Blast blast)
    {
        this.blast = blast;
        if (blast != null)
        {
            this.blast.controller = this;
            this.setPosition(blast.position.x(), !blast.isMovable() ? -1 : blast.y(), blast.position.z());
            blastYOffset = blast.isMovable() ? 0 : blast.y() + 1;
        }
    }
}

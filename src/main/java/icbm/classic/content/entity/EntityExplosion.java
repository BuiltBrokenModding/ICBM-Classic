package icbm.classic.content.entity;

import icbm.classic.ICBMClassic;
import icbm.classic.content.explosive.blast.Blast;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.Entity;
import net.minecraft.entity.MoverType;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.registry.IEntityAdditionalSpawnData;

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
        //this.yOffset = this.height / 2.0F;
        //this.renderDistanceWeight = 2f;
        this.ignoreFrustumCheck = true;
        this.ticksExisted = 0;
    }

    public EntityExplosion(Blast blast)
    {
        this(blast.world());
        this.setBlast(blast);
    }

    @Override
    public String getName()
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
            return;
        }

        if (this.getBlast().isMovable() && (this.motionX != 0 || this.motionY != 0 || this.motionZ != 0))
        {
            //Slow entity down
            this.motionX *= .98;
            this.motionY *= .98;
            this.motionZ *= .98;

            //Normalize
            float speed = MathHelper.sqrt(motionX * motionX + motionY * motionY + motionZ * motionZ);
            this.motionX /= (double) speed;
            this.motionY /= (double) speed;
            this.motionZ /= (double) speed;

            //Apply Speed
            speed = Math.min(speed, 0.5f);
            this.motionX *= (double) speed;
            this.motionY *= (double) speed;
            this.motionZ *= (double) speed;

            //Move box
            this.getEntityBoundingBox().offset(motionX, motionY, motionZ);

            //Reset position based on box
            this.posX = (this.getEntityBoundingBox().minX + this.getEntityBoundingBox().maxX) / 2.0D;
            this.posY = (this.getEntityBoundingBox().minY + this.getEntityBoundingBox().maxY) / 2.0D;
            this.posZ = (this.getEntityBoundingBox().minZ + this.getEntityBoundingBox().maxZ) / 2.0D;

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
                this.setDead();
                this.getBlast().postExplode();
            }
        }
    }

    @Override
    public void move(MoverType type, double p_70091_1_, double p_70091_3_, double p_70091_5_)
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
                this.setBlast((Blast) constructor.newInstance(this.world, null, posX, posY + blastYOffset, posZ, 0));
            }

            this.getBlast().readFromNBT(blastSave);
        }
        catch (Exception e)
        {
            ICBMClassic.logger().error("ICBM error in loading an explosion!");
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

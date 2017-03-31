package icbm.classic.content.entity;

import com.builtbroken.jlib.data.vector.IPos3D;
import com.builtbroken.mc.core.Engine;
import com.builtbroken.mc.imp.transform.vector.Pos;
import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.common.registry.IEntityAdditionalSpawnData;
import io.netty.buffer.ByteBuf;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.DamageSource;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.fluids.IFluidBlock;

/** @author Calclavia */
public class EntityFlyingBlock extends Entity implements IEntityAdditionalSpawnData
{
    public Block block = null;
    public int metadata = 0;

    public float yawChange = 0;
    public float pitchChange = 0;

    public float gravity = 0.045f;

    public EntityFlyingBlock(World world)
    {
        super(world);
        this.ticksExisted = 0;
        this.preventEntitySpawning = true;
        this.isImmuneToFire = true;
        this.yOffset = height / 2.0F;
        this.setSize(0.98F, 0.98F);
    }

    public EntityFlyingBlock(World world, IPos3D position, Block blockID, int metadata)
    {
        this(world);
        this.setPosition(position.x() + 0.5, position.y(), position.z() + 0.5);
        this.motionX = 0D;
        this.motionY = 0D;
        this.motionZ = 0D;
        this.block = blockID;
        this.metadata = metadata;
    }

    public EntityFlyingBlock(World world, Pos position, Block blockID, int metadata, float gravity)
    {
        this(world, position, blockID, metadata);
        this.gravity = gravity;
    }

    @Override
    public String getCommandSenderName()
    {
        return "Flying Block [" + (block != null ? block.getUnlocalizedName() : "null") + ", " + metadata + ", " + hashCode() + "]";
    }

    @Override
    public void writeSpawnData(ByteBuf data)
    {
        ByteBufUtils.writeUTF8String(data, this.block != null ? Block.blockRegistry.getNameForObject(block) : "");
        data.writeInt(this.metadata);
        data.writeFloat(this.gravity);
        data.writeFloat(yawChange);
        data.writeFloat(pitchChange);
    }

    @Override
    public void readSpawnData(ByteBuf data)
    {
        String name = ByteBufUtils.readUTF8String(data);
        if(!name.isEmpty())
        {
            block = (Block) Block.blockRegistry.getObject(name);
        }
        else
        {
            block = Blocks.stone;
        }
        metadata = data.readInt();
        gravity = data.readFloat();
        yawChange = data.readFloat();
        pitchChange = data.readFloat();
    }

    @Override
    protected void entityInit()
    {
    }

    @Override
    public void onUpdate()
    {
        if (this.block == null)
        {
            this.setDead();
            return;
        }

        //TODO make a black list of blocks that shouldn't be a flying entity block
        if (this.posY > 400 || this.block == Engine.multiBlock || this.block == Blocks.piston_extension || this.block instanceof IFluidBlock)
        {
            this.setDead();
            return;
        }

        this.motionY -= gravity;

        if (this.isCollided)
        {
            this.func_145771_j(this.posX, (this.boundingBox.minY + this.boundingBox.maxY) / 2.0D, this.posZ);
        }

        this.moveEntity(this.motionX, this.motionY, this.motionZ);

        if (this.yawChange > 0)
        {
            this.rotationYaw += this.yawChange;
            this.yawChange -= 2;
        }

        if (this.pitchChange > 0)
        {
            this.rotationPitch += this.pitchChange;
            this.pitchChange -= 2;
        }

        if ((this.onGround && this.ticksExisted > 20) || this.ticksExisted > 20 * 120)
        {
            this.setBlock();
            return;
        }

        this.ticksExisted++;

        /*
        if(worldObj.isRemote && (motionX > 0.001 || motionZ > 0.001 || motionY > 0.001))
        {
            if (ICBMClassic.proxy.getParticleSetting() == 0)
            {
                if (worldObj.rand.nextInt(5) == 0)
                {
                    FMLClientHandler.instance().getClient().effectRenderer.addEffect(new EntityDiggingFX(worldObj, posX, posY, posZ, motionX, motionY, motionZ, block, 0, metadata));
                }
            }
        }
        */
    }

    public void setBlock()
    {
        if (!this.worldObj.isRemote)
        {
            int i = MathHelper.floor_double(posX);
            int j = MathHelper.floor_double(posY);
            int k = MathHelper.floor_double(posZ);

            this.worldObj.setBlock(i, j, k, this.block, this.metadata, 2);
        }

        this.setDead();
    }

    /** Checks to see if and entity is touching the missile. If so, blow up! */

    @Override
    public AxisAlignedBB getCollisionBox(Entity par1Entity)
    {
        // Make sure the entity is not an item
        if (par1Entity instanceof EntityLiving)
        {
            if (block != null)
            {
                if (!(block instanceof IFluidBlock) && (this.motionX > 2 || this.motionY > 2 || this.motionZ > 2))
                {
                    int damage = (int) (1.2 * (Math.abs(this.motionX) + Math.abs(this.motionY) + Math.abs(this.motionZ)));
                    ((EntityLiving) par1Entity).attackEntityFrom(DamageSource.fallingBlock, damage);
                }
            }
        }

        return null;
    }

    @Override
    protected void writeEntityToNBT(NBTTagCompound nbttagcompound)
    {
        nbttagcompound.setInteger("metadata", this.metadata);
        if(block != null)
        {
            nbttagcompound.setString("block", Block.blockRegistry.getNameForObject(block));
        }
        nbttagcompound.setFloat("gravity", this.gravity);
    }

    @Override
    protected void readEntityFromNBT(NBTTagCompound nbttagcompound)
    {
        this.metadata = nbttagcompound.getInteger("metadata");
        if(nbttagcompound.hasKey("block"))
        {
            this.block = (Block) Block.blockRegistry.getObject(nbttagcompound.getString("block"));
        }
        this.gravity = nbttagcompound.getFloat("gravity");
    }

    @Override
    public float getShadowSize()
    {
        return 0.5F;
    }

    @Override
    public boolean canBePushed()
    {
        return true;
    }

    @Override
    protected boolean canTriggerWalking()
    {
        return true;
    }

    @Override
    public boolean canBeCollidedWith()
    {
        return true;
    }
}
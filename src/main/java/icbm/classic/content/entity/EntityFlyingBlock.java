package icbm.classic.content.entity;

import icbm.classic.api.NBTConstants;
import io.netty.buffer.ByteBuf;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.MoverType;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.fluids.IFluidBlock;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.registry.IEntityAdditionalSpawnData;

/** @author Calclavia */
public class EntityFlyingBlock extends Entity implements IEntityAdditionalSpawnData
{
    private IBlockState _blockState;

    public float yawChange = 0;
    public float pitchChange = 0;

    public float gravity = 0.045f;

    public EntityFlyingBlock(World world)
    {
        super(world);
        this.ticksExisted = 0;
        this.preventEntitySpawning = true;
        this.isImmuneToFire = true;
        //this.yOffset = height / 2.0F;
        this.setSize(0.98F, 0.98F);
    }

    public EntityFlyingBlock(World world, BlockPos position, IBlockState state)
    {
        this(world);
        this.setPosition(position.getX() + 0.5, position.getY(), position.getZ() + 0.5);
        this.motionX = 0D;
        this.motionY = 0D;
        this.motionZ = 0D;
        this._blockState = state;
    }

    public EntityFlyingBlock(World world, BlockPos position, IBlockState state, float gravity)
    {
        this(world, position, state);
        this.gravity = gravity;
    }

    public IBlockState getBlockState()
    {
        if (_blockState == null)
        {
            _blockState = Blocks.STONE.getDefaultState();
        }
        return _blockState;
    }

    @Override
    public String getName()
    {
        return "Flying Block [" + getBlockState() + ", " + hashCode() + "]";
    }

    @Override
    public void writeSpawnData(ByteBuf data)
    {
        ByteBufUtils.writeTag(data, NBTUtil.writeBlockState(new NBTTagCompound(), getBlockState()));
        data.writeFloat(this.gravity);
        data.writeFloat(yawChange);
        data.writeFloat(pitchChange);
    }

    @Override
    public void readSpawnData(ByteBuf data)
    {
        _blockState = NBTUtil.readBlockState(ByteBufUtils.readTag(data));
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
        if (_blockState == null || ticksExisted > 20 * 60)
        {
            this.setDead();
            return;
        }

        //TODO make a black list of blocks that shouldn't be a flying entity block
        if (this.posY > 400 || this.posY < -40)
        {
            this.setDead();
            return;
        }

        this.motionY -= gravity;

        if(isWet())

        if (this.collided)
        {
            this.setPosition(this.posX, (this.getEntityBoundingBox().minY + this.getEntityBoundingBox().maxY) / 2.0D, this.posZ);
        }

        this.move(MoverType.SELF, this.motionX, this.motionY, this.motionZ);

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

        if ((this.onGround && this.ticksExisted > 20))
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
        if (!this.world.isRemote)
        {
            int i = MathHelper.floor(posX);
            int j = MathHelper.floor(posY);
            int k = MathHelper.floor(posZ);

            this.world.setBlockState(new BlockPos(i, j, k), getBlockState(), 2);
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
            if (getBlockState() != null)
            {
                if (!(getBlockState().getBlock() instanceof IFluidBlock) && (this.motionX > 2 || this.motionY > 2 || this.motionZ > 2))
                {
                    int damage = (int) (1.2 * (Math.abs(this.motionX) + Math.abs(this.motionY) + Math.abs(this.motionZ)));
                    ((EntityLiving) par1Entity).attackEntityFrom(DamageSource.FALLING_BLOCK, damage);
                }
            }
        }

        return null;
    }

    @Override
    protected void writeEntityToNBT(NBTTagCompound nbttagcompound)
    {
        if (_blockState != null)
        {
            nbttagcompound.setTag(NBTConstants.BLOCK_STATE, NBTUtil.writeBlockState(new NBTTagCompound(), _blockState));
        }
        nbttagcompound.setFloat(NBTConstants.GRAVITY, this.gravity);
    }

    @Override
    protected void readEntityFromNBT(NBTTagCompound nbttagcompound)
    {
        if (nbttagcompound.hasKey(NBTConstants.BLOCK_STATE))
        {
            _blockState = NBTUtil.readBlockState(nbttagcompound.getCompoundTag(NBTConstants.BLOCK_STATE));
        }
        this.gravity = nbttagcompound.getFloat(NBTConstants.GRAVITY);
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
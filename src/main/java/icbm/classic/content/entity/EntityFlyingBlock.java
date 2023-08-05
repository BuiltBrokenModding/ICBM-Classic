package icbm.classic.content.entity;

import icbm.classic.lib.NBTConstants;
import io.netty.buffer.ByteBuf;
import lombok.Setter;
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
    public static final float GRAVITY_DEFAULT = 0.045f;

    @Setter
    private IBlockState blockState;

    public float yawChange = 0;
    public float pitchChange = 0;

    public float gravity = GRAVITY_DEFAULT;

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
        this.blockState = state;
    }

    public EntityFlyingBlock(World world, BlockPos position, IBlockState state, float gravity)
    {
        this(world, position, state);
        this.gravity = gravity;
    }

    public void restoreGravity()
    {
        gravity = GRAVITY_DEFAULT;
    }

    public IBlockState getBlockState()
    {
        if (blockState == null)
        {
            blockState = Blocks.STONE.getDefaultState();
        }
        return blockState;
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
        blockState = NBTUtil.readBlockState(ByteBufUtils.readTag(data));
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
        //Death state handling
        if (!world.isRemote)
        {
            if (blockState == null || ticksExisted > 20 * 60) //1 min despawn timer
            {
                this.placeBlockIntoWorld();
                return;
            }

            //TODO make a black list of blocks that shouldn't be a flying entity block
            if (this.posY > 400 || this.posY < -40)
            {
                this.setDead();
                return;
            }

            if ((this.onGround && this.ticksExisted > 20))
            {
                this.placeBlockIntoWorld();
                return;
            }
        }

        //Apply gravity acceleration
        this.motionY -= gravity;

        //Do movement
        this.move(MoverType.SELF, this.motionX, this.motionY, this.motionZ);

        //Handle collisions
        if (this.collided)
        {
            this.setPosition(this.posX, (this.getEntityBoundingBox().minY + this.getEntityBoundingBox().maxY) / 2.0D, this.posZ);
        }

        //Animation
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

        //Tick update
        this.ticksExisted++;
    }

    public void placeBlockIntoWorld()
    {
        if (!this.world.isRemote)
        {
            final int i = MathHelper.floor(posX);
            final int j = MathHelper.floor(posY);
            final int k = MathHelper.floor(posZ);

            final BlockPos pos = new BlockPos(i, j, k);

            final IBlockState currentState = world.getBlockState(pos);

            if (currentState.getBlock().isReplaceable(this.world, pos))
            {
                this.world.setBlockState(pos, getBlockState(), 3);
            }
            //TODO find first block if not replaceable
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
        if (blockState != null)
        {
            nbttagcompound.setTag(NBTConstants.BLOCK_STATE, NBTUtil.writeBlockState(new NBTTagCompound(), blockState));
        }
        nbttagcompound.setFloat(NBTConstants.GRAVITY, this.gravity);
    }

    @Override
    protected void readEntityFromNBT(NBTTagCompound nbttagcompound)
    {
        if (nbttagcompound.hasKey(NBTConstants.BLOCK_STATE))
        {
            blockState = NBTUtil.readBlockState(nbttagcompound.getCompoundTag(NBTConstants.BLOCK_STATE));
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
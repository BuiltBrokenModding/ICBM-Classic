package icbm.classic.world.entity.flyingblock;

import icbm.classic.lib.NBTConstants;
import icbm.classic.world.IcbmEntityTypes;
import io.netty.buffer.ByteBuf;
import lombok.AccessLevel;
import lombok.Setter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityLiving;
import net.minecraft.world.entity.MoverType;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.core.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.neoforged.fluids.IFluidBlock;
import net.neoforged.fml.common.network.ByteBufUtils;
import net.neoforged.fml.common.registry.IEntityAdditionalSpawnData;

/**
 * @author Calclavia
 */
public class FlyingBlockEntity extends Entity implements IEntityAdditionalSpawnData {
    public static final float GRAVITY_DEFAULT = 0.045f;

    @Setter(value = AccessLevel.PACKAGE)
    private BlockState blockState;

    public float yawChange = 0;
    public float pitchChange = 0;

    public float gravity = GRAVITY_DEFAULT;

    public FlyingBlockEntity(EntityType<?> type, Level level) {
        super(type, level);
        this.ticksExisted = 0;
        this.preventEntitySpawning = true;
        this.isImmuneToFire = true;
        //this.yOffset = height / 2.0F;
        this.setSize(0.98F, 0.98F);
    }

    public void restoreGravity() {
        gravity = GRAVITY_DEFAULT;
    }

    public BlockState getBlockState() {
        if (blockState == null) {
            blockState = Blocks.STONE.getDefaultState();
        }
        return blockState;
    }

    @Override
    public String getName() {
        return "Flying Block [" + getBlockState() + ", " + hashCode() + "]";
    }

    @Override
    public void writeSpawnData(ByteBuf data) {
        ByteBufUtils.writeTag(data, NBTUtil.writeBlockState(new CompoundTag(), getBlockState()));
        data.writeFloat(this.gravity);
        data.writeFloat(yawChange);
        data.writeFloat(pitchChange);
    }

    @Override
    public void readSpawnData(ByteBuf data) {
        blockState = NBTUtil.readBlockState(ByteBufUtils.readTag(data));
        gravity = data.readFloat();
        yawChange = data.readFloat();
        pitchChange = data.readFloat();
    }

    @Override
    protected void entityInit() {
    }

    @Override
    public void onUpdate() {
        //Death state handling
        if (!world.isClientSide()) {
            if (blockState == null || ticksExisted > 20 * 60) //1 min despawn timer
            {
                this.placeBlockIntoLevel();
                return;
            }

            //TODO make a black list of blocks that shouldn't be a flying entity block
            if (this.getY() > 400 || this.getY() < -40) {
                this.setDead();
                return;
            }

            if ((this.onGround && this.ticksExisted > 20)) {
                this.placeBlockIntoLevel();
                return;
            }
        }

        //Apply gravity acceleration
        this.motionY -= gravity;

        //Do movement
        this.move(MoverType.SELF, this.motionX, this.motionY, this.motionZ);

        //Handle collisions
        if (this.collided) {
            this.setPosition(this.getX(), (this.getEntityBoundingBox().minY + this.getEntityBoundingBox().maxY) / 2.0D, this.getZ());
        }

        //Animation
        if (this.yawChange > 0) {
            this.getYRot() += this.yawChange;
            this.yawChange -= 2;
        }

        if (this.pitchChange > 0) {
            this.getXRot() += this.pitchChange;
            this.pitchChange -= 2;
        }

        //Tick update
        this.ticksExisted++;
    }

    public void placeBlockIntoLevel() {
        if (!this.world.isClientSide()) {
            final int i = MathHelper.floor(posX);
            final int j = MathHelper.floor(posY);
            final int k = MathHelper.floor(posZ);

            final BlockPos pos = new BlockPos(i, j, k);

            final BlockState currentState = world.getBlockState(pos);

            if (currentState.getBlock().isReplaceable(this.world, pos)) {
                this.world.setBlockState(pos, getBlockState(), 3);
            }
            //TODO find first block if not replaceable
        }

        this.setDead();
    }

    /**
     * Checks to see if and entity is touching the missile. If so, blow up!
     */

    @Override
    public AxisAlignedBB getCollisionBox(Entity par1Entity) {
        // Make sure the entity is not an item
        if (par1Entity instanceof EntityLiving) {
            if (getBlockState() != null) {
                if (!(getBlockState().getBlock() instanceof IFluidBlock) && (this.motionX > 2 || this.motionY > 2 || this.motionZ > 2)) {
                    int damage = (int) (1.2 * (Math.abs(this.motionX) + Math.abs(this.motionY) + Math.abs(this.motionZ)));
                    ((EntityLiving) par1Entity).attackEntityFrom(DamageSource.FALLING_BLOCK, damage);
                }
            }
        }

        return null;
    }

    @Override
    protected void writeEntityToNBT(CompoundTag nbttagcompound) {
        if (blockState != null) {
            nbttagcompound.put(NBTConstants.BLOCK_STATE, NBTUtil.writeBlockState(new CompoundTag(), blockState));
        }
        nbttagcompound.setFloat(NBTConstants.GRAVITY, this.gravity);
    }

    @Override
    protected void readEntityFromNBT(CompoundTag nbttagcompound) {
        if (nbttagcompound.contains(NBTConstants.BLOCK_STATE)) {
            blockState = NBTUtil.readBlockState(nbttagcompound.getCompound(NBTConstants.BLOCK_STATE));
        }
        this.gravity = nbttagcompound.getFloat(NBTConstants.GRAVITY);
    }

    @Override
    public boolean canBePushed() {
        return true;
    }

    @Override
    protected boolean canTriggerWalking() {
        return true;
    }

    @Override
    public boolean canBeCollidedWith() {
        return true;
    }
}
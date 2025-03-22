package icbm.classic.content.entity.flyingblock;

import icbm.classic.ICBMClassic;
import icbm.classic.ICBMConstants;
import icbm.classic.config.ConfigFlyingBlocks;
import icbm.classic.content.entity.EntityPlayerSeat;
import icbm.classic.lib.projectile.EntityProjectile;
import icbm.classic.lib.saving.NbtSaveHandler;
import io.netty.buffer.ByteBuf;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.block.SoundType;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemDoor;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.*;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.registry.IEntityAdditionalSpawnData;

import javax.annotation.Nonnull;

/**
 * Similiar to {@link net.minecraft.entity.item.EntityFallingBlock} but specifically as a projectile to be spawned
 * by {@link icbm.classic.content.blast.redmatter.EntityRedmatter} and other sources.
 *
 * Normally created by {@link FlyingBlock} handling logic
 */
public class EntityFlyingBlock extends EntityProjectile<EntityFlyingBlock> implements IEntityAdditionalSpawnData
{
    /** Data about the block we are replicating */
    @Setter(value = AccessLevel.PACKAGE)
    private BlockCaptureData blockData;

    public float yawChange = 0;
    public float pitchChange = 0;

    @Getter
    @Setter
    private float gravity = DEFAULT_GRAVITY;

    public EntityFlyingBlock(World world)
    {
        super(world);
        this.ticksExisted = 0;
        this.preventEntitySpawning = true;
        this.isImmuneToFire = true;
        this.setSize(0.98F, 0.98F);
        this.inGroundKillTime = ICBMConstants.TICKS_SEC;
    }

    public void restoreGravity()
    {
        gravity = DEFAULT_GRAVITY;
    }

    public BlockCaptureData getBlockData()
    {
        if (this.blockData == null)
        {
            this.blockData = new BlockCaptureData();
        }
        return this.blockData;
    }

    @Nonnull
    @Override
    public String getName()
    {
        return "Flying Block [" + getBlockData().getBlockState() + ", " + hashCode() + "]";
    }

    @Override
    public void writeSpawnData(ByteBuf data)
    {
        ByteBufUtils.writeTag(data, this.getBlockData().serializeNBT());
        data.writeFloat(this.gravity);
        data.writeFloat(yawChange);
        data.writeFloat(pitchChange);
    }

    @Override
    public void readSpawnData(ByteBuf data)
    {
        this.getBlockData().deserializeNBT(ByteBufUtils.readTag(data));
        gravity = data.readFloat();
        yawChange = data.readFloat();
        pitchChange = data.readFloat();
    }

    @Override
    public void onUpdate()
    {
        super.onUpdate();

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
    }

    @Override
    protected boolean shouldExpire() {
        return super.shouldExpire() || this.posY > 400;
    }

    @Override
    protected void destroy() {
        this.placeBlockIntoWorld(this.getPos(), new RayTraceResult(this.getPositionVector(), EnumFacing.UP));
        this.setDead();
    }

    @Override
    protected boolean shouldCollideWith(Entity entity) {
        return super.shouldCollideWith(entity) && !(entity instanceof EntityFlyingBlock);
    }

    @Override
    protected void onImpact(@Nonnull RayTraceResult impactLocation) {
        //Grace period to get away from the ground
        if(ticksInAir < 5) return;

        if(impactLocation.entityHit == null) {
            this.placeBlockIntoWorld(new BlockPos(impactLocation.hitVec), impactLocation);
        }
        else {
            // TODO spawn fragments based on block in some cases (wood material -> wood fragments)
            // TODO if entity is dead, continue moving under previous speed minus some for impact
            dropSourceStack(getBlockData().getSourceStack());
        }
    }

    public void placeBlockIntoWorld(BlockPos pos, RayTraceResult hit)
    {
        this.setDead();

        if (!this.world.isRemote)
        {
            if (isServer() && !tryPlacement(pos, hit))
            {
                dropSourceStack(getBlockData().getSourceStack());
            }
        }
    }

    protected boolean tryPlacement(BlockPos pos, RayTraceResult hit) {

        // Attempt to place using item, as it will better handle unique rules
        final ItemStack sourceStack = this.blockData.getSourceStack();
        if(!sourceStack.isEmpty()) {

            // Use fake player to handle TE placement or other special placement rules
            final FakePlayer player = FakePlayerFactory.getMinecraft((WorldServer) world); //TODO attempt to get actual player who create the source of this entity
            player.setHeldItem(EnumHand.MAIN_HAND, sourceStack);

            //TODO pull entityYaw to get placement direction. This way furnace places facing the same way as it renders.
            final EnumActionResult result =  sourceStack.getItem()
                .onItemUse(player, world, pos, EnumHand.MAIN_HAND, hit.sideHit, (float)hit.hitVec.x, (float)hit.hitVec.y, (float)hit.hitVec.z);

            // cleanup fake
            player.setHeldItem(EnumHand.MAIN_HAND, ItemStack.EMPTY);

            if(result == EnumActionResult.SUCCESS) {
                return true;
            }
        }

        // Backup plan if we can't place via item, this will break in some cases TODO implement a config to disable blocks or change placement rules
        final IBlockState blockState = this.getBlockData().getBlockState();
        if (this.world.mayPlace(blockState.getBlock(), pos, true, EnumFacing.UP, this)) {

            if (!world.setBlockState(pos, blockState, 11)) return false;

            if (this.getBlockData().getTileEntityData() != null)
            {
                final TileEntity tileentity = this.world.getTileEntity(pos);

                if (tileentity != null)
                {
                    final NBTTagCompound currentSave = tileentity.writeToNBT(new NBTTagCompound());

                    // Ensure these are the same tile saves, otherwise we can corrupt a block badly
                    if(currentSave.getString("id").equals(this.getBlockData().getTileEntityData().getString("id"))) {
                        currentSave.merge(this.getBlockData().getTileEntityData());
                        currentSave.setInteger("x", pos.getX());
                        currentSave.setInteger("y", pos.getY());
                        currentSave.setInteger("z", pos.getZ());
                        tileentity.readFromNBT(currentSave);
                        tileentity.markDirty();
                    }
                }
            }

            // Play placement audio, RIP headphone users when redmatter spams :D
            SoundType soundtype = blockState.getBlock().getSoundType(blockState, world, pos, this);
            world.playSound(null, pos, soundtype.getPlaceSound(), SoundCategory.BLOCKS, (soundtype.getVolume() + 1.0F) / 2.0F, soundtype.getPitch() * 0.8F);

            return true;
        }
        return false;
    }

    protected void dropSourceStack(ItemStack itemStack) {
        // TODO implement this.shouldDropItem && this.world.getGameRules().getBoolean("doEntityDrops")
        if(itemStack != null && !itemStack.isEmpty()) {
            final EntityItem entityItem = new EntityItem(world, posX, posY, posZ);
            entityItem.setItem(itemStack);
            if(!world.spawnEntity(entityItem)) {
                ICBMClassic.logger().error("EntityFlyingBlock: Failed to drop source stack '{}' at dim[{}] pos[{}]", itemStack, this.world.provider.getDimension(), this.getPos());
            }
        }
    }

    @Override
    protected DamageSource getImpactDamageSource(Entity entityHit, float velocity, RayTraceResult hit) {
        return DamageSource.FALLING_BLOCK;
    }

    @Override
    protected float getImpactDamage(Entity entityHit, float velocity, RayTraceResult hit) {
        return MathHelper.ceil(velocity * ConfigFlyingBlocks.damageScale);
    }

    @Override
    public void writeEntityToNBT(NBTTagCompound save)
    {
        super.writeEntityToNBT(save);
        SAVE_LOGIC.save(this, save);
    }

    @Override
    public void readEntityFromNBT(NBTTagCompound save)
    {
       super.readEntityFromNBT(save);
       SAVE_LOGIC.load(this, save);
    }

    private static final NbtSaveHandler<EntityFlyingBlock> SAVE_LOGIC = new NbtSaveHandler<EntityFlyingBlock>()
        //Stuck in ground data
        .mainRoot()
        .nodeFloat("gravity", EntityFlyingBlock::getGravity, EntityFlyingBlock::setGravity)
        .nodeINBTSerializable("mimic_block", EntityFlyingBlock::getBlockData)
        .base();


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
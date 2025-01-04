package icbm.classic.content.entity.flyingblock;

import icbm.classic.ICBMClassic;
import icbm.classic.ICBMConstants;
import icbm.classic.config.ConfigFlyingBlocks;
import icbm.classic.lib.projectile.EntityProjectile;
import icbm.classic.lib.saving.NbtSaveHandler;
import io.netty.buffer.ByteBuf;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.block.BlockState;
import net.minecraft.block.SoundType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.item.FallingBlockEntity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.block.Blocks;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.IPacket;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.server.SSpawnObjectPacket;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.*;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.registry.IEntityAdditionalSpawnData;

import javax.annotation.Nonnull;

/**
 * Similiar to {@link FallingBlockEntity} but specifically as a projectile to be spawned
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

    public EntityFlyingBlock(EntityType<EntityFlyingBlock> type, World world)
    {
        super(type, world);
        this.ticksExisted = 0;
        this.preventEntitySpawning = true;
        this.inGroundKillTime = ICBMConstants.TICKS_SEC;
    }

    @Override
    public IPacket<?> createSpawnPacket() {
        return new SSpawnObjectPacket(this); //TODO figure out what this is
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
    public ITextComponent getName()
    {
       return new TranslationTextComponent(this.getType().getTranslationKey(), getBlockData().getBlockState(), hashCode());
    }

    @Override
    public void writeSpawnData(PacketBuffer data)
    {
        data.writeCompoundTag(this.getBlockData().serializeNBT());
        data.writeFloat(this.gravity);
        data.writeFloat(yawChange);
        data.writeFloat(pitchChange);
    }

    @Override
    public void readSpawnData(PacketBuffer data)
    {
        this.getBlockData().deserializeNBT(data.readCompoundTag() );
        gravity = data.readFloat();
        yawChange = data.readFloat();
        pitchChange = data.readFloat();
    }

    @Override
    public void tick()
    {
        super.tick();

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
        this.placeBlockIntoWorld(this.getPos(), new BlockRayTraceResult(this.getPositionVector(), Direction.UP, getPosition(), false));
        this.remove();
    }

    @Override
    protected boolean shouldCollideWith(Entity entity) {
        return super.shouldCollideWith(entity) && !(entity instanceof EntityFlyingBlock);
    }

    @Override
    protected void onImpact(RayTraceResult impactLocation) {
        //Grace period to get away from the ground
        if(ticksInAir < 5) return;

        if(impactLocation.getType() == RayTraceResult.Type.BLOCK) {
            this.placeBlockIntoWorld(new BlockPos(impactLocation.getHitVec()), impactLocation);
        }
        else {
            // TODO spawn fragments based on block in some cases (wood material -> wood fragments)
            // TODO if entity is dead, continue moving under previous speed minus some for impact
            dropSourceStack(getBlockData().getSourceStack());
        }
    }

    public void placeBlockIntoWorld(BlockPos pos, RayTraceResult hit)
    {
        this.remove();

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
            final FakePlayer player = FakePlayerFactory.getMinecraft((ServerWorld) world); //TODO attempt to get actual player who create the source of this entity
            player.setHeldItem(Hand.MAIN_HAND, sourceStack);

            //TODO pull entityYaw to get placement direction. This way furnace places facing the same way as it renders.
            final ActionResultType result =  sourceStack.getItem()
                .onItemUse(player, world, pos, Hand.MAIN_HAND, hit.sideHit, (float)hit.hitVec.x, (float)hit.hitVec.y, (float)hit.hitVec.z);

            // cleanup fake
            player.setHeldItem(Hand.MAIN_HAND, ItemStack.EMPTY);

            if(result == ActionResultType.SUCCESS) {
                return true;
            }
        }

        // Backup plan if we can't place via item, this will break in some cases TODO implement a config to disable blocks or change placement rules
        final BlockState blockState = this.getBlockData().getBlockState();
        if (this.world.mayPlace(blockState.getBlock(), pos, true, Direction.UP, this)) {

            if (!world.setBlockState(pos, blockState, 11)) return false;

            if (this.getBlockData().getTileEntityData() != null)
            {
                final TileEntity tileentity = this.world.getTileEntity(pos);

                if (tileentity != null)
                {
                    final CompoundNBT currentSave = tileentity.write(new CompoundNBT());

                    // Ensure these are the same tile saves, otherwise we can corrupt a block badly
                    if(currentSave.getString("id").equals(this.getBlockData().getTileEntityData().getString("id"))) {
                        currentSave.merge(this.getBlockData().getTileEntityData());
                        currentSave.putInt("x", pos.getX());
                        currentSave.putInt("y", pos.getY());
                        currentSave.putInt("z", pos.getZ());
                        tileentity.read(currentSave);
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
            final ItemEntity entityItem = new ItemEntity(world, posX, posY, posZ);
            entityItem.setItem(itemStack);
            if(!world.addEntity(entityItem)) {
                ICBMClassic.logger().error("EntityFlyingBlock: Failed to drop source stack '{}' at dim[{}] pos[{}]", itemStack, this.world.getDimension().getType(), this.getPos());
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
    public void writeAdditional(CompoundNBT save)
    {
        super.writeAdditional(save);
        SAVE_LOGIC.save(this, save);
    }

    @Override
    public void readAdditional(CompoundNBT save)
    {
       super.readAdditional(save);
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
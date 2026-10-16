package icbm.classic.content.entity.flyingblock;

import icbm.classic.lib.saving.NbtSaveHandler;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.util.INBTSerializable;

@Data
@NoArgsConstructor
public class BlockCaptureData implements INBTSerializable<CompoundTag> {
    //TODO maybe replace with BlockSnapshot?

    /** Pulled from {@link Level#getBlockState(BlockPos)} */
    private BlockState blockState;
    /** Pulled from {@link net.minecraft.world.level.block.Block#getCloneItemStack} */
    private ItemStack sourceStack;
    /** Saved tile data */
    private CompoundTag tileEntityData;

    public BlockCaptureData(BlockState state, ItemStack stack) {
        this.blockState = state;
        this.sourceStack = stack;
    }

    public BlockCaptureData(Level world, BlockPos blockPos) {
        this.blockState = world.getBlockState(blockPos);
        this.sourceStack = blockState.getBlock().getCloneItemStack(world, blockPos, blockState);

        final BlockEntity tileEntity = world.getBlockEntity(blockPos); //TODO add config to disable this for specific blocks
        if(tileEntity != null) {
            this.tileEntityData = tileEntity.saveWithFullMetadata(world.registryAccess());
        }
    }

    public BlockState getBlockState() {
        if(blockState == null) {
            this.blockState = Blocks.STONE.defaultBlockState();
        }
        return blockState;
    }

    public ItemStack getSourceStack() {
        if(this.sourceStack == null) {
            this.sourceStack = new ItemStack(Blocks.STONE);
        }
        return sourceStack;
    }

    @Override
    public CompoundTag serializeNBT(HolderLookup.Provider provider) {
        return SAVE_LOGIC.save(this);
    }

    @Override
    public void deserializeNBT(HolderLookup.Provider provider, CompoundTag nbt) {
        SAVE_LOGIC.load(this, nbt);
    }

    private static final NbtSaveHandler<BlockCaptureData> SAVE_LOGIC = new NbtSaveHandler<BlockCaptureData>()
        //Stuck in ground data
        .mainRoot()
        .nodeBlockState("block_state", BlockCaptureData::getBlockState, BlockCaptureData::setBlockState)
        .nodeItemStack("source_stack", BlockCaptureData::getSourceStack, BlockCaptureData::setSourceStack)
        .nodeCompoundTag("block_entity", BlockCaptureData::getTileEntityData, BlockCaptureData::setTileEntityData)
        .base();
}
// PORTED

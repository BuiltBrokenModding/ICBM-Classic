package icbm.classic.config.util;

import lombok.Data;
import lombok.experimental.Accessors;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

/**
 * Handles block placement with a chance
 */
@Data
@Accessors(chain = true)
public class BlockReplacementData {

    /** Block state to place */
    private BlockState blockState;

    /** NBT data to load into the block */
    private CompoundTag blockNBT = null;

    /** Chance of placement, 1 is always and 0 is never */
    private float chance = -1;

    /**
     * Alias of {@link #setBlockState(BlockState)} used by config parsers and handlers
     */
    public BlockReplacementData setBlock(BlockState state) {
        this.blockState = state;
        return this;
    }

    public void apply(Level world, BlockPos pos) {
        if (shouldPlace(world)) {
            placeBlock(world, pos);
            applyNBT(world, pos);
        }
    }

    private boolean shouldPlace(Level world) {
        return getChance() < 0 || world.random.nextFloat() <= getChance();
    }

    private void placeBlock(Level world, BlockPos pos) {
        if (getBlockState() != null) {
            world.setBlock(pos, getBlockState(), 3);
        }
    }

    private void applyNBT(Level world, BlockPos pos) {
        if(blockNBT != null && !blockNBT.isEmpty()) {
            final BlockEntity tileEntity = world.getBlockEntity(pos);
            if(tileEntity != null) {
                final CompoundTag existingSave = tileEntity.saveWithoutMetadata(world.registryAccess());

                // Remove internal tags to prevent issues
                blockNBT.remove("id");
                blockNBT.remove("x");
                blockNBT.remove("y");
                blockNBT.remove("z");

                existingSave.merge(blockNBT);
                tileEntity.loadWithComponents(existingSave, world.registryAccess());
                tileEntity.setChanged();
            }
        }
    }
}
// PORTED

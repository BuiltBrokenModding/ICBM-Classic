package icbm.classic.config.util;

import lombok.Data;
import lombok.experimental.Accessors;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * Handles block placement with a chance
 */
@Data
@Accessors(chain = true)
public class BlockReplacementData {

    /** Block state to place */
    private BlockState blockState;

    /** NBT data to load into the block */
    private CompoundNBT blockNBT = null;

    /** Chance of placement, 1 is always and 0 is never */
    private float chance = -1;

    public void apply(World world, BlockPos pos) {
        if (shouldPlace(world)) {
            placeBlock(world, pos);
            applyNBT(world, pos);
        }
    }

    private boolean shouldPlace(World world) {
        return getChance() < 0 || world.rand.nextFloat() <= getChance();
    }

    private void placeBlock(World world, BlockPos pos) {
        if (getBlockState() != null) {
            if(getBlockState().getBlock() == Blocks.AIR) {
                world.removeBlock(pos, false);
            }
            else {
                world.setBlockState(pos, getBlockState(), 3);
            }
        }
    }

    private void applyNBT(World world, BlockPos pos) {
        if(blockNBT != null && !blockNBT.isEmpty()) {
            final TileEntity tileEntity = world.getTileEntity(pos);
            if(tileEntity != null) {
                final CompoundNBT existingSave = tileEntity.write(new CompoundNBT());

                // Remove internal tags to prevent issues
                blockNBT.remove("id");
                blockNBT.remove("x");
                blockNBT.remove("y");
                blockNBT.remove("z");

                existingSave.merge(blockNBT);
                tileEntity.read(existingSave);
            }
        }
    }
}

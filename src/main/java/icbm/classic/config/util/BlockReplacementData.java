package icbm.classic.config.util;

import lombok.Data;
import lombok.experimental.Accessors;
import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
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
    private IBlockState blockState;

    /** NBT data to load into the block */
    private NBTTagCompound blockNBT = null;

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
            world.setBlockState(pos, getBlockState(), 3);
        }
    }

    private void applyNBT(World world, BlockPos pos) {
        if(blockNBT != null && !blockNBT.hasNoTags()) {
            final TileEntity tileEntity = world.getTileEntity(pos);
            if(tileEntity != null) {
                final NBTTagCompound existingSave = tileEntity.writeToNBT(new NBTTagCompound());

                // Remove internal tags to prevent issues
                blockNBT.removeTag("id");
                blockNBT.removeTag("x");
                blockNBT.removeTag("y");
                blockNBT.removeTag("z");

                existingSave.merge(blockNBT);
                tileEntity.readFromNBT(existingSave);
            }
        }
    }
}

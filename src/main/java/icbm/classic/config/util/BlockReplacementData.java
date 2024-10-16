package icbm.classic.config.util;

import lombok.Data;
import lombok.experimental.Accessors;
import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;

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

    /** Should the target block's nbt be cloned before reading */
    private boolean copyTargetNBT = false;

    /** Should the source block's nbt be cloned before reading */
    private boolean copySourceNBT = false;

    /** Chance of placement, 1 is always and 0 is never */
    private float chance = -1;
}

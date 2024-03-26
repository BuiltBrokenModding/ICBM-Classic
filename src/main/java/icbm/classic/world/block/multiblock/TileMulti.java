package icbm.classic.world.block.multiblock;

import icbm.classic.lib.NBTConstants;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;

/**
 * Basic implementation of a multi block
 * Created by Dark on 8/9/2015.
 */
@Deprecated
public class TileMulti extends BlockEntity {
    private BlockPos hostPosition = null;

    @Override
    public void onLoad() {
        // TODO place blocks
    }

    @Override
    public void readFromNBT(CompoundTag compound) {
        super.readFromNBT(compound);
        if (compound.contains(NBTConstants.HOST_POS)) {
            int[] data = compound.getIntArray(NBTConstants.HOST_POS);
            hostPosition = new BlockPos(data[0], data[1], data[2]);
        }
    }

    @Override
    public CompoundTag writeToNBT(CompoundTag compound) {
        if (hostPosition != null) {
            compound.setIntArray(NBTConstants.HOST_POS, new int[]{hostPosition.getX(), hostPosition.getY(), hostPosition.getZ()});
        }
        return super.writeToNBT(compound);
    }
}

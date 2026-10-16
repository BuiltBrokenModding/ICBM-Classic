package icbm.classic.content.blocks.multiblock;

import icbm.classic.lib.NBTConstants;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Basic implementation of a multi block
 * Created by Dark on 8/9/2015.
 * <p>
 * PORT: unused in 1.12 (never registered), kept for reference. Takes the type as subclasses would register their own.
 */
@Deprecated
public class TileMulti extends BlockEntity
{
    private BlockPos hostPosition = null;

    public TileMulti(BlockEntityType<?> type, BlockPos pos, BlockState state)
    {
        super(type, pos, state);
    }

    @Override
    public void onLoad()
    {
        // TODO place blocks
    }

    @Override
    protected void loadAdditional(CompoundTag compound, HolderLookup.Provider registries)
    {
        super.loadAdditional(compound, registries);
        if (compound.contains(NBTConstants.HOST_POS))
        {
            int[] data = compound.getIntArray(NBTConstants.HOST_POS);
            hostPosition = new BlockPos(data[0], data[1], data[2]);
        }
    }

    @Override
    protected void saveAdditional(CompoundTag compound, HolderLookup.Provider registries)
    {
        if (hostPosition != null)
        {
            compound.putIntArray(NBTConstants.HOST_POS, new int[]{hostPosition.getX(), hostPosition.getY(), hostPosition.getZ()});
        }
        super.saveAdditional(compound, registries);
    }
}
// PORTED

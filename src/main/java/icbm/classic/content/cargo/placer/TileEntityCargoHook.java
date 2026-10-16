package icbm.classic.content.cargo.placer;

import icbm.classic.prefab.tile.TileMachine;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

/** PORT: unfinished in 1.12, no BlockEntityType is registered */
public class TileEntityCargoHook extends TileMachine {
    public TileEntityCargoHook(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }
}
// PORTED

package icbm.classic.content.blocks.emptower;

import icbm.classic.content.reg.BlockReg;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/** Extender section of the EMP tower, links to the tower below for rendering and range */
public class TileEmpTowerFake extends BlockEntity {

    private TileEMPTower host;

    public TileEmpTowerFake(BlockPos pos, BlockState state) {
        super(BlockReg.TILE_EMP_TOWER_FAKE.get(), pos, state);
    }

    public void setHost(TileEMPTower tower) {
        this.host = tower;
    }

    public TileEMPTower getHost() {
        return host;
    }
}
// PORTED

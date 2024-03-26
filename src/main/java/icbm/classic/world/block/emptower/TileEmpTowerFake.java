package icbm.classic.world.block.emptower;

import net.minecraft.world.level.block.entity.BlockEntity;

public class TileEmpTowerFake extends BlockEntity {

    private EmpTowerBlockEntity host;

    public void setHost(EmpTowerBlockEntity tower) {
        this.host = tower;
    }

    public EmpTowerBlockEntity getHost() {
        return host;
    }
}

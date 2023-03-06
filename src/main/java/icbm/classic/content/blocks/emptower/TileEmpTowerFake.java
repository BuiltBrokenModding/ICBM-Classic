package icbm.classic.content.blocks.emptower;

import net.minecraft.tileentity.TileEntity;

public class TileEmpTowerFake extends TileEntity {

    private TileEMPTower host;

    public void setHost(TileEMPTower tower) {
        this.host = tower;
    }

    public TileEMPTower getHost() {
        return host;
    }
}

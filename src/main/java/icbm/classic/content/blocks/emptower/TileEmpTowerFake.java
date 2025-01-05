package icbm.classic.content.blocks.emptower;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;

public class TileEmpTowerFake extends TileEntity {

    private TileEMPTower host;

    public TileEmpTowerFake(TileEntityType<?> tileEntityTypeIn) {
        super(tileEntityTypeIn);
    }

    public void setHost(TileEMPTower tower) {
        this.host = tower;
    }

    public TileEMPTower getHost() {
        return host;
    }
}

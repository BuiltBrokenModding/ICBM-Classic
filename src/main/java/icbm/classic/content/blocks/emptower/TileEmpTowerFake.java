package icbm.classic.content.blocks.emptower;

import icbm.classic.ICBMConstants;
import icbm.classic.content.reg.TileReg;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;

public class TileEmpTowerFake extends TileEntity {

    public static final ResourceLocation REGISTRY_NAME = new ResourceLocation(ICBMConstants.DOMAIN, "emp_tower_coil");

    private TileEMPTower host;

    public TileEmpTowerFake() {
        super(TileReg.EMP_TOWER_COIL.get());
    }

    public void setHost(TileEMPTower tower) {
        this.host = tower;
    }

    public TileEMPTower getHost() {
        return host;
    }
}

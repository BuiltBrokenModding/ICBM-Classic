package icbm.classic.content.blocks.launcher.connector;

import icbm.classic.content.blocks.launcher.frame.TileLauncherFrame;
import icbm.classic.content.reg.TileReg;
import net.minecraft.tileentity.TileEntityType;

public class TileLauncherConnector extends TileLauncherFrame {
    public TileLauncherConnector() {
        super(TileReg.LAUNCHER_CONNECTOR.get());
    }
}

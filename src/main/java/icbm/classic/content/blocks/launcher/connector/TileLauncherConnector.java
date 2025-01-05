package icbm.classic.content.blocks.launcher.connector;

import icbm.classic.ICBMConstants;
import icbm.classic.content.blocks.launcher.frame.TileLauncherFrame;
import icbm.classic.content.reg.TileReg;
import net.minecraft.util.ResourceLocation;

public class TileLauncherConnector extends TileLauncherFrame {
    public static final ResourceLocation REGISTRY_NAME = new ResourceLocation(ICBMConstants.DOMAIN, "launcher_connector");
    public TileLauncherConnector() {
        super(TileReg.LAUNCHER_CONNECTOR.get());
    }
}

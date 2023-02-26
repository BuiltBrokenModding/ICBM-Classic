package icbm.classic.content.blocks.launcher.frame;

import icbm.classic.content.blocks.launcher.ILauncherComponent;
import icbm.classic.content.blocks.launcher.LauncherReference;
import net.minecraft.tileentity.TileEntity;

public class TileLauncherFrame extends TileEntity implements ILauncherComponent {

    private final LauncherReference launcherReference = new LauncherReference(this);
    @Override
    public LauncherReference getReference() {
        return launcherReference;
    }
}

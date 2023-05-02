package icbm.classic.content.blocks.launcher.network;

import icbm.classic.api.launcher.IMissileLauncher;
import lombok.Data;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;

@Data
public class LauncherEntry {
    private final IMissileLauncher launcher;
    private final TileEntity host;
    private final EnumFacing side;
}

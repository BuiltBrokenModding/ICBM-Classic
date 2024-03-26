package icbm.classic.world.block.launcher.network;

import icbm.classic.api.launcher.IActionStatus;
import icbm.classic.api.launcher.IMissileLauncher;
import icbm.classic.world.block.launcher.FiringPackage;
import lombok.Data;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;

@Data
public class LauncherEntry {
    private final IMissileLauncher launcher;
    private final BlockEntity host;
    private final Direction side;

    // Internal data, don't use in mods as this will be moved to launcher itself and exposed as an event list
    private IActionStatus lastFiringStatus;
    private FiringPackage lastFiringPackage;
    private long lastFiringTime;
}

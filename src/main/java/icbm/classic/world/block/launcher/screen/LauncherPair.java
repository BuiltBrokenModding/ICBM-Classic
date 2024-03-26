package icbm.classic.world.block.launcher.screen;

import icbm.classic.api.launcher.IActionStatus;
import lombok.Data;

@Data
public class LauncherPair {
    private final Integer group;
    private final Integer index;
    private final IActionStatus status;
}

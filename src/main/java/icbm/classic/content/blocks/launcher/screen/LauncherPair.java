package icbm.classic.content.blocks.launcher.screen;

import icbm.classic.api.launcher.IActionStatus;
import lombok.Data;

@Data
public class LauncherPair {
    private final Integer group;
    private final Integer index;
    private final IActionStatus status;
}

package icbm.classic.lib.actions;

import icbm.classic.api.actions.IAction;
import icbm.classic.api.actions.IActionData;
import icbm.classic.api.actions.cause.IActionSource;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.Level;

@Getter @Setter @RequiredArgsConstructor
public abstract class ActionBase implements IAction {

    private final Level world;
    private final Vec3 position;
    private final IActionSource source;
    private final IActionData actionData;

    private BlockPos pos;

    public BlockPos getBlockPos()
    {
        if(pos == null) {
            pos = BlockPos.containing(position);
        }
        return pos;
    }
}
// PORTED

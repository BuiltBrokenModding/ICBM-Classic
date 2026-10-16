package icbm.classic.api.data;

import net.minecraft.world.entity.player.Player;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

/**
 * Created by Dark(DarkGuardsman, Robin) on 1/7/19.
 */
@FunctionalInterface
public interface BlockActivateFunction
{
    boolean onActivate(Level world, BlockPos pos, Player entityPlayer, InteractionHand hand, Direction facing, float hitX, float hitY, float hitZ);
}
// PORTED

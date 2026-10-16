package icbm.classic.api.data;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.InteractionHand;

/**
 * Created by Dark(DarkGuardsman, Robin) on 1/7/19.
 */
@FunctionalInterface
public interface EntityInteractionFunction
{
    boolean onInteraction(Entity entity, Player player, InteractionHand hand);
}
// PORTED

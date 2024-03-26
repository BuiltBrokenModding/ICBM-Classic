package icbm.classic.api.data;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

/**
 * Created by Dark(DarkGuardsman, Robert) on 1/7/19.
 */
@FunctionalInterface
public interface EntityInteractionFunction {
    boolean onInteraction(Entity entity, Player player, InteractionHand hand);
}

package icbm.classic.api.data;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumHand;

/**
 * Created by Dark(DarkGuardsman, Robert) on 1/7/19.
 */
@FunctionalInterface
public interface EntityInteractionFunction
{
    boolean onInteraction(Entity entity, EntityPlayer player, EnumHand hand);
}

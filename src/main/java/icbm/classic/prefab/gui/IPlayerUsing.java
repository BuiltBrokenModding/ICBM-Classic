package icbm.classic.prefab.gui;

import net.minecraft.world.entity.player.Player;

import java.util.Collection;

/**
 * Used to track players currently using an object. Primaryly used
 * for GUI handling.
 * Created by robert on 1/12/2015.
 */
public interface IPlayerUsing {
    Collection<Player> getPlayersUsing();

    default boolean addPlayerToUseList(Player player) {
        return getPlayersUsing().add(player);
    }

    default boolean removePlayerToUseList(Player player) {
        return getPlayersUsing().remove(player);
    }
}

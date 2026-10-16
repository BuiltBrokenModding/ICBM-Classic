package icbm.classic.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.world.entity.player.Player;

import javax.annotation.Nullable;

/**
 * Small client only accessors for common code. Only call behind a client side check
 * ({@code FMLEnvironment.dist.isClient()} or {@code level.isClientSide()}).
 */
public final class ClientHelpers
{
    private ClientHelpers()
    {
    }

    /** @return client player, null if not in a world */
    @Nullable
    public static Player getPlayer()
    {
        return Minecraft.getInstance().player;
    }

    /** @return true if either shift key is held */
    public static boolean isShiftHeld()
    {
        return Screen.hasShiftDown();
    }
}
// PORTED

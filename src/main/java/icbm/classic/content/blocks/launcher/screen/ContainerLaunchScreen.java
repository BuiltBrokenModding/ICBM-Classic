package icbm.classic.content.blocks.launcher.screen;

import icbm.classic.prefab.gui.ContainerBase;
import net.minecraft.entity.player.EntityPlayer;

/**
 *
 * Created by Dark(DarkGuardsman, Robert) on 1/27/2018.
 */
public class ContainerLaunchScreen extends ContainerBase<TileLauncherScreen>
{
    public ContainerLaunchScreen(EntityPlayer player, TileLauncherScreen node)
    {
        super(player, node);
    }
}

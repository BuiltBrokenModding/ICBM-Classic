package icbm.classic.content.machines.launcher.screen;

import icbm.classic.prefab.gui.ContainerBase;
import net.minecraft.entity.player.EntityPlayer;

/**
 * @see <a href="https://github.com/BuiltBrokenModding/VoltzEngine/blob/development/license.md">License</a> for what you can and can't do with the code.
 * Created by Dark(DarkGuardsman, Robert) on 1/27/2018.
 */
public class ContainerLaunchScreen extends ContainerBase<TileLauncherScreen>
{
    public ContainerLaunchScreen(EntityPlayer player, TileLauncherScreen node)
    {
        super(player, node);
    }
}

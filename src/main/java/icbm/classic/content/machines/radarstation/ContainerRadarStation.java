package icbm.classic.content.machines.radarstation;

import com.builtbroken.mc.prefab.gui.ContainerBase;
import net.minecraft.entity.player.EntityPlayer;

/**
 * @see <a href="https://github.com/BuiltBrokenModding/VoltzEngine/blob/development/license.md">License</a> for what you can and can't do with the code.
 * Created by Dark(DarkGuardsman, Robert) on 1/26/2018.
 */
public class ContainerRadarStation extends ContainerBase<TileRadarStation>
{
    public ContainerRadarStation(EntityPlayer player, TileRadarStation node)
    {
        super(player, node);
    }
}

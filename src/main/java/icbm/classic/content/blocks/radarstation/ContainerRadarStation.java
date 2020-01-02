package icbm.classic.content.blocks.radarstation;

import icbm.classic.prefab.gui.ContainerBase;
import net.minecraft.entity.player.EntityPlayer;

/**
 *
 * Created by Dark(DarkGuardsman, Robert) on 1/26/2018.
 */
public class ContainerRadarStation extends ContainerBase<TileRadarStation>
{
    public ContainerRadarStation(EntityPlayer player, TileRadarStation node)
    {
        super(player, node);
    }
}

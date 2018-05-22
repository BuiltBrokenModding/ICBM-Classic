package icbm.classic.mods.ic2;

import ic2.api.energy.event.EnergyTileLoadEvent;
import ic2.api.energy.event.EnergyTileUnloadEvent;
import ic2.api.energy.tile.IEnergyTile;
import icbm.classic.config.ConfigIC2;
import icbm.classic.mods.ModProxy;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Optional;

/**
 * @see <a href="https://github.com/BuiltBrokenModding/VoltzEngine/blob/development/license.md">License</a> for what you can and can't do with the code.
 * Created by Dark(DarkGuardsman, Robert) on 5/22/2018.
 */
public class IC2Proxy extends ModProxy
{
    public static final IC2Proxy INSTANCE = new IC2Proxy();

    @Override
    @Optional.Method(modid = "ic2")
    public void init()
    {
        if (!ConfigIC2.DISABLED)
        {

        }
    }

    @Override
    @Optional.Method(modid = "ic2")
    public void onTileValidate(TileEntity tile)
    {
        if (!ConfigIC2.DISABLED && tile instanceof IEnergyTile && !tile.getWorld().isRemote)
        {
            MinecraftForge.EVENT_BUS.post(new EnergyTileLoadEvent((IEnergyTile) tile));
        }
    }

    @Override
    @Optional.Method(modid = "ic2")
    public void onTileInvalidate(TileEntity tile)
    {
        if (!ConfigIC2.DISABLED && tile instanceof IEnergyTile && !tile.getWorld().isRemote)
        {
            MinecraftForge.EVENT_BUS.post(new EnergyTileUnloadEvent((IEnergyTile) tile));
        }
    }
}

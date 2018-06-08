package icbm.classic.mod.IC2Proxy;

import cpw.mods.fml.common.Optional;
import ic2.api.energy.event.EnergyTileLoadEvent;
import ic2.api.energy.event.EnergyTileUnloadEvent;
import ic2.api.energy.tile.IEnergyTile;
import icbm.classic.mod.ModProxy;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.MinecraftForge;

/**
 * @see <a href="https://github.com/BuiltBrokenModding/VoltzEngine/blob/development/license.md">License</a> for what you can and can't do with the code.
 * Created by Dark(DarkGuardsman, Robert) on 5/22/2018.
 */
public class IC2Proxy extends ModProxy
{
    public static final IC2Proxy INSTANCE = new IC2Proxy();

    @Optional.Method(modid = "IC2")
    public void onTileValidate(TileEntity tile)
    {
        if (tile instanceof IEnergyTile && !tile.getWorldObj().isRemote)
        {
            MinecraftForge.EVENT_BUS.post(new EnergyTileLoadEvent((IEnergyTile) tile));
        }
    }

    @Optional.Method(modid = "IC2")
    public void onTileInvalidate(TileEntity tile)
    {
        if (tile instanceof IEnergyTile && !tile.getWorldObj().isRemote)
        {
            MinecraftForge.EVENT_BUS.post(new EnergyTileUnloadEvent((IEnergyTile) tile));
        }
    }
}

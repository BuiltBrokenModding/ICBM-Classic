package icbm.classic.mod.IC2Proxy;

import ic2.api.energy.event.EnergyTileLoadEvent;
import ic2.api.energy.event.EnergyTileUnloadEvent;
import ic2.api.energy.tile.IEnergyTile;
import icbm.classic.ICBMClassic;
import icbm.classic.mod.ModProxy;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.MinecraftForge;

/**
 * @see <a href="https://github.com/BuiltBrokenModding/VoltzEngine/blob/development/license.md">License</a> for what you can and can't do with the code.
 * Created by Dark(DarkGuardsman, Robert) on 5/22/2018.
 */
public class IC2Proxy extends ModProxy
{
    @Override
    public void preInit()
    {
        super.preInit();
        ICBMClassic.IC2PROXY = this;
    }

    @Override
    public void onTileValidate(TileEntity tile)
    {
        if (tile instanceof IEnergyTile && !tile.getWorldObj().isRemote)
        {
            MinecraftForge.EVENT_BUS.post(new EnergyTileLoadEvent((IEnergyTile) tile));
        }
    }

    @Override
    public void onTileInvalidate(TileEntity tile)
    {
        if (tile instanceof IEnergyTile && !tile.getWorldObj().isRemote)
        {
            MinecraftForge.EVENT_BUS.post(new EnergyTileUnloadEvent((IEnergyTile) tile));
        }
    }
}

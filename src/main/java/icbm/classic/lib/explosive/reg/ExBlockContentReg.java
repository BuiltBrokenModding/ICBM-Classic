package icbm.classic.lib.explosive.reg;

import icbm.classic.api.ICBMClassicAPI;
import icbm.classic.api.data.BlockActivateFunction;
import icbm.classic.api.data.WorldPosIntSupplier;
import icbm.classic.api.data.WorldTickFunction;
import icbm.classic.api.reg.content.IExBlockRegistry;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

/**
 *
 * Created by Dark(DarkGuardsman, Robert) on 1/7/19.
 */
public class ExBlockContentReg extends ExplosiveContentRegistry implements IExBlockRegistry
{
    public ExBlockContentReg()
    {
        super(ICBMClassicAPI.EX_BLOCK);
    }

    @Override
    public void setFuseSupplier(ResourceLocation exName, WorldPosIntSupplier fuseTimer)
    {

    }

    @Override
    public void setFuseTickListener(ResourceLocation exName, WorldTickFunction function)
    {

    }

    @Override
    public void setActivationListener(ResourceLocation exName, BlockActivateFunction function)
    {

    }

    @Override
    public void tickFuse(World world, double posX, double posY, double posZ, int ticksExisted, int explosiveID)
    {

    }

    @Override
    public int getFuseTime(World world, double posX, double posY, double posZ, int explosiveID)
    {
        return 0;
    }
}

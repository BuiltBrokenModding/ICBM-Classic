package icbm.classic.api;

import icbm.classic.api.caps.IEMPReceiver;
import icbm.classic.api.reg.IExplosiveRegistry;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;

/**
 * API reference class for ICBM-Classic mod
 *
 * @see <a href="https://github.com/BuiltBrokenModding/VoltzEngine/blob/development/license.md">License</a> for what you can and can't do with the code.
 * Created by Dark(DarkGuardsman, Robert) on 3/12/2018.
 */
public final class ICBMClassicAPI
{
    public static IExplosiveRegistry EXPLOSIVE_REGISTRY;
    /**
     * Called to register an EMP handler for the {@link Block}
     * and related {@link net.minecraft.block.state.IBlockState}
     *
     * Allows several receiver to be registered per block.
     *
     * @param block    - block
     * @param receiver - receiver
     */
    public void registerBlockEmpHandler(Block block, IEMPReceiver receiver)
    {
        //TODO implement
    }

    public static boolean hasEmpHandler(IBlockState iBlockState)
    {
        return false; //TODO implement
    }
}

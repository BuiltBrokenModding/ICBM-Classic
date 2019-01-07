package icbm.classic.api;

import icbm.classic.api.caps.IEMPReceiver;
import icbm.classic.api.caps.IExplosive;
import icbm.classic.api.caps.IMissile;
import icbm.classic.api.reg.content.*;
import icbm.classic.api.reg.IExplosiveRegistry;
import net.minecraft.block.Block;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;

/**
 * API reference class for ICBM-Classic mod
 * <p>
 * <p>
 * Created by Dark(DarkGuardsman, Robert) on 3/12/2018.
 */
public final class ICBMClassicAPI
{

    //=========================
    //=== Registry ============
    //=========================
    /**
     * Main handler for explosives, do not override as this will break the mod
     */
    public static IExplosiveRegistry EXPLOSIVE_REGISTRY;

    public static IExMissileRegistry EX_MISSILE_REGISTRY;
    public static IExGrenadeRegistry EX_GRENADE_REGISTRY;
    public static IExBlockRegistry EX_BLOCK_REGISTRY;
    public static IExMinecartRegistry EX_MINECRT_REGISTRY;


    //=========================
    //=== References ==========
    //=========================
    public static final String ID = "icbmclassic";


    //=========================
    //=== Content keys ========
    //=========================
    public static final ResourceLocation EX_MISSILE = new ResourceLocation(ID, "ex_missile");
    public static final ResourceLocation EX_GRENADE = new ResourceLocation(ID, "ex_grenade");
    public static final ResourceLocation EX_BLOCK = new ResourceLocation(ID, "ex_block");
    public static final ResourceLocation EX_MINECART = new ResourceLocation(ID, "ex_minecart");

    //=========================
    //=== Capabilities ========
    //=========================

    @CapabilityInject(IEMPReceiver.class)
    public static Capability<IEMPReceiver> EMP_CAPABILITY = null;

    @CapabilityInject(IExplosive.class)
    public static Capability<IExplosive> EXPLOSIVE_CAPABILITY = null;

    @CapabilityInject(IMissile.class)
    public static Capability<IMissile> MISSILE_CAPABILITY = null;

    /**
     * Called to register an EMP handler for the {@link Block}
     * and related {@link net.minecraft.block.state.IBlockState}
     * <p>
     * Allows several receiver to be registered per block.
     *
     * @param block    - block
     * @param receiver - receiver
     */
    @Deprecated //Will be placed in a registry/handler
    public void registerBlockEmpHandler(Block block, IEMPReceiver receiver)
    {
        //TODO implement
    }

}

package icbm.classic.api;

import icbm.classic.api.caps.IEMPReceiver;
import icbm.classic.api.caps.IExplosive;
import icbm.classic.api.caps.IGPSData;
import icbm.classic.api.caps.IMissileHolder;
import icbm.classic.api.explosion.IBlast;
import icbm.classic.api.explosion.redmatter.IBlastVelocity;
import icbm.classic.api.launcher.IActionStatus;
import icbm.classic.api.launcher.IMissileLauncher;
import icbm.classic.api.missiles.ICapabilityMissileStack;
import icbm.classic.api.missiles.IMissile;
import icbm.classic.api.missiles.cause.IMissileCause;
import icbm.classic.api.missiles.parts.IMissileFlightLogic;
import icbm.classic.api.missiles.parts.IMissileTarget;
import icbm.classic.api.radio.IRadio;
import icbm.classic.api.reg.IExplosiveRegistry;
import icbm.classic.api.reg.content.IExBlockRegistry;
import icbm.classic.api.reg.content.IExGrenadeRegistry;
import icbm.classic.api.reg.content.IExMinecartRegistry;
import icbm.classic.api.reg.content.IExMissileRegistry;
import icbm.classic.api.reg.obj.IBuilderRegistry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.neoforged.common.capabilities.Capability;
import net.neoforged.common.capabilities.CapabilityInject;
import net.neoforged.neoforge.capabilities.BaseCapability;
import net.neoforged.neoforge.capabilities.EntityCapability;
import net.neoforged.neoforge.capabilities.ItemCapability;

/**
 * API reference class for ICBM-Classic mod
 * <p>
 * <p>
 * Created by Dark(DarkGuardsman, Robert) on 3/12/2018.
 */
public final class ICBMClassicAPI {

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
    public static IExMinecartRegistry EX_MINECART_REGISTRY;

    /**
     * Registry for target data save/load in missiles
     */
    public static IBuilderRegistry<IMissileTarget> MISSILE_TARGET_DATA_REGISTRY;
    /**
     * Registry for flight logic save/load in missiles
     */
    public static IBuilderRegistry<IMissileFlightLogic> MISSILE_FLIGHT_LOGIC_REGISTRY;
    /**
     * Registry for missile cause save/load in missiles
     */
    public static IBuilderRegistry<IMissileCause> MISSILE_CAUSE_REGISTRY;
    /**
     * Registry for status messages produced by machines, items, and entities in the mod
     */
    public static IBuilderRegistry<IActionStatus> ACTION_STATUS_REGISTRY;

    //TODO create missile builder handler that will allow API driven calls to create and spawn missiles in world


    //=========================
    //=== References ==========
    //=========================
    public static final String ID = "icbmclassic";


    //=========================
    //=== Content keys ========
    //=========================
    public static final ResourceLocation EX_MISSILE = new ResourceLocation(ID, "missile");
    public static final ResourceLocation EX_GRENADE = new ResourceLocation(ID, "grenade");
    public static final ResourceLocation EX_BLOCK = new ResourceLocation(ID, "block");
    public static final ResourceLocation EX_MINECART = new ResourceLocation(ID, "minecart");

    //=========================
    //=== Capabilities ========
    //=========================
    @CapabilityInject(IEMPReceiver.class)
    public static Capability<IEMPReceiver> EMP_CAPABILITY = null;

    @CapabilityInject(IExplosive.class)
    public static Capability<IExplosive> EXPLOSIVE_CAPABILITY = null;

    /**
     * Only applies to entities
     */
    public static final EntityCapability<IMissile, Void> MISSILE_CAPABILITY =
        EntityCapability.createVoid(new ResourceLocation("missile"), IMissile.class);

    /**
     * Only applies to ItemStack
     */
    public static final ItemCapability<ICapabilityMissileStack, Void> MISSILE_STACK_CAPABILITY =
        ItemCapability.createVoid(new ResourceLocation("missile_stack"), ICapabilityMissileStack.class);

    public static BaseCapability<IMissileHolder, Void> MISSILE_HOLDER_CAPABILITY = null;

    public static BaseCapability<IMissileLauncher, Void> MISSILE_LAUNCHER_CAPABILITY = null;

    public static BaseCapability<IRadio, Void> RADIO_CAPABILITY = null;

    public static BaseCapability<IBlastVelocity, Void> BLAST_VELOCITY_CAPABILITY = null;

    public static BaseCapability<IBlast, Void> BLAST_CAPABILITY = null;

    public static BaseCapability<IGPSData, Void> GPS_CAPABILITY = null;

    /**
     * Called to register an EMP handler for the {@link Block}
     * and related {@link net.minecraft.world.level.block.state.BlockState}
     * <p>
     * Allows several receiver to be registered per block.
     *
     * @param block    - block
     * @param receiver - receiver
     */
    @Deprecated //Will be placed in a registry/handler
    public void registerBlockEmpHandler(Block block, IEMPReceiver receiver) {
        //TODO implement
    }

}

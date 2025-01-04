package icbm.classic.api;

import icbm.classic.api.actions.IActionData;
import icbm.classic.api.actions.IPotentialAction;
import icbm.classic.api.actions.cause.IActionCause;
import icbm.classic.api.actions.conditions.ICondition;
import icbm.classic.api.actions.listener.IActionListenerHandler;
import icbm.classic.api.actions.status.IActionStatus;
import icbm.classic.api.caps.IEMPReceiver;
import icbm.classic.api.caps.IExplosive;
import icbm.classic.api.caps.IGPSData;
import icbm.classic.api.caps.IMissileHolder;
import icbm.classic.api.explosion.IBlast;
import icbm.classic.api.explosion.redmatter.IBlastVelocity;
import icbm.classic.api.launcher.IMissileLauncher;
import icbm.classic.api.missiles.ICapabilityMissileStack;
import icbm.classic.api.missiles.IMissile;
import icbm.classic.api.missiles.parts.IMissileFlightLogic;
import icbm.classic.api.missiles.parts.IMissileTarget;
import icbm.classic.api.missiles.projectile.IProjectileDataRegistry;
import icbm.classic.api.missiles.projectile.IProjectileStack;
import icbm.classic.api.radio.IRadio;
import icbm.classic.api.reg.IExplosiveCustomization;
import icbm.classic.api.reg.IExplosiveRegistry;
import icbm.classic.api.reg.content.IExBlockRegistry;
import icbm.classic.api.reg.content.IExGrenadeRegistry;
import icbm.classic.api.reg.content.IExMinecartRegistry;
import icbm.classic.api.reg.content.IExMissileRegistry;
import icbm.classic.api.reg.obj.IBuilderRegistry;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;

/**
 * API reference class for ICBM-Classic mod
 */
public final class ICBMClassicAPI
{

    //=========================
    //=== Registry ============
    //=========================

    /** Registry for target data save/load in missiles */
    public static IBuilderRegistry<IMissileTarget> MISSILE_TARGET_DATA_REGISTRY;
    /** Registry for flight logic save/load in missiles */
    public static IBuilderRegistry<IMissileFlightLogic> MISSILE_FLIGHT_LOGIC_REGISTRY;

    /** Registry for conditional logic, often used for actions */
    public static IBuilderRegistry<ICondition> CONDITION_REGISTRY;

    /** Registry for actions that can be run */
    public static IBuilderRegistry<IActionData> ACTION_REGISTRY;
    /** Registry for actions with conditionals that may be run */
    public static IBuilderRegistry<IPotentialAction> ACTION_POTENTIAL_REGISTRY;
    /** Registry for status messages produced by machines, items, and entities in the mod */
    public static IBuilderRegistry<IActionStatus> ACTION_STATUS_REGISTRY;
    /** Registry for missile cause save/load in missiles */
    public static IBuilderRegistry<IActionCause> ACTION_CAUSE_REGISTRY;
    /** Event system for actions */
    public static IActionListenerHandler ACTION_LISTENER;

    /** Registry for projectile information */
    public static IProjectileDataRegistry PROJECTILE_DATA_REGISTRY;

    //=========================
    //=== References ==========
    //=========================
    public static final String ID = "icbmclassic";


    //=========================
    //=== Capabilities ========
    //=========================

    /** @deprecated used the injector */
    @Deprecated
    @CapabilityInject(IEMPReceiver.class)
    public static Capability<IEMPReceiver> EMP_CAPABILITY = null;


    /** Only applies to entities */
    @CapabilityInject(IMissile.class)
    public static Capability<IMissile> MISSILE_CAPABILITY = null;


    /** @deprecated used the injector */
    @Deprecated
    @CapabilityInject(IMissileHolder.class)
    public static Capability<IMissileHolder> MISSILE_HOLDER_CAPABILITY = null;

    /** @deprecated used the injector */
    @Deprecated
    @CapabilityInject(IMissileLauncher.class)
    public static Capability<IMissileLauncher> MISSILE_LAUNCHER_CAPABILITY = null;

    /** @deprecated used the injector */
    @Deprecated
    @CapabilityInject(IRadio.class)
    public static Capability<IRadio> RADIO_CAPABILITY = null;

    /** @deprecated used the injector */
    @Deprecated
    @CapabilityInject(IGPSData.class)
    public static Capability<IGPSData> GPS_CAPABILITY = null;

    /** @deprecated used the injector */
    @Deprecated
    @CapabilityInject(IProjectileStack.class)
    public static Capability<IProjectileStack> PROJECTILE_STACK_CAPABILITY = null;


    //=========================
    //=== Will be removed =====
    //=========================
    /**
     * Main handler for explosives, do not override as this will break the mod
     * @deprecated replaced with {@link #ACTION_REGISTRY}
     */
    @Deprecated
    public static IExplosiveRegistry EXPLOSIVE_REGISTRY;

    /** Registry for explosive customizations
     * @deprecated replaced with {@link icbm.classic.api.actions.data.IActionFieldProvider}
     * */
    @Deprecated
    public static IBuilderRegistry<IExplosiveCustomization> EXPLOSIVE_CUSTOMIZATION_REGISTRY;

    @Deprecated
    public static final ResourceLocation EX_MISSILE = new ResourceLocation(ID, "missile");
    @Deprecated
    public static final ResourceLocation EX_GRENADE = new ResourceLocation(ID, "grenade");
    @Deprecated
    public static final ResourceLocation EX_BLOCK = new ResourceLocation(ID, "block");
    @Deprecated
    public static final ResourceLocation EX_MINECART = new ResourceLocation(ID, "minecart");


    @Deprecated
    @CapabilityInject(IExplosive.class)
    public static Capability<IExplosive> EXPLOSIVE_CAPABILITY = null;

    /** Only applies to ItemStack */
    @Deprecated
    @CapabilityInject(ICapabilityMissileStack.class)
    public static Capability<ICapabilityMissileStack> MISSILE_STACK_CAPABILITY = null;

    @Deprecated
    @CapabilityInject(IBlastVelocity.class)
    public static Capability<IBlastVelocity> BLAST_VELOCITY_CAPABILITY = null;

    @Deprecated
    @CapabilityInject(IBlast.class)
    public static Capability<IBlast> BLAST_CAPABILITY = null;

}

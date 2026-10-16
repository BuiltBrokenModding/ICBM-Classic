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
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.capabilities.BlockCapability;
import net.neoforged.neoforge.capabilities.EntityCapability;
import net.neoforged.neoforge.capabilities.ItemCapability;
import org.jetbrains.annotations.Nullable;

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

    private static ResourceLocation cap(String name)
    {
        return ResourceLocation.fromNamespaceAndPath(ID, name);
    }

    /** EMP handling for entities */
    public static final EntityCapability<IEMPReceiver, Void> EMP_ENTITY_CAPABILITY = EntityCapability.createVoid(cap("emp"), IEMPReceiver.class);
    /** EMP handling for block entities */
    public static final BlockCapability<IEMPReceiver, @Nullable Direction> EMP_BLOCK_CAPABILITY = BlockCapability.createSided(cap("emp"), IEMPReceiver.class);
    /** EMP handling for item stacks */
    public static final ItemCapability<IEMPReceiver, Void> EMP_ITEM_CAPABILITY = ItemCapability.createVoid(cap("emp"), IEMPReceiver.class);

    /** Only applies to entities */
    public static final EntityCapability<IMissile, Void> MISSILE_CAPABILITY = EntityCapability.createVoid(cap("missile"), IMissile.class);

    public static final BlockCapability<IMissileHolder, @Nullable Direction> MISSILE_HOLDER_CAPABILITY = BlockCapability.createSided(cap("missile_holder"), IMissileHolder.class);

    public static final BlockCapability<IMissileLauncher, @Nullable Direction> MISSILE_LAUNCHER_CAPABILITY = BlockCapability.createSided(cap("missile_launcher"), IMissileLauncher.class);

    public static final BlockCapability<IRadio, @Nullable Direction> RADIO_CAPABILITY = BlockCapability.createSided(cap("radio"), IRadio.class);
    public static final ItemCapability<IRadio, Void> RADIO_ITEM_CAPABILITY = ItemCapability.createVoid(cap("radio"), IRadio.class);

    public static final ItemCapability<IGPSData, Void> GPS_CAPABILITY = ItemCapability.createVoid(cap("gps"), IGPSData.class);

    public static final ItemCapability<IProjectileStack, Void> PROJECTILE_STACK_CAPABILITY = ItemCapability.createVoid(cap("projectile_stack"), IProjectileStack.class);

    //=========================
    //=== Will be removed =====
    //=========================
    /**
     * Main handler for explosives, do not override as this will break the mod
     * @deprecated replaced with {@link #ACTION_REGISTRY}
     */
    @Deprecated
    public static IExplosiveRegistry EXPLOSIVE_REGISTRY;

    @Deprecated
    public static IExMissileRegistry EX_MISSILE_REGISTRY;
    @Deprecated
    public static IExGrenadeRegistry EX_GRENADE_REGISTRY;
    @Deprecated
    public static IExBlockRegistry EX_BLOCK_REGISTRY;
    @Deprecated
    public static IExMinecartRegistry EX_MINECART_REGISTRY;

    /** Registry for explosive customizations
     * @deprecated replaced with {@link icbm.classic.api.actions.data.IActionFieldProvider}
     * */
    @Deprecated
    public static IBuilderRegistry<IExplosiveCustomization> EXPLOSIVE_CUSTOMIZATION_REGISTRY;

    @Deprecated
    public static final ResourceLocation EX_MISSILE = ResourceLocation.fromNamespaceAndPath(ID, "missile");
    @Deprecated
    public static final ResourceLocation EX_GRENADE = ResourceLocation.fromNamespaceAndPath(ID, "grenade");
    @Deprecated
    public static final ResourceLocation EX_BLOCK = ResourceLocation.fromNamespaceAndPath(ID, "block");
    @Deprecated
    public static final ResourceLocation EX_MINECART = ResourceLocation.fromNamespaceAndPath(ID, "minecart");


    @Deprecated
    public static final ItemCapability<IExplosive, Void> EXPLOSIVE_ITEM_CAPABILITY = ItemCapability.createVoid(cap("explosive"), IExplosive.class);
    @Deprecated
    public static final EntityCapability<IExplosive, Void> EXPLOSIVE_ENTITY_CAPABILITY = EntityCapability.createVoid(cap("explosive"), IExplosive.class);
    @Deprecated
    public static final BlockCapability<IExplosive, @Nullable Direction> EXPLOSIVE_BLOCK_CAPABILITY = BlockCapability.createSided(cap("explosive"), IExplosive.class);

    /** Only applies to ItemStack */
    @Deprecated
    public static final ItemCapability<ICapabilityMissileStack, Void> MISSILE_STACK_CAPABILITY = ItemCapability.createVoid(cap("missile_stack"), ICapabilityMissileStack.class);

    @Deprecated
    public static final EntityCapability<IBlastVelocity, Void> BLAST_VELOCITY_CAPABILITY = EntityCapability.createVoid(cap("blast_velocity"), IBlastVelocity.class);

    @Deprecated
    public static final EntityCapability<IBlast, Void> BLAST_CAPABILITY = EntityCapability.createVoid(cap("blast"), IBlast.class);

}

package icbm.classic.lib.actions;

import icbm.classic.ICBMConstants;
import icbm.classic.api.ICBMClassicAPI;
import icbm.classic.api.actions.IPotentialAction;
import icbm.classic.api.actions.cause.IActionCause;
import icbm.classic.api.actions.conditions.ICondition;
import icbm.classic.api.actions.data.ActionField;
import icbm.classic.api.actions.data.ActionFields;
import icbm.classic.api.actions.status.IActionStatus;
import icbm.classic.api.reg.events.*;
import icbm.classic.content.actions.ActionProvider;
import icbm.classic.content.actions.causeby.CauseByTimer;
import icbm.classic.content.actions.conditionals.ConditionAnd;
import icbm.classic.content.actions.conditionals.ConditionOR;
import icbm.classic.content.actions.conditionals.ConditionTargetDistance;
import icbm.classic.content.actions.conditionals.ConditionalImpact;
import icbm.classic.content.actions.emp.ActionDataEmpArea;
import icbm.classic.content.actions.entity.ActionSpawnEntity;
import icbm.classic.content.blocks.launcher.screen.BlockScreenCause;
import icbm.classic.content.blocks.launcher.status.LauncherStatus;
import icbm.classic.content.cluster.action.ActionDataCluster;
import icbm.classic.content.missile.logic.source.cause.CausedByBlock;
import icbm.classic.content.missile.logic.source.cause.EntityCause;
import icbm.classic.content.missile.logic.source.cause.RedstoneCause;
import icbm.classic.lib.actions.conditionals.timer.TimerCondition;
import icbm.classic.lib.actions.conditionals.timer.TimerTickingStatus;
import icbm.classic.lib.actions.listners.ActionListenerHandler;
import icbm.classic.lib.actions.status.ActionResponses;
import icbm.classic.lib.actions.status.MissingFieldStatus;
import icbm.classic.lib.buildable.BuildableObjectRegistry;
import icbm.classic.lib.saving.nodes.SaveNodeBoolean;
import icbm.classic.lib.saving.nodes.SaveNodeFacing;
import icbm.classic.lib.saving.nodes.SaveNodeResourceLocation;
import icbm.classic.lib.saving.nodes.SaveNodeVec3d;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import net.minecraft.world.entity.Entity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.FloatTag;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.NeoForge;

@NoArgsConstructor(access = AccessLevel.NONE)
public final class ActionSystem {
    public static final ResourceLocation ACTION_ENTITY_SPAWN = ResourceLocation.fromNamespaceAndPath(ICBMConstants.DOMAIN, "entity.spawn");

    public static void setup()
    {
        ICBMClassicAPI.ACTION_LISTENER = new ActionListenerHandler();
        setupActionFields();
        setupConditionalRegistry();
        setupCauseRegistry();
        setupStatusRegistry();
        setupActionRegistry();
        setupActionPotentialRegistry();
        ActionProvider.register();
    }

    private static void setupActionFields() {
        ActionFields.AREA_SIZE = ActionField.getOrCreate("area.size", Float.class, FloatTag::valueOf, FloatTag::getAsFloat);
        ActionFields.HOST_ENTITY = ActionField.getOrCreate("host.entity", Entity.class, null, null);
        ActionFields.HOST_POSITION = ActionField.getOrCreate("host.vec3d", Vec3.class, SaveNodeVec3d::save, SaveNodeVec3d::load);
        ActionFields.HOST_DIRECTION = ActionField.getOrCreate("host.direction", Direction.class, SaveNodeFacing::save, SaveNodeFacing::load);
        ActionFields.IMPACTED = ActionField.getOrCreate("impacted", Boolean.class, SaveNodeBoolean::save, SaveNodeBoolean::load);
        ActionFields.TARGET_POSITION = ActionField.getOrCreate("target.vec3d", Vec3.class, SaveNodeVec3d::save, SaveNodeVec3d::load);
        ActionFields.MOTION_VECTOR = ActionField.getOrCreate("motion.vec3d", Vec3.class, SaveNodeVec3d::save, SaveNodeVec3d::load);
        ActionFields.YAW = ActionField.getOrCreate("yaw", Float.class, FloatTag::valueOf, FloatTag::getAsFloat);
        ActionFields.PITCH = ActionField.getOrCreate("pitch", Float.class, FloatTag::valueOf, FloatTag::getAsFloat);
        ActionFields.ENTITY_REG_NAME = ActionField.getOrCreate("entity.regName", ResourceLocation.class, SaveNodeResourceLocation::save, SaveNodeResourceLocation::load);
        ActionFields.ENTITY_DATA = ActionField.getOrCreate("entity.data", CompoundTag.class, (v) -> v, (t) -> t);
    }

    private static void setupConditionalRegistry() {
        ICBMClassicAPI.CONDITION_REGISTRY =  new BuildableObjectRegistry<ICondition>("CONDITIONALS", "conditionals");

        ICBMClassicAPI.CONDITION_REGISTRY.register(TimerCondition.REG_NAME, TimerCondition::new);
        ICBMClassicAPI.CONDITION_REGISTRY.register(ConditionalImpact.REG_NAME, ConditionalImpact::new);
        ICBMClassicAPI.CONDITION_REGISTRY.register(ConditionTargetDistance.REG_NAME, ConditionTargetDistance::new);
        ICBMClassicAPI.CONDITION_REGISTRY.register(ConditionAnd.REG_NAME, ConditionAnd::new);
        ICBMClassicAPI.CONDITION_REGISTRY.register(ConditionOR.REG_NAME, ConditionOR::new);

        //Fire registry event
        NeoForge.EVENT_BUS.post(new ConditionalRegistryEvent(ICBMClassicAPI.CONDITION_REGISTRY));

        //Lock to prevent late registry
        ((BuildableObjectRegistry)ICBMClassicAPI.CONDITION_REGISTRY).lock();
    }

    private static void setupStatusRegistry() {
        ICBMClassicAPI.ACTION_STATUS_REGISTRY =  new BuildableObjectRegistry<IActionStatus>("ACTION_STATUS", "action.status");

        // Register defaults
        LauncherStatus.registerTypes();
        ActionResponses.registerTypes();
        ICBMClassicAPI.ACTION_STATUS_REGISTRY.register(TimerTickingStatus.REG_NAME, TimerTickingStatus::new);
        ICBMClassicAPI.ACTION_STATUS_REGISTRY.register(MissingFieldStatus.REG_NAME, MissingFieldStatus::new);

        //Fire registry event
        NeoForge.EVENT_BUS.post(new ActionStatusRegistryEvent(ICBMClassicAPI.ACTION_STATUS_REGISTRY));

        //Lock to prevent late registry
        ((BuildableObjectRegistry)ICBMClassicAPI.ACTION_STATUS_REGISTRY).lock();
    }

    private static void setupActionRegistry() {
        ICBMClassicAPI.ACTION_REGISTRY =  new BuildableObjectRegistry<>("ACTION_DATA", "action.data");

        // Register defaults
        new ActionDataEmpArea().register();
        ICBMClassicAPI.ACTION_REGISTRY.register(ActionDataCluster.REG_NAME, ActionDataCluster::new);
        ICBMClassicAPI.ACTION_REGISTRY.register(ACTION_ENTITY_SPAWN,
            () -> new ActionDataGeneric(ACTION_ENTITY_SPAWN,
                (w, x, y, z, s, d) -> new ActionSpawnEntity(w, new Vec3(x, y, z), s, d),
                ActionSpawnEntity.FIELDS
            ));

        //Fire registry event
        NeoForge.EVENT_BUS.post(new ActionRegistryEvent(ICBMClassicAPI.ACTION_REGISTRY));

        //Lock to prevent late registry
        ((BuildableObjectRegistry)ICBMClassicAPI.ACTION_REGISTRY).lock();
    }

    private static void setupCauseRegistry()
    {
        ICBMClassicAPI.ACTION_CAUSE_REGISTRY =  new BuildableObjectRegistry<IActionCause>("ACTION_CAUSE", "action.cause");

        // Register defaults
        ICBMClassicAPI.ACTION_CAUSE_REGISTRY.register(EntityCause.REG_NAME, EntityCause::new);
        ICBMClassicAPI.ACTION_CAUSE_REGISTRY.register(CausedByBlock.REG_NAME, CausedByBlock::new);
        ICBMClassicAPI.ACTION_CAUSE_REGISTRY.register(BlockScreenCause.REG_NAME, BlockScreenCause::new);
        ICBMClassicAPI.ACTION_CAUSE_REGISTRY.register(RedstoneCause.REG_NAME, RedstoneCause::new);
        ICBMClassicAPI.ACTION_CAUSE_REGISTRY.register(CauseByTimer.REG_NAME, CauseByTimer::new);

        //Fire registry event
        NeoForge.EVENT_BUS.post(new ActionCauseRegistryEvent(ICBMClassicAPI.ACTION_CAUSE_REGISTRY));

        //Lock to prevent late registry
        ((BuildableObjectRegistry)ICBMClassicAPI.ACTION_CAUSE_REGISTRY).lock();
    }

    private static void setupActionPotentialRegistry()
    {
        ICBMClassicAPI.ACTION_POTENTIAL_REGISTRY =  new BuildableObjectRegistry<IPotentialAction>("ACTION_POTENTIAL", "action.potential");

        // Register defaults
        ICBMClassicAPI.ACTION_POTENTIAL_REGISTRY.register(PotentialAction.REG_NAME, PotentialAction::new);

        //Fire registry event
        NeoForge.EVENT_BUS.post(new ActionPotentialRegistryEvent(ICBMClassicAPI.ACTION_POTENTIAL_REGISTRY));

        //Lock to prevent late registry
        ((BuildableObjectRegistry)ICBMClassicAPI.ACTION_POTENTIAL_REGISTRY).lock();
    }
}
// PORTED

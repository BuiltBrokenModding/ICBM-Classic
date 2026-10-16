package icbm.classic.content.actions.conditionals;

import icbm.classic.ICBMConstants;
import icbm.classic.api.ICBMClassicAPI;
import icbm.classic.api.actions.cause.IActionCause;
import icbm.classic.api.actions.conditions.ICondition;
import icbm.classic.api.actions.conditions.IConditionCause;
import icbm.classic.api.actions.conditions.IConditionLayer;
import icbm.classic.api.actions.status.ActionStatusTypes;
import icbm.classic.api.actions.status.IActionStatus;
import icbm.classic.lib.actions.status.ActionResponses;
import icbm.classic.lib.saving.NbtSaveHandler;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.common.util.INBTSerializable;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public class ConditionAnd implements IConditionLayer, IConditionCause, INBTSerializable<CompoundTag> {

    public static final ResourceLocation REG_NAME = ResourceLocation.fromNamespaceAndPath(ICBMConstants.DOMAIN, "composition.and");

    private final List<ICondition> children = new ArrayList<>();

    @Getter @Setter @Accessors(chain = true)
    private boolean shortCircuit = true;

    @Override
    public IActionStatus getCondition() {
        for(ICondition condition: children) {
            final IActionStatus status = condition.getCondition();

            // Short-circuit
            if(status.isType(ActionStatusTypes.BLOCKING)) {
                return status;
            }
        }
        return ActionResponses.READY; //TODO provide composition status
    }

    @Override
    public void onTick() {
        for(ICondition condition: children) {
            condition.onTick();

            // If blocking, short-circuit and don't tick next
            final IActionStatus status = condition.getCondition();
            if(shortCircuit && status.isType(ActionStatusTypes.BLOCKING)) {
               return;
            }
        }
    }

    @Override
    public List<ICondition> getConditions() {
        return children;
    }

    @Nonnull
    @Override
    public ResourceLocation getRegistryKey() {
        return REG_NAME;
    }

    @Override
    public IActionCause getCause() {
        return null; //TODO implement
    }

    @Override
    public CompoundTag serializeNBT(HolderLookup.Provider provider) {
        return SAVE_LOGIC.save(this);
    }

    @Override
    public void deserializeNBT(HolderLookup.Provider provider, CompoundTag nbt) {
        SAVE_LOGIC.load(this, nbt);
    }

    private static final NbtSaveHandler<ConditionAnd> SAVE_LOGIC = new NbtSaveHandler<ConditionAnd>()
        .mainRoot()
        /* */.nodeBuildableObjectList("conditions", () -> ICBMClassicAPI.CONDITION_REGISTRY, ConditionAnd::getConditions)
        /* */.nodeBoolean("short_circuit", ConditionAnd::isShortCircuit, ConditionAnd::setShortCircuit)
        .base();
}
// PORTED

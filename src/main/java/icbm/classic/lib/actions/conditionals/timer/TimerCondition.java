package icbm.classic.lib.actions.conditionals.timer;

import icbm.classic.ICBMConstants;
import icbm.classic.api.actions.cause.IActionCause;
import icbm.classic.api.actions.conditions.IConditionCause;
import icbm.classic.api.actions.status.IActionStatus;
import icbm.classic.content.actions.causeby.CauseByTimer;
import icbm.classic.lib.actions.conditionals.Condition;
import icbm.classic.lib.actions.status.ActionResponses;
import icbm.classic.lib.saving.NbtSaveHandler;
import lombok.*;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nonnull;

@NoArgsConstructor
@AllArgsConstructor
public class TimerCondition extends Condition implements IConditionCause {
    public static final ResourceLocation REG_NAME = ResourceLocation.fromNamespaceAndPath(ICBMConstants.DOMAIN, "timer");

    @Getter @Setter
    private int target = 0;

    @Getter @Setter(AccessLevel.PRIVATE)
    private int current = 0;

    private IActionStatus statusCache; //TODO cache on action status for reduce churn

    @Override
    public void onTick()
    {
        if(current < target) {
            current++;
            statusCache = null;
        }
    }

    @Override
    public void reset() {
        this.current = 0;
    }

    @Override
    public IActionStatus getCondition() {
        if(current <= 0) {
            return ActionResponses.READY;
        }
        else if(statusCache == null) {
           statusCache = new TimerTickingStatus(current, target);
        }
        return statusCache;
    }

    @Nonnull
    @Override
    public ResourceLocation getRegistryKey() {
        return REG_NAME;
    }

    @Override
    public CompoundTag serializeNBT(HolderLookup.Provider provider) {
        return SAVE_LOGIC.save(this, super.serializeNBT(provider));
    }

    @Override
    public void deserializeNBT(HolderLookup.Provider provider, CompoundTag nbt) {
        super.deserializeNBT(provider, nbt);
        SAVE_LOGIC.load(this, nbt);
    }

    private static final NbtSaveHandler<TimerCondition> SAVE_LOGIC = new NbtSaveHandler<TimerCondition>()
        .mainRoot()
        /* */.nodeInteger("tick_current", TimerCondition::getCurrent, TimerCondition::setCurrent)
        /* */.nodeInteger("tick_target", TimerCondition::getTarget, TimerCondition::setTarget)
        .base();

    @Override
    public IActionCause getCause() {
        return new CauseByTimer(this.getName(), target);
    }
}
// PORTED

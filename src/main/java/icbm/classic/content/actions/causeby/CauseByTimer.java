package icbm.classic.content.actions.causeby;

import icbm.classic.ICBMConstants;
import icbm.classic.api.ICBMClassicAPI;
import icbm.classic.api.actions.cause.IActionCause;
import icbm.classic.content.missile.logic.source.cause.ActionCause;
import icbm.classic.lib.actions.conditionals.timer.TimerCondition;
import icbm.classic.lib.saving.NbtSaveHandler;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nonnull;

/**
 * Cause metadata to note a timer was used such as {@link TimerCondition}
 */
@NoArgsConstructor
public class CauseByTimer extends ActionCause implements IActionCause {

    public static final ResourceLocation REG_NAME = ResourceLocation.fromNamespaceAndPath(ICBMConstants.DOMAIN, "conditional.timer");

    @Setter @Getter
    private int timer;

    public CauseByTimer(String name, int timer) {
        setName(name);
        this.timer = timer;
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

    private static final NbtSaveHandler<CauseByTimer> SAVE_LOGIC = new NbtSaveHandler<CauseByTimer>()
        .mainRoot()
        /* */.nodeInteger("timer", CauseByTimer::getTimer, CauseByTimer::setTimer)
        .base();
}
// PORTED

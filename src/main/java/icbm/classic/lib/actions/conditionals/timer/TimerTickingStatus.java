package icbm.classic.lib.actions.conditionals.timer;

import com.google.common.collect.ImmutableList;
import icbm.classic.ICBMConstants;
import icbm.classic.api.actions.status.ActionStatusTypes;
import icbm.classic.api.actions.status.IActionStatus;
import icbm.classic.api.data.meta.MetaTag;
import icbm.classic.lib.saving.NbtSaveHandler;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.common.util.INBTSerializable;

import javax.annotation.Nonnull;
import java.util.Collection;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TimerTickingStatus implements IActionStatus, INBTSerializable<CompoundTag> {

    public static final ResourceLocation REG_NAME = ResourceLocation.fromNamespaceAndPath(ICBMConstants.DOMAIN, "conditional.timer.ticking");
    public static final ImmutableList<MetaTag> TAGS = ImmutableList.of(ActionStatusTypes.WAITING, ActionStatusTypes.BLOCKING);
    //TODO consider caching common ticks as 0/10 to 10/10 will be reused a lot

    private int tickCurrent = 0;
    private int tickTarget = 0;

    @Nonnull
    @Override
    public Collection<MetaTag> getTypeTags() {
        return TAGS;
    }

    @Override
    public Component message() {
        return Component.translatable(getTranslationKey(), tickCurrent, tickTarget);
    }

    @Nonnull
    @Override
    public ResourceLocation getRegistryKey() {
        return REG_NAME;
    }

    @Override
    public CompoundTag serializeNBT(HolderLookup.Provider provider) {
        return SAVE_LOGIC.save(this);
    }

    @Override
    public void deserializeNBT(HolderLookup.Provider provider, CompoundTag nbt) {
        SAVE_LOGIC.load(this, nbt);
    }

    private static final NbtSaveHandler<TimerTickingStatus> SAVE_LOGIC = new NbtSaveHandler<TimerTickingStatus>()
        .mainRoot()
        /* */.nodeInteger("tick_current", TimerTickingStatus::getTickCurrent, TimerTickingStatus::setTickCurrent)
        /* */.nodeInteger("tick_target", TimerTickingStatus::getTickTarget, TimerTickingStatus::setTickTarget)
        .base();
}
// PORTED

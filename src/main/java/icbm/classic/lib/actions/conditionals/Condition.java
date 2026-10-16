package icbm.classic.lib.actions.conditionals;

import icbm.classic.api.actions.conditions.ICondition;
import icbm.classic.lib.saving.NbtSaveHandler;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.common.util.INBTSerializable;

public abstract class Condition implements ICondition, INBTSerializable<CompoundTag> {
    /**
     * Display name
     */
    @Getter
    @Setter
    @Accessors(chain = true)
    private String name;

    @Override
    public Component getDisplayName() {
        if (name != null) {
            return Component.translatable(getTranslationKey() + ".user_defined_name", name);
        }
        return Component.translatable(getTranslationKey());
    }

    @Override
    public CompoundTag serializeNBT(HolderLookup.Provider provider) {
        return SAVE_LOGIC.save(this);
    }

    @Override
    public void deserializeNBT(HolderLookup.Provider provider, CompoundTag nbt) {
        SAVE_LOGIC.load(this, nbt);
    }

    private static final NbtSaveHandler<Condition> SAVE_LOGIC = new NbtSaveHandler<Condition>()
        .mainRoot()
        /* */.nodeString("display_name", Condition::getName, Condition::setName)
        .base();
}
// PORTED

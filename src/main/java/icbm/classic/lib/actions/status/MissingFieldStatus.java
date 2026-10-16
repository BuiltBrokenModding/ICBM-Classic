package icbm.classic.lib.actions.status;

import icbm.classic.ICBMConstants;
import icbm.classic.api.actions.status.ActionStatusTypes;
import icbm.classic.lib.saving.NbtSaveHandler;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.common.util.INBTSerializable;

public final class MissingFieldStatus extends ImmutableStatus implements INBTSerializable<CompoundTag> {

    public static final ResourceLocation REG_NAME = ResourceLocation.fromNamespaceAndPath(ICBMConstants.DOMAIN, "missing.field");

    @Setter @Getter @Accessors(chain = true)
    private String source;
    @Setter @Getter @Accessors(chain = true)
    private String field;

    public MissingFieldStatus() {
        super(REG_NAME, ActionStatusTypes.BLOCKING, ActionStatusTypes.ERROR);
    }

    public MissingFieldStatus(ResourceLocation regName, String source, String field) {
        super(regName, ActionStatusTypes.BLOCKING, ActionStatusTypes.ERROR);
        this.source = source;
        this.field = field;
    }

    @Override
    public Component message() {
        if(textComponent == null) {
            textComponent = Component.translatable(translationKey, source, field);
        }
        return textComponent;
    }

    @Override
    public CompoundTag serializeNBT(HolderLookup.Provider provider) {
        return SAVE_LOGIC.save(this);
    }

    @Override
    public void deserializeNBT(HolderLookup.Provider provider, CompoundTag nbt) {
        SAVE_LOGIC.load(this, nbt);
    }

    private static final NbtSaveHandler<MissingFieldStatus> SAVE_LOGIC = new NbtSaveHandler<MissingFieldStatus>()
        .mainRoot()
        /* */.nodeString("source", MissingFieldStatus::getSource, MissingFieldStatus::setSource)
        /* */.nodeString("field", MissingFieldStatus::getField, MissingFieldStatus::setField)
        .base();
}
// PORTED

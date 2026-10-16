package icbm.classic.content.actions.conditionals;

import icbm.classic.ICBMConstants;
import icbm.classic.api.actions.conditions.ICondition;
import icbm.classic.api.actions.data.ActionFields;
import icbm.classic.api.actions.data.IActionFieldProvider;
import icbm.classic.api.actions.status.IActionStatus;
import icbm.classic.lib.actions.status.ActionResponses;
import icbm.classic.lib.actions.status.MissingFieldStatus;
import icbm.classic.lib.saving.NbtSaveHandler;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.common.util.INBTSerializable;

import javax.annotation.Nonnull;

public class ConditionalImpact implements ICondition, INBTSerializable<CompoundTag> {
    public static final ResourceLocation REG_NAME = ResourceLocation.fromNamespaceAndPath(ICBMConstants.DOMAIN, "impact");

    private IActionFieldProvider provider;

    @Getter @Setter @Accessors(chain = true)
    private boolean impactDesired = true;

    @Override
    public void init(IActionFieldProvider provider) {
       this.provider = provider;
    }

    @Override
    public IActionStatus getCondition() {
        if(provider == null || !provider.hasField(ActionFields.IMPACTED)) {
            return new MissingFieldStatus().setSource("ConditionalImpact#provider").setField(ActionFields.IMPACTED.getKey());
        }
        Boolean value = provider.getValue(ActionFields.IMPACTED);
        if(value == null || value != impactDesired) {
            return ActionResponses.WAITING;
        }
        return ActionResponses.READY;
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

    private static final NbtSaveHandler<ConditionalImpact> SAVE_LOGIC = new NbtSaveHandler<ConditionalImpact>()
        .mainRoot()
        /* */.nodeBoolean("impact_desired", ConditionalImpact::isImpactDesired, ConditionalImpact::setImpactDesired)
        .base();
}
// PORTED

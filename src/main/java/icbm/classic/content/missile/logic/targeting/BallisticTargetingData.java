package icbm.classic.content.missile.logic.targeting;

import icbm.classic.ICBMConstants;
import lombok.NoArgsConstructor;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nonnull;

@NoArgsConstructor
public class BallisticTargetingData extends BasicTargetData {
    public static final ResourceLocation REG_NAME = ResourceLocation.fromNamespaceAndPath(ICBMConstants.DOMAIN, "ballistic");

    public static final String NBT_IMPACT_HEIGHT = "impact_height";
    /**
     * Height above target to detonate
     */
    private double impactHeightOffset;

    public BallisticTargetingData(Vec3 position, int impactHeightOffset) {
        super(position);
        this.impactHeightOffset = impactHeightOffset;
    }

    @Nonnull
    @Override
    public ResourceLocation getRegistryKey()
    {
        return REG_NAME;
    }

    public double getImpactHeightOffset() {
        return impactHeightOffset;
    }

    @Override
    public CompoundTag serializeNBT(HolderLookup.Provider provider) {
        final CompoundTag saveData = super.serializeNBT(provider);
        saveData.putDouble(NBT_IMPACT_HEIGHT, impactHeightOffset);
        return saveData;
    }

    @Override
    public void deserializeNBT(HolderLookup.Provider provider, CompoundTag nbt) {
        super.deserializeNBT(provider, nbt);
        impactHeightOffset = nbt.getDouble(NBT_IMPACT_HEIGHT);
    }
}
// PORTED

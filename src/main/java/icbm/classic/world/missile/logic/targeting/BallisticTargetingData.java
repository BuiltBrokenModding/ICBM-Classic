package icbm.classic.world.missile.logic.targeting;

import icbm.classic.IcbmConstants;
import lombok.NoArgsConstructor;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;

@NoArgsConstructor
public class BallisticTargetingData extends BasicTargetData {
    public static final ResourceLocation REG_NAME = new ResourceLocation(IcbmConstants.MOD_ID, "ballistic");

    public static final String NBT_IMPACT_HEIGHT = "impact_height";
    /**
     * Height above target to detonate
     */
    private double impactHeightOffset;

    public BallisticTargetingData(Vec3 position, int impactHeightOffset) {
        super(position);
        this.impactHeightOffset = impactHeightOffset;
    }

    @Override
    public ResourceLocation getRegistryName() {
        return REG_NAME;
    }

    public double getImpactHeightOffset() {
        return impactHeightOffset;
    }

    @Override
    public CompoundTag serializeNBT() {
        final CompoundTag saveData = super.serializeNBT();
        saveData.setDouble(NBT_IMPACT_HEIGHT, impactHeightOffset);
        return saveData;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        super.deserializeNBT(nbt);
        impactHeightOffset = nbt.getDouble(NBT_IMPACT_HEIGHT);
    }
}

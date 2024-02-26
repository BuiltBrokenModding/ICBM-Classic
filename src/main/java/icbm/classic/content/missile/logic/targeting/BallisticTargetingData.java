package icbm.classic.content.missile.logic.targeting;

import icbm.classic.ICBMConstants;
import lombok.NoArgsConstructor;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import javax.annotation.Nonnull;

@NoArgsConstructor
public class BallisticTargetingData extends BasicTargetData {
    public static final ResourceLocation REG_NAME = new ResourceLocation(ICBMConstants.DOMAIN, "ballistic");

    public static final String NBT_IMPACT_HEIGHT = "impact_height";
    /**
     * Height above target to detonate
     */
    private double impactHeightOffset;

    public BallisticTargetingData(Vec3d position, int impactHeightOffset) {
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
    public NBTTagCompound serializeNBT() {
        final NBTTagCompound saveData = super.serializeNBT();
        saveData.setDouble(NBT_IMPACT_HEIGHT, impactHeightOffset);
        return saveData;
    }

    @Override
    public void deserializeNBT(NBTTagCompound nbt) {
        super.deserializeNBT(nbt);
        impactHeightOffset = nbt.getDouble(NBT_IMPACT_HEIGHT);
    }
}

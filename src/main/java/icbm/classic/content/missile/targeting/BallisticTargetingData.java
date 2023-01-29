package icbm.classic.content.missile.targeting;

import icbm.classic.ICBMConstants;
import icbm.classic.lib.transform.vector.Pos;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public class BallisticTargetingData extends BasicTargetData {
    public static final ResourceLocation REG_NAME = new ResourceLocation(ICBMConstants.DOMAIN, "ballistic");

    public static final String NBT_IMPACT_HEIGHT = "impact_height";
    /**
     * Height above target to detonate
     */
    private double impactHeightOffset;

    public BallisticTargetingData() {
        //Used for save/load
    }

    @Deprecated //TODO remove pos references in code
    public BallisticTargetingData(Pos position, int impactHeightOffset) {
        super(position.x(), position.y(), position.z());
        this.impactHeightOffset = impactHeightOffset;
    }

    public BallisticTargetingData(Vec3d position, int impactHeightOffset) {
        super(position);
        this.impactHeightOffset = impactHeightOffset;
    }

    public BallisticTargetingData(BlockPos pos, int impactHeightOffset) {
        super(pos);
        this.impactHeightOffset = impactHeightOffset;
    }

    @Override
    public ResourceLocation getRegistryName()
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

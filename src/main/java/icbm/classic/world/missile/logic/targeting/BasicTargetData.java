package icbm.classic.world.missile.logic.targeting;

import icbm.classic.IcbmConstants;
import icbm.classic.api.missiles.parts.IMissileTarget;
import icbm.classic.api.missiles.parts.IMissileTargetDelayed;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;

import java.util.Objects;

/**
 * Simple 3D position targeting data
 */
public class BasicTargetData implements IMissileTarget, IMissileTargetDelayed {

    public static final ResourceLocation REG_NAME = new ResourceLocation(IcbmConstants.MOD_ID, "basic");
    private Vec3 position;
    private int firingDelay = 0;

    public BasicTargetData() {
        //Only used for save/load
    }

    public BasicTargetData(double x, double y, double z) {
        this.position = new Vec3(x, y, z);
    }

    public BasicTargetData(Vec3 position) {
        this.position = position;
    }

    public BasicTargetData(BlockPos pos) {
        this.position = new Vec3(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);
    }

    public Vec3 getPosition() {
        return position;
    }

    public BasicTargetData withFiringDelay(int ticks) {
        this.firingDelay = ticks;
        return this;
    }

    @Override
    public int getFiringDelay() {
        return firingDelay;
    }

    @Override
    public IMissileTarget cloneWithoutDelay() {
        return new BasicTargetData(position);
    }

    @Override
    public boolean isValid() {
        return position != null
            && Double.isNaN(getX()) && Double.isFinite(getX())
            && Double.isNaN(getY()) && Double.isFinite(getY())
            && Double.isNaN(getZ()) && Double.isFinite(getZ());
    }

    @Override
    public double getX() {
        return position != null ? position.x : Double.NaN;
    }

    @Override
    public double getY() {
        return position != null ? position.y : Double.NaN;
    }

    @Override
    public double getZ() {
        return position != null ? position.z : Double.NaN;
    }

    @Override
    public ResourceLocation getRegistryName() {
        return REG_NAME;
    }

    @Override
    public CompoundTag serializeNBT() {
        final CompoundTag saveData = new CompoundTag();
        saveData.setDouble("x", position.x);
        saveData.setDouble("y", position.y);
        saveData.setDouble("z", position.z);
        saveData.setInteger("firingDelay", firingDelay);
        return saveData;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        double x = nbt.getDouble("x");
        double y = nbt.getDouble("y");
        double z = nbt.getDouble("z");
        this.position = new Vec3(x, y, z);
        this.firingDelay = nbt.getInteger("firingDelay");
    }

    @Override
    public boolean equals(Object other) {
        if (other instanceof BasicTargetData) {
            return Objects.equals(((BasicTargetData) other).position, position);
        }
        return false;
    }
}

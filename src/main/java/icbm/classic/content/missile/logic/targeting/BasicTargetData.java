package icbm.classic.content.missile.logic.targeting;

import icbm.classic.ICBMConstants;
import icbm.classic.api.missiles.parts.IMissileTarget;
import icbm.classic.api.missiles.parts.IMissileTargetDelayed;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import java.util.Objects;

/**
 * Simple 3D position targeting data
 */
public class BasicTargetData implements IMissileTarget, IMissileTargetDelayed {

    public static final ResourceLocation REG_NAME = new ResourceLocation(ICBMConstants.DOMAIN, "basic");
    private Vec3d position;
    private int firingDelay = 0;

    public BasicTargetData() {
        //Only used for save/load
    }

    public BasicTargetData(double x, double y, double z) {
        this.position = new Vec3d(x, y, z);
    }

    public BasicTargetData(Vec3d position) {
        this.position = position;
    }

    public BasicTargetData(BlockPos pos) {
        this.position = new Vec3d(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);
    }

    public Vec3d getPosition() {
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
    public ResourceLocation getRegistryName()
    {
        return REG_NAME;
    }

    @Override
    public NBTTagCompound serializeNBT() {
        final NBTTagCompound saveData = new NBTTagCompound();
        saveData.setDouble("x", position.x);
        saveData.setDouble("y", position.y);
        saveData.setDouble("z", position.z);
        saveData.setInteger("firingDelay", firingDelay);
        return saveData;
    }

    @Override
    public void deserializeNBT(NBTTagCompound nbt) {
        double x = nbt.getDouble("x");
        double y = nbt.getDouble("y");
        double z = nbt.getDouble("z");
        this.position = new Vec3d(x, y, z);
        this.firingDelay = nbt.getInteger("firingDelay");
    }

    @Override
    public boolean equals(Object other) {
        if(other instanceof BasicTargetData) {
            return Objects.equals(((BasicTargetData) other).position, position);
        }
        return false;
    }
}

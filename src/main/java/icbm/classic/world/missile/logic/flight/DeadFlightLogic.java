package icbm.classic.world.missile.logic.flight;

import icbm.classic.IcbmConstants;
import icbm.classic.api.missiles.IMissile;
import icbm.classic.api.missiles.parts.IMissileFlightLogic;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.entity.Entity;

/**
 * Flight computer that does nothing, acts as a placeholder for when we fire missiles like an arrow or are using
 * raw motion setting logic in another system.
 */
public class DeadFlightLogic implements IMissileFlightLogic {
    public static final ResourceLocation REG_NAME = new ResourceLocation(IcbmConstants.MOD_ID, "dead");

    public int fuelTicks = 0;

    public DeadFlightLogic() {
        //for save/load logic
    }

    public DeadFlightLogic(int fuelTicks) {
        this.fuelTicks = fuelTicks;
    }

    @Override
    public boolean shouldRunEngineEffects(Entity entity) {
        return hasFuel(entity);
    }

    protected boolean hasFuel(Entity entity) {
        return fuelTicks > 0;
    }

    @Override
    public void onEntityTick(Entity entity, IMissile missile, int ticksInAir) {
        fuelTicks--;

        if (hasFuel(entity)) {
            float f3 = MathHelper.sqrt(entity.motionX * entity.motionX + entity.motionZ * entity.motionZ);
            entity.prevRotationYaw = entity.getYRot() = (float) (Math.atan2(entity.motionX, entity.motionZ) * 180.0D / Math.PI);
            entity.prevRotationPitch = entity.getXRot() = (float) (Math.atan2(entity.motionY, (double) f3) * 180.0D / Math.PI);
        }
    }

    @Override
    public boolean shouldAlignWithMotion(Entity entity) {
        return false; //TODO allow alignment at a much slower rate once out of fuel
    }

    @Override
    public CompoundTag serializeNBT() {
        final CompoundTag tagCompound = new CompoundTag();
        tagCompound.setInteger("fuel", fuelTicks);
        return tagCompound;
    }

    @Override
    public void deserializeNBT(CompoundTag save) {
        if (save.contains("fuel")) {
            fuelTicks = save.getInteger("fuel");
        }
    }

    @Override
    public <V> V predictPosition(Entity entity, VecBuilderFunc<V> builder, int ticks) {
        return builder.apply(
            entity.getX() + entity.motionX * ticks, //TODO add gravity
            entity.getY() + entity.motionY * ticks,
            entity.getZ() + entity.motionZ * ticks
        );
    }

    @Override
    public ResourceLocation getRegistryName() {
        return REG_NAME;
    }

    @Override
    public boolean shouldDecreaseMotion(Entity entity) {
        return !hasFuel(entity);
    }

    @Override
    public boolean equals(Object other) {
        if (other instanceof DeadFlightLogic) {
            return fuelTicks == ((DeadFlightLogic) other).fuelTicks && getRegistryName() == ((DeadFlightLogic) other).getRegistryName();
        }
        return false;
    }
}

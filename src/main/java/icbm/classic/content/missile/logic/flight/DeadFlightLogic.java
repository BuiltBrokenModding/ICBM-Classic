package icbm.classic.content.missile.logic.flight;

import icbm.classic.ICBMConstants;
import icbm.classic.api.ICBMClassicAPI;
import icbm.classic.api.missiles.IMissile;
import icbm.classic.api.missiles.parts.IMissileFlightLogic;
import icbm.classic.api.reg.obj.IBuilderRegistry;
import net.minecraft.world.entity.Entity;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.neoforged.neoforge.common.util.INBTSerializable;

import javax.annotation.Nonnull;

/**
 * Flight computer that does nothing, acts as a placeholder for when we fire missiles like an arrow or are using
 * raw motion setting logic in another system.
 */
public class DeadFlightLogic implements IMissileFlightLogic, INBTSerializable<CompoundTag>
{
    public static final ResourceLocation REG_NAME = ResourceLocation.fromNamespaceAndPath(ICBMConstants.DOMAIN, "dead");

    public int fuelTicks = 0;

    public DeadFlightLogic()
    {
        //for save/load logic
    }

    public DeadFlightLogic(int fuelTicks)
    {
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
    public void onEntityTick(Entity entity, IMissile missile, int ticksInAir)
    {
        fuelTicks--;

        if(hasFuel(entity)) {
            float f3 = (float) Math.sqrt(entity.getDeltaMovement().x * entity.getDeltaMovement().x + entity.getDeltaMovement().z * entity.getDeltaMovement().z);
            MotionHelper.setYawAndPrev(entity, (float) (Math.atan2(entity.getDeltaMovement().x, entity.getDeltaMovement().z) * 180.0D / Math.PI));
            MotionHelper.setPitchAndPrev(entity, (float) (Math.atan2(entity.getDeltaMovement().y, (double) f3) * 180.0D / Math.PI));
        }
    }

    @Override
    public boolean shouldAlignWithMotion(Entity entity) {
        return false; //TODO allow alignment at a much slower rate once out of fuel
    }

    @Override
    public CompoundTag serializeNBT(HolderLookup.Provider provider)
    {
        final CompoundTag tagCompound = new CompoundTag();
        tagCompound.putInt("fuel", fuelTicks);
        return tagCompound;
    }

    @Override
    public void deserializeNBT(HolderLookup.Provider provider, CompoundTag save)
    {
        if(save.contains("fuel")) {
            fuelTicks = save.getInt("fuel");
        }
    }

    @Override
    public <V> V predictPosition(Entity entity, VecBuilderFunc<V> builder, int ticks)
    {
        return builder.apply(
            entity.getX() + entity.getDeltaMovement().x * ticks, //TODO add gravity
            entity.getY() + entity.getDeltaMovement().y * ticks,
            entity.getZ() + entity.getDeltaMovement().z * ticks
        );
    }

    @Nonnull
    @Override
    public ResourceLocation getRegistryKey()
    {
        return REG_NAME;
    }

    @Nonnull
    @Override
    public IBuilderRegistry<IMissileFlightLogic> getRegistry() {
        return ICBMClassicAPI.MISSILE_FLIGHT_LOGIC_REGISTRY;
    }

    @Override
    public boolean shouldDecreaseMotion(Entity entity)
    {
        return !hasFuel(entity);
    }

    @Override
    public boolean equals(Object other) {
        if(other instanceof DeadFlightLogic) {
            return fuelTicks == ((DeadFlightLogic) other).fuelTicks && getRegistryKey() == ((DeadFlightLogic) other).getRegistryKey();
        }
        return false;
    }
}
// PORTED

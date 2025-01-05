package icbm.classic.content.missile.logic.flight;

import icbm.classic.ICBMConstants;
import icbm.classic.api.ICBMClassicAPI;
import icbm.classic.api.missiles.IMissile;
import icbm.classic.api.missiles.parts.IMissileFlightLogic;
import icbm.classic.api.reg.obj.IBuilderRegistry;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.common.util.INBTSerializable;

import javax.annotation.Nonnull;

/**
 * Flight computer that does nothing, acts as a placeholder for when we fire missiles like an arrow or are using
 * raw motion setting logic in another system.
 */
public class DeadFlightLogic implements IMissileFlightLogic, INBTSerializable<CompoundNBT>
{
    public static final ResourceLocation REG_NAME = new ResourceLocation(ICBMConstants.DOMAIN, "dead");

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
            float f3 = MathHelper.sqrt(entity.getMotion().x * entity.getMotion().x + entity.getMotion().z * entity.getMotion().z);
            entity.prevRotationYaw = entity.rotationYaw = (float) (Math.atan2(entity.getMotion().x, entity.getMotion().z) * 180.0D / Math.PI);
            entity.prevRotationPitch = entity.rotationPitch = (float) (Math.atan2(entity.getMotion().y, (double) f3) * 180.0D / Math.PI);
        }
    }

    @Override
    public boolean shouldAlignWithMotion(Entity entity) {
        return false; //TODO allow alignment at a much slower rate once out of fuel
    }

    @Override
    public CompoundNBT serializeNBT()
    {
        final CompoundNBT tagCompound = new CompoundNBT();
        tagCompound.putInt("fuel", fuelTicks);
        return tagCompound;
    }

    @Override
    public void deserializeNBT(CompoundNBT save)
    {
        if(save.contains("fuel")) {
            fuelTicks = save.getInt("fuel");
        }
    }

    @Override
    public <V> V predictPosition(Entity entity, VecBuilderFunc<V> builder, int ticks)
    {
        return builder.apply(
            entity.posX + entity.getMotion().x * ticks, //TODO add gravity
            entity.posY + entity.getMotion().y * ticks,
            entity.posZ + entity.getMotion().z * ticks
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

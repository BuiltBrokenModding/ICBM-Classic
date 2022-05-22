package icbm.classic.content.entity.missile.logic.flight;

import icbm.classic.ICBMConstants;
import icbm.classic.api.missiles.IMissileFlightLogic;
import icbm.classic.api.missiles.IMissileTarget;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

/**
 * Flight computer that does nothing, acts as a placeholder for when we fire missiles like an arrow or are using
 * raw motion setting logic in another system.
 */
public class DeadFlightLogic implements IMissileFlightLogic
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
        return entity.ticksExisted <= fuelTicks;
    }

    @Override
    public NBTTagCompound save()
    {
        final NBTTagCompound tagCompound = new NBTTagCompound();
        tagCompound.setInteger("fuel", fuelTicks);
        return tagCompound;
    }

    @Override
    public void load(NBTTagCompound save)
    {
        fuelTicks = save.getInteger("fuel");
    }

    @Override
    public <V> V predictPosition(Entity entity, VecBuilderFunc<V> builder, int ticks)
    {
        return builder.apply(
            entity.posX + entity.motionX * ticks, //TODO add gravity
            entity.posY + entity.motionY * ticks,
            entity.posZ + entity.motionZ * ticks
        );
    }

    @Override
    public ResourceLocation getRegistryName()
    {
        return REG_NAME;
    }

    @Override
    public boolean shouldDecreaseMotion(Entity entity)
    {
        return !hasFuel(entity);
    }
}

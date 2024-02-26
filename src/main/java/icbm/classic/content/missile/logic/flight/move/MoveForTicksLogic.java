package icbm.classic.content.missile.logic.flight.move;

import icbm.classic.ICBMConstants;
import icbm.classic.api.missiles.IMissile;
import icbm.classic.content.missile.logic.flight.prefab.AccelerateByFacingLogic;
import icbm.classic.lib.saving.NbtSaveHandler;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nonnull;

/**
 * Flight logic to move in a direction matching {@link EnumFacing} until a set distance is meet
 */
public class MoveForTicksLogic extends AccelerateByFacingLogic {
    public static final ResourceLocation REG_NAME = new ResourceLocation(ICBMConstants.DOMAIN, "engine.move.ticks");

    /** Distance to cover */
    @Getter @Setter @Accessors(chain = true)
    private int ticks;

    @Nonnull
    @Override
    public ResourceLocation getRegistryKey() {
        return REG_NAME;
    }

    @Override
    public void onEntityTick(Entity entity, IMissile missile, int ticksInAir) {
        super.onEntityTick(entity, missile, ticksInAir);
        if (ticks >= 0) {
            ticks--;
        }
    }

    @Override
    public boolean isDone() {
        return ticks <= 0;
    }

    @Override
    public boolean canSafelyExitLogic() {
        return super.canSafelyExitLogic();
    }

    @Override
    public NBTTagCompound serializeNBT() {
        return SAVE_LOGIC.save(this, super.serializeNBT());
    }

    @Override
    public void deserializeNBT(NBTTagCompound nbt) {
        super.deserializeNBT(nbt);
        SAVE_LOGIC.load(this, nbt);
    }

    private static final NbtSaveHandler<MoveForTicksLogic> SAVE_LOGIC = new NbtSaveHandler<MoveForTicksLogic>()
        .mainRoot()
        /* */.nodeInteger("ticks", MoveForTicksLogic::getTicks, MoveForTicksLogic::setTicks)
        .base();
}

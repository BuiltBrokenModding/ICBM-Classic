package icbm.classic.world.missile.logic.flight.move;

import icbm.classic.IcbmConstants;
import icbm.classic.api.missiles.IMissile;
import icbm.classic.lib.saving.NbtSaveHandler;
import icbm.classic.world.missile.logic.flight.prefab.AccelerateByFacingLogic;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;

/**
 * Flight logic to move in a direction matching {@link Direction} until a set distance is meet
 */
public class MoveForTicksLogic extends AccelerateByFacingLogic {
    public static final ResourceLocation REG_NAME = new ResourceLocation(IcbmConstants.MOD_ID, "engine.move.ticks");

    /**
     * Distance to cover
     */
    @Getter
    @Setter
    @Accessors(chain = true)
    private int ticks;

    @Override
    public ResourceLocation getRegistryName() {
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
    public CompoundTag serializeNBT() {
        return SAVE_LOGIC.save(this, super.serializeNBT());
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        super.deserializeNBT(nbt);
        SAVE_LOGIC.load(this, nbt);
    }

    private static final NbtSaveHandler<MoveForTicksLogic> SAVE_LOGIC = new NbtSaveHandler<MoveForTicksLogic>()
        .mainRoot()
        /* */.nodeInteger("ticks", MoveForTicksLogic::getTicks, MoveForTicksLogic::setTicks)
        .base();
}

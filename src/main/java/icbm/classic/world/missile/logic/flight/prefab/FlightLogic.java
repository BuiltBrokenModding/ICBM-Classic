package icbm.classic.world.missile.logic.flight.prefab;

import icbm.classic.ICBMClassic;
import icbm.classic.api.ICBMClassicAPI;
import icbm.classic.api.missiles.IMissile;
import icbm.classic.api.missiles.parts.IMissileFlightLogic;
import icbm.classic.api.missiles.parts.IMissileFlightLogicStep;
import icbm.classic.config.ConfigDebug;
import icbm.classic.lib.saving.NbtSaveHandler;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;

import java.util.function.Consumer;

public abstract class FlightLogic implements IMissileFlightLogic, IMissileFlightLogicStep {

    @Getter
    @Setter
    @Accessors(chain = true)
    private IMissileFlightLogic nextStep;

    @Override
    public void start(Entity entity, IMissile missile) {
        if (!isValid()) {
            ICBMClassic.logger().error(this + ": was not setup correctly. Setting flight logic to next step or null to avoid problems. Issue is likely due to custom properties set into the missile.");
            missile.switchFlightLogic(getNextStep());
        }

        if (ConfigDebug.DEBUG_MISSILE_LOGIC) {
            dumpInformation((str) -> ICBMClassic.logger().info(str));
        }
    }

    protected void dumpInformation(Consumer<String> outputLines) {
        outputLines.accept(this + ": Debug Info");
        outputLines.accept("\tNext: " + nextStep);
    }

    @Override
    public void onEntityTick(Entity entity, IMissile missile, int ticksInAir) {
        if (isDone() && !entity.world.isClientSide()) {
            missile.switchFlightLogic(getNextStep());
        }
    }

    @Override
    public <V> V predictPosition(Entity entity, VecBuilderFunc<V> builder, int ticks) {
        return null;
    }

    @Override
    public boolean shouldDecreaseMotion(Entity entity) {
        return !isValid();
    }

    /**
     * Checks if the missile was setup correctly and is in a valid state
     *
     * @return true if invalid and should cause the missile to fail
     */
    public boolean isValid() {
        return true;
    }

    /**
     * Checks if the step is done and we should move to the next
     *
     * @return true if complete
     */
    public abstract boolean isDone();

    @Override
    public CompoundTag serializeNBT() {
        return SAVE_LOGIC.save(this);
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        SAVE_LOGIC.load(this, nbt);
    }

    private static final NbtSaveHandler<FlightLogic> SAVE_LOGIC = new NbtSaveHandler<FlightLogic>()
        .mainRoot()
        /* */.nodeBuildableObject("next", ICBMClassicAPI.MISSILE_FLIGHT_LOGIC_REGISTRY, FlightLogic::getNextStep, FlightLogic::setNextStep)
        .base();
}

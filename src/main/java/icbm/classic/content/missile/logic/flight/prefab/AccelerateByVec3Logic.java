package icbm.classic.content.missile.logic.flight.prefab;

import icbm.classic.content.missile.logic.flight.MotionHelper;

import icbm.classic.api.missiles.IMissile;
import icbm.classic.lib.saving.NbtSaveHandler;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.minecraft.world.entity.Entity;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;

import java.util.function.Consumer;

/**
 * Flight logic to move in a direction vector
 */
public abstract class AccelerateByVec3Logic extends FlightLogic {

    /** Direction to move */
    @Getter @Setter @Accessors(chain = true)
    private Vec3 direction;

    /** Acceleration to move at per tick */
    @Getter @Setter @Accessors(chain = true)
    private double acceleration;

    @Getter
    private double velocityAdded = 0;

    @Override
    protected void dumpInformation(Consumer<String> outputLines) {
        super.dumpInformation(outputLines);
        outputLines.accept("\tDirection: " + direction);
        outputLines.accept("\tAcceleration: " + acceleration);
    }

    @Override
    public void onEntityTick(Entity entity, IMissile missile, int ticksInAir) {
        super.onEntityTick(entity, missile, ticksInAir);
        if(isValid() && !isDone()) {
            // Move missile
            MotionHelper.addMotionX(entity, direction.x * acceleration);
            MotionHelper.addMotionY(entity, direction.y * acceleration);
            MotionHelper.addMotionZ(entity, direction.z * acceleration);

            // Track acceleration added
            velocityAdded += acceleration;
        }
    }

    @Override
    public boolean isValid() {
        return direction != null;
    }

    @Override
    public CompoundTag serializeNBT(HolderLookup.Provider provider) {
        return SAVE_LOGIC.save(this, super.serializeNBT(provider));
    }

    @Override
    public void deserializeNBT(HolderLookup.Provider provider, CompoundTag nbt) {
        super.deserializeNBT(provider, nbt);
        SAVE_LOGIC.load(this, nbt);
    }

    private static final NbtSaveHandler<AccelerateByVec3Logic> SAVE_LOGIC = new NbtSaveHandler<AccelerateByVec3Logic>()
        .mainRoot()
        /* */.nodeDouble("acceleration", AccelerateByVec3Logic::getAcceleration, AccelerateByVec3Logic::setAcceleration)
        /* */.nodeVec3d("direction", AccelerateByVec3Logic::getDirection, AccelerateByVec3Logic::setDirection)
        .base();
}
// PORTED

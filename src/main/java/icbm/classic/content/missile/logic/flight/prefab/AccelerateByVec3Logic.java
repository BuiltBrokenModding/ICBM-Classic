package icbm.classic.content.missile.logic.flight.prefab;

import icbm.classic.api.missiles.IMissile;
import icbm.classic.lib.saving.NbtSaveHandler;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.math.Vec3d;

import java.util.function.Consumer;

/**
 * Flight logic to move in a direction vector
 */
public abstract class AccelerateByVec3Logic extends FlightLogic {

    /** Direction to move */
    @Getter @Setter @Accessors(chain = true)
    private Vec3d direction;

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
            entity.addVelocity(
                direction.x * acceleration,
                direction.y * acceleration,
                direction.z * acceleration
            );

            // Track acceleration added
            velocityAdded += acceleration;
        }
    }

    @Override
    public boolean isValid() {
        return direction != null;
    }

    @Override
    public CompoundNBT serializeNBT() {
        return SAVE_LOGIC.save(this, super.serializeNBT());
    }

    @Override
    public void deserializeNBT(CompoundNBT nbt) {
        super.deserializeNBT(nbt);
        SAVE_LOGIC.load(this, nbt);
    }

    private static final NbtSaveHandler<AccelerateByVec3Logic> SAVE_LOGIC = new NbtSaveHandler<AccelerateByVec3Logic>()
        .mainRoot()
        /* */.nodeDouble("acceleration", AccelerateByVec3Logic::getAcceleration, AccelerateByVec3Logic::setAcceleration)
        /* */.nodeVec3d("direction", AccelerateByVec3Logic::getDirection, AccelerateByVec3Logic::setDirection)
        .base();
}

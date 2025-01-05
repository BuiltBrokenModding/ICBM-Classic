package icbm.classic.content.missile.logic.flight.move;

import icbm.classic.ICBMConstants;
import icbm.classic.api.missiles.IMissile;
import icbm.classic.content.missile.logic.flight.prefab.AccelerateByVec3Logic;
import icbm.classic.lib.saving.NbtSaveHandler;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nonnull;
import java.util.function.Consumer;

/**
 * Flight logic to move in a direction vector
 */
public class MoveByVec3Logic extends AccelerateByVec3Logic {
    public static final ResourceLocation REG_NAME = new ResourceLocation(ICBMConstants.DOMAIN, "engine.move.vector");

    /** Distance to cover */
    @Getter @Setter @Accessors(chain = true)
    private double distance;

    /** When set to true it will track distance relative to the acceleration and not actual amount moved */
    @Getter @Setter @Accessors(chain = true)
    private boolean relative = false;

    @Override
    protected void dumpInformation(Consumer<String> outputLines) {
        super.dumpInformation(outputLines);
        outputLines.accept("\tDistance: " + distance);
        outputLines.accept("\tRelative: " + relative);
    }

    @Nonnull
    @Override
    public ResourceLocation getRegistryKey() {
        return REG_NAME;
    }

    @Override
    public void onEntityTick(Entity entity, IMissile missile, int ticksInAir) {
        super.onEntityTick(entity, missile, ticksInAir);

        // Movement ignoring outside forces
        if(relative) {
            distance -= getVelocityAdded();
        }
        // Measured movement in direction, technically can run forever if something stops movement
        else
        {
            distance -= getDirection().x * entity.getMotion().x;
            distance -= getDirection().y * entity.getMotion().y;
            distance -= getDirection().z * entity.getMotion().z;
        }
    }

    @Override
    public boolean isDone() {
        return distance <= 0;
    }

    @Override
    public boolean canSafelyExitLogic() {
        return super.canSafelyExitLogic();
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

    private static final NbtSaveHandler<MoveByVec3Logic> SAVE_LOGIC = new NbtSaveHandler<MoveByVec3Logic>()
        .mainRoot()
        /* */.nodeDouble("distance", MoveByVec3Logic::getDistance, MoveByVec3Logic::setDistance)
        .base();
}

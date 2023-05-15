package icbm.classic.content.missile.logic.flight.prefab;

import icbm.classic.ICBMClassic;
import icbm.classic.ICBMConstants;
import icbm.classic.api.missiles.IMissile;
import icbm.classic.lib.saving.NbtSaveHandler;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;

import java.util.function.Consumer;

/**
 * Flight logic to move in a direction matching {@link EnumFacing} until a set distance is meet
 */
public abstract class AccelerateByFacingLogic extends FlightLogic {

    /** Direction to move */
    @Getter @Setter @Accessors(chain = true)
    private EnumFacing direction;

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
            entity.motionX += direction.getFrontOffsetX() * acceleration;
            entity.motionY += direction.getFrontOffsetY() * acceleration;
            entity.motionZ += direction.getFrontOffsetZ() * acceleration;

            // Track acceleration added
            velocityAdded += acceleration;
        }
    }

    @Override
    public boolean isValid() {
        return direction != null;
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

    private static final NbtSaveHandler<AccelerateByFacingLogic> SAVE_LOGIC = new NbtSaveHandler<AccelerateByFacingLogic>()
        .mainRoot()
        /* */.nodeDouble("acceleration", AccelerateByFacingLogic::getAcceleration, AccelerateByFacingLogic::setAcceleration)
        /* */.nodeFacing("direction", AccelerateByFacingLogic::getDirection, AccelerateByFacingLogic::setDirection)
        .base();
}

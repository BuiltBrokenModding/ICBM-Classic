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
import java.util.function.Consumer;

/**
 * Flight logic to move in a direction matching {@link EnumFacing} until a set distance is meet
 */
public class MoveByFacingLogic extends AccelerateByFacingLogic {
    public static final ResourceLocation REG_NAME = new ResourceLocation(ICBMConstants.DOMAIN, "engine.move.facing");

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
            distance -= getDirection().getFrontOffsetX() * entity.motionX;
            distance -= getDirection().getFrontOffsetY() * entity.motionY;
            distance -= getDirection().getFrontOffsetZ() * entity.motionZ;
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
    public NBTTagCompound serializeNBT() {
        return SAVE_LOGIC.save(this, super.serializeNBT());
    }

    @Override
    public void deserializeNBT(NBTTagCompound nbt) {
        super.deserializeNBT(nbt);
        SAVE_LOGIC.load(this, nbt);
    }

    private static final NbtSaveHandler<MoveByFacingLogic> SAVE_LOGIC = new NbtSaveHandler<MoveByFacingLogic>()
        .mainRoot()
        /* */.nodeDouble("distance", MoveByFacingLogic::getDistance, MoveByFacingLogic::setDistance)
        .base();
}

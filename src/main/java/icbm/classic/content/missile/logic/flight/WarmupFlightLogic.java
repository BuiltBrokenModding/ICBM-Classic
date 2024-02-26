package icbm.classic.content.missile.logic.flight;

import icbm.classic.ICBMClassic;
import icbm.classic.ICBMConstants;
import icbm.classic.api.missiles.IMissile;
import icbm.classic.content.missile.logic.flight.prefab.FlightLogic;
import icbm.classic.lib.saving.NbtSaveHandler;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nonnull;
import java.util.function.Consumer;

/**
 * Handles engine warmup logic
 */
public class WarmupFlightLogic extends FlightLogic {

    public static final ResourceLocation REG_NAME = new ResourceLocation(ICBMConstants.DOMAIN, "engine.warmup");

    /** Timer for missile to wait on pad before climbing */
    @Getter @Setter @Accessors(chain = true)
    private int timer = 0;

    @Override
    protected void dumpInformation(Consumer<String> outputLines) {
        super.dumpInformation(outputLines);
        outputLines.accept("\tTimer: " + timer);
    }

    @Nonnull
    @Override
    public ResourceLocation getRegistryKey() {
        return REG_NAME;
    }

    @Override
    public void onEntityTick(Entity entity, IMissile missile, int ticksInAir) {
        super.onEntityTick(entity, missile, ticksInAir);
        if (--timer > 0) {
            // Generate engine smoke to simulate a rocket engine starting but forces are not high enough to move up yet
            ICBMClassic.proxy.spawnPadSmoke(entity, this, ticksInAir);
        }
    }

    @Override
    public boolean shouldDecreaseMotion(Entity entity)
    {
        //Disable gravity and friction
        return timer < 0 && getNextStep() == null;
    }

    @Override
    public boolean isDone() {
        return timer <= 0;
    }

    @Override
    public boolean shouldRunEngineEffects(Entity entity) {
        return timer < 0;
    }

    @Override
    public boolean canSafelyExitLogic() {
        return timer < 0;
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

    private static final NbtSaveHandler<WarmupFlightLogic> SAVE_LOGIC = new NbtSaveHandler<WarmupFlightLogic>()
        .mainRoot()
            /* */.nodeInteger("timer", (bl) -> null, (bl, data) -> bl.timer = data)
        .base();
}

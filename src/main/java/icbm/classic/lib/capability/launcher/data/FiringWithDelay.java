package icbm.classic.lib.capability.launcher.data;

import icbm.classic.ICBMConstants;
import icbm.classic.api.ICBMClassicAPI;
import icbm.classic.api.launcher.IActionStatus;
import icbm.classic.api.reg.obj.IBuilderRegistry;
import icbm.classic.content.blocks.launcher.LauncherLangs;
import icbm.classic.content.missile.logic.flight.prefab.FlightLogic;
import icbm.classic.lib.buildable.BuildableObject;
import icbm.classic.lib.saving.NbtSaveHandler;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;

import javax.annotation.Nonnull;

public class FiringWithDelay extends BuildableObject<FiringWithDelay, IBuilderRegistry<IActionStatus>> implements IActionStatus {

    public static final ResourceLocation REG_NAME = new ResourceLocation(ICBMConstants.DOMAIN, "firing.delayed");

    @Getter @Setter(AccessLevel.PROTECTED)
    private int delay;
    private ITextComponent message;

    public FiringWithDelay() {
        super(REG_NAME, ICBMClassicAPI.ACTION_STATUS_REGISTRY, SAVE_LOGIC);
    }

    public FiringWithDelay(int delay) {
        this();
        this.delay = delay;
    }

    @Override
    public boolean isError() {
        return false;
    }

    public boolean shouldBlockInteraction() {
        return true;
    }

    @Override
    public ITextComponent message() {
        if(message == null) {
            message = new TextComponentTranslation(LauncherLangs.STATUS_FIRING_DELAYED, delay);
        }
        return message;
    }

    private static final NbtSaveHandler<FiringWithDelay> SAVE_LOGIC = new NbtSaveHandler<FiringWithDelay>()
        .mainRoot()
        /* */.nodeInteger("delay", FiringWithDelay::getDelay, FiringWithDelay::setDelay)
        .base();
}

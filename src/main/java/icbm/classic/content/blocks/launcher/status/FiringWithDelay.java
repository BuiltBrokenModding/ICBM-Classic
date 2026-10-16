package icbm.classic.content.blocks.launcher.status;

import icbm.classic.ICBMConstants;
import icbm.classic.api.ICBMClassicAPI;
import icbm.classic.api.actions.status.IActionStatus;
import icbm.classic.api.reg.obj.IBuilderRegistry;
import icbm.classic.content.blocks.launcher.LauncherLangs;
import icbm.classic.lib.buildable.BuildableObject;
import icbm.classic.lib.saving.NbtSaveHandler;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.chat.Component;

public class FiringWithDelay extends BuildableObject<FiringWithDelay, IBuilderRegistry<IActionStatus>> implements IActionStatus {

    public static final ResourceLocation REG_NAME = ResourceLocation.fromNamespaceAndPath(ICBMConstants.DOMAIN, "firing.delayed");

    @Getter @Setter(AccessLevel.PROTECTED)
    private int delay;
    private Component message;

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

    public boolean isBlocking() {
        return true;
    }

    @Override
    public Component message() {
        if(message == null) {
            message = Component.translatable(LauncherLangs.STATUS_FIRING_DELAYED, delay);
        }
        return message;
    }

    private static final NbtSaveHandler<FiringWithDelay> SAVE_LOGIC = new NbtSaveHandler<FiringWithDelay>()
        .mainRoot()
        /* */.nodeInteger("delay", FiringWithDelay::getDelay, FiringWithDelay::setDelay)
        .base();
}
// PORTED

package icbm.classic.lib.capability.launcher.data;

import icbm.classic.IcbmConstants;
import icbm.classic.api.launcher.IActionStatus;
import icbm.classic.world.block.launcher.LauncherLangs;
import lombok.NoArgsConstructor;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.text.TextComponentTranslation;

@NoArgsConstructor
public class FiringWithDelay implements IActionStatus {

    public static final ResourceLocation regName = new ResourceLocation(IcbmConstants.MOD_ID, "firing.delayed");

    private int delay;
    private Component message;

    public FiringWithDelay(int delay) {
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
    public Component message() {
        if (message == null) {
            message = new TextComponentTranslation(LauncherLangs.STATUS_FIRING_DELAYED, delay);
        }
        return message;
    }

    @Override
    public ResourceLocation getRegistryName() {
        return regName;
    }

    @Override
    public CompoundTag serializeNBT() {
        final CompoundTag tag = new CompoundTag();
        tag.setInteger("delay", delay);
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        delay = nbt.getInteger("delay");
    }
}

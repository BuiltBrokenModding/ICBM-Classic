package icbm.classic.lib.capability.launcher.data;

import icbm.classic.ICBMConstants;
import icbm.classic.api.launcher.IActionStatus;
import icbm.classic.content.blocks.launcher.LauncherLangs;
import lombok.NoArgsConstructor;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;

@NoArgsConstructor
public class FiringWithDelay implements IActionStatus {

    public static final ResourceLocation regName = new ResourceLocation(ICBMConstants.DOMAIN, "firing.delayed");

    private int delay;
    private ITextComponent message;

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
    public ITextComponent message() {
        if(message == null) {
            message = new TextComponentTranslation(LauncherLangs.STATUS_FIRING_DELAYED, delay);
        }
        return message;
    }

    @Override
    public ResourceLocation getRegistryName() {
        return regName;
    }

    @Override
    public NBTTagCompound serializeNBT() {
        final NBTTagCompound tag = new NBTTagCompound();
        tag.setInteger("delay", delay);
        return tag;
    }

    @Override
    public void deserializeNBT(NBTTagCompound nbt) {
        delay = nbt.getInteger("delay");
    }
}

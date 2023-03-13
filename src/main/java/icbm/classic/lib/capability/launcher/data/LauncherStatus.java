package icbm.classic.lib.capability.launcher.data;

import icbm.classic.ICBMConstants;
import icbm.classic.api.ICBMClassicAPI;
import icbm.classic.api.launcher.IActionStatus;
import icbm.classic.api.reg.obj.IBuilderRegistry;
import icbm.classic.content.blocks.launcher.LauncherLangs;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;


@NoArgsConstructor
public class LauncherStatus implements IActionStatus {

    public static final LauncherStatus ERROR_GENERIC = new LauncherStatus().withRegName("error.generic").asError().withTranslation(LauncherLangs.ERROR);
    public static final LauncherStatus ERROR_SPAWN = new LauncherStatus().withRegName("error.spawning").asError().withTranslation(LauncherLangs.ERROR_MISSILE_SPAWNING);
    public static final LauncherStatus ERROR_MIN_RANGE = new LauncherStatus().withRegName("error.range.min").asError().withTranslation(LauncherLangs.ERROR_TARGET_MIN); //TODO use factory to provide range
    public static final LauncherStatus ERROR_MAX_RANGE = new LauncherStatus().withRegName("error.range.max").asError().withTranslation(LauncherLangs.ERROR_TARGET_MAX);
    public static final LauncherStatus ERROR_TARGET_NULL = new LauncherStatus().withRegName("error.target.null").asError().withTranslation(LauncherLangs.ERROR_TARGET_NONE);
    public static final LauncherStatus ERROR_POWER = new LauncherStatus().withRegName("error.power").asError().withTranslation(LauncherLangs.ERROR_NO_POWER);
    public static final LauncherStatus ERROR_INVALID_STACK = new LauncherStatus().withRegName("error.missile.invalid").asError().withTranslation(LauncherLangs.ERROR_MISSILE_INVALID);
    public static final LauncherStatus ERROR_EMPTY_STACK = new LauncherStatus().withRegName("error.missile.empty").asError().withTranslation(LauncherLangs.ERROR_MISSILE_NONE);

    public static final LauncherStatus LAUNCHED = new LauncherStatus().withRegName("launched").withTranslation(LauncherLangs.STATUS_LAUNCHED);
    public static final LauncherStatus CANCELED = new LauncherStatus().withRegName("canceled").withTranslation(LauncherLangs.STATUS_CANCELED);

    private boolean error = false;
    private String message;
    private ITextComponent textComponent;
    private ResourceLocation regName;

    public LauncherStatus asError() {
        this.error = true;
        return this;
    }

    public LauncherStatus withTranslation(String key) {
        this.message = key;
        return this;
    }

    public LauncherStatus withRegName(String key) {
        return withRegName(ICBMConstants.DOMAIN, key);
    }

    public LauncherStatus withRegName(String domain, String key) {
        this.regName = new ResourceLocation(domain, key);
        return this;
    }

    @Override
    public boolean isError() {
        return error;
    }

    @Override
    public ITextComponent message() {
        if(textComponent == null) {
            textComponent = new TextComponentTranslation(message);
        }
        return textComponent;
    }

    @Override
    public ResourceLocation getRegistryName() {
        return regName;
    }

    @Override
    public NBTTagCompound serializeNBT() {
        return null;
    }

    @Override
    public void deserializeNBT(NBTTagCompound nbt) {

    }

    public static void registerTypes() {

        register(ERROR_GENERIC);
        register(ERROR_SPAWN);
        register(ERROR_MIN_RANGE);
        register(ERROR_MAX_RANGE);
        register(ERROR_TARGET_NULL);
        register(ERROR_POWER);
        register(ERROR_INVALID_STACK);
        register(ERROR_EMPTY_STACK);
        register(LAUNCHED);
        register(CANCELED);
    }

    private static void register(LauncherStatus constantStatus) {
        ICBMClassicAPI.ACTION_STATUS_REGISTRY.register(constantStatus.getRegistryName(), () -> constantStatus);
    }
}

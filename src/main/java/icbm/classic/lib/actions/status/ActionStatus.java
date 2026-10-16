package icbm.classic.lib.actions.status;

import icbm.classic.ICBMConstants;
import icbm.classic.api.actions.status.IActionStatus;
import lombok.NoArgsConstructor;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.chat.Component;

import javax.annotation.Nonnull;

/**
 * Generic status messages that provide no details and are cached as static immutable versions
 */
@NoArgsConstructor
public class ActionStatus implements IActionStatus {

    private boolean error = false;
    private boolean block = false;
    protected String translationKey;
    protected Component textComponent;
    private ResourceLocation regName;

    public ActionStatus asError() {
        this.error = true;
        return this;
    }

    public ActionStatus asBlocking() {
        this.block = true;
        return this;
    }

    public ActionStatus withTranslation(String key) {
        this.translationKey = key;
        return this;
    }

    public ActionStatus withRegName(String key) {
        return withRegName(ICBMConstants.DOMAIN, key);
    }

    public ActionStatus withRegName(String domain, String key) {
        this.regName = ResourceLocation.fromNamespaceAndPath(domain, key);
        return this;
    }

    @Override
    public boolean isError() {
        return error;
    }

    @Override
    public boolean isBlocking() {
        return isError() || block;
    }

    @Override
    public Component message() {
        if(textComponent == null) {
            textComponent = Component.translatable(translationKey);
        }
        return textComponent;
    }

    @Nonnull
    @Override
    public ResourceLocation getRegistryKey() {
        return regName;
    }

    @Override
    public String toString() {
        return "ActionStatus[ '" + getRegistryKey() + "' , '" + translationKey + "' ]@" + hashCode();
    }
}
// PORTED

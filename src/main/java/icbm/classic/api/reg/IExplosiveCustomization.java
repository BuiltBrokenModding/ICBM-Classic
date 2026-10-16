package icbm.classic.api.reg;

import icbm.classic.api.explosion.IBlast;
import icbm.classic.api.reg.obj.IBuildableObject;

import java.util.function.Consumer;

/**
 * Way to customize how a blast is built before it is spawned into the world
 *
 * @deprecated replaced by {@link icbm.classic.api.actions.data.IActionFieldProvider}
 */
public interface IExplosiveCustomization extends IBuildableObject {

    /**
     * Called to apply the settings
     *
     * Use instanceof checks to match on setters in the blast object.
     *
     * @param explosiveData used to create the blast instance
     * @param blast instance created
     */
    void apply(IExplosiveData explosiveData, IBlast blast);

    /**
     * Collects customization information for display in item tooltips
     * and user interfaces.
     *
     * @param collector to pass data into
     */
    default void collectCustomizationInformation(Consumer<String> collector) {

    }
}
// PORTED

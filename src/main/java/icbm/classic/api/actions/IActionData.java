package icbm.classic.api.actions;

import icbm.classic.api.ICBMClassicAPI;
import icbm.classic.api.actions.cause.IActionSource;
import icbm.classic.api.actions.data.ActionField;
import icbm.classic.api.actions.data.IActionFieldProvider;
import icbm.classic.api.data.meta.ITypeTaggable;
import icbm.classic.api.reg.obj.IBuildableObject;
import icbm.classic.api.reg.obj.IBuilderRegistry;
import net.minecraft.world.level.Level;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;

/**
 * Registry data used to create actions in a world position
 */
public interface IActionData extends IBuildableObject, ITypeTaggable {

    /**
     * Creates a new action
     *
     * @param world to create action in
     * @param x location
     * @param y location
     * @param z location
     * @param source of the action, isn't always the same as position
     * @param fieldAccessor to get customization data, this is only used by data layer to generate an action
     *
     * @return action
     */
    @Nonnull
    IAction create(Level world, double x, double y, double z, @Nonnull IActionSource source, @Nullable IActionFieldProvider fieldAccessor);

    /**
     * List of supported fields for use in {@link IActionFieldProvider}
     *
     * @return immutable list, defaults to empty
     */
    @Nullable
    default List<ActionField> getSupportedFields() {
        return Collections.EMPTY_LIST;
    }

    @Nonnull
    @Override
    default IBuilderRegistry getRegistry() {
        return ICBMClassicAPI.ACTION_REGISTRY;
    }
}
// PORTED

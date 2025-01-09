package icbm.classic.api.actions.data;


import net.minecraft.nbt.INBT;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Collections;

/**
 * Wrapper to access fields dynamically. Keeps action instances seperated from caller. Allowing dynamic generation
 * and switching based on data provided. To figure out which keys to use see documentation for each IActionData registered.
 *
 * It is recommended to cache this accessor in each action user. For example, if the action user is a TileEntity then a field
 * should be created as a wrapper/adapter pattern. With the field access being redirected to internal fields and functions. Using
 * lambda expressions to ensure the values are accessed with minimal performance impact.
 *
 */
public interface IActionFieldProvider {

    /**
     * Gets a valued assigned to a field by key. If you need a specific
     * type without the risk call one of the other functions. As the
     * accessor instance may provide data in a safer way.
     *
     * @param key to lookup
     * @return value
     * @param <VALUE> to allow generic assign, this isn't checked so have fun
     */
    default <VALUE, TAG extends INBT> VALUE getValue(ActionField<VALUE, TAG> key) {
        return null;
    }

    /**
     * Locates a field
     *
     * @param key  to match to field
     * @param type to match, can be null to get first type match
     * @return field
     */
    default ActionField getField(String key, @Nullable Type type) {
        return getFields().stream().filter(field -> field.getKey().equals(key) && (type == null || field.getType() == type)).findFirst().orElse(null);
    }

    /**
     * List of fields provided or supported
     *
     * @return immutable fields, defaults to empty
     */
    @Nonnull
    default Collection<ActionField> getFields() {
        return Collections.EMPTY_LIST;
    }

    /**
     * Has values for the given field
     *
     * @param key to use for lookups
     * @return true if has the field
     */
    default <VALUE, TAG extends INBT> boolean hasField(ActionField<VALUE, TAG> key) {
        return getFields().contains(key);
    }
}

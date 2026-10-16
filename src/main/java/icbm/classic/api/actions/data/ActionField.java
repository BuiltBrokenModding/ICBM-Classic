package icbm.classic.api.actions.data;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import net.minecraft.nbt.Tag;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Metadata about dynamic fields
 */
@Data
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ActionField<VALUE, TAG extends Tag> {

    private static Map<String, ActionField> fields = new HashMap<>();

    private final String key;
    private final Type type;
    private final Function<VALUE, TAG> save;
    private final Function<TAG, VALUE> load;

    public static <VALUE, TAG extends Tag> ActionField<VALUE, TAG> getOrCreate(String key, Type type, Function<VALUE, TAG> save, Function<TAG, VALUE> load) {
        final ActionField field = find(key, null);
        if(field != null && field.type != type) {
            throw new IllegalArgumentException("Key " + key + " was requested with different type " + type + " when it already uses " + field.type);
        }
        return fields.computeIfAbsent(key, (k) -> new ActionField<>(key, type, save, load));
    }

    public static <VALUE, TAG extends Tag> ActionField<VALUE, TAG> find(String key, Type type) {
        final ActionField field = fields.get(key);
        if(field != null && (type == null || field.type == type)) {
            return field;
        }
        return null;
    }

    public VALUE cast(Object v) {
        return (VALUE) v;
    }

    public TAG save(VALUE v) {
        return save != null ? save.apply(v): null;
    }

    public VALUE load(TAG v) {
        return load != null ? load.apply(v): null;
    }
}
// PORTED

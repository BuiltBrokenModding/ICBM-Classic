package icbm.classic.lib.actions.fields;

import icbm.classic.api.actions.data.ActionField;
import icbm.classic.api.actions.data.IActionFieldProvider;
import lombok.NoArgsConstructor;
import net.minecraft.nbt.INBT;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

@NoArgsConstructor
public final class ActionFieldProvider implements IActionFieldProvider {
    private final Map<ActionField, Supplier> fieldAccessors = new HashMap<>();

    public ActionFieldProvider(IActionFieldProvider provider) {
        provider.getFields().stream().forEach(field -> {
            fieldAccessors.put(field, () -> provider.getValue(field));
        });
    }

    public <VALUE, TAG extends INBT> ActionFieldProvider field(ActionField<VALUE, TAG> field, Supplier<VALUE> accessor) {
        fieldAccessors.put(field, accessor);
        return this;
    }

    @Override
    public <VALUE, TAG extends INBT> VALUE getValue(ActionField<VALUE, TAG> key) {
        Supplier<VALUE> supplier = fieldAccessors.get(key);
        if(supplier == null) {
            return null;
        }
        return supplier.get();
    }

    @Override
    public <VALUE, TAG extends INBT> boolean hasField(ActionField<VALUE, TAG> key) {
        return fieldAccessors.containsKey(key);
    }

    @Nonnull
    public Collection<ActionField> getFields() {
        return fieldAccessors.keySet();
    }
}

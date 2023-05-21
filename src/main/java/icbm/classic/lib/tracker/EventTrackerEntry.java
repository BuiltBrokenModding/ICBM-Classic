package icbm.classic.lib.tracker;

import lombok.Data;
import net.minecraft.util.ResourceLocation;

import java.util.Optional;

/**
 * Single entry of a tracker event
 */
@Data
public class EventTrackerEntry {
    private final EventTrackerType type;
    private final Object[] data;

    public <DATA> DATA get(ResourceLocation key) {
        return get(key, null);
    }
    public <DATA> DATA get(ResourceLocation key, DATA placeholder) {
        return (DATA) Optional.ofNullable(type.getField(key).get(this)).orElse(placeholder);
    }

    public String getString(ResourceLocation key, String placeholder) {
        return Optional.ofNullable(type.getField(key).get(this)).map(Object::toString).orElse(placeholder);
    }
}

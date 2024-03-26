package icbm.classic.lib.tracker;

import com.google.common.collect.ImmutableList;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Event type, used to encode/decode information about the event.
 */
public class EventTrackerType {
    /**
     * Unique id of the tracker
     */
    @Setter(value = AccessLevel.PACKAGE)
    @Getter
    private int id;

    /**
     * Registry name of the type
     */
    @Getter
    private final ResourceLocation name;

    @Getter
    @Accessors(fluent = true)
    private final boolean isError;

    @Getter
    @Accessors(fluent = true)
    private final boolean isWarn;

    /**
     * Column data for the event
     */
    private final List<EventTrackerField> fields;

    @Getter
    private final List<ListenerEntry> listeners;

    /**
     * key to value map for column data
     */
    private final HashMap<ResourceLocation, EventTrackerField> keyToField = new HashMap();

    private EventTrackerType(ResourceLocation name, boolean isError, boolean isWarn, List<EventTrackerField> fields, List<ListenerEntry> listeners) {
        this.name = name;
        this.isError = isError;
        this.isWarn = isWarn;

        this.fields = fields;
        this.fields.forEach(field -> {
            keyToField.put(field.getKey(), field);
        });

        this.listeners = listeners;
    }


    /**
     * Gets the field by id
     *
     * @param index  of the field
     * @param <DATA> type to auto cast, type isn't checked so pay attention
     * @return field instance
     */
    public <DATA> EventTrackerField<DATA> getField(int index) {
        return fields.get(index);
    }

    /**
     * Gets the field by key
     *
     * @param key    of the field
     * @param <DATA> type to auto cast, type isn't checked so pay attention
     * @return field instance
     */
    public <DATA> EventTrackerField<DATA> getField(ResourceLocation key) {
        return keyToField.get(key);
    }

    @Override
    public String toString() {
        return String.format("%s(%s, %s)", getClass().getName(), id, name);
    }

    public static final class Builder {
        private final ResourceLocation name;
        private boolean isError = false;
        private boolean isWarn = false;
        private List<EventTrackerField> fields = new ArrayList<>();
        private List<ListenerEntry> listeners = new ArrayList<>();

        public Builder(ResourceLocation name) {
            this.name = name;
        }

        public Builder asError() {
            this.isError = true;
            this.isWarn = false;
            return this;
        }

        public Builder asWarn() {
            this.isError = false;
            this.isWarn = true;
            return this;
        }

        public Builder with(EventTrackerField field) {
            fields.add(field);
            return this;
        }

        public Builder with(ResourceLocation key, Type type, Function<EventTrackerEntry, Object> accessor) {
            return with(new EventTrackerField<Object>(key, type, accessor));
        }

        public Builder withString(ResourceLocation key, Function<EventTrackerEntry, String> accessor) {
            return with(new EventTrackerField<String>(key, String.class, accessor));
        }

        public Builder withInt(ResourceLocation key, Function<EventTrackerEntry, Integer> accessor) {
            return with(new EventTrackerField<Integer>(key, Integer.class, accessor));
        }

        public Builder withDouble(ResourceLocation key, Function<EventTrackerEntry, Double> accessor) {
            return with(new EventTrackerField<Double>(key, Double.class, accessor));
        }

        public Builder withBlockPos(ResourceLocation prefix, Function<EventTrackerEntry, BlockPos> accessor) {
            return withInt(suffix(prefix, "x"),
                (entry) -> Optional.ofNullable(accessor.apply(entry))
                    .map(BlockPos::getX).orElse(null))
                .withInt(suffix(prefix, "y"),
                    (entry) -> Optional.ofNullable(accessor.apply(entry))
                        .map(BlockPos::getY).orElse(null))
                .withInt(suffix(prefix, "z"),
                    (entry) -> Optional.ofNullable(accessor.apply(entry))
                        .map(BlockPos::getZ).orElse(null));
        }

        public Builder withVec3(ResourceLocation prefix, Function<EventTrackerEntry, Vec3> accessor) {
            return withDouble(suffix(prefix, "x"),
                (entry) -> Optional.ofNullable(accessor.apply(entry))
                    .map(v -> v.x).orElse(null))
                .withDouble(suffix(prefix, "y"),
                    (entry) -> Optional.ofNullable(accessor.apply(entry))
                        .map(v -> v.y).orElse(null))
                .withDouble(suffix(prefix, "z"),
                    (entry) -> Optional.ofNullable(accessor.apply(entry))
                        .map(v -> v.z).orElse(null));
        }

        private ResourceLocation suffix(ResourceLocation prefix, String suffix) {
            return new ResourceLocation(prefix.getResourceDomain(), prefix.getResourcePath() + "." + suffix);
        }

        public Builder listen(ResourceLocation name, IEventTrackerListener listener) {
            this.listeners.add(new ListenerEntry(name, listener));
            return this;
        }

        public Builder listen(ResourceLocation name, Supplier<Boolean> conditional, IEventTrackerListener listener) {
            this.listeners.add(new ListenerEntry(name, listener));
            return this;
        }

        public EventTrackerType build() {
            final List<EventTrackerField> fields = ImmutableList.copyOf(this.fields); //TODO maybe sort by key?
            final List<ListenerEntry> listeners = ImmutableList.copyOf(this.listeners);
            final EventTrackerType type = new EventTrackerType(name, isError, isWarn, fields, listeners);
            for (int i = 0; i < fields.size(); i++) {
                fields.get(i).setIndex(i);
                fields.get(i).setParent(type);
            }
            //TODO fire event to allow adding custom columns and listeners
            return type;
        }
    }
}

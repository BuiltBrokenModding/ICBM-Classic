package icbm.classic.lib.tracker;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.util.ResourceLocation;

import java.lang.reflect.Type;
import java.util.Optional;
import java.util.function.Function;

/**
 * Column within an {@link EventTrackerType} to allow supplying
 * name of each field, how to access the data, and event utilities
 * to translate.
 *
 * @param <DATA> to provide from ane entry
 */
public class EventTrackerField<DATA> {

    /**
     * Unique name, within {@link EventTrackerType}, to identify this column.
     *
     * Is also used to generate visual information in various applications.
     * Will prefix 'event' plus {@link EventTrackerType#name} and suffix type.
     *
     * If domain matches {@link EventTrackerType#name} then it will be ignored.
     * Otherwise, it will be added to the translation key. Allowing for tracking
     * of which mod added the column and to avoid collisions in the translation file.
     *
     * Example:
     *  type#name -> icbm:missile.systems.flight.on_switch
     *  this#key -> icbm:component.new
     *  columnName -> event.icbm:missile.systems.flight.switch.component.new.column = New Logic
     *  column -> event.icbm:missile.systems.flight.switch.component.new.sql=new_type
     */
    @Getter
    private final ResourceLocation key;

    /** Data type of the field, used by other systems to understand how to consume the output of {@link #accessor} */
    @Getter
    private final Type type;

    /**
     * Index of the column in the list, this isn't the same as data index. As some columns
     * pull from the same data index or may combine data.
     */
    @Getter @Setter(value = AccessLevel.PACKAGE)
    private int index;

    @Getter @Setter(value = AccessLevel.PACKAGE)
    private EventTrackerType parent;

    /**
     * Logic to pull the data from the event entry
     */
    private final Function<EventTrackerEntry, DATA> accessor;

    public EventTrackerField(ResourceLocation key, Type type, Function<EventTrackerEntry, DATA> accessor) {
        this.key = key;
        this.type = type;
        this.accessor = accessor;
    }
    public DATA get(EventTrackerEntry entry) {
        return Optional.ofNullable(accessor).map(f -> f.apply(entry)).orElse(null);
    }

    /**
     * Creates a new copy for use in a different {@link EventTrackerType} useful
     * when building repeat data. Such as player name or location information.
     *
     * @return copy of only final fields.
     */
    public EventTrackerField<DATA> copy() {
        return new EventTrackerField<>(key, type, accessor);
    }
}

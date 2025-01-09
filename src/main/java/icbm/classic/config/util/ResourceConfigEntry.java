package icbm.classic.config.util;

import lombok.Data;
import lombok.experimental.Accessors;
import net.minecraft.util.ResourceLocation;

import java.util.function.Function;

@Data
public class ResourceConfigEntry<CONTENT, VALUE> implements Function<CONTENT, VALUE> {

    /** Type description, mostly exists for debugging */
    private String type;
    /** Sort order */
    private Integer order;
    /** Function used to match the content input to the value output, returns null if doesn't match */
    private Function<CONTENT, VALUE> function;

    @Accessors(chain = true)
    private ResourceLocation key;

    public ResourceConfigEntry(String type, Integer order, Function<CONTENT, VALUE> function) {
        this.type = type;
        this.order = order;
        this.function = function;
    }

    @Override
    public VALUE apply(CONTENT content) {
        return function.apply(content);
    }
}

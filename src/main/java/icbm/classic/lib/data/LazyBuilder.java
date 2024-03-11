package icbm.classic.lib.data;

import lombok.Data;

import java.util.function.Supplier;

@Data
public class LazyBuilder<T> implements Supplier<T> {

    private final Supplier<T> builder;
    private T thing;

    @Override
    public T get() {
        if(thing == null) {
            thing = builder.get();
        }
        return thing;
    }
}

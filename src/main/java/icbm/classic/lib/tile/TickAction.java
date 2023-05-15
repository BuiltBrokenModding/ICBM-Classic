package icbm.classic.lib.tile;

import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

@Data
@RequiredArgsConstructor
public class TickAction implements ITick {

    public final BiFunction<Integer, Boolean, Boolean> shouldRun;
    public final Consumer<Integer> action;

    public TickAction(int ticks, Consumer<Integer> action) {
        this.shouldRun = (t, server) -> t % ticks == 0;
        this.action = action;
    }

    public TickAction(int ticks, boolean isServer, Consumer<Integer> action) {
        this.shouldRun = (t, server) -> t % ticks == 0 && server == isServer;
        this.action = action;
    }

    public TickAction(int ticks, boolean isServer, Runnable action) {
        this.shouldRun = (t, server) -> t % ticks == 0 && server == isServer;
        this.action = (t) -> action.run();
    }

    public TickAction(Supplier<Boolean> shouldRun, Runnable action) {
        this.shouldRun = (t, server) -> shouldRun.get();
        this.action = (t) -> action.run();
    }

    @Override
    public void update(int tick, boolean isServer) {
        if(shouldRun.apply(tick, isServer)) {
            action.accept(tick);
        }
    }
}

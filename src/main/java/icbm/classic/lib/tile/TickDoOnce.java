package icbm.classic.lib.tile;

import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.util.function.Consumer;

@Data
@RequiredArgsConstructor
public class TickDoOnce implements ITick {

    private boolean doAction = false;
    private final Consumer<Integer> action;

    @Override
    public void update(int tick, boolean isServer) {
        if(doAction) {
            this.doAction = false;
            action.accept(tick);
        }
    }

    public void doNext() {
        this.doAction = true;
    }
}

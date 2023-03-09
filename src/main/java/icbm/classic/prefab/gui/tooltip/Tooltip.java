package icbm.classic.prefab.gui.tooltip;

import icbm.classic.lib.transform.region.Rectangle;
import icbm.classic.prefab.gui.IGuiComponent;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.function.Supplier;

@Data
@AllArgsConstructor
public class Tooltip implements IToolTip, IGuiComponent {

    private final Rectangle bounds;
    private final Supplier<String> tooltip;

    @Override
    public boolean isWithin(int x, int y) {
        return bounds.isWithin(x, y);
    }

    @Override
    public String getTooltip() {
        return tooltip.get();
    }
}

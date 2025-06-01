package icbm.classic.prefab.gui.tooltip;

import icbm.classic.lib.LanguageUtility;
import icbm.classic.prefab.gui.GuiContainerBase;
import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.fml.client.config.GuiUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

/**
 * Simple tooltip component for showing users additional information
 */
public abstract class TooltipBase<GUI extends GuiContainerBase> extends Widget {

    /**
     * Delay in seconds to wait to show tooltip
     */
    @Getter
    private float hoverDelay;
    /**
     * Current hover tick in seconds
     */
    private float hoveringTicks = 0;

    /** Current tooltip */
    private ITextComponent currentTooltip;

    protected final GUI host;

    public TooltipBase(GUI host, int x, int y, int width, int height) {
        super(x, y, width, height, "");
        this.host = host;
    }

    public TooltipBase withDelay(float delay) {
        this.hoverDelay = delay;
        return this;
    }

    @Override
    public void renderButton(int mouseX, int mouseY, float partialTicks) {
        if (!this.isHovered()) {
            this.hoveringTicks = 0;
            return;
        }
        this.hoveringTicks += partialTicks;

        if (hoveringTicks < hoverDelay) {
            return;
        }

        this.currentTooltip = this.getTooltip();

        // Render current tooltip if not empty
        final String currentTooltipText = this.currentTooltip == null ? "" : this.currentTooltip.getFormattedText();
        if (!StringUtils.isEmpty(currentTooltipText))
        {
            final List<String> lines = LanguageUtility.splitByLine(currentTooltipText);
            GuiUtils.drawHoveringText(lines,
                mouseX - host.getGuiLeft(),
                mouseY - host.getGuiTop(),
                host.width,
                host.height,
                -1, Minecraft.getInstance().fontRenderer);
        }

        // Dev debug to see tooltip area TODO make toggle driven via hotkey
        /*AbstractGui.fill(
            x + container.getGuiLeft(),
            y + container.getGuiTop(),
            x + width + container.getGuiLeft(),
            y + width + container.getGuiTop(),
            -6250336);*/
    }

    protected abstract ITextComponent getTooltip();
}

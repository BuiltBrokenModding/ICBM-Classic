package icbm.classic.content.blocks.radarstation.gui;

import icbm.classic.content.blocks.radarstation.data.RadarDotType;
import icbm.classic.content.blocks.radarstation.data.RadarRenderData;
import icbm.classic.content.blocks.radarstation.data.RadarRenderDot;
import icbm.classic.lib.colors.ColorHelper;
import icbm.classic.prefab.gui.ITickingWidget;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.widget.Widget;

import java.util.List;

public class RadarComponent extends Widget implements ITickingWidget {

    final int MARKER_COLOR = ColorHelper.toARGB(0, 255, 0, 255);
    final int HOSTILE_COLOR = ColorHelper.toARGB(255, 255, 0, 255);
    final int INCOMING_COLOR = ColorHelper.toARGB(255, 0, 0, 255);
    final int TRIGGER_RANGE = ColorHelper.toARGB(255, 0, 255, 255);

    private final GuiRadarStation host;

    int meterSpacing = 0;

    public RadarComponent(GuiRadarStation host, int x, int y) {
        super(x, y, RadarRenderData.UV_SIZE + 1, RadarRenderData.UV_SIZE + 1, "");
        this.host = host;
    }

    @Override
    public void update() {
        //TODO consider center grid to chunk bounds
        meterSpacing = (int)Math.floor((this.host.getContainer().getHost().getDetectionRange() / (float)100) * 40); // TODO magic numbers
    }

    @Override
    public void renderButton(int mouseX, int mouseY, float f) {
        this.drawForegroundLayer(mouseX, mouseY);
        this.drawBackgroundLayer(f, mouseX, mouseY);
    }

    public void drawForegroundLayer(int mouseX, int mouseY) {

        final List<RadarRenderDot> dots = this.host.getContainer().getHost().getRadarRenderData().getDots();

        drawString(this.host.getMinecraft().fontRenderer, String.format("%dm", meterSpacing), x + 56, y + 46, MARKER_COLOR);
        drawString(this.host.getMinecraft().fontRenderer, String.format("%d", dots.stream().filter(d -> d.getType() == RadarDotType.HOSTILE).count()), x + 56, y + 2, HOSTILE_COLOR);
        drawString(this.host.getMinecraft().fontRenderer, String.format("%d", dots.stream().filter(d -> d.getType() == RadarDotType.INCOMING).count()), x + 56, y + 14, INCOMING_COLOR);
    }

    public void drawBackgroundLayer(float f, int mouseX, int mouseY) {

        final int halfUV = RadarRenderData.UV_SIZE / 2;
        final int left = this.host.getGuiLeft() + x;
        final int top = this.host.getGuiTop() + y;

        // Background
        AbstractGui.fill(left, top, left + RadarRenderData.UV_SIZE + 1, top + RadarRenderData.UV_SIZE + 1, -16777216);


        final int gx = left + halfUV;
        final int gy = top + halfUV;

        // Grid lines
        int lineCount = (int) Math.floor(this.host.getContainer().getHost().getDetectionRange() / (float)meterSpacing) * 2 + 1;
        float lineSpacing = (meterSpacing / (float)this.host.getContainer().getHost().getDetectionRange()) * halfUV;

        final int lx = gx - (int)((lineCount / 2) * lineSpacing);
        final int ly = gy - (int)((lineCount / 2) * lineSpacing);

        for(int i = 0; i < lineCount; i++) {
            final int x = lx + (int)Math.floor(i * lineSpacing);
            AbstractGui.fill(x, gy + halfUV, x + 1, gy - halfUV, MARKER_COLOR);
        }

        for(int i = 0; i < lineCount; i++) {
            final int y = ly + (int)Math.floor(i * lineSpacing);
            AbstractGui.fill(gx + halfUV, y, gx - halfUV, y + 1, MARKER_COLOR);
        }

        // Trigger area
        int triggerRange = (int)Math.ceil((this.host.getContainer().getHost().getTriggerRange() / (float)this.host.getContainer().getHost().getDetectionRange()) * halfUV);

        // Trigger bottom line TODO could render as edges as a square then background then lines?
        AbstractGui.fill(gx - triggerRange, gy + triggerRange, gx + triggerRange, gy + triggerRange + 1, TRIGGER_RANGE);

        // Trigger top line
        AbstractGui.fill(gx - triggerRange, gy - triggerRange -1, gx + triggerRange, gy - triggerRange, TRIGGER_RANGE);

        // Trigger left line
        AbstractGui.fill(gx - triggerRange - 1, gy - triggerRange, gx - triggerRange, gy + triggerRange, TRIGGER_RANGE);

        // Trigger right line
        AbstractGui.fill(gx + triggerRange, gy - triggerRange, gx + triggerRange + 1, gy + triggerRange, TRIGGER_RANGE);

        // Target data

        for(int i = 0 ; i < this.host.getContainer().getHost().getRadarRenderData().getDots().size(); i++) {
            final RadarRenderDot dot = this.host.getContainer().getHost().getRadarRenderData().getDots().get(i);

            final int x = gx + dot.getX();
            final int y = gy + dot.getY();
            if(dot.getType() == RadarDotType.MARKER) {
                AbstractGui.fill(x, y, x + 1, y + 1, MARKER_COLOR);
            }
            else if(dot.getType() == RadarDotType.HOSTILE) {
                AbstractGui.fill(x, y, x + 2, y + 2, HOSTILE_COLOR);
            }
            else if(dot.getType() == RadarDotType.INCOMING) {
                AbstractGui.fill(x, y, x + 2, y + 2, INCOMING_COLOR);
            }
        }
    }
}

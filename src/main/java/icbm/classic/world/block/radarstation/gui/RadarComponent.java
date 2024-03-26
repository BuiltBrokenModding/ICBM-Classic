package icbm.classic.world.block.radarstation.gui;

import icbm.classic.lib.colors.ColorHelper;
import icbm.classic.prefab.gui.GuiContainerBase;
import icbm.classic.prefab.gui.IGuiComponent;
import icbm.classic.world.block.radarstation.TileRadarStation;
import icbm.classic.world.block.radarstation.data.RadarDotType;
import icbm.classic.world.block.radarstation.data.RadarRenderData;
import icbm.classic.world.block.radarstation.data.RadarRenderDot;
import net.minecraft.client.gui.Gui;

import java.util.List;

public class RadarComponent implements IGuiComponent {

    final int MARKER_COLOR = ColorHelper.toARGB(0, 255, 0, 255);
    final int HOSTILE_COLOR = ColorHelper.toARGB(255, 255, 0, 255);
    final int INCOMING_COLOR = ColorHelper.toARGB(255, 0, 0, 255);
    final int TRIGGER_RANGE = ColorHelper.toARGB(255, 0, 255, 255);

    private final TileRadarStation tile;
    private final int x;
    private final int y;

    private GuiContainerBase container;

    int meterSpacing = 0;

    public RadarComponent(TileRadarStation tile, int x, int y) {
        this.tile = tile;
        this.x = x;
        this.y = y;
    }

    @Override
    public void onAddedToHost(GuiContainerBase container) {
        this.container = container;
    }

    @Override
    public void onUpdate() {
        meterSpacing = (int) Math.floor((this.tile.getDetectionRange() / (float) 100) * 40); //TODO consider center grid to chunk bounds
    }

    @Override
    public void drawForegroundLayer(int mouseX, int mouseY) {

        final List<RadarRenderDot> dots = this.tile.getRadarRenderData().getDots();

        container.drawString(container.mc.fontRenderer, String.format("%dm", meterSpacing), x + 56, y + 46, MARKER_COLOR);
        container.drawString(container.mc.fontRenderer, String.format("%d", dots.stream().filter(d -> d.getType() == RadarDotType.HOSTILE).count()), x + 56, y + 2, HOSTILE_COLOR);
        container.drawString(container.mc.fontRenderer, String.format("%d", dots.stream().filter(d -> d.getType() == RadarDotType.INCOMING).count()), x + 56, y + 14, INCOMING_COLOR);
    }

    @Override
    public void drawBackgroundLayer(float f, int mouseX, int mouseY) {

        final int halfUV = RadarRenderData.UV_SIZE / 2;
        final int left = container.getGuiLeft() + x;
        final int top = container.getGuiTop() + y;

        // Background
        Gui.drawRect(left, top, left + RadarRenderData.UV_SIZE + 1, top + RadarRenderData.UV_SIZE + 1, -16777216);


        final int gx = left + halfUV;
        final int gy = top + halfUV;

        // Grid lines
        int lineCount = (int) Math.floor(this.tile.getDetectionRange() / (float) meterSpacing) * 2 + 1;
        float lineSpacing = (meterSpacing / (float) this.tile.getDetectionRange()) * halfUV;

        final int lx = gx - (int) ((lineCount / 2) * lineSpacing);
        final int ly = gy - (int) ((lineCount / 2) * lineSpacing);

        for (int i = 0; i < lineCount; i++) {
            final int x = lx + (int) Math.floor(i * lineSpacing);
            Gui.drawRect(x, gy + halfUV, x + 1, gy - halfUV, MARKER_COLOR);
        }

        for (int i = 0; i < lineCount; i++) {
            final int y = ly + (int) Math.floor(i * lineSpacing);
            Gui.drawRect(gx + halfUV, y, gx - halfUV, y + 1, MARKER_COLOR);
        }

        // Trigger area
        int triggerRange = (int) Math.ceil((this.tile.getTriggerRange() / (float) this.tile.getDetectionRange()) * halfUV);

        // Trigger bottom line
        Gui.drawRect(gx - triggerRange, gy + triggerRange, gx + triggerRange, gy + triggerRange + 1, TRIGGER_RANGE);

        // Trigger top line
        Gui.drawRect(gx - triggerRange, gy - triggerRange - 1, gx + triggerRange, gy - triggerRange, TRIGGER_RANGE);

        // Trigger left line
        Gui.drawRect(gx - triggerRange - 1, gy - triggerRange, gx - triggerRange, gy + triggerRange, TRIGGER_RANGE);

        // Trigger right line
        Gui.drawRect(gx + triggerRange, gy - triggerRange, gx + triggerRange + 1, gy + triggerRange, TRIGGER_RANGE);

        // Target data

        for (int i = 0; i < tile.getRadarRenderData().getDots().size(); i++) {
            final RadarRenderDot dot = tile.getRadarRenderData().getDots().get(i);

            final int x = gx + dot.getX();
            final int y = gy + dot.getY();
            if (dot.getType() == RadarDotType.MARKER) {
                Gui.drawRect(x, y, x + 1, y + 1, MARKER_COLOR);
            } else if (dot.getType() == RadarDotType.HOSTILE) {
                Gui.drawRect(x, y, x + 2, y + 2, HOSTILE_COLOR);
            } else if (dot.getType() == RadarDotType.INCOMING) {
                Gui.drawRect(x, y, x + 2, y + 2, INCOMING_COLOR);
            }
        }
    }
}

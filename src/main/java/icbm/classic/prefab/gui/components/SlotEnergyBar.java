package icbm.classic.prefab.gui.components;

import icbm.classic.ICBMClassic;
import icbm.classic.prefab.gui.GuiContainerBase;
import icbm.classic.prefab.gui.IGuiComponent;
import icbm.classic.prefab.gui.tooltip.IToolTip;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;

import java.util.function.Supplier;


public class SlotEnergyBar implements IGuiComponent, IToolTip {

    //UV
    private static final int ENERGY_BAR_WIDTH = 16;
    private static final int ENERGY_BAR_HEIGHT = 2;

    private static final String TOOLTIP_FORMAT = "gui.icbmclassic:energy";

    private final int x;
    private final int y;

    /** Energy getter */
    private final Supplier<Integer> energyGetter;
    /** Max energy getter */
    private final Supplier<Integer> energyMaxGetter;

    /** Parent */
    private GuiContainerBase container;

    private float energyPercent = 0;

    private int prevEnergy = 0;
    private int prevMaxEnergy = 0;
    private boolean prevShift = false;

    private ITextComponent tooltip;


    public SlotEnergyBar(int x, int y, Supplier<Integer> energyGetter, Supplier<Integer> energyMaxGetter) {
        this.x = x;
        this.y = y;
        this.energyGetter = energyGetter;
        this.energyMaxGetter = energyMaxGetter;
    }

    @Override
    public void onUpdate() {
        // Cached data to avoid redoing each frame render
        final int energy = energyGetter.get();
        final int maxEnergy = energyMaxGetter.get();
        final boolean shift = GuiScreen.isShiftKeyDown();

        if(energy != prevEnergy || maxEnergy != prevMaxEnergy || shift != prevShift) {
            prevEnergy = energy;
            prevMaxEnergy = maxEnergy;
            prevShift = shift;

            energyPercent = energy / (float) maxEnergy;

            if(shift) {
                tooltip = new TextComponentTranslation(TOOLTIP_FORMAT, energy, maxEnergy, String.format("%d", (int) Math.floor(energyPercent * 100)));
            }
            else {
                tooltip = new TextComponentTranslation(TOOLTIP_FORMAT, formatEnergy(energy), formatEnergy(maxEnergy), String.format("%d", (int) Math.floor(energyPercent * 100)));
            }
        }
    }

    private static String formatEnergy(int energy) {
        int number = energy;
        String type = "";
        // Mega
        if(energy > 1_000_000_000) {
            number = energy / 1_000_000_000;
            type = "G";
        }
        // Mega
        else if(energy > 1_000_000) {
            number = energy / 1_000_000;
            type = "M";
        }
        // Kilo
        else if(energy > 1_000) {
            number = energy / 1_000;
            type = "k";
        }
        return String.format("%d%s", number, type);
    }

    @Override
    public void onAddedToHost(GuiContainerBase container) {
        this.container = container;
    }

    @Override
    public void drawBackgroundLayer(float f, int mouseX, int mouseY) {

        container.mc.renderEngine.bindTexture(container.getBackground());
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

        // Calculate bar ratio
        final float barRatio = (float)Math.floor(ENERGY_BAR_WIDTH * energyPercent);

        // Calculate bar width
        final int minBar = energyPercent > 0 ? 1 : 0;
        int renderWidth = (int)Math.min(Math.max(minBar, barRatio), ENERGY_BAR_WIDTH);

        // Render box
        container.drawTexturedModalRect(container.getGuiLeft() + x, container.getGuiTop() + y, 256 - ENERGY_BAR_WIDTH, 0, renderWidth, ENERGY_BAR_HEIGHT);
    }

    @Override
    public boolean isWithin(int mouseX, int mouseY) {
        final int cursorX = mouseX - container.getGuiLeft();
        final int cursorY = mouseY - container.getGuiTop();
        return cursorX >= this.x && cursorY >= this.y && cursorX < this.x + ENERGY_BAR_WIDTH && cursorY < this.y + ENERGY_BAR_HEIGHT;
    }

    @Override
    public ITextComponent getTooltip() {
        return tooltip;
    }
}

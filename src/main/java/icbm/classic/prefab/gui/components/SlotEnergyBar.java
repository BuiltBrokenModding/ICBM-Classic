package icbm.classic.prefab.gui.components;

import icbm.classic.prefab.gui.GuiContainerBase;
import icbm.classic.prefab.gui.IGuiComponent;
import icbm.classic.prefab.gui.tooltip.IToolTip;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;

import java.util.Optional;
import java.util.function.Supplier;


public class SlotEnergyBar implements IGuiComponent, IToolTip {

    //UV
    private static final int ENERGY_BAR_WIDTH = 16;
    private static final int ENERGY_BAR_HEIGHT = 2;

    private static final String TOOLTIP_FORMAT = "gui.icbmclassic:energy";
    private static final String TOOLTIP_FORMAT_COST = "gui.icbmclassic:energy.withCost";
    private static final String TOOLTIP_FORMAT_ACTION = "gui.icbmclassic:energy.withAction";
    private static final String TOOLTIP_FORMAT_COST_ACTION = "gui.icbmclassic:energy.withCostAndAction";

    private final int x;
    private final int y;

    /** Energy getter */
    private final Supplier<Integer> energyGetter;
    /** Max energy getter */
    private final Supplier<Integer> energyMaxGetter;
    /** Cost per tick getter */
    private Supplier<Integer> tickingCostGetter;
    /** Cost per action getter */
    private Supplier<Integer> actionCostGetter;

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

    public SlotEnergyBar withTickingCost(Supplier<Integer> getter) {
        this.tickingCostGetter = getter;
        return this;
    }

    public SlotEnergyBar withActionCost(Supplier<Integer> getter) {
        this.actionCostGetter = getter;
        return this;
    }

    @Override
    public void onUpdate() {
        // Cached data to avoid redoing each frame render
        final int energy = energyGetter.get();
        final int maxEnergy = energyMaxGetter.get();
        final int tickingCost = Optional.ofNullable(tickingCostGetter).map(Supplier::get).orElse(0);
        final int actionCost = Optional.ofNullable(actionCostGetter).map(Supplier::get).orElse(0);
        final boolean shift = GuiScreen.isShiftKeyDown();

        if(energy != prevEnergy || maxEnergy != prevMaxEnergy || shift != prevShift) {
            prevEnergy = energy;
            prevMaxEnergy = maxEnergy;
            prevShift = shift;

            energyPercent = energy / (float) maxEnergy;

            String translationToUse = TOOLTIP_FORMAT;
            if(tickingCost > 0) {
                translationToUse = TOOLTIP_FORMAT_COST;
                if(actionCost > 0) {
                    translationToUse = TOOLTIP_FORMAT_COST_ACTION;
                }
            }
            else if(actionCost > 0) {
                translationToUse = TOOLTIP_FORMAT_ACTION;
            }

            if(shift) {
                tooltip = new TextComponentTranslation(translationToUse, energy, maxEnergy, -tickingCost, -actionCost);
            }
            else {
                tooltip = new TextComponentTranslation(translationToUse, formatEnergy(energy), formatEnergy(maxEnergy), formatEnergy(-tickingCost),formatEnergy(-actionCost));
            }
        }
    }

    private static String formatEnergy(final int energy) {
        boolean neg = energy < 0;
        int number = Math.abs(energy);
        String type = "";
        // Mega
        if(number >= 1_000_000_000) {
            number = number / 1_000_000_000;
            type = "G";
        }
        // Mega
        else if(number >= 1_000_000) {
            number = number / 1_000_000;
            type = "M";
        }
        // Kilo
        else if(number >= 1_000) {
            number = number / 1_000;
            type = "k";
        }
        return String.format("%s%d%s", neg ? "-" :  "", number, type); //TODO add decimal place
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

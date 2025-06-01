package icbm.classic.content.blocks.emptower.gui;

import icbm.classic.ICBMConstants;
import icbm.classic.content.blocks.emptower.TileEMPTower;
import icbm.classic.content.blocks.launcher.LauncherLangs;
import icbm.classic.content.blocks.launcher.cruise.gui.LaunchButton;
import icbm.classic.prefab.gui.GuiContainerBase;
import icbm.classic.prefab.gui.TextInput;
import icbm.classic.prefab.gui.button.DisableButton;
import icbm.classic.prefab.gui.components.SlotEnergyBar;
import icbm.classic.prefab.gui.tooltip.TooltipTranslations;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;

public class GuiEMPTower extends GuiContainerBase<ContainerEMPTower> {
    // Localizations
    private static final ITextComponent TITLE = new TranslationTextComponent("block.icbm.emp_tower_base.gui.title");
    private static final ITextComponent READY = new TranslationTextComponent("block.icbm.emp_tower_base.gui.ready");
    private static final ITextComponent TRANSLATION_TOOLTIP_RANGE = new TranslationTextComponent("block.icbm.emp_tower_base.gui.range");

    private static final String POWER_NEEDED = "block.icbm.emp_tower_base.gui.power";
    private static final String COOLING_NEEDED = "block.icbm.emp_tower_base.gui.cooling";

    // Texture
    public static final ResourceLocation TEXTURE = new ResourceLocation(ICBMConstants.DOMAIN, ICBMConstants.GUI_DIRECTORY + "gui_emp_tower.png");


    public GuiEMPTower(ContainerEMPTower container, PlayerInventory inventory, ITextComponent title) {
        super(container, inventory, title);
        this.ySize = 166;
        this.xSize = 175;
    }

    @Override
    public ResourceLocation getBackground() {
        return TEXTURE;
    }

    @Override
    public void init() {
        super.init();

        int componentID = 0;

        // Target field
        addButton(TextInput.intField(componentID++, this.font, 18, 17, 40, 12,
            container.getHost()::getRange, container.getHost()::setRange, (r) -> TileEMPTower.PACKET_RADIUS.sendToServer(container.getHost())));

        // Frequency field
        addButton(TextInput.textField(componentID++, this.font, 135, 17, 34, 12,
            container.getHost().radioCap::getChannel, container.getHost().radioCap::setChannel, (r) -> TileEMPTower.PACKET_RADIO_HZ.sendToServer(container.getHost())));

        // Launch button
        addButton(new LaunchButton(guiLeft + 24, guiTop + 38)
            .doDrawDisabledGlass()
            .setTooltip(() -> {
                if (!container.getHost().isReady()) {
                    if (container.getHost().getCooldown() > 0) {
                        return new TranslationTextComponent(COOLING_NEEDED, String.format("%.2f", container.getHost().getCooldownPercentage() * 100)); //TODO cache until change
                    } else if (!container.getHost().energyStorage.consumePower(container.getHost().getFiringCost(), false)) {
                        return new TranslationTextComponent(POWER_NEEDED, String.format("%.2f", container.getHost().getChargePercentage() * 100));  //TODO cache until change
                    }
                }
                return READY;
            })
            .setAction(() -> TileEMPTower.PACKET_FIRE.sendToServer(container.getHost()))
            .setEnabledCheck(container.getHost()::isReady)
        );

        addButton(new SlotEnergyBar(141, 66,
            container.getHost().energyStorage::getEnergyStored,
            container.getHost().energyStorage::getMaxEnergyStored, TEXTURE)
            .withTickingCost(container.getHost()::getTickingCost)
            .withActionCost(container.getHost()::getFiringCost)
        );

        // Radio tooltip TODO remove I18n usage
        addButton(new DisableButton( guiLeft + 119, guiTop + 16, I18n.format(ICBMConstants.PREFIX + "button.disable.machine"), container.getHost().radioCap::isDisabled)
            .setAction(() -> TileEMPTower.PACKET_RADIO_DISABLE.sendToServer(container.getHost()))
        );
        addButton(new TooltipTranslations(this, 119, 16, 14, 14, LauncherLangs.TRANSLATION_TOOLTIP_RADIO).withDelay(1));

        // Range tooltip
        addButton(new TooltipTranslations(this,2, 16, 14, 14, TRANSLATION_TOOLTIP_RANGE).withDelay(1));
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        // Draw text
        this.font.drawString("\u00a77" + TITLE.getFormattedText(), 52, 6, 4210752);
        this.font.drawString(" / " + container.getHost().getMaxRange(), 62, 19, 4210752);

        // Goes last so tooltips render above our UI elements
        super.drawGuiContainerForegroundLayer(mouseX, mouseY);
    }
}

package icbm.classic.content.blocks.emptower.gui;

import icbm.classic.ICBMClassic;
import icbm.classic.ICBMConstants;
import icbm.classic.content.blocks.emptower.TileEMPTower;
import icbm.classic.content.blocks.launcher.cruise.gui.LaunchButton;
import icbm.classic.lib.LanguageUtility;
import icbm.classic.lib.network.packet.PacketTile;
import icbm.classic.prefab.gui.GuiContainerBase;
import icbm.classic.prefab.gui.TextInput;
import icbm.classic.prefab.gui.components.SlotEnergyBar;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextComponentTranslation;

public class GuiEMPTower extends GuiContainerBase {
    // Localizations
    private static final String LANG_KEY = "gui.icbmclassic:empTower";
    private static final String GUI_NAME = LANG_KEY + ".name";
    private static final String POWER_NEEDED = LANG_KEY + ".power";
    private static final String COOLING_NEEDED = LANG_KEY + ".cooling";
    private static final String READY = LANG_KEY + ".ready";

    // Texture
    public static final ResourceLocation TEXTURE = new ResourceLocation(ICBMConstants.DOMAIN, ICBMConstants.GUI_DIRECTORY + "gui_emp_tower.png");

    private final TileEMPTower tileEntity;

    public GuiEMPTower(EntityPlayer player, TileEMPTower tileEntity) {
        super(new ContainerEMPTower(player, tileEntity));
        this.tileEntity = tileEntity;
        this.ySize = 166;
        this.xSize = 175;
    }

    @Override
    public ResourceLocation getBackground() {
        return TEXTURE;
    }

    @Override
    public void initGui() {
        super.initGui();

        int componentID = 0;

        // Target field
        addComponent(TextInput.intField(componentID++, fontRenderer, 18, 17, 40, 12,
            tileEntity::getRange, tileEntity::setRange, tileEntity::sendRangePacket));

        // Frequency field
        addComponent(TextInput.textField(componentID++, fontRenderer, 135, 17, 34, 12,
            tileEntity.radioCap::getChannel, tileEntity.radioCap::setChannel, tileEntity::sendHzPacket));

        // Launch button
        addButton(new LaunchButton(0, guiLeft + 24, guiTop + 38)
            .doDrawDisabledGlass()
            .setTooltip(() -> {
                if (!tileEntity.isReady()) {
                    if (tileEntity.getCooldown() > 0) {
                        return new TextComponentTranslation(COOLING_NEEDED, String.format("%.2f", tileEntity.getCooldownPercentage() * 100));
                    } else if (!tileEntity.checkExtract()) {
                        return new TextComponentTranslation(POWER_NEEDED, String.format("%.2f", tileEntity.getChargePercentage() * 100));
                    }
                }
                return new TextComponentTranslation(READY);
            })
            .setAction(() -> ICBMClassic.packetHandler.sendToServer(new PacketTile("fire_C>S", TileEMPTower.FIRE_PACKET_ID, this.tileEntity)))
            .setEnabledCheck(tileEntity::isReady)
        );

        addComponent(new SlotEnergyBar(141, 66, tileEntity::getEnergy, tileEntity::getEnergyBufferSize));
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        // Draw text
        this.fontRenderer.drawString("\u00a77" + LanguageUtility.getLocal(GUI_NAME), 52, 6, 4210752);
        this.fontRenderer.drawString("/ " + tileEntity.getMaxRadius(), 62, 19, 4210752);

        // Goes last so tooltips render above our UI elements
        super.drawGuiContainerForegroundLayer(mouseX, mouseY);
    }
}

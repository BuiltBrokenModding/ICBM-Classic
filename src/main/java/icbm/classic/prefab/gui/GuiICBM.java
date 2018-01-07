package icbm.classic.prefab.gui;

import com.builtbroken.mc.core.References;
import com.builtbroken.mc.prefab.gui.ContainerDummy;
import com.builtbroken.mc.prefab.gui.GuiContainerBase;
import icbm.classic.ICBMClassic;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.client.FMLClientHandler;
import org.lwjgl.opengl.GL11;

public abstract class GuiICBM extends GuiContainerBase
{
    public static final ResourceLocation TEXTURE = new ResourceLocation(ICBMClassic.DOMAIN, References.GUI_DIRECTORY + "gui_empty.png");

    protected int containerWidth;
    protected int containerHeight;

    public GuiICBM()
    {
        super(new ContainerDummy());
    }

    public GuiICBM(TileEntity tileEntity)
    {
        super(new ContainerDummy(tileEntity));
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float f, int mouseX, int mouseY)
    {
        FMLClientHandler.instance().getClient().renderEngine.bindTexture(TEXTURE);
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);

        this.containerWidth = (this.width - this.xSize) / 2;
        this.containerHeight = (this.height - this.ySize) / 2;
        this.drawTexturedModalRect(containerWidth, containerHeight, 0, 0, this.xSize, this.ySize);
    }

}

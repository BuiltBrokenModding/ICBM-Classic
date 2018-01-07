package icbm.classic.prefab.gui;

import com.builtbroken.mc.core.References;
import com.builtbroken.mc.prefab.gui.GuiContainerBase;
import icbm.classic.ICBMClassic;
import net.minecraft.inventory.Container;
import net.minecraft.util.ResourceLocation;

public abstract class GuiICBMContainer extends GuiContainerBase
{
    public static final ResourceLocation ICBM_EMPTY_TEXTURE = new ResourceLocation(ICBMClassic.DOMAIN, References.GUI_DIRECTORY + "gui_container.png");

    public GuiICBMContainer(Container container)
    {
        super(container);
        this.baseTexture = ICBM_EMPTY_TEXTURE;
    }
}

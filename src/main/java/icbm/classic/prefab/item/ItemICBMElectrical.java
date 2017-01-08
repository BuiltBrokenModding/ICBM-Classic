package icbm.classic.prefab.item;

import com.builtbroken.jlib.data.Colors;
import com.builtbroken.mc.lib.helper.LanguageUtility;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import org.lwjgl.input.Keyboard;

import java.util.List;

public abstract class ItemICBMElectrical extends ItemICBMBase
{
    public ItemICBMElectrical(String name)
    {
        super(name);
    }

    @Override
    public void addInformation(ItemStack itemStack, EntityPlayer par2EntityPlayer, List list, boolean par4)
    {
        super.addInformation(itemStack, par2EntityPlayer, list, par4);
        String tooltip = LanguageUtility.getLocal(getUnlocalizedName(itemStack) + ".tooltip");

        if (tooltip != null && tooltip.length() > 0)
        {
            if (!Keyboard.isKeyDown(Keyboard.KEY_LSHIFT))
            {
                list.add(LanguageUtility.getLocal("tooltip.noShift").replace("%0", Colors.AQUA.toString()).replace("%1", Colors.GREY.toString()));
            }
            else
            {
                list.addAll(LanguageUtility.splitStringPerWord(tooltip, 5));
            }
        }
    }

    public int getEnergy(ItemStack itemStack)
    {
        return Integer.MAX_VALUE;
    }

    public void discharge(ItemStack itemStack, int energy, boolean b)
    {

    }
}

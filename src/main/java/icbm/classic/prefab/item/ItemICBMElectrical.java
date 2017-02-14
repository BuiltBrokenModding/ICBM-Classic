package icbm.classic.prefab.item;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

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
    }

    public int getEnergy(ItemStack itemStack)
    {
        return Integer.MAX_VALUE;
    }

    public void discharge(ItemStack itemStack, int energy, boolean b)
    {

    }
}

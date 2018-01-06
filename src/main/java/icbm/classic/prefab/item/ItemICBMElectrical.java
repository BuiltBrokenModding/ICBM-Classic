package icbm.classic.prefab.item;

import net.minecraft.item.ItemStack;

public abstract class ItemICBMElectrical extends ItemICBMBase
{
    public ItemICBMElectrical(String name)
    {
        super(name);
    }

    public int getEnergy(ItemStack itemStack)
    {
        return Integer.MAX_VALUE;
    }

    public void discharge(ItemStack itemStack, int energy, boolean b)
    {

    }
}

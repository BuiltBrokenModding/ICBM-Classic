package icbm.classic.prefab.item;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public abstract class ItemICBMElectrical extends ItemICBMBase
{
    public ItemICBMElectrical(String name)
    {
        super(name);
    }

    public ItemICBMElectrical(String name, Item.Properties properties)
    {
        super(name, properties);
    }

    public int getEnergy(ItemStack itemStack)
    {
        return Integer.MAX_VALUE;
    }

    public void discharge(ItemStack itemStack, int energy, boolean b)
    {

    }
}
// PORTED

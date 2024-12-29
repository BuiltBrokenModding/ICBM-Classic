package icbm.classic.prefab.item;

import net.minecraft.item.ItemStack;

public abstract class ItemICBMElectrical extends ItemBase
{
    public ItemICBMElectrical(Properties p_i48487_1_) {
        super(p_i48487_1_);
    }

    public int getEnergy(ItemStack itemStack)
    {
        return Integer.MAX_VALUE;
    }

    public void discharge(ItemStack itemStack, int energy, boolean b)
    {

    }
}

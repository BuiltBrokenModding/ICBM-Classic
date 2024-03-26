package icbm.classic.prefab.item;

import net.minecraft.world.item.ItemStack;

public abstract class ItemICBMElectrical extends ItemICBMBase {
    public ItemICBMElectrical(Properties properties, String name) {
        super(properties, name);
    }

    public int getEnergy(ItemStack itemStack) {
        return Integer.MAX_VALUE;
    }

    public void discharge(ItemStack itemStack, int energy, boolean b) {

    }
}

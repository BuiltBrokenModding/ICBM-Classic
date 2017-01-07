package icbm.classic.content.explosive.tile;

import com.builtbroken.mc.prefab.items.ItemAbstract;
import icbm.classic.content.explosive.ExplosiveRegistry;
import net.minecraft.item.ItemStack;

public class ItemBlockExplosive extends ItemAbstract
{
    public ItemBlockExplosive()
    {
        this.setMaxDamage(0);
        this.setHasSubtypes(true);
    }

    @Override
    public int getMetadata(int damage)
    {
        return damage;
    }

    @Override
    public String getUnlocalizedName(ItemStack itemstack)
    {
        return this.getUnlocalizedName() + "." + ExplosiveRegistry.get(itemstack.getItemDamage()).getUnlocalizedName();
    }

    @Override
    public String getUnlocalizedName()
    {
        return "icbm.explosive";
    }
}

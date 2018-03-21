package icbm.classic.content.items;

import icbm.classic.prefab.item.ItemICBMBase;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;

/**
 * Prefab for use in generating new sets of crafting items
 *
 * @see <a href="https://github.com/BuiltBrokenModding/VoltzEngine/blob/development/license.md">License</a> for what you can and can't do with the code.
 * Created by Dark(DarkGuardsman, Robert) on 3/20/2018.
 */
public class ItemCrafting extends ItemICBMBase
{
    protected final String[] subItems;

    /**
     * @param name  - registry name of the item, also used for unlocalized
     * @param items - list of sub-items, used for unlocalized
     */
    public ItemCrafting(String name, String... items)
    {
        super(name);
        subItems = items;
        setHasSubtypes(true);
    }

    @Override
    public String getUnlocalizedName(ItemStack stack)
    {
        String subName = getSubItemName(stack.getItemDamage());
        return super.getUnlocalizedName(stack) + (subName != null ? "." + subName : "");
    }

    public String getSubItemName(int index)
    {
        if (index >= 0 && index < subItems.length)
        {
            return subItems[index];
        }
        return null;
    }

    @Override
    public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> items)
    {
        if (this.isInCreativeTab(tab))
        {
            for (int i = 0; i < subItems.length; i++)
            {
                items.add(new ItemStack(this, 1, i));
            }
        }
    }
}

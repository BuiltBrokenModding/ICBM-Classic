package icbm.classic.content.items;

import icbm.classic.prefab.item.ItemICBMBase;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraftforge.oredict.OreDictionary;

/**
 * Prefab for use in generating new sets of crafting items
 *
 * @see <a href="https://github.com/BuiltBrokenModding/VoltzEngine/blob/development/license.md">License</a> for what you can and can't do with the code.
 * Created by Dark(DarkGuardsman, Robert) on 3/20/2018.
 */
public class ItemCrafting extends ItemICBMBase
{
    /** Array of sub types (e.g. Iron, Steel, Copper) */
    public final String[] subItems;
    /** Name of the set of items (Ingot, Plate, Circuit) */
    public final String oreName;

    /**
     * @param oreName - registry name, localization, and ore prefix
     * @param items   - list of sub-items, used for unlocalized
     */
    public ItemCrafting(String oreName, String... items)
    {
        super(oreName);
        this.oreName = oreName;
        subItems = items;
        setHasSubtypes(true);
    }

    public void registerOreNames()
    {
        for (int i = 0; i < subItems.length; i++)
        {
            //Get name
            final String name = subItems[i];

            //Turn into ore name
            String oreName = name.substring(0, 1).toUpperCase() + name.substring(1, name.length());
            oreName = this.oreName + oreName;

            //Register
            OreDictionary.registerOre(oreName, new ItemStack(this, 1, i));
        }
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

    public int getIndexForName(String name)
    {
        for (int i = 0; i < subItems.length; i++)
        {
            String s = subItems[i];
            if (s.equalsIgnoreCase(name))
            {
                return i;
            }
        }
        return -1;
    }

    public ItemStack getStack(String name)
    {
        return getStack(name, 1);
    }

    public ItemStack getStack(String name, int count)
    {
        int index = getIndexForName(name);
        if (index > 0)
        {
            return new ItemStack(this, count, index);
        }
        return ItemStack.EMPTY;
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

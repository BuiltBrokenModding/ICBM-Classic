package icbm.classic;

import net.minecraft.block.Block;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;

/**
 * Prefab creative tab to either create a fast creative tab or reduce code
 * need to make a more complex tab
 * Created by robert on 11/25/2014.
 */
public class ICBMCreativeTab extends CreativeTabs
{
    public ItemStack itemStack = null;

    public ICBMCreativeTab(String name)
    {
        super(name);
    }

    @Override
    public void displayAllRelevantItems(NonNullList<ItemStack> list)
    {
        for (Item item : Item.REGISTRY)
        {
            if (item != null)
            {
                for (CreativeTabs tab : item.getCreativeTabs())
                {
                    if (tab == this)
                    {
                        NonNullList<ItemStack> temp_list = NonNullList.create();
                        item.getSubItems(this, temp_list);
                        for (ItemStack stack : temp_list)
                        {
                            if (stack.getItem() != null)
                            {
                                list.add(stack);
                            }
                            else
                            {
                                ICBMClassic.logger().error("Item: " + item + "  attempted to add a stack with a null item to creative tab " + this);
                            }
                        }
                    }
                }
            }
        }
    }

    @Override
    public ItemStack createIcon()
    {
        if (itemStack == null || itemStack.getItem() == null)
        {
            //Display error for devs to see
            if (itemStack == null)
            {
                ICBMClassic.logger().error("ItemStack used for creative tab " + this.getTabLabel() + " is null");
            }
            else
            {
                ICBMClassic.logger().error("ItemStack used for creative tab " + this.getTabLabel() + " contains a null Item reference");
            }

            //Attempt to use a random item in the tab
            NonNullList<ItemStack> list = NonNullList.create();
            displayAllRelevantItems(list);
            for (ItemStack stack : list)
            {
                itemStack = stack;
                return itemStack;
            }

            //If that fails set to redstone block as backup
            itemStack = new ItemStack(Blocks.REDSTONE_LAMP);
        }
        return itemStack;
    }

    /**
     * Helper method to add the item and it's sub types to a list
     *
     * @param list
     * @param item
     */
    protected void add(NonNullList<ItemStack> list, Item item)
    {
        if (item != null)
        {
            item.getSubItems(this, list);
        }
    }

    /**
     * Helper method to add the item and it's sub types to a list
     *
     * @param list
     * @param block
     */
    protected void add(NonNullList<ItemStack> list, Block block)
    {
        if (block != null)
        {
            block.getSubBlocks(this, list);
        }
    }
}

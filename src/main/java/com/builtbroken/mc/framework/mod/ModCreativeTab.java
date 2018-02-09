package com.builtbroken.mc.framework.mod;

import com.builtbroken.mc.core.Engine;
import net.minecraft.block.Block;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;

import java.util.Collections;
import java.util.Comparator;

/**
 * Prefab creative tab to either create a fast creative tab or reduce code
 * need to make a more complex tab
 * Created by robert on 11/25/2014.
 */
public class ModCreativeTab extends CreativeTabs
{
    public ItemStack itemStack = null;
    public Comparator itemSorter = null;

    public ModCreativeTab(String name)
    {
        super(name);
    }

    public ModCreativeTab(String name, Block block)
    {
        super(name);
        this.itemStack = new ItemStack(block);
    }

    public ModCreativeTab(String name, Item item)
    {
        super(name);
        this.itemStack = new ItemStack(item);
    }

    public ModCreativeTab(String name, ItemStack stack)
    {
        super(name);
        this.itemStack = stack;
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
                                Engine.logger().error("Item: " + item + "  attempted to add a stack with a null item to creative tab " + this);
                            }
                        }
                    }
                }
            }
        }

        if (itemSorter != null && !list.isEmpty())
        {
            Collections.sort(list, itemSorter);
        }
    }

    @Override
    public ItemStack getIconItemStack()
    {
        if (itemStack == null || itemStack.getItem() == null)
        {
            //Display error for devs to see
            if (itemStack == null)
            {
                Engine.logger().error("ItemStack used for creative tab " + this.getTabLabel() + " is null");
            }
            else
            {
                Engine.logger().error("ItemStack used for creative tab " + this.getTabLabel() + " contains a null Item reference");
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

    @Override
    public ItemStack getTabIconItem()
    {
        return getIconItemStack();
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

    public static abstract class ItemSorter implements Comparator
    {
        @Override
        public int compare(Object o1, Object o2)
        {
            if (o1 instanceof ItemStack && o2 instanceof ItemStack)
            {
                return compareItem((ItemStack) o1, (ItemStack) o2);
            }
            return -1;
        }

        public abstract int compareItem(ItemStack o1, ItemStack o2);
    }

    public static class NameSorter extends ItemSorter
    {
        @Override
        public int compareItem(ItemStack a, ItemStack b)
        {
            return getLabel(a).compareToIgnoreCase(getLabel(b));
        }

        public String getLabel(ItemStack stack)
        {
            return stack.getDisplayName();
        }
    }
}

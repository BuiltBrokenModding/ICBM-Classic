package icbm.classic.client;

import icbm.classic.ICBMClassic;
import icbm.classic.api.EnumTier;
import icbm.classic.api.ICBMClassicHelpers;
import icbm.classic.content.blocks.explosive.ItemBlockExplosive;
import icbm.classic.content.items.ItemBombCart;
import icbm.classic.content.items.ItemGrenade;
import icbm.classic.content.items.ItemMissile;
import icbm.classic.content.reg.BlockReg;
import icbm.classic.content.reg.ItemReg;
import net.minecraft.block.Block;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;

import java.util.ArrayList;
import java.util.List;

/**
 * Prefab creative tab to either create a fast creative tab or reduce code
 * need to make a more complex tab
 * Created by robert on 11/25/2014.
 */
public class ICBMCreativeTab extends CreativeTabs
{
    public ItemStack itemStack = null;
    public List<Item> definedTabItemsInOrder = new ArrayList();

    public ICBMCreativeTab(String name)
    {
        super(name);
    }

    //call during FMLInitializationEvent as registries need to be frozen for this
    public void init()
    {
        definedTabItemsInOrder.clear();
        //define items in order
        orderItem(BlockReg.blockLaunchBase);
        orderItem(BlockReg.blockLaunchScreen);
        orderItem(BlockReg.blockLaunchSupport);
        orderItem(BlockReg.blockEmpTower);
        orderItem(BlockReg.blockRadarStation);

        orderItem(BlockReg.blockConcrete);
        orderItem(BlockReg.blockReinforcedGlass);
        orderItem(BlockReg.blockSpikes);

        orderItem(ItemReg.itemRocketLauncher);
        orderItem(ItemReg.itemRadarGun);
        orderItem(ItemReg.itemRemoteDetonator);
        orderItem(ItemReg.itemLaserDetonator);
        orderItem(ItemReg.itemTracker);
        orderItem(ItemReg.itemSignalDisrupter);
        orderItem(ItemReg.itemDefuser);
        orderItem(ItemReg.itemBattery);

        orderItem(BlockReg.blockExplosive);
        orderItem(ItemReg.itemMissile);
        orderItem(ItemReg.itemGrenade);
        orderItem(ItemReg.itemBombCart);

        //Collect any non-defined items
        for (Item item : Item.REGISTRY) //registries are frozen during FMLInitializationEvent, can safely iterate
        {
            if (item != null)
            {
                for (CreativeTabs tab : item.getCreativeTabs())
                {
                    if (tab == this && !definedTabItemsInOrder.contains(item))
                    {
                        orderItem(item);
                    }
                }
            }
        }
    }

    private void orderItem(Block item)
    {
        orderItem(Item.getItemFromBlock(item));
    }

    private void orderItem(Item item)
    {
        definedTabItemsInOrder.add(item);
    }

    @Override
    public void displayAllRelevantItems(NonNullList<ItemStack> list)
    {
        //Insert items in order
        definedTabItemsInOrder.forEach(item -> collectSubItems(item, list));
    }

    protected void collectSubItems(Item item, NonNullList<ItemStack> list)
    {
        //Collect stacks
        NonNullList<ItemStack> temp_list = NonNullList.create();
        item.getSubItems(this, temp_list);

        //this sorting process leads to the tiers being sorted, but also the metadata being in the correct order within the tiers
        //example:
        //      tier 1: metadata 0, 1, 5
        //      tier 2: metadata 2, 7
        //      tier 3: metadata 3, 6
        //      tier 4: metadata 4
        //      end result: 0, 1, 5, 2, 7, 3, 6, 4
        if(item instanceof ItemBlockExplosive || item instanceof ItemMissile || item instanceof ItemBombCart)
        {
            //sort by tier first
            temp_list.sort((e1, e2) -> {
                final EnumTier tier1 = ICBMClassicHelpers.getExplosive(e1.getItemDamage(), true).getTier();
                final EnumTier tier2 = ICBMClassicHelpers.getExplosive(e2.getItemDamage(), true).getTier();

                if(tier1 != null && tier2 != null)
                    return tier1.ordinal() - tier2.ordinal();
                else return 0;
            });
            //then sort by damage, but keep the tiers themselves sorted
            temp_list.sort((e1, e2) -> {
                final EnumTier tier1 = ICBMClassicHelpers.getExplosive(e1.getItemDamage(), true).getTier();
                final EnumTier tier2 = ICBMClassicHelpers.getExplosive(e2.getItemDamage(), true).getTier();

                if(tier1 != tier2) //if the two entries are different tiers, do not sort them as mixing up tiers is not wanted
                    return 0;
                else return e1.getItemDamage() - e2.getItemDamage();
            });
        }
        else if(item instanceof ItemGrenade)
        {
            temp_list.sort((e1, e2) -> e1.getItemDamage() - e2.getItemDamage());
        }

        //Merge into list with null check
        mergeIntoList(item, list, temp_list);
    }

    protected void mergeIntoList(Item item, NonNullList<ItemStack> list, NonNullList<ItemStack> listToMerge)
    {
        for (ItemStack stack : listToMerge)
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

package icbm.classic.client;

import icbm.classic.content.reg.ItemReg;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.List;

/**
 * Prefab creative tab to either create a fast creative tab or reduce code
 * need to make a more complex tab
 * Created by Robin on 11/25/2014.
 */
public class ICBMCreativeTab extends ItemGroup
{
    private final List<Item> definedTabItemsInOrder = new ArrayList();

    public ICBMCreativeTab(String name)
    {
        super(name);
    }


    //call during FMLInitializationEvent as registries need to be frozen for this
    public void init()
    {
        definedTabItemsInOrder.clear();
        //define items in order
        /*orderItem(BlockReg.blockLaunchBase);
        orderItem(BlockReg.blockLaunchScreen);
        orderItem(BlockReg.blockLaunchSupport);
        orderItem(BlockReg.blockLaunchConnector);
        orderItem(BlockReg.blockCruiseLauncher);
        orderItem(BlockReg.EMP_TOWER_BASE_BLOCK);
        orderItem(BlockReg.blockRadarStation);

        orderItem(BlockReg.blockConcrete);
        orderItem(BlockReg.blockReinforcedGlass);
        orderItem(BlockReg.blockSpikes);

        orderItem(ItemReg.itemRocketLauncher);
        orderItem(ItemReg.itemBallisticLauncher);
        orderItem(ItemReg.itemRadarGun);
        orderItem(ItemReg.itemRemoteDetonator);
        orderItem(ItemReg.itemLaserDetonator);
        orderItem(ItemReg.itemDefuser);

        orderItem(ItemReg.itemExplosiveMissile);
        orderItem(ItemReg.itemSAM);
        orderItem(ItemReg.heldItemMissile);
        orderItem(ItemReg.itemClusterMissile);

        orderItem(ItemReg.itemGrenade);
        orderItem(BlockReg.blockExplosive);
        orderItem(ItemReg.itemBombCart);*/

        //Collect any non-defined items
        for (Item item : ForgeRegistries.ITEMS) //registries are frozen during FMLInitializationEvent, can safely iterate
        {
            if (item != null)
            {
                for (ItemGroup tab : item.getCreativeTabs())
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
    @OnlyIn(Dist.CLIENT)
    public void fill(final NonNullList<ItemStack> list)
    {
        //Insert items in order
        definedTabItemsInOrder.forEach(item -> collectSubItems(item, list));
    }

    protected void collectSubItems(final Item item, final NonNullList<ItemStack> masterList)
    {
        if (item == null)
        {
            return;
        }

        //Collect stacks
        final NonNullList<ItemStack> collectedItemStacks = NonNullList.create();
        item.fillItemGroup(this, collectedItemStacks);

        //Merge into list with null check
        masterList.addAll(collectedItemStacks);
    }

    @Override
    public ItemStack createIcon()
    {
        return new ItemStack(ItemReg.MISSILE_CONDENSED::get);
    }
}

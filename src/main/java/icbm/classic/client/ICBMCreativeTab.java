package icbm.classic.client;

import icbm.classic.api.ICBMClassicHelpers;
import icbm.classic.api.caps.IExplosive;
import icbm.classic.api.refs.ICBMExplosives;
import icbm.classic.api.reg.IExplosiveData;
import icbm.classic.content.reg.BlockReg;
import icbm.classic.content.reg.ItemReg;
import net.minecraft.block.Block;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Prefab creative tab to either create a fast creative tab or reduce code
 * need to make a more complex tab
 * Created by Robin on 11/25/2014.
 */
public class ICBMCreativeTab extends CreativeTabs
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
        orderItem(BlockReg.blockLaunchBase);
        orderItem(BlockReg.blockLaunchScreen);
        orderItem(BlockReg.blockLaunchSupport);
        orderItem(BlockReg.blockLaunchConnector);
        orderItem(BlockReg.blockCruiseLauncher);
        orderItem(BlockReg.blockEmpTower);
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
        orderItem(ItemReg.itemBombCart);

        //Collect any non-defined items
        for (Item item : ForgeRegistries.ITEMS) //registries are frozen during FMLInitializationEvent, can safely iterate
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
    @SideOnly(Side.CLIENT)
    public void displayAllRelevantItems(final NonNullList<ItemStack> list)
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
        item.getSubItems(this, collectedItemStacks);

        //Sort explosive types, if not explosive it will leave it alone
        collectedItemStacks.sort(this::compareExplosives);

        //Merge into list with null check
        masterList.addAll(collectedItemStacks);
    }

    private int compareExplosives(ItemStack itemA, ItemStack itemB)
    {
        final IExplosive explosiveA = ICBMClassicHelpers.getExplosive(itemA);
        final IExplosive explosiveB = ICBMClassicHelpers.getExplosive(itemB);
        if (explosiveA != null && explosiveB != null)
        {
            return compareExplosives(explosiveA, explosiveB);
        }
        return 0;
    }

    private int compareExplosives(IExplosive explosiveA, IExplosive explosiveB)
    {
        final IExplosiveData dataA = Optional.ofNullable(explosiveA.getExplosiveData()).orElse(ICBMExplosives.CONDENSED);
        final IExplosiveData dataB = Optional.ofNullable(explosiveB.getExplosiveData()).orElse(ICBMExplosives.CONDENSED);
        final int tierA = dataA.getTier().ordinal();
        final int tierB = dataB.getTier().ordinal();

        //If tiers are the same move to sorting by explosive registry index
        if (tierA == tierB)
        {
            return dataA.getRegistryID() - dataB.getRegistryID();
        }
        return tierA - tierB;
    }

    @Override
    public ItemStack getTabIconItem()
    {
        return new ItemStack(ItemReg.itemExplosiveMissile);
    }
}

package icbm.classic.client;

import icbm.classic.api.ICBMClassicHelpers;
import icbm.classic.api.caps.IExplosive;
import icbm.classic.api.refs.ICBMExplosives;
import icbm.classic.api.reg.ExplosiveType;
import icbm.classic.world.IcbmItems;
import net.minecraft.core.registries.Registries;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.util.NonNullList;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.fml.common.registry.ForgeRegistries;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Prefab creative tab to either create a fast creative tab or reduce code
 * need to make a more complex tab
 * Created by robert on 11/25/2014.
 */
public class ICBMCreativeTab extends CreativeModeTab {

    private static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, "icbmclassic");

    public static final Object INSTANCE = TABS.register("icbm", () -> CreativeModeTab.builder()
        .icon(() -> new ItemStack(IcbmItems.itemExplosiveMissile))
        .build());

    private final List<Item> definedTabItemsInOrder = new ArrayList();

    public ICBMCreativeTab(String name) {
        super(name);
    }

    //call during FMLInitializationEvent as registries need to be frozen for this
    public void init() {
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

        orderItem(IcbmItems.itemRocketLauncher);
        orderItem(IcbmItems.itemRadarGun);
        orderItem(IcbmItems.itemRemoteDetonator);
        orderItem(IcbmItems.itemLaserDetonator);
        orderItem(IcbmItems.itemTracker);
        orderItem(IcbmItems.itemSignalDisrupter);
        orderItem(IcbmItems.itemDefuser);

        orderItem(IcbmItems.itemExplosiveMissile);
        orderItem(IcbmItems.itemSAM);
        orderItem(IcbmItems.itemGrenade);
        orderItem(BlockReg.blockExplosive);
        orderItem(IcbmItems.itemBombCart);

        //Collect any non-defined items
        for (Item item : ForgeRegistries.ITEMS) //registries are frozen during FMLInitializationEvent, can safely iterate
        {
            if (item != null) {
                for (CreativeTabs tab : item.getCreativeTabs()) {
                    if (tab == this && !definedTabItemsInOrder.contains(item)) {
                        orderItem(item);
                    }
                }
            }
        }
    }

    private void orderItem(Block item) {
        orderItem(Item.getItemFromBlock(item));
    }

    private void orderItem(Item item) {
        definedTabItemsInOrder.add(item);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void displayAllRelevantItems(final NonNullList<ItemStack> list) {
        //Insert items in order
        definedTabItemsInOrder.forEach(item -> collectSubItems(item, list));
    }

    protected void collectSubItems(final Item item, final NonNullList<ItemStack> masterList) {
        if (item == null) {
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

    private int compareExplosives(ItemStack itemA, ItemStack itemB) {
        final IExplosive explosiveA = ICBMClassicHelpers.getExplosive(itemA);
        final IExplosive explosiveB = ICBMClassicHelpers.getExplosive(itemB);
        if (explosiveA != null && explosiveB != null) {
            return compareExplosives(explosiveA, explosiveB);
        }
        return 0;
    }

    private int compareExplosives(IExplosive explosiveA, IExplosive explosiveB) {
        final ExplosiveType dataA = Optional.ofNullable(explosiveA.getExplosiveData()).orElse(ICBMExplosives.CONDENSED);
        final ExplosiveType dataB = Optional.ofNullable(explosiveB.getExplosiveData()).orElse(ICBMExplosives.CONDENSED);
        final int tierA = dataA.getTier().ordinal();
        final int tierB = dataB.getTier().ordinal();

        //If tiers are the same move to sorting by explosive registry index
        if (tierA == tierB) {
            return dataA.getRegistryID() - dataB.getRegistryID();
        }
        return tierA - tierB;
    }

    @Override
    public ItemStack getTabIconItem() {
        return new ItemStack(IcbmItems.itemExplosiveMissile);
    }
}

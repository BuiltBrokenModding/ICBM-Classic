package icbm.classic.client;

import icbm.classic.ICBMConstants;
import icbm.classic.api.ICBMClassicHelpers;
import icbm.classic.api.caps.IExplosive;
import icbm.classic.api.refs.ICBMExplosives;
import icbm.classic.api.reg.IExplosiveData;
import icbm.classic.content.reg.BlockReg;
import icbm.classic.content.reg.ItemReg;
import icbm.classic.prefab.item.ISubItemProvider;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * ICBM creative tab. Keeps the 1.12 ordering: launchers and machines first, then blocks, tools, missiles,
 * explosives, followed by anything else ICBM registers. Variant items are expanded via {@link ISubItemProvider}
 * and sorted by explosive tier then registry id.
 */
public final class ICBMCreativeTab
{
    public static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, ICBMConstants.DOMAIN);

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> TAB = TABS.register(ICBMConstants.DOMAIN, () -> CreativeModeTab.builder()
        .title(Component.translatable("itemGroup." + ICBMConstants.DOMAIN))
        .icon(() -> new ItemStack(ItemReg.itemExplosiveMissile.get()))
        .displayItems((params, output) -> collectItems().forEach(output::accept))
        .build());

    private ICBMCreativeTab()
    {
    }

    public static void register(IEventBus modBus)
    {
        TABS.register(modBus);
    }

    private static List<Item> orderedItems()
    {
        final List<Item> items = new ArrayList<>();
        // define items in order
        add(items, BlockReg.blockLaunchBase);
        add(items, BlockReg.blockLaunchScreen);
        add(items, BlockReg.blockLaunchSupport);
        add(items, BlockReg.blockLaunchConnector);
        add(items, BlockReg.blockCruiseLauncher);
        add(items, BlockReg.blockEmpTower);
        add(items, BlockReg.blockRadarStation);

        add(items, BlockReg.blockConcrete);
        add(items, BlockReg.blockConcreteCompact);
        add(items, BlockReg.blockConcreteReinforced);
        add(items, BlockReg.blockReinforcedGlass);
        add(items, BlockReg.blockSpikes);
        add(items, BlockReg.blockSpikesPoison);
        add(items, BlockReg.blockSpikesFire);

        add(items, ItemReg.itemRocketLauncher);
        add(items, ItemReg.itemBallisticLauncher);
        add(items, ItemReg.itemRadarGun);
        add(items, ItemReg.itemRemoteDetonator);
        add(items, ItemReg.itemLaserDetonator);
        add(items, ItemReg.itemDefuser);

        add(items, ItemReg.itemExplosiveMissile);
        add(items, ItemReg.itemSAM);
        add(items, ItemReg.heldItemMissile);
        add(items, ItemReg.itemClusterMissile);

        add(items, ItemReg.itemGrenade);
        add(items, BlockReg.blockExplosive);
        add(items, ItemReg.itemBombCart);

        // Collect any non-defined items
        ItemReg.ITEMS.getEntries().forEach(holder -> {
            final Item item = holder.get();
            if (!items.contains(item))
            {
                items.add(item);
            }
        });
        return items;
    }

    private static void add(List<Item> items, Supplier<? extends ItemLike> entry)
    {
        if (entry != null)
        {
            final Item item = entry.get().asItem();
            if (item != null && !items.contains(item))
            {
                items.add(item);
            }
        }
    }

    public static List<ItemStack> collectItems()
    {
        final NonNullList<ItemStack> list = NonNullList.create();
        orderedItems().forEach(item -> collectSubItems(item, list));
        return list;
    }

    private static void collectSubItems(final Item item, final NonNullList<ItemStack> masterList)
    {
        // Collect stacks
        final NonNullList<ItemStack> collectedItemStacks = NonNullList.create();
        if (item instanceof ISubItemProvider provider)
        {
            provider.getSubItems(collectedItemStacks);
        }
        else
        {
            collectedItemStacks.add(new ItemStack(item));
        }

        // Sort explosive types, if not explosive it will leave it alone
        collectedItemStacks.sort(ICBMCreativeTab::compareExplosives);
        masterList.addAll(collectedItemStacks);
    }

    private static int compareExplosives(ItemStack itemA, ItemStack itemB)
    {
        final IExplosive explosiveA = ICBMClassicHelpers.getExplosive(itemA);
        final IExplosive explosiveB = ICBMClassicHelpers.getExplosive(itemB);
        if (explosiveA != null && explosiveB != null)
        {
            return compareExplosives(explosiveA, explosiveB);
        }
        return 0;
    }

    private static int compareExplosives(IExplosive explosiveA, IExplosive explosiveB)
    {
        final IExplosiveData dataA = Optional.ofNullable(explosiveA.getExplosiveData()).orElse(ICBMExplosives.CONDENSED);
        final IExplosiveData dataB = Optional.ofNullable(explosiveB.getExplosiveData()).orElse(ICBMExplosives.CONDENSED);
        final int tierA = dataA.getTier().ordinal();
        final int tierB = dataB.getTier().ordinal();

        // If tiers are the same move to sorting by explosive registry index
        if (tierA == tierB)
        {
            return dataA.getRegistryID() - dataB.getRegistryID();
        }
        return tierA - tierB;
    }
}
// PORTED

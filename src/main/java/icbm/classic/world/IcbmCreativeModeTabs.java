package icbm.classic.world;

import icbm.classic.IcbmConstants;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public final class IcbmCreativeModeTabs {

    public static final DeferredRegister<CreativeModeTab> REGISTER = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, IcbmConstants.MOD_ID);

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> ICBM = REGISTER.register("icbm", resourceLocation -> CreativeModeTab.builder()
        .title(Component.translatable("itemGroup.icbm"))
        .icon(() -> new ItemStack(IcbmItems.ROCKET_LAUNCHER.asItem()))
        .displayItems((parameters, output) -> {
            output.accept(IcbmItems.ANTIDOTE);
            output.acceptAll(group(IcbmBlocks.SPIKES, IcbmBlocks.FIRE_SPIKES, IcbmBlocks.POISON_SPIKES));
            output.acceptAll(group(IcbmBlocks.WHITE_COMPACT_CONCRETE, IcbmBlocks.ORANGE_COMPACT_CONCRETE,
                IcbmBlocks.MAGENTA_COMPACT_CONCRETE, IcbmBlocks.LIGHT_BLUE_COMPACT_CONCRETE,
                IcbmBlocks.YELLOW_COMPACT_CONCRETE, IcbmBlocks.LIME_COMPACT_CONCRETE, IcbmBlocks.PINK_COMPACT_CONCRETE,
                IcbmBlocks.GRAY_COMPACT_CONCRETE, IcbmBlocks.LIGHT_GRAY_COMPACT_CONCRETE,
                IcbmBlocks.CYAN_COMPACT_CONCRETE, IcbmBlocks.PURPLE_COMPACT_CONCRETE, IcbmBlocks.BLUE_COMPACT_CONCRETE,
                IcbmBlocks.BROWN_COMPACT_CONCRETE, IcbmBlocks.GREEN_COMPACT_CONCRETE, IcbmBlocks.RED_COMPACT_CONCRETE,
                IcbmBlocks.BLACK_COMPACT_CONCRETE));
            output.acceptAll(group(IcbmBlocks.WHITE_REINFORCED_CONCRETE, IcbmBlocks.ORANGE_REINFORCED_CONCRETE,
                IcbmBlocks.MAGENTA_REINFORCED_CONCRETE, IcbmBlocks.LIGHT_BLUE_REINFORCED_CONCRETE,
                IcbmBlocks.YELLOW_REINFORCED_CONCRETE, IcbmBlocks.LIME_REINFORCED_CONCRETE, IcbmBlocks.PINK_REINFORCED_CONCRETE,
                IcbmBlocks.GRAY_REINFORCED_CONCRETE, IcbmBlocks.LIGHT_GRAY_REINFORCED_CONCRETE,
                IcbmBlocks.CYAN_REINFORCED_CONCRETE, IcbmBlocks.PURPLE_REINFORCED_CONCRETE, IcbmBlocks.BLUE_REINFORCED_CONCRETE,
                IcbmBlocks.BROWN_REINFORCED_CONCRETE, IcbmBlocks.GREEN_REINFORCED_CONCRETE, IcbmBlocks.RED_REINFORCED_CONCRETE,
                IcbmBlocks.BLACK_REINFORCED_CONCRETE));
        })
        .build());

    private static Collection<ItemStack> group(ItemLike... items) {
        List<ItemStack> list = new ArrayList<>(items.length);
        for (ItemLike item : items) {
            list.add(new ItemStack(item));
        }
        return list;
    }
}

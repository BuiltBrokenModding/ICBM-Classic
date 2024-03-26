package icbm.classic.prefab.item;

import icbm.classic.ICBMClassic;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;

/**
 * Prefab for ICBM items that sets the creative tab, texture name, and translation name
 *
 * @author DarkGuardsman
 */
@Deprecated
public abstract class ItemICBMBase extends ItemBase {
    public ItemICBMBase(Properties properties, String name) {
        super(properties);
        setName(name);
        setCreativeTab(ICBMClassic.CREATIVE_TAB);
    }

    @Nullable
    public abstract ItemStackCapProvider initCapabilities(ItemStack stack, @Nullable CompoundTag nbt);
}

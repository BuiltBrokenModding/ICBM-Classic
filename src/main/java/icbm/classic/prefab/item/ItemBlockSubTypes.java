package icbm.classic.prefab.item;

import icbm.classic.lib.LanguageUtility;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/**
 * Implementation of a block with subtypes that extends the base ItemBlock class used by VoltzEngine
 * <p>
 * <p>
 * Created by Dark(DarkGuardsman, Robert) on 2/14/2017.
 */
public class ItemBlockSubTypes extends IcbmBlockItem {
    public ItemBlockSubTypes(Block block) {
        super(block);
        setHasSubtypes(true);
        setRegistryName(block.getRegistryName());
    }

    @Override
    public int getMetadata(int damage) {
        return damage;
    }

    @Override
    public String getUnlocalizedName(ItemStack itemstack) {
        String localized = LanguageUtility.getLocal(getUnlocalizedName() + "." + itemstack.getItemDamage() + ".name");
        if (localized != null && !localized.isEmpty()) {
            return getUnlocalizedName() + "." + itemstack.getItemDamage();
        }
        return getUnlocalizedName();
    }

    @Override
    protected boolean hasShiftInfo(ItemStack stack, Player player) {
        final String translationKey = getUnlocalizedName(stack) + ".info.detailed";
        final String translation = LanguageUtility.getLocal(translationKey);
        return !translation.trim().isEmpty() && !translation.equals(translationKey);
    }
}

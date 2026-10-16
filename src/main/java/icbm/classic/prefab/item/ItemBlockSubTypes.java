package icbm.classic.prefab.item;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;

import javax.annotation.Nullable;

/**
 * Implementation of a block with subtypes that extends the base BlockItem class used by VoltzEngine
 * <p>
 * PORT: metadata is gone, flattened items pass their old meta value as {@code subType}. The translation
 * key keeps the 1.12 format {@code <block key>.<meta>} (e.g. tile.icbmclassic:emptower.1).
 * <p>
 * Created by Dark(DarkGuardsman, Robin) on 2/14/2017.
 */
public class ItemBlockSubTypes extends ItemBlockAbstract
{
    /** Old metadata value of this item */
    protected final int subType;

    public ItemBlockSubTypes(Block block)
    {
        this(block, 0, new Item.Properties());
    }

    public ItemBlockSubTypes(Block block, Item.Properties properties)
    {
        this(block, 0, properties);
    }

    public ItemBlockSubTypes(Block block, int subType)
    {
        this(block, subType, new Item.Properties());
    }

    public ItemBlockSubTypes(Block block, int subType, Item.Properties properties)
    {
        super(block, properties);
        this.subType = subType;
    }

    /** @return old metadata value */
    public int getSubType()
    {
        return subType;
    }

    @Override
    public String getDescriptionId()
    {
        final String base = super.getDescriptionId();
        final String variantKey = base + "." + subType;
        // 1.12 checked for "<key>.name", lang conversion keeps both forms
        if (ItemBase.hasTranslation(variantKey) || ItemBase.hasTranslation(variantKey + ".name"))
        {
            return variantKey;
        }
        return base;
    }

    @Override
    public String getDescriptionId(ItemStack stack)
    {
        return getDescriptionId();
    }

    @Override
    protected boolean hasShiftInfo(ItemStack stack, @Nullable Player player)
    {
        final String translationKey = getDescriptionId(stack) + ".info.detailed";
        final String translation = ItemBase.translate(translationKey);
        return !translation.trim().isEmpty() && !translation.equals(translationKey);
    }
}
// PORTED

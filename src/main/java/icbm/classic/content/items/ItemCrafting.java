package icbm.classic.content.items;

import icbm.classic.prefab.item.ItemICBMBase;
import net.minecraft.world.item.ItemStack;

/**
 * Prefab for use in generating new sets of crafting items
 * <p>
 * PORT: 1.12 used one item per group with a metadata per type (circuit:basic). Items are flattened, one item
 * per group + type. The translation key stays the 1.12 variant key {@code item.icbmclassic:<group>.<type>}.
 * Ore dictionary names are replaced by item tags in data (c:circuits/basic ...).
 * <p>
 * Created by Dark(DarkGuardsman, Robin) on 3/20/2018.
 */
public class ItemCrafting extends ItemICBMBase
{
    /** Sub type (e.g. iron, steel, copper) */
    public final String type;
    /** Name of the set of items (ingot, plate, circuit) */
    public final String oreName;

    /**
     * @param oreName - group name, localization, and old ore prefix
     * @param type    - sub-item, used for unlocalized
     */
    public ItemCrafting(String oreName, String type)
    {
        super(oreName);
        this.oreName = oreName;
        this.type = type;
    }

    @Override
    public String getDescriptionId()
    {
        return super.getDescriptionId() + "." + type;
    }

    @Override
    public String getDescriptionId(ItemStack stack)
    {
        return getDescriptionId();
    }

    /** @return old 1.12 ore dictionary name, e.g. circuitBasic */
    public String getOreName()
    {
        return oreName + type.substring(0, 1).toUpperCase() + type.substring(1);
    }

    public ItemStack getStack(int count)
    {
        return new ItemStack(this, count);
    }
}
// PORTED

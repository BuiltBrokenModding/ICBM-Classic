package icbm.classic.prefab.item;

import net.minecraft.world.item.Item;

/**
 * Prefab for ICBM items that sets the translation name
 * <p>
 * PORT: creative tab is no longer set per item, {@link icbm.classic.client.ICBMCreativeTab} lists every ICBM item.
 *
 * @author DarkGuardsman
 */
@Deprecated
public class ItemICBMBase extends ItemBase
{
    public ItemICBMBase(String name)
    {
        this(name, new Item.Properties());
    }

    public ItemICBMBase(String name, Item.Properties properties)
    {
        super(properties);
        setName(name);
    }
}
// PORTED

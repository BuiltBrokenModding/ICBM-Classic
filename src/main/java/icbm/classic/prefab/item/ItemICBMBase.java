package icbm.classic.prefab.item;

import icbm.classic.ICBMClassic;

/**
 * Prefab for ICBM items that sets the creative tab, texture name, and translation name
 *
 * @author DarkGuardsman
 */
@Deprecated
public class ItemICBMBase extends ItemBase
{
    public ItemICBMBase(String name)
    {
        setName(name);
        setCreativeTab(ICBMClassic.CREATIVE_TAB);
    }
}

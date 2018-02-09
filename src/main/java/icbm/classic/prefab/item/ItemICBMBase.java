package icbm.classic.prefab.item;

import icbm.classic.ICBMClassic;

/** Prefab for ICBM items that sets the creative tab, texture name, and translation name
 *
 * @author DarkGuardsman */
public class ItemICBMBase extends ItemAbstract
{
    public ItemICBMBase(String name)
    {
        this.setUnlocalizedName(ICBMClassic.PREFIX + name);
        this.setRegistryName(ICBMClassic.PREFIX + name);
        this.setCreativeTab(ICBMClassic.CREATIVE_TAB);
    }
}

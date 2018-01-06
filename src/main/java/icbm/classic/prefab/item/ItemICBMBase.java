package icbm.classic.prefab.item;

import com.builtbroken.mc.prefab.items.ItemAbstract;
import icbm.classic.ICBMClassic;

/** Prefab for ICBM items that sets the creative tab, texture name, and translation name
 *
 * @author DarkGuardsman */
public class ItemICBMBase extends ItemAbstract
{
    public ItemICBMBase(String name)
    {
        this.setUnlocalizedName(ICBMClassic.PREFIX + name);
    }
}

package icbm.classic.prefab.item;

import com.builtbroken.mc.prefab.items.ItemAbstract;
import icbm.classic.Reference;

/** Prefab for ICBM items that sets the creative tab, texture name, and translation name
 *
 * @author DarkGuardsman */
public class ItemICBMBase extends ItemAbstract
{
    public ItemICBMBase(String name)
    {
        this.setUnlocalizedName(Reference.PREFIX + name);
        this.setTextureName(Reference.PREFIX + name);
    }
}

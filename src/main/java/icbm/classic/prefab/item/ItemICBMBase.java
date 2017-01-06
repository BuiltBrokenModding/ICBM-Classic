package icbm.classic.prefab.item;

import com.builtbroken.mc.prefab.items.ItemAbstract;
import icbm.Reference;
import icbm.TabICBM;

/** Prefab for ICBM items that sets the creative tab, texture name, and translation name
 *
 * @author DarkGuardsman */
public class ItemICBMBase extends ItemAbstract
{
    public ItemICBMBase(String name)
    {
        this.setUnlocalizedName(Reference.PREFIX + name);
        this.setCreativeTab(TabICBM.INSTANCE);
        this.setTextureName(Reference.PREFIX + name);
    }
}

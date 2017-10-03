package icbm.classic.content.items;

import com.builtbroken.mc.lib.Holiday;
import com.builtbroken.mc.lib.helper.LanguageUtility;
import icbm.classic.ICBMClassic;
import icbm.classic.Settings;
import icbm.classic.content.explosive.Explosives;
import icbm.classic.content.explosive.ex.ExNightmare;
import icbm.classic.content.explosive.tile.ItemBlockExplosive;
import icbm.classic.prefab.item.ItemICBMBase;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import java.util.List;

public class ItemMissile extends ItemICBMBase
{
    public ItemMissile()
    {
        super("missile");
        this.setMaxDamage(0);
        this.setHasSubtypes(true);
        this.setMaxStackSize(1);
    }

    @Override
    public int getMetadata(int damage)
    {
        return damage;
    }

    @Override
    public String getUnlocalizedName(ItemStack itemStack)
    {
        return this.getUnlocalizedName() + "." + Explosives.get(itemStack.getItemDamage()).handler.getUnlocalizedName();
    }

    @Override
    public String getUnlocalizedName()
    {
        return "icbm.missile";
    }

    @Override
    public void getSubItems(Item par1, CreativeTabs par2CreativeTabs, List par3List)
    {
        for (Explosives explosive : Explosives.values())
        {
            if (explosive.handler.hasMissileForm() && (!(explosive.handler instanceof ExNightmare) || Settings.FORCE_ENABLE_NIGHTMARE || Holiday.isHalloween()))
            {
                par3List.add(new ItemStack(par1, 1, explosive.ordinal()));
            }
        }
    }

    @Override
    protected boolean hasDetailedInfo(ItemStack stack, EntityPlayer player)
    {
        return true;
    }

    @Override
    protected void getDetailedInfo(ItemStack stack, EntityPlayer player, List list)
    {
        ((ItemBlockExplosive) Item.getItemFromBlock(ICBMClassic.blockExplosive)).getDetailedInfo(stack, player, list);
        int tierdata = Explosives.get(stack.getItemDamage()).handler.getTier();
        list.add(LanguageUtility.getLocal("info.misc.tier") + ": " + tierdata);
    }
}

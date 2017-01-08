package icbm.classic.content.items;

import com.builtbroken.mc.lib.helper.LanguageUtility;
import icbm.classic.content.explosive.ExplosiveRegistry;
import icbm.classic.content.explosive.Explosives;
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
        return this.getUnlocalizedName() + "." + ExplosiveRegistry.get(itemStack.getItemDamage()).getUnlocalizedName();
    }

    @Override
    public String getUnlocalizedName()
    {
        return "icbm.missile";
    }

    @Override
    public void getSubItems(Item par1, CreativeTabs par2CreativeTabs, List par3List)
    {
        for (Explosives zhaPin : Explosives.values())
        {
            if (zhaPin.handler.hasMissileForm())
            {
                par3List.add(new ItemStack(par1, 1, zhaPin.ordinal()));
            }
        }
    }

    @Override
    public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean bool)
    {
        int tierdata = ExplosiveRegistry.get(stack.getItemDamage()).getTier();
        list.add(LanguageUtility.getLocal("info.misc.tier") + ": " + tierdata);

		super.addInformation(stack, player, list, bool);

    }
}

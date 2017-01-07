package icbm.classic.content.items;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import icbm.classic.Reference;
import icbm.classic.prefab.item.ItemICBMBase;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;

import java.util.List;

public class ItemSulfurDust extends ItemICBMBase
{
    @SideOnly(Side.CLIENT)
    IIcon salt_icon;

    public ItemSulfurDust()
    {
        super("sulfur");
        this.setHasSubtypes(true);
    }

    @Override
    public IIcon getIconFromDamage(int meta)
    {
        // Damage value of 1 is saltpeter.
        if (meta == 1)
        {
            return salt_icon;
        }

        return super.getIconFromDamage(meta);
    }

    @Override
    public void registerIcons(IIconRegister iconRegister)
    {
        super.registerIcons(iconRegister);
        this.salt_icon = iconRegister.registerIcon(Reference.PREFIX + "saltpeter");
    }

    @Override
    public String getUnlocalizedName(ItemStack stack)
    {
        // Damage value of 1 returns name of saltpeter.
        if (stack.getItemDamage() == 1)
        {
            return "item." + Reference.PREFIX + "saltpeter";
        }

        return super.getUnlocalizedName();
    }

    @Override
    public void getSubItems(Item item, CreativeTabs tab, List items)
    {
        super.getSubItems(item, tab, items);
        items.add(new ItemStack(item, 1, 1));
    }
}

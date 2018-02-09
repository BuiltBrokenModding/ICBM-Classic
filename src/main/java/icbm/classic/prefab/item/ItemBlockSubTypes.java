package icbm.classic.prefab.item;

import icbm.classic.lib.LanguageUtility;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

/**
 * Implementation of a block with subtypes that extends the base ItemBlock class used by VoltzEngine
 *
 * @see <a href="https://github.com/BuiltBrokenModding/VoltzEngine/blob/development/license.md">License</a> for what you can and can't do with the code.
 * Created by Dark(DarkGuardsman, Robert) on 2/14/2017.
 */
public class ItemBlockSubTypes extends ItemBlockAbstract
{
    public ItemBlockSubTypes(Block block)
    {
        super(block);
        setHasSubtypes(true);
    }

    @Override
    public int getMetadata(int damage)
    {
        return damage;
    }

    @Override
    public String getUnlocalizedName(ItemStack itemstack)
    {
        String localized = LanguageUtility.getLocal(getUnlocalizedName() + "." + itemstack.getItemDamage() + ".name");
        if (localized != null && !localized.isEmpty())
        {
            return getUnlocalizedName() + "." + itemstack.getItemDamage();
        }
        return getUnlocalizedName();
    }

    @Override
    protected boolean hasShiftInfo(ItemStack stack, EntityPlayer player)
    {
        final String translationKey = getUnlocalizedName(stack) + ".info.detailed";
        final String translation = LanguageUtility.getLocal(translationKey);
        return !translation.trim().isEmpty() && !translation.equals(translationKey);
    }
}

package icbm.classic.content.explosive.tile;

import icbm.classic.content.explosive.Explosives;
import net.minecraft.block.Block;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;

public class ItemBlockExplosive extends ItemBlock
{
    public ItemBlockExplosive(Block block)
    {
        super(block);
        this.setMaxDamage(0);
        this.setHasSubtypes(true);
    }

    @Override
    public int getMetadata(int damage)
    {
        return damage;
    }

    @Override
    public String getUnlocalizedName(ItemStack itemstack)
    {
        return this.getUnlocalizedName() + "." + Explosives.get(itemstack.getItemDamage()).handler.getUnlocalizedName();
    }

    @Override
    public String getUnlocalizedName()
    {
        return "icbm.explosive";
    }
}

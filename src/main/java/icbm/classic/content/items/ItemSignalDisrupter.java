package icbm.classic.content.items;

import icbm.classic.prefab.item.ItemICBMElectrical;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

import java.util.List;

public class ItemSignalDisrupter extends ItemICBMElectrical
{
    public ItemSignalDisrupter()
    {
        super("signalDisrupter");
        setMaxStackSize(1);
    }

    @Override
    protected void getDetailedInfo(ItemStack stack, EntityPlayer player, List list)
    {
        super.getDetailedInfo(stack, player, list);
        list.add("Not Implemented");
    }
}

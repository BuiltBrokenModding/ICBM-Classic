package icbm.classic.content.items;

import icbm.classic.prefab.item.ItemICBMElectrical;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.List;

public class ItemSignalDisrupter extends ItemICBMElectrical
{
    public ItemSignalDisrupter()
    {
        super("signalDisrupter");
        setMaxStackSize(1);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, World world, List<String> list, ITooltipFlag flag)
    {
        list.add("\u00a7cNot Implemented");
    }
}

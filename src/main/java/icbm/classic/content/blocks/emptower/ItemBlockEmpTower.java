package icbm.classic.content.blocks.emptower;

import icbm.classic.config.machines.ConfigEmpTower;
import icbm.classic.lib.LanguageUtility;
import icbm.classic.prefab.item.ItemBlockSubTypes;
import net.minecraft.block.Block;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.List;

public class ItemBlockEmpTower extends ItemBlockSubTypes {
    public ItemBlockEmpTower(Block block) {
        super(block);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World world, List list, ITooltipFlag flag)
    {
        if(stack.getMetadata() == 1){
            list.add(LanguageUtility.getLocal(getUnlocalizedName(stack) + ".info").replace("%1$s", Integer.toString(ConfigEmpTower.BONUS_RADIUS)));
        }
        else {
            list.add(LanguageUtility.getLocal(getUnlocalizedName(stack) + ".info"));
        }
    }
}

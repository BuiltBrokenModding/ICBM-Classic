package icbm.classic.world.block.emptower;

import icbm.classic.config.machines.ConfigEmpTower;
import icbm.classic.lib.LanguageUtility;
import icbm.classic.prefab.item.ItemBlockSubTypes;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import javax.annotation.Nullable;
import java.util.List;

public class ItemBlockEmpTower extends ItemBlockSubTypes {
    public ItemBlockEmpTower(Block block) {
        super(block);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void addInformation(ItemStack stack, @Nullable Level level, List list, ITooltipFlag flag) {
        if (stack.getMetadata() == 1) {
            list.add(LanguageUtility.getLocal(getUnlocalizedName(stack) + ".info").replace("%1$s", Integer.toString(ConfigEmpTower.BONUS_RADIUS)));
        } else {
            list.add(LanguageUtility.getLocal(getUnlocalizedName(stack) + ".info"));
        }
    }
}

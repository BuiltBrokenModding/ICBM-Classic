package icbm.classic.content.blocks.emptower;

import icbm.classic.ICBMConstants;
import icbm.classic.config.machines.ConfigEmpTower;
import icbm.classic.prefab.item.ItemBlockAbstract;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;

/**
 * Item for the EMP tower (extender = false) and the EMP tower extender (extender = true, 1.12 meta 1)
 */
public class ItemBlockEmpTower extends ItemBlockAbstract {

    private final boolean extender;

    public ItemBlockEmpTower(Block block, boolean extender) {
        super(block);
        this.extender = extender;
    }

    public boolean isExtender() {
        return extender;
    }

    @Override
    public String getDescriptionId()
    {
        return "tile." + ICBMConstants.PREFIX + "emptower." + (extender ? 1 : 0);
    }

    @Override
    public String getDescriptionId(ItemStack stack)
    {
        return getDescriptionId();
    }

    @Override
    public void registerBlocks(Map<Block, Item> blockToItemMap, Item item)
    {
        // Only the main tower item is the block's item, otherwise Block#asItem would return the extender
        if(!extender) {
            super.registerBlocks(blockToItemMap, item);
        }
    }

    @Nullable
    @Override
    protected BlockState getPlacementState(BlockPlaceContext context)
    {
        final BlockState state = super.getPlacementState(context);
        if(state != null && extender && state.hasProperty(BlockEmpTower.TOWER_MODELS)) {
            return state.setValue(BlockEmpTower.TOWER_MODELS, PropertyTowerStates.EnumTowerTypes.SPIN);
        }
        return state;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> list, TooltipFlag flag)
    {
        // PORT: 1.12 replaced the generic ItemBlockAbstract info with only this line
        if(extender){
            list.add(Component.translatable(getDescriptionId(stack) + ".info", Integer.toString(ConfigEmpTower.BONUS_RADIUS)));
        }
        else {
            list.add(Component.translatable(getDescriptionId(stack) + ".info"));
        }
    }
}
// PORTED

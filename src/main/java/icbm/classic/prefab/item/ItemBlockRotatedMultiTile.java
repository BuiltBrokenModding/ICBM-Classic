package icbm.classic.prefab.item;

import icbm.classic.content.blocks.multiblock.MultiBlockHelper;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.List;
import java.util.function.Function;

/**
 * Handles placement check for asymmetric multi-block structures that need rotation
 *
 *
 * Created by Dark(DarkGuardsman, Robert) on 3/16/2018.
 */
public class ItemBlockRotatedMultiTile extends ItemBlockSubTypes
{
    //Wrapper for getting the multi-block data for the rotation
    protected final Function<EnumFacing, List<BlockPos>> multiBlockGetter;

    public ItemBlockRotatedMultiTile(Block block, Function<EnumFacing, List<BlockPos>> multiBlockGetter)
    {
        super(block);
        this.multiBlockGetter = multiBlockGetter;
    }

    @Override
    protected boolean canPlace(EntityPlayer player, World worldIn, BlockPos pos, ItemStack itemstack, EnumFacing facing, float hitX, float hitY, float hitZ)
    {
        if (super.canPlace(player, worldIn, pos, itemstack, facing, hitX, hitY, hitZ))
        {
            List<BlockPos> multi_blocks = multiBlockGetter.apply(player.getHorizontalFacing());
            if (multi_blocks != null)
            {
                return MultiBlockHelper.canBuild(worldIn, pos, multi_blocks, true);
            }
        }
        return false;
    }
}

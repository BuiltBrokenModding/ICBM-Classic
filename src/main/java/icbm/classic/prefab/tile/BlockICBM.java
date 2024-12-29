package icbm.classic.prefab.tile;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;

@Deprecated
public abstract class BlockICBM extends Block
{
    public BlockICBM(Properties p_i48440_1_) {
        super(p_i48440_1_);
    }

    @Override
    public void onPlayerDestroy(IWorld worldIn, BlockPos pos, BlockState state)
    {
        /*if(dropInventory) {
            InventoryUtility.dropInventory(worldIn, pos);
        }*/
    }
}

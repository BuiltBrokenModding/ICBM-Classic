package icbm.classic.client.fx;

import com.google.common.collect.Lists;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

public final class ParticleCollisionLogic
{
    //Taken from world code, customized to allow ignoring some blocks
    public static List<AxisAlignedBB> getCollisionBoxes(World world, AxisAlignedBB aabb, Function<IBlockState, Boolean> allowCollision)
    {
        final List<AxisAlignedBB> outList = Lists.<AxisAlignedBB>newArrayList();

        final int startX = MathHelper.floor(aabb.minX) - 1;
        final int endX = MathHelper.ceil(aabb.maxX) + 1;
        final int startY = MathHelper.floor(aabb.minY) - 1;
        final int endY = MathHelper.ceil(aabb.maxY) + 1;
        final int startZ = MathHelper.floor(aabb.minZ) - 1;
        final int endZ = MathHelper.ceil(aabb.maxZ) + 1;

        final BlockPos.PooledMutableBlockPos blockPos = BlockPos.PooledMutableBlockPos.retain();

        try
        {
            for (int posX = startX; posX < endX; ++posX)
            {
                for (int posZ = startZ; posZ < endZ; ++posZ)
                {
                    boolean flag2 = posX == startX || posX == endX - 1;
                    boolean flag3 = posZ == startZ || posZ == endZ - 1;

                    if ((!flag2 || !flag3) && world.isBlockLoaded(blockPos.setPos(posX, 64, posZ)))
                    {
                        for (int posY = startY; posY < endY; ++posY)
                        {
                            if (!flag2 && !flag3 || posY != endY - 1)
                            {
                                blockPos.setPos(posX, posY, posZ);
                                IBlockState blockState = world.getBlockState(blockPos);

                                if(allowCollision.apply(blockState))
                                {
                                    blockState.addCollisionBoxToList(world, blockPos, aabb, outList, null, false);
                                }

                            }
                        }
                    }
                }
            }
        } finally
        {
            blockPos.release();
        }

        return outList;
    }
}

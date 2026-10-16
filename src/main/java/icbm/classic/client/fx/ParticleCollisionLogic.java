package icbm.classic.client.fx;

import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public final class ParticleCollisionLogic
{
    private ParticleCollisionLogic()
    {
    }

    /**
     * Taken from world code, customized to allow ignoring some blocks
     * <p>
     * PORT: returns world space voxel shapes for use with {@link net.minecraft.world.phys.shapes.Shapes#collide}.
     */
    public static List<VoxelShape> getCollisionBoxes(Level world, AABB aabb, Function<BlockState, Boolean> allowCollision)
    {
        final List<VoxelShape> outList = new ArrayList<>();

        final int startX = Mth.floor(aabb.minX) - 1;
        final int endX = Mth.ceil(aabb.maxX) + 1;
        final int startY = Mth.floor(aabb.minY) - 1;
        final int endY = Mth.ceil(aabb.maxY) + 1;
        final int startZ = Mth.floor(aabb.minZ) - 1;
        final int endZ = Mth.ceil(aabb.maxZ) + 1;

        final BlockPos.MutableBlockPos blockPos = new BlockPos.MutableBlockPos();

        for (int posX = startX; posX < endX; ++posX)
        {
            for (int posZ = startZ; posZ < endZ; ++posZ)
            {
                boolean flag2 = posX == startX || posX == endX - 1;
                boolean flag3 = posZ == startZ || posZ == endZ - 1;

                if ((!flag2 || !flag3) && world.hasChunkAt(blockPos.set(posX, 64, posZ)))
                {
                    for (int posY = startY; posY < endY; ++posY)
                    {
                        if (!flag2 && !flag3 || posY != endY - 1)
                        {
                            blockPos.set(posX, posY, posZ);
                            final BlockState blockState = world.getBlockState(blockPos);

                            if (allowCollision.apply(blockState))
                            {
                                final VoxelShape shape = blockState.getCollisionShape(world, blockPos);
                                if (!shape.isEmpty())
                                {
                                    final VoxelShape moved = shape.move(posX, posY, posZ);
                                    if (moved.bounds().intersects(aabb))
                                    {
                                        outList.add(moved);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        return outList;
    }
}
// PORTED

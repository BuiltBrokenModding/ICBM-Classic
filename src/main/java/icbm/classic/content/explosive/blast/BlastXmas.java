package icbm.classic.content.explosive.blast;

import icbm.classic.content.entity.mobs.*;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.IEntityLivingData;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;

/**
 * @see <a href="https://github.com/BuiltBrokenModding/VoltzEngine/blob/development/license.md">License</a> for what you can and can't do with the code.
 * Created by Dark(DarkGuardsman, Robert) on 12/29/2018.
 */
public class BlastXmas extends Blast
{
    public int callCountEnd = 20;

    final boolean zombie;

    public BlastXmas(boolean zombie)
    {
        this.zombie = zombie;
    }

    @Override
    protected void doExplode(int callCount)
    {
        if (!world.isRemote)
        {
            if (callCount == 0)
            {
                generateGround();
            }
            else if (callCount % 2 == 0)
            {
                spawnMobs();
            }

            //End explosion if we hit the timer end
            if (callCount > this.callCountEnd)
            {
                spawnEntity(zombie ? new EntityXmasZombieBoss(world()) : new EntityXmasSkeletonBoss(world()), x, y + 4, z);
                this.controller.endExplosion();
            }
        }
    }

    @Override
    public int proceduralInterval()
    {
        return 1;
    }

    public void generateGround()
    {
        final EnumFacing[] directions = new EnumFacing[]{EnumFacing.NORTH, EnumFacing.EAST, EnumFacing.SOUTH, EnumFacing.WEST, EnumFacing.DOWN};
        final Queue<BlockPos> blocksToPath = new LinkedList();
        final Set<BlockPos> pathed = new HashSet();

        final Queue<BlockPos> editBlocks = new LinkedList();

        final BlockPos center = new BlockPos(xi(), yi() + 1, zi());

        //Add center as first path
        blocksToPath.offer(center);
        pathed.add(center);

        //Loop until down
        while (blocksToPath.peek() != null)
        {
            //Get next on path
            final BlockPos pos = blocksToPath.poll();

            //Get block
            IBlockState blockState = world.getBlockState(pos);
            Block block = blockState.getBlock();

            //If is replaceable add to edit list
            if (block.isReplaceable(world, pos))
            {
                editBlocks.offer(pos);
            }

            //Path
            for (EnumFacing dir : directions)
            {
                final BlockPos next = pos.offset(dir);
                if (!pathed.contains(next))
                {
                    //Get block data
                    blockState = world.getBlockState(next);
                    block = blockState.getBlock();

                    //Check if is replaceable and is in range
                    if (block.isReplaceable(world, next) && isInRange(center, next))
                    {
                        //Add to path
                        blocksToPath.offer(next);
                    }

                    //Add to pathed so we do not path check again
                    pathed.add(next);
                }
            }
        }

        //Place blocks
        editBlocks.forEach(edit -> world.setBlockState(edit, Blocks.SNOW.getDefaultState()));
    }

    public boolean isInRange(BlockPos center, BlockPos pos)
    {
        //Get difference in distance per axis
        final int deltaX = Math.abs(center.getX() - pos.getX());
        final int deltaZ = Math.abs(center.getZ() - pos.getZ());
        final int deltaY = Math.abs(center.getY() - pos.getY());

        //Map Y limit
        if (pos.getY() > center.getY() || pos.getY() <= 0 || pos.getY() >= 254)
        {
            return false;
        }

        //Limit by flat distance in each axis
        final int distanceAllowed = (int) getBlastRadius() + deltaY;
        if (deltaX <= distanceAllowed && deltaZ <= distanceAllowed)
        {
            //Limit by manhattan distance
            return deltaX + deltaZ <= distanceAllowed;
        }
        return false;
    }


    public void spawnMobs()
    {
        if (!this.world().isRemote)
        {
            int spawned = 0;

            //Loop several times, more than spawned so we have a few extra random tries
            for (int i = 0; i < 8; i++)
            {
                //Random location
                float range = getBlastRadius() - 3;
                double x = this.location.x() + 0.5 + (world.rand.nextFloat() * range - world.rand.nextFloat() * range);
                double z = this.location.z() + 0.5 + (world.rand.nextFloat() * range - world.rand.nextFloat() * range);
                double y = findEmptyY(x, this.location.y() + 2, z);

                //If empty spot, spawn mob
                if (y >= 0)
                {
                    spawnMob(x, y, z);

                    //Count mobs spawned
                    spawned++;

                    //Only spawn so many mods
                    if (spawned >= 4)
                    {
                        return;
                    }
                }
            }
        }
    }

    protected void spawnMob(double x, double y, double z)
    {
        EntityXmasMob entity;

        //Get entity to spawn
        final float randomSpawnChance = world.rand.nextFloat();
        if(!zombie)
        {
            if (randomSpawnChance < 0.8)
            {
                entity = new EntityXmasSkeleton(world());
            }
            else
            {
                entity = new EntityXmasSnowman(world());
            }
        }
        else
        {
            if (randomSpawnChance < 0.8)
            {
                entity = new EntityXmasZombie(world());
            }
            else
            {
                entity = new EntityXmasCreeper(world());
            }

        }
        spawnEntity(entity, x, y, z);
    }

    protected void spawnEntity(EntityXmasMob entity, double x, double y, double z)
    {
        //Update position and trigger init
        entity.setPositionAndRotation(x, y, z, MathHelper.wrapDegrees(world.rand.nextFloat() * 360.0F), 0);
        entity.onInitialSpawn(world.getDifficultyForLocation(new BlockPos(entity)), (IEntityLivingData) null);

        //Place in world
        this.world().spawnEntity(entity);
    }

    protected double findEmptyY(double x, double y, double z)
    {
        for (int j = 0; j < 10; j++)
        {
            BlockPos pos = new BlockPos(x, y, z);
            BlockPos pos2 = pos.up();

            if (world.isAirBlock(pos) && world.isAirBlock(pos2))
            {
                return y;
            }
            else
            {
                y++;
            }
        }
        return -1;
    }


    @Override
    public float getBlastRadius()
    {
        return 20;
    }
}

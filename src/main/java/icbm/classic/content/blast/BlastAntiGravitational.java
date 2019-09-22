package icbm.classic.content.blast;

import icbm.classic.ICBMClassic;
import icbm.classic.api.explosion.IBlastTickable;
import icbm.classic.content.blast.thread.ThreadSmallExplosion;
import icbm.classic.content.blast.threaded.BlastThreaded;
import icbm.classic.content.entity.EntityFlyingBlock;
import icbm.classic.lib.transform.PosDistanceSorter;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

public class BlastAntiGravitational extends BlastThreaded implements IBlastTickable
{
    protected ThreadSmallExplosion thread;
    protected Set<EntityFlyingBlock> flyingBlocks = new HashSet<EntityFlyingBlock>();

    @Override
    public void setupBlast()
    {
        if (!this.world().isRemote)
        {
            this.thread = new ThreadSmallExplosion(this, (int) this.getBlastRadius(), this.exploder);
            this.thread.start();
        }

        //this.oldWorld().playSoundEffect(position.x(), position.y(), position.z(), References.PREFIX + "antigravity", 6.0F, (1.0F + (oldWorld().rand.nextFloat() - oldWorld().rand.nextFloat()) * 0.2F) * 0.7F);
    }

    @Override
    public boolean doRun(int loops, Consumer<BlockPos> edits)
    {
        int ymin = -this.getPos().getY();
        int ymax = 255 -this.getPos().getY();
        BlastHelpers.loopInRadius(this.getBlastRadius(), (x, y, z) ->{

            if (y >= ymin && y < ymax)
            {
                edits.accept(new BlockPos(xi() + x, yi() + y, zi() + z));
            }
        });
        return false;
    }

    @Override
    public boolean doExplode(int callCount) //TODO rewrite entire method
    {
        int r = this.callCount;

        if (world() != null && !this.world().isRemote)
        {
            try
            {
                if (this.thread != null) //TODO replace thread check with callback triggered by thread and delayed into main thread
                {
                    if (this.thread.isComplete)
                    {
                        //Copy as concurrent list is not fast to sort
                        List<BlockPos> results = new ArrayList();
                        results.addAll(getThreadResults());

                        if (r == 0)
                        {
                            Collections.sort(results, new PosDistanceSorter(location, true));
                        }
                        int blocksToTake = 20;

                        for (BlockPos targetPosition : results)
                        {
                            final IBlockState blockState = world.getBlockState(targetPosition);
                            if (!blockState.getBlock().isAir(blockState, world, targetPosition))
                            {
                                float hardness = blockState.getBlockHardness(world, targetPosition);
                                if (hardness >= 0 && hardness < 1000)
                                {
                                    if (world().rand.nextInt(3) > 0)
                                    {
                                        //Remove block
                                        world.setBlockToAir(targetPosition);

                                        //Mark blocks taken
                                        blocksToTake--;
                                        if (blocksToTake <= 0)
                                        {
                                            break;
                                        }

                                        //Create flying block
                                        EntityFlyingBlock entity = new EntityFlyingBlock(world(), targetPosition, blockState, 0);
                                        entity.yawChange = 50 * world().rand.nextFloat();
                                        entity.pitchChange = 100 * world().rand.nextFloat();
                                        entity.motionY += Math.max(0.15 * world().rand.nextFloat(), 0.1);
                                        entity.noClip = true;
                                        world().spawnEntity(entity);

                                        //Track flying block
                                        flyingBlocks.add(entity);
                                    }
                                }
                            }
                        }
                    }
                }
                else
                {
                    String msg = String.format("BlastAntiGravitational#doPostExplode() -> Failed to run due to null thread" +
                                    "\nWorld = %s " +
                                    "\nThread = %s" +
                                    "\nSize = %s" +
                                    "\nPos = ",
                            world, thread, size, location);
                    ICBMClassic.logger().error(msg);
                }
            }
            catch (Exception e)
            {
                String msg = String.format("BlastAntiGravitational#doPostExplode() ->  Unexpected error while running post detonation code " +
                                "\nWorld = %s " +
                                "\nThread = %s" +
                                "\nSize = %s" +
                                "\nPos = ",
                        world, thread, size, location);
                ICBMClassic.logger().error(msg, e);
            }
        }

        int radius = (int) this.getBlastRadius();
        AxisAlignedBB bounds = new AxisAlignedBB(location.x() - radius, location.y() - radius, location.z() - radius, location.y() + radius, 100, location.z() + radius);
        List<Entity> allEntities = world().getEntitiesWithinAABB(Entity.class, bounds);

        for (Entity entity : allEntities)
        {
            if (!(entity instanceof EntityFlyingBlock) && entity.posY < 100 + location.y())
            {
                if (entity.motionY < 0.4)
                {
                    entity.motionY += 0.15;
                }
            }
        }

        return this.callCount > 20 * 120;
    }

    @Override
    protected void onBlastCompleted()
    {
        flyingBlocks.forEach(EntityFlyingBlock::restoreGravity);
    }
}

package icbm.classic.content.blast;

import icbm.classic.ICBMClassic;
import icbm.classic.api.explosion.IBlastTickable;
import icbm.classic.client.ICBMSounds;
import icbm.classic.config.ConfigDebug;
import icbm.classic.content.blast.thread.ThreadLargeExplosion;
import icbm.classic.content.blocks.explosive.BlockExplosive;
import icbm.classic.content.entity.flyingblock.FlyingBlock;
import icbm.classic.content.reg.BlockReg;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;

import java.util.Iterator;
import java.util.List;

public class BlastSonic extends Blast implements IBlastTickable
{
    private boolean hasShockWave = false;

    public BlastSonic()
    {

    }

    public Blast setShockWave()
    {
        this.hasShockWave = true;
        return this;
    }

    public void firstTick()
    {
        //TODO remove thread
        createAndStartThread(new ThreadLargeExplosion(this, (int) this.getBlastRadius(), getBlastRadius() * 2, this.exploder));

        ICBMSounds.SONICWAVE.play(world, x(), y(), z(), 4.0F, (1.0F + (this.world().rand.nextFloat() - this.world().rand.nextFloat()) * 0.2F) * 0.7F, true);
    }

    @Override
    public boolean doExplode(int callCount) //TODO Rewrite this entire method
    {
        if (callCount <= 0)
        {
            firstTick();
        }
        final int radius = Math.max(1, this.callCount); //TODO scale off of size & callcout
        if (callCount % 4 == 0)
        {
            if (isThreadCompleted())
            {
                if (!getThreadResults().isEmpty())
                {
                    final Iterator<BlockPos> it = getThreadResults().iterator();
                    //TODO rewrite this to not be threaded, instead iterate out each tick and remove X number of blocks

                    while (it.hasNext())
                    {
                        final BlockPos targetPosition = it.next();

                        final double deltaX = targetPosition.getX() - x();
                        final double deltaY = targetPosition.getY() - y();
                        final double deltaZ = targetPosition.getZ() - z();
                        final double distance = Math.sqrt(deltaX * deltaX + deltaY * deltaY + deltaZ * deltaZ);

                        //Only act on blocks inside the current radius TODO scale radius separate from ticks so we can control block creation
                        if (distance <= radius) //TODO consider making less round?
                        {
                            //Remove
                            it.remove();

                            //Get data
                            final BlockState blockState = world.getBlockState(targetPosition);
                            final Block block = blockState.getBlock();

                            //Only act on movable blocks
                            if (!block.isAir(blockState, world, targetPosition) && blockState.getBlockHardness(world, targetPosition) >= 0)
                            {
                                //Trigger explosions
                                if (block instanceof BlockExplosive)  //TODO abstract away into an action to allow flexiblity per block
                                {
                                    ((BlockExplosive) block).doAction(world, targetPosition, null); //TODO add cause
                                }

                                if(this.world().rand.nextFloat() < 0.1) {  //TODO add config for chance, increase chance if we fail to spawn a block
                                    FlyingBlock.spawnFlyingBlock(world, targetPosition, null, null);
                                }
                                this.world().removeBlock(targetPosition, false);
                            }
                        }
                    }
                }
                else
                {
                    this.endBlast();
                    if (ConfigDebug.DEBUG_THREADS)
                    {
                        String msg = String.format("BlastSonic#doPostExplode() -> Thread failed to find blocks to edit. Either thread failed or no valid blocks were found in range." +
                                "\nWorld = %s " +
                                "\nThread = %s" +
                                "\nSize = %s" +
                                "\nPos = %s",
                                world, getThread(), size, getPosition());
                        ICBMClassic.logger().error(msg);
                    }
                }
            }
        }

        final int entityEffectRadius = 2 * this.callCount; //TODO scale to radius
        final AxisAlignedBB bounds = new AxisAlignedBB(
                x() - entityEffectRadius, y() - entityEffectRadius, z() - entityEffectRadius,
                x() + entityEffectRadius, y() + entityEffectRadius, z() + entityEffectRadius);

        final List<Entity> allEntities = this.world().getEntitiesWithinAABB(Entity.class, bounds);
        for (Entity entity : allEntities)
        {
            if (!(entity instanceof PlayerEntity) || !((PlayerEntity) entity).isCreative())
            {
                //Get difference
                double xDelta = entity.posX - x();
                double yDelta = entity.posY - y();
                double zDelta = entity.posZ - z();

                //Normalize
                float distance = MathHelper.sqrt(xDelta * xDelta + yDelta * yDelta + zDelta * zDelta);
                xDelta = xDelta / (double) distance;
                yDelta = yDelta / (double) distance;
                zDelta = zDelta / (double) distance;

                //Scale
                final double scale = Math.max(0, (1 - (distance / getBlastRadius()))) * 3;
                xDelta *= scale;
                yDelta *= scale;
                zDelta *= scale;

                entity.addVelocity(
                    xDelta * this.world().rand.nextFloat() * 0.2,
                    Math.abs(yDelta * this.world().rand.nextFloat()) * 1,
                    zDelta * this.world().rand.nextFloat() * 0.2
                );
            }
        }

        return this.callCount > this.getBlastRadius();
    }
}

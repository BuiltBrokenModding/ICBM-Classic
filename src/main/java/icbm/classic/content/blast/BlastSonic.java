package icbm.classic.content.blast;

import icbm.classic.ICBMClassic;
import icbm.classic.api.explosion.IBlastTickable;
import icbm.classic.client.ICBMSounds;
import icbm.classic.config.ConfigDebug;
import icbm.classic.content.blast.thread.ThreadLargeExplosion;
import icbm.classic.content.blocks.explosive.TileEntityExplosive;
import icbm.classic.content.entity.EntityFlyingBlock;
import icbm.classic.content.reg.BlockReg;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fluids.BlockFluidBase;
import net.minecraftforge.fluids.IFluidBlock;

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

        ICBMSounds.SONICWAVE.play(world, location.x(), location.y(), location.z(), 4.0F, (1.0F + (this.world().rand.nextFloat() - this.world().rand.nextFloat()) * 0.2F) * 0.7F, true);
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

                        final double distance = location.distance(targetPosition);

                        //Only act on blocks inside the current radius TODO scale radius separate from ticks so we can control block creation
                        if (distance <= radius) //TODO consider making less round?
                        {
                            //Remove
                            it.remove();

                            //Get data
                            final IBlockState blockState = world.getBlockState(targetPosition);
                            final Block block = blockState.getBlock();

                            //Only act on movable blocks
                            if (!block.isAir(blockState, world, targetPosition) && blockState.getBlockHardness(world, targetPosition) >= 0)
                            {
                                //Trigger explosions
                                if (block == BlockReg.blockExplosive)  //TODO add handle to trigger more blocks
                                {
                                    final TileEntity tile = this.world().getTileEntity(targetPosition);
                                    if(tile instanceof TileEntityExplosive) {
                                        ((TileEntityExplosive)tile).trigger(false);
                                    }
                                }

                                //Destroy block
                                this.world().setBlockToAir(targetPosition);

                                //Create floating block
                                if (!(block instanceof IFluidBlock) //TODO add ban list covered by a utility, but also try fixing fluids by causing a rain/slash effect
                                        && this.world().rand.nextFloat() < 0.1) //TODO add config for chance, increase chance if we fail to spawn a block
                                {
                                    this.world().spawnEntity(new EntityFlyingBlock(this.world(), targetPosition, blockState)); //TODO move to helper wrapped with validation
                                }
                            }
                        }
                    }
                }
                else
                {
                    isAlive = false;
                    if (ConfigDebug.DEBUG_THREADS)
                    {
                        String msg = String.format("BlastSonic#doPostExplode() -> Thread failed to find blocks to edit. Either thread failed or no valid blocks were found in range." +
                                "\nWorld = %s " +
                                "\nThread = %s" +
                                "\nSize = %s" +
                                "\nPos = %s",
                                world, getThread(), size, location);
                        ICBMClassic.logger().error(msg);
                    }
                }
            }
        }

        final int entityEffectRadius = 2 * this.callCount; //TODO scale to radius
        final AxisAlignedBB bounds = new AxisAlignedBB(
                location.x() - entityEffectRadius, location.y() - entityEffectRadius, location.z() - entityEffectRadius,
                location.x() + entityEffectRadius, location.y() + entityEffectRadius, location.z() + entityEffectRadius);

        final List<Entity> allEntities = this.world().getEntitiesWithinAABB(Entity.class, bounds);
        for (Entity entity : allEntities)
        {
            if (!(entity instanceof EntityPlayer) || !((EntityPlayer) entity).isCreative())
            {
                //Get difference
                double xDelta = entity.posX - location.x();
                double yDelta = entity.posY - location.y();
                double zDelta = entity.posZ - location.z();

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

                entity.motionX += xDelta * this.world().rand.nextFloat() * 0.2;
                entity.motionY += Math.abs(yDelta * this.world().rand.nextFloat()) * 1;
                entity.motionZ += zDelta * this.world().rand.nextFloat() * 0.2;
            }
        }

        return this.callCount > this.getBlastRadius();
    }
}

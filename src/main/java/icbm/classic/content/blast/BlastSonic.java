package icbm.classic.content.blast;

import icbm.classic.ICBMClassic;
import icbm.classic.api.caps.IMissile;
import icbm.classic.api.events.BlastBlockModifyEvent;
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
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.common.MinecraftForge;
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
        /* TODO re-add?
            if (this.hasShockWave)
            {
                for (int x = (int) (-this.getRadius() * 2); x < this.getRadius() * 2; ++x)
                {
                    for (int y = (int) (-this.getRadius() * 2); y < this.getRadius() * 2; ++y)
                    {
                        for (int z = (int) (-this.getRadius() * 2); z < this.getRadius() * 2; ++z)
                        {
                            Location targetPosition = position.add(new Pos(x, y, z));
                            Block blockID = world().getBlock(targetPosition.xi(), targetPosition.yi(), targetPosition.zi());

                            if (blockID != Blocks.air)
                            {
                                Material material = blockID.getMaterial();

                                if (blockID != Blocks.bedrock && !(material.isLiquid()) && (blockID.getExplosionResistance(this.exploder, world(), targetPosition.xi(), targetPosition.yi(), targetPosition.zi(), position.xi(), position.yi(), position.zi()) > this.power || material == Material.glass))
                                {
                                    targetPosition.setBlock(world(), Blocks.air);
                                }
                            }
                        }
                    }
                }
            } */

        //TODO remove thread
        createAndStartThread(new ThreadLargeExplosion(this, (int) this.getBlastRadius(), getBlastRadius() * 2, this.exploder));

        if (this.hasShockWave)
        {
            ICBMSounds.HYPERSONIC.play(world, location.x(), location.y(), location.z(), 4.0F, (1.0F + (this.world().rand.nextFloat() - this.world().rand.nextFloat()) * 0.2F) * 0.7F, true);
        }
        else
        {
            ICBMSounds.SONICWAVE.play(world, location.x(), location.y(), location.z(), 4.0F, (1.0F + (this.world().rand.nextFloat() - this.world().rand.nextFloat()) * 0.2F) * 0.7F, true);
        }
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
                        if (distance <= radius)
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
                                if (block == BlockReg.blockExplosive)
                                {
                                    ((TileEntityExplosive) this.world().getTileEntity(targetPosition)).trigger(false);
                                }

                                // Note that we still want to trigger the explosives as above no matter what
                                MinecraftForge.EVENT_BUS.post(new BlastBlockModifyEvent(world, targetPosition,
                                    () -> {
                                        //Destroy block
                                        this.world().setBlockToAir(targetPosition);

                                        //Create floating block
                                        if (!(block instanceof BlockFluidBase || block instanceof IFluidBlock)
                                                && this.world().rand.nextFloat() < 0.1)
                                        {
                                            this.world().spawnEntity(new EntityFlyingBlock(this.world(), targetPosition, blockState));
                                        }
                                    }
                                ));
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
            if (entity instanceof IMissile)
            {
                ((IMissile) entity).destroyMissile(true); //TODO change from guided to dummy fire
            }
            else if (!(entity instanceof EntityPlayer) || !((EntityPlayer) entity).isCreative())
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

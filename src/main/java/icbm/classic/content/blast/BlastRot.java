package icbm.classic.content.blast;

import icbm.classic.ICBMClassic;
import icbm.classic.api.explosion.IBlastTickable;
import icbm.classic.config.ConfigDebug;
import icbm.classic.content.blast.thread.ThreadLargeExplosion;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fluids.FluidRegistry;

import java.util.Iterator;

/**
 * Creates radiation spawning
 *
 * @author Calclavia
 */
public class BlastRot extends Blast implements IBlastTickable
{
    private float energy;

    public BlastRot(float energy)
    {
        this.energy = energy;
    }

    @Override
    public void setupBlast()
    {
        if (!this.world().isRemote)
        {
            createAndStartThread(new ThreadLargeExplosion(this, (int) this.getBlastRadius(), this.energy, this.exploder));
        }
    }

    @Override
    public boolean doExplode(int callCount)
    {
        if (isThreadCompleted())
        {
            if (!getThreadResults().isEmpty()) //TODO replace thread check with callback triggered by thread and delayed into main thread
            {
                Iterator<BlockPos> it = getThreadResults().iterator();
                while (it.hasNext())
                {
                    BlockPos targetPosition = it.next();
                    /** Decay the blocks. */
                    IBlockState blockState = world.getBlockState(targetPosition);
                    Block block = blockState.getBlock();

                    if (block == Blocks.GRASS || block == Blocks.SAND)
                    {
                        if (this.world().rand.nextFloat() > 0.96)
                        {
                            world.setBlockState(targetPosition, ICBMClassic.blockRadioactive.getDefaultState(), 3);
                        }
                    }

                    if (block == Blocks.STONE)
                    {
                        if (this.world().rand.nextFloat() > 0.99)
                        {
                            world.setBlockState(targetPosition, ICBMClassic.blockRadioactive.getDefaultState(), 3);
                        }
                    }

                    else if (blockState.getMaterial() == Material.LEAVES || blockState.getMaterial() == Material.PLANTS)
                    {
                        world.setBlockToAir(targetPosition);
                    }
                    else if (block == Blocks.FARMLAND)
                    {
                        world.setBlockState(targetPosition, ICBMClassic.blockRadioactive.getDefaultState(), 3);
                    }
                    else if (blockState.getMaterial() == Material.WATER)
                    {
                        if (FluidRegistry.getFluid("toxicwaste") != null)
                        {
                            Block blockToxic = FluidRegistry.getFluid("toxicwaste").getBlock();
                            if (blockToxic != null)
                            {
                                world.setBlockState(targetPosition, blockToxic.getDefaultState(), 3);
                            }
                        }
                    }
                }

                return true;

            }
            else
            {
                if (ConfigDebug.DEBUG_THREADS)
                {
                    String msg = String.format("BlastRot#doPostExplode() -> Thread failed to find blocks to edit. Either thread failed or no valid blocks were found in range." +
                                    "\nWorld = %s " +
                                    "\nThread = %s" +
                                    "\nSize = %s" +
                                    "\nPos = %s",
                            world, getThread(), size, location);
                    ICBMClassic.logger().error(msg);
                }
                return true;
            }
        }
        return false;
    }
}

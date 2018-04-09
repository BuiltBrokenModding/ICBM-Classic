package icbm.classic.content.explosive.blast;

import icbm.classic.ICBMClassic;
import icbm.classic.content.explosive.thread.ThreadLargeExplosion;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidRegistry;

/**
 * Creates radiation spawning
 *
 * @author Calclavia
 */
public class BlastRot extends Blast
{
    private ThreadLargeExplosion thread;
    private float energy;

    public BlastRot(World world, Entity entity, double x, double y, double z, float size)
    {
        super(world, entity, x, y, z, size);
    }

    public BlastRot(World world, Entity entity, double x, double y, double z, float size, float energy)
    {
        this(world, entity, x, y, z, size);
        this.energy = energy;
    }

    @Override
    public void doPreExplode()
    {
        if (!this.world().isRemote)
        {
            this.thread = new ThreadLargeExplosion(this, (int) this.getBlastRadius(), this.energy, this.exploder);
            this.thread.start();
        }
    }

    @Override
    public void doExplode()
    {
        try
        {
            if (this.thread != null) //TODO replace thread check with callback triggered by thread and delayed into main thread
            {
                if (!this.world().isRemote)
                {
                    if (this.thread.isComplete)
                    {
                        for (BlockPos targetPosition : this.thread.results)
                        {
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

                        this.controller.endExplosion();
                    }
                }
            }
            else
            {
                String msg = String.format("BlastNuclear#doPostExplode() -> Failed to run due to null thread" +
                                "\nWorld = %s " +
                                "\nThread = %s" +
                                "\nSize = %s" +
                                "\nPos = ",
                        world, thread, size, position);
                ICBMClassic.logger().error(msg);
            }
        }
        catch (Exception e)
        {
            String msg = String.format("BlastRot#doPostExplode() ->  Unexpected error while running post detonation code " +
                            "\nWorld = %s " +
                            "\nThread = %s" +
                            "\nSize = %s" +
                            "\nPos = ",
                    world, thread, size, position);
            ICBMClassic.logger().error(msg, e);
        }
    }

    @Override
    public int proceduralInterval()
    {
        return 1;
    }

    @Override
    public long getEnergy()
    {
        return 100;
    }
}

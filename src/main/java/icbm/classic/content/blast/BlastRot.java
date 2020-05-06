package icbm.classic.content.blast;

import icbm.classic.ICBMClassic;
import icbm.classic.api.events.BlastBlockModifyEvent;
import icbm.classic.api.explosion.IBlastTickable;
import icbm.classic.content.blast.threaded.BlastThreaded;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fluids.FluidRegistry;

import java.util.function.Consumer;

/**
 * Creates radiation spawning
 *
 * @author Calclavia
 */
public class BlastRot extends BlastThreaded implements IBlastTickable
{
    @Override
    public boolean doRun(int loops, Consumer<BlockPos> edits)
    {
        BlastHelpers.forEachPosInRadius(this.getBlastRadius(), (x, y, z) ->
        edits.accept(new BlockPos(xi() + x, yi() + y, zi() + z)));
        //TODO implement pathfinder so virus doesn't go through unbreakable air-tight walls
        return false;
    }

    @Override
    public void destroyBlock(BlockPos targetPosition)
    {
        //get block
        final IBlockState blockState = world.getBlockState(targetPosition);
        final Block block = blockState.getBlock();

        if (block == Blocks.GRASS || block == Blocks.SAND)
        {
            if (this.world().rand.nextFloat() > 0.96)
            {
                MinecraftForge.EVENT_BUS.post(new BlastBlockModifyEvent(world, targetPosition, ICBMClassic.blockRadioactive.getDefaultState(), 3));
            }
        }

        if (block == Blocks.STONE)
        {
            if (this.world().rand.nextFloat() > 0.99)
            {
                MinecraftForge.EVENT_BUS.post(new BlastBlockModifyEvent(world, targetPosition, ICBMClassic.blockRadioactive.getDefaultState(), 3));
            }
        }

        else if (blockState.getMaterial() == Material.LEAVES || blockState.getMaterial() == Material.PLANTS)
        {
            MinecraftForge.EVENT_BUS.post(new BlastBlockModifyEvent(world, targetPosition));
        }
        else if (block == Blocks.FARMLAND)
        {
            MinecraftForge.EVENT_BUS.post(new BlastBlockModifyEvent(world, targetPosition, ICBMClassic.blockRadioactive.getDefaultState(), 3));
        }
        else if (blockState.getMaterial() == Material.WATER)
        {
            if (FluidRegistry.getFluid("toxicwaste") != null)
            {
                Block blockToxic = FluidRegistry.getFluid("toxicwaste").getBlock();
                if (blockToxic != null)
                {
                    MinecraftForge.EVENT_BUS.post(new BlastBlockModifyEvent(world, targetPosition, blockToxic.getDefaultState(), 3));
                }
            }
        }
    }
}

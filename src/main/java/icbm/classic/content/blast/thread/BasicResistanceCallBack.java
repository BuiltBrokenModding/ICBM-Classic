package icbm.classic.content.blast.thread;

import icbm.classic.content.blast.Blast;
import net.minecraft.block.BlockState;
import net.minecraft.block.FlowingFluidBlock;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fluids.IFluidBlock;

@Deprecated
public class BasicResistanceCallBack implements IThreadCallBack
{
    public final Blast blast;

    public BasicResistanceCallBack(Blast blast)
    {
        this.blast = blast;
    }

    @Override
    public float getResistance(World world, Vec3d blastCenter, BlockPos pos, Entity source, BlockState block)
    {
        if (block.getBlock() instanceof FlowingFluidBlock || block.getBlock() instanceof IFluidBlock)
        {
            return 0.25f;
        }
        else
        {
            return block.getExplosionResistance(world, pos, source, blast);
        }
    }
}
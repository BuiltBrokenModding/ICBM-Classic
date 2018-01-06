package icbm.classic.content.explosive.thread;

import com.builtbroken.jlib.data.vector.IPos3D;
import icbm.classic.content.explosive.blast.Blast;
import net.minecraft.block.Block;
import net.minecraft.block.BlockLiquid;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fluids.IFluidBlock;

public class BasicResistanceCallBack implements IThreadCallBack
{
    public final Blast blast;

    public BasicResistanceCallBack(Blast blast)
    {
        this.blast = blast;
    }

    @Override
    public float getResistance(World world, IPos3D blastCenter, BlockPos pos, Entity source, Block block)
    {
        if (block instanceof BlockLiquid || block instanceof IFluidBlock)
        {
            return 0.25f;
        }
        else
        {
            return block.getExplosionResistance(world, pos, source, blast);
        }
    }
}
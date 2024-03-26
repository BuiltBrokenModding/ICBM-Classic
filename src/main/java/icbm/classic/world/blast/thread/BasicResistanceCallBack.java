package icbm.classic.world.blast.thread;

import com.builtbroken.jlib.data.vector.Vec3;
import icbm.classic.world.blast.Blast;
import net.minecraft.world.level.block.Block;
import net.minecraft.block.BlockLiquid;
import net.minecraft.world.entity.Entity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.neoforged.fluids.IFluidBlock;

@Deprecated
public class BasicResistanceCallBack implements IThreadCallBack {
    public final Blast blast;

    public BasicResistanceCallBack(Blast blast) {
        this.blast = blast;
    }

    @Override
    public float getResistance(Level level, Vec3 blastCenter, BlockPos pos, Entity source, Block block) {
        if (block instanceof BlockLiquid || block instanceof IFluidBlock) {
            return 0.25f;
        } else {
            return block.getExplosionResistance(world, pos, source, blast);
        }
    }
}
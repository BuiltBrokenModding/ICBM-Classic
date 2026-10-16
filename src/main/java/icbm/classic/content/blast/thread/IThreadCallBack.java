package icbm.classic.content.blast.thread;

import com.builtbroken.jlib.data.vector.IPos3D;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.entity.Entity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

@Deprecated
public interface IThreadCallBack
{
    // PORT: last parameter was Block, 1.21 resistance lookups need the state
    float getResistance(Level world, IPos3D blastCenter, BlockPos pos, Entity source, BlockState state);
}
// PORTED

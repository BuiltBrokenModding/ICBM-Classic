package icbm.classic.world.blast.thread;

import com.builtbroken.jlib.data.vector.Vec3;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;

@Deprecated
public interface IThreadCallBack {
    float getResistance(Level level, Vec3 blastCenter, BlockPos pos, Entity source, Block block);
}
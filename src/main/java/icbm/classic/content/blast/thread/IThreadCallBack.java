package icbm.classic.content.blast.thread;

import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

@Deprecated
public interface IThreadCallBack
{
    float getResistance(World world, Vec3d blastCenter, BlockPos pos, Entity source, BlockState block);
}
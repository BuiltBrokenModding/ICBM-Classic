package icbm.classic.lib.data;

import icbm.classic.api.data.IBoundBox;
import lombok.AllArgsConstructor;
import lombok.Data;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;

@Data
@AllArgsConstructor
public class BoundBlockPos implements IBoundBox<BlockPos> {

    private BlockPos min;
    private BlockPos max;

    public BoundBlockPos(int x, int y, int z, int i, int j, int k) {
        this.min = new BlockPos(x, y, z);
        this.max = new BlockPos(i, j, k);
    }

    public BoundBlockPos(Vec3 pos, int range) {
        this.min = new BlockPos((int) Math.floor(pos.x) - range, (int) Math.floor(pos.y) - range, (int) Math.floor(pos.z) - range);
        this.max = new BlockPos((int) Math.floor(pos.x) + range, (int) Math.floor(pos.y) + range, (int) Math.floor(pos.z) + range);
    }

    public BoundBlockPos(BlockPos pos, int range) {
        this.min = new BlockPos(pos.getX() - range, pos.getY() - range, pos.getZ() - range);
        this.max = new BlockPos(pos.getX() + range, pos.getY() + range, pos.getZ() + range);
    }

    @Override
    public BlockPos lowerBound() {
        return min;
    }

    @Override
    public BlockPos upperBound() {
        return max;
    }
}

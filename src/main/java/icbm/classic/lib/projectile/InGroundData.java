package icbm.classic.lib.projectile;

import icbm.classic.lib.saving.NbtSaveHandler;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.world.World;
import net.minecraftforge.common.util.INBTSerializable;

/**
 * Stores data about position, side, and block projectile is inside
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class InGroundData implements INBTSerializable<CompoundNBT> {
    /**
     * Block position projectile is stuck inside
     */
    private BlockPos pos;

    /**
     * Face of tile we are stuck inside
     */
    private Direction side;

    /**
     * Block state we are stuck inside
     */
    private BlockState state;

    public InGroundData(World world, BlockRayTraceResult hit) {
        this.pos = hit.getPos();
        this.side = hit.getFace();
        this.state = world.getBlockState(pos);
    }

    public Material getMaterial() {
        return state.getMaterial();
    }

    @Override
    public CompoundNBT serializeNBT() {
        return SAVE_LOGIC.save(this);
    }

    @Override
    public void deserializeNBT(CompoundNBT nbt) {
        SAVE_LOGIC.load(this, nbt);
    }

    private static final NbtSaveHandler<InGroundData> SAVE_LOGIC = new NbtSaveHandler<InGroundData>()
        .mainRoot()
        .nodeBlockPos("pos", InGroundData::getPos, InGroundData::setPos)
        .nodeFacing("side", InGroundData::getSide, InGroundData::setSide)
        .nodeBlockState("state", InGroundData::getState, InGroundData::setState)
        .base();
}

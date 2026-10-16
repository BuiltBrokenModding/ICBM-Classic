package icbm.classic.lib.projectile;

import icbm.classic.lib.saving.NbtSaveHandler;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.util.INBTSerializable;

/**
 * Stores data about position, side, and block projectile is inside
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class InGroundData implements INBTSerializable<CompoundTag> {
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

    public InGroundData(Level world, BlockHitResult hit) {
        this.pos = hit.getBlockPos();
        this.side = hit.getDirection();
        this.state = world.getBlockState(pos);
    }

    // PORT: getMaterial() removed, Material no longer exists. Use state.isAir() etc.

    @Override
    public CompoundTag serializeNBT(HolderLookup.Provider provider) {
        return SAVE_LOGIC.save(this);
    }

    @Override
    public void deserializeNBT(HolderLookup.Provider provider, CompoundTag nbt) {
        SAVE_LOGIC.load(this, nbt);
    }

    private static final NbtSaveHandler<InGroundData> SAVE_LOGIC = new NbtSaveHandler<InGroundData>()
        .mainRoot()
        .nodeBlockPos("pos", InGroundData::getPos, InGroundData::setPos)
        .nodeFacing("side", InGroundData::getSide, InGroundData::setSide)
        .nodeBlockState("state", InGroundData::getState, InGroundData::setState)
        .base();
}
// PORTED

package icbm.classic.lib.projectile;

import icbm.classic.lib.saving.NbtSaveHandler;
import icbm.classic.lib.saving.nodes.SaveNodeBlockPos;
import icbm.classic.lib.saving.nodes.SaveNodeBlockState;
import icbm.classic.lib.saving.nodes.SaveNodeFacing;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Value;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraftforge.common.util.INBTSerializable;

/**
 * Stores data about position, side, and block projectile is inside
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class InGroundData implements INBTSerializable<NBTTagCompound> {
    /**
     * Block position projectile is stuck inside
     */
    private BlockPos pos;

    /**
     * Face of tile we are stuck inside
     */
    private EnumFacing side;

    /**
     * Block state we are stuck inside
     */
    private IBlockState state;

    public InGroundData(World world, RayTraceResult hit) {
        this.pos = hit.getBlockPos();
        this.side = hit.sideHit;
        this.state = world.getBlockState(pos);
    }

    public Material getMaterial() {
        return state.getMaterial();
    }

    @Override
    public NBTTagCompound serializeNBT() {
        return SAVE_LOGIC.save(this);
    }

    @Override
    public void deserializeNBT(NBTTagCompound nbt) {
        SAVE_LOGIC.load(this, nbt);
    }

    private static final NbtSaveHandler<InGroundData> SAVE_LOGIC = new NbtSaveHandler<InGroundData>()
        .mainRoot()
        .nodeBlockPos("pos", InGroundData::getPos, InGroundData::setPos)
        .nodeFacing("side", InGroundData::getSide, InGroundData::setSide)
        .nodeBlockState("state", InGroundData::getState, InGroundData::setState)
        .base();
}

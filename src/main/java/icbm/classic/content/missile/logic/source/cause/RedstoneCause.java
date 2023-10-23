package icbm.classic.content.missile.logic.source.cause;

import icbm.classic.ICBMConstants;
import icbm.classic.lib.saving.NbtSaveHandler;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class RedstoneCause extends BlockCause {

    public static final ResourceLocation REG_NAME = new ResourceLocation(ICBMConstants.DOMAIN, "block.redstone");

    private EnumFacing side;

    public RedstoneCause(World world, BlockPos pos, IBlockState state, EnumFacing side) {
        super(world, pos, state);
        this.side = side;
    }

    @Override
    public ResourceLocation getRegistryName() {
        return REG_NAME;
    }

    @Override
    public NBTTagCompound serializeNBT() {
        return SAVE_LOGIC.save(this, super.serializeNBT());
    }

    @Override
    public void deserializeNBT(NBTTagCompound nbt) {
        super.deserializeNBT(nbt);
        SAVE_LOGIC.load(this, nbt);
    }

    private static final NbtSaveHandler<RedstoneCause> SAVE_LOGIC = new NbtSaveHandler<RedstoneCause>()
        .mainRoot()
        /* */.nodeFacing("side", RedstoneCause::getSide, RedstoneCause::setSide)
        .base();
}

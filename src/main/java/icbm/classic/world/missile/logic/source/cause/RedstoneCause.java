package icbm.classic.world.missile.logic.source.cause;

import icbm.classic.IcbmConstants;
import icbm.classic.lib.saving.NbtSaveHandler;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class RedstoneCause extends BlockCause {

    public static final ResourceLocation REG_NAME = new ResourceLocation(IcbmConstants.MOD_ID, "block.redstone");

    private Direction side;

    public RedstoneCause(Level level, BlockPos pos, BlockState state, Direction side) {
        super(world, pos, state);
        this.side = side;
    }

    @Override
    public ResourceLocation getRegistryName() {
        return REG_NAME;
    }

    @Override
    public CompoundTag serializeNBT() {
        return SAVE_LOGIC.save(this, super.serializeNBT());
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        super.deserializeNBT(nbt);
        SAVE_LOGIC.load(this, nbt);
    }

    private static final NbtSaveHandler<RedstoneCause> SAVE_LOGIC = new NbtSaveHandler<RedstoneCause>()
        .mainRoot()
        /* */.nodeFacing("side", RedstoneCause::getSide, RedstoneCause::setSide)
        .base();
}

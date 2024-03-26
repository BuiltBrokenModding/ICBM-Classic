package icbm.classic.world.missile.logic.source.cause;

import icbm.classic.IcbmConstants;
import icbm.classic.api.missiles.cause.IMissileCause;
import icbm.classic.lib.saving.NbtSaveHandler;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.common.DimensionManager;

/**
 * General purpose block cause
 */
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class BlockCause extends MissileCause implements IMissileCause.IBlockCause {

    public static final ResourceLocation REG_NAME = new ResourceLocation(IcbmConstants.MOD_ID, "block");

    private Level level;
    private BlockPos blockPos;
    private BlockState blockState;

    private int worldId;

    public BlockCause(Level level, BlockPos pos, BlockState state) {
        this.level = level;
        this.worldId = world.provider.getDimension();
        this.blockPos = pos;
        this.blockState = state;
    }

    public Level getLevel() {
        if (world == null) {
            world = DimensionManager.getLevel(worldId);
        }
        return world;
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

    private static final NbtSaveHandler<BlockCause> SAVE_LOGIC = new NbtSaveHandler<BlockCause>()
        .mainRoot()
        /* */.nodeInteger("level", BlockCause::getLevelId, BlockCause::setWorldId)
        /* */.nodeBlockPos("pos", BlockCause::getBlockPos, BlockCause::setBlockPos)
        /* */.nodeBlockState("state", BlockCause::getBlockState, BlockCause::setBlockState)
        .base();
}

package icbm.classic.content.missile.logic.source.cause;

import icbm.classic.ICBMConstants;
import icbm.classic.api.ICBMClassicAPI;
import icbm.classic.api.actions.cause.IActionCause;
import icbm.classic.api.actions.cause.ICausedByBlock;
import icbm.classic.api.reg.obj.IBuilderRegistry;
import icbm.classic.lib.saving.NbtSaveHandler;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.fml.loading.FMLEnvironment;

import javax.annotation.Nonnull;
import java.util.Optional;

/**
 * General purpose block cause
 */
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class CausedByBlock extends ActionCause implements ICausedByBlock {

    public static final ResourceLocation REG_NAME = new ResourceLocation(ICBMConstants.DOMAIN, "block");

    private BlockPos blockPos;
    private BlockState blockState;
    private ResourceLocation dimensionKey;

    public CausedByBlock(World world, BlockPos pos, BlockState state) {
        this.dimensionKey = DimensionType.getKey(world.getDimension().getType());
        this.blockPos = pos;
        this.blockState = state;
    }

    @Nonnull
    @Override
    public ResourceLocation getRegistryKey() {
        return REG_NAME;
    }

    @Nonnull
    @Override
    public IBuilderRegistry<IActionCause> getRegistry() {
        return ICBMClassicAPI.ACTION_CAUSE_REGISTRY;
    }

    @Override
    public CompoundNBT serializeNBT() {
        return SAVE_LOGIC.save(this, super.serializeNBT());
    }

    @Override
    public void deserializeNBT(CompoundNBT nbt) {
        super.deserializeNBT(nbt);
        SAVE_LOGIC.load(this, nbt);
    }

    private static final NbtSaveHandler<CausedByBlock> SAVE_LOGIC = new NbtSaveHandler<CausedByBlock>()
        .mainRoot()
        /* */.nodeResourceLocation("level", CausedByBlock::getDimensionKey, CausedByBlock::setDimensionKey)
        /* */.nodeBlockPos("pos", CausedByBlock::getBlockPos, CausedByBlock::setBlockPos)
        /* */.nodeBlockState("state", CausedByBlock::getBlockState, CausedByBlock::setBlockState)
        .base();
}

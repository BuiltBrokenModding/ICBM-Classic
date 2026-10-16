package icbm.classic.content.cargo.placer;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.MapColor;

import javax.annotation.Nullable;

/**
 * Logic block that is placed by parachute, or other cargo projectile, to act as a cargo hook and trigger
 * mechanism.
 * <p>
 * PORT: unfinished in 1.12 and never registered, kept unregistered.
 */
public class BlockCargoHook extends BaseEntityBlock {

    public static final MapCodec<BlockCargoHook> CODEC = simpleCodec(p -> new BlockCargoHook());

    public static final BooleanProperty TICKING_PROPERTY = BooleanProperty.create("ticking");

    protected BlockCargoHook() {
        // PORT: Material.IRON
        super(BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(5.0F, 6.0F).sound(SoundType.METAL));
        this.registerDefaultState(this.stateDefinition.any().setValue(TICKING_PROPERTY, false));
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder)
    {
        builder.add(TICKING_PROPERTY);
    }

    @Override
    protected RenderShape getRenderShape(BlockState state)
    {
        return RenderShape.MODEL;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        // PORT-TODO: no BlockEntityType is registered for the cargo hook (it was never registered in 1.12 either)
        return null;
    }
}
// PORTED

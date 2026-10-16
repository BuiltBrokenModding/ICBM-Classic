package icbm.classic.client.mapper;

import com.mojang.math.Transformation;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.BlockModelRotation;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.BakedModelWrapper;
import net.neoforged.neoforge.client.model.IQuadTransformer;
import net.neoforged.neoforge.client.model.QuadTransformers;
import net.neoforged.neoforge.client.model.data.ModelData;
import org.joml.Matrix4f;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * PORT: 1.12 explosive blockstates rotated some explosive models with the block facing (x/y variants per
 * rotation). The models are standalone in 1.21, so the rotation is applied to the baked quads instead.
 * This model itself is the "up" facing (no rotation).
 */
public class RotatableModel extends BakedModelWrapper<BakedModel>
{
    private final Map<Direction, BakedModel> rotated = new ConcurrentHashMap<>();

    public RotatableModel(BakedModel originalModel)
    {
        super(originalModel);
    }

    /** @return model rotated like the 1.12 blockstate rotation variant of the facing */
    public BakedModel forFacing(Direction facing)
    {
        if (facing == null || facing == Direction.UP)
        {
            return this;
        }
        return rotated.computeIfAbsent(facing, f -> new Rotated(originalModel, rotationFor(f)));
    }

    /** Same values as the 1.12 blockstate json rotation variants */
    public static BlockModelRotation rotationFor(Direction facing)
    {
        return switch (facing)
        {
            case DOWN -> BlockModelRotation.X180_Y0;
            case NORTH -> BlockModelRotation.X90_Y0;
            case SOUTH -> BlockModelRotation.X270_Y0;
            case EAST -> BlockModelRotation.X90_Y90;
            case WEST -> BlockModelRotation.X90_Y270;
            default -> BlockModelRotation.X0_Y0;
        };
    }

    private static class Rotated extends BakedModelWrapper<BakedModel>
    {
        private final Matrix4f matrix;
        private final IQuadTransformer transformer;
        /** rotated side -> side of the original model */
        private final Map<Direction, Direction> sourceSide = new EnumMap<>(Direction.class);

        Rotated(BakedModel original, BlockModelRotation rotation)
        {
            super(original);
            final Transformation transformation = rotation.getRotation();
            this.matrix = transformation.getMatrix();
            // Vanilla bakes block rotations around the block center
            this.transformer = QuadTransformers.applying(transformation.blockCenterToCorner()); // PORT-CHECK: QuadTransformers.applying rotates positions and normals
            for (Direction direction : Direction.values())
            {
                sourceSide.put(Direction.rotate(matrix, direction), direction);
            }
        }

        @Override
        public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, RandomSource rand)
        {
            return getQuads(state, side, rand, ModelData.EMPTY, null);
        }

        @Override
        public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, RandomSource rand, ModelData extraData, @Nullable RenderType renderType)
        {
            final Direction source = side == null ? null : sourceSide.get(side);
            final List<BakedQuad> quads = originalModel.getQuads(state, source, rand, extraData, renderType);
            final List<BakedQuad> out = new ArrayList<>(quads.size());
            for (BakedQuad quad : quads)
            {
                final BakedQuad moved = transformer.process(quad);
                out.add(new BakedQuad(moved.getVertices(), moved.getTintIndex(), Direction.rotate(matrix, quad.getDirection()),
                    moved.getSprite(), moved.isShade(), moved.hasAmbientOcclusion()));
            }
            return out;
        }
    }
}
// PORTED

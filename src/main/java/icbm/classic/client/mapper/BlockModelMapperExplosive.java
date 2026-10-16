package icbm.classic.client.mapper;

import icbm.classic.api.ICBMClassicAPI;
import icbm.classic.api.reg.IExplosiveData;
import icbm.classic.content.blocks.explosive.TileEntityExplosive;
import icbm.classic.prefab.tile.BlockICBM;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.ChunkRenderTypeSet;
import net.neoforged.neoforge.client.model.BakedModelWrapper;
import net.neoforged.neoforge.client.model.data.ModelData;

import javax.annotation.Nullable;
import java.util.List;

/**
 * Picks the model of a placed explosive block.
 * <p>
 * PORT: 1.12 used a StateMapper on the explosive + rotation block state properties. The explosive is no longer a
 * block state property, it comes from the block entity model data ({@link TileEntityExplosive#EXPLOSIVE_PROPERTY})
 * and the facing from {@link BlockICBM#ROTATION_PROP}. Models are the standalone {@code <ns>:block/explosives/<path>}
 * models, see {@link ExplosiveModels}.
 * <p>
 * Created by Dark(DarkGuardsman, Robin) on 1/21/2019.
 */
public class BlockModelMapperExplosive extends BakedModelWrapper<BakedModel>
{
    public BlockModelMapperExplosive(BakedModel fallBack)
    {
        super(fallBack);
    }

    /** @return model for the state and model data */
    public BakedModel getModel(@Nullable BlockState state, ModelData data)
    {
        final IExplosiveData explosiveData = getExplosive(data);
        final Direction facing = state != null && state.hasProperty(BlockICBM.ROTATION_PROP) ? state.getValue(BlockICBM.ROTATION_PROP) : Direction.UP;
        if (explosiveData == null && ExplosiveModels.getModel(ExplosiveModels.BLOCK_FALLBACK) == null)
        {
            return originalModel;
        }
        return ExplosiveModels.getBlockModel(explosiveData, facing);
    }

    @Nullable
    private static IExplosiveData getExplosive(@Nullable ModelData data)
    {
        if (data == null || ICBMClassicAPI.EXPLOSIVE_REGISTRY == null)
        {
            return null;
        }
        // Accept the common ways a tile may expose the explosive
        final Object value = data.get(TileEntityExplosive.EXPLOSIVE_PROPERTY);
        if (value instanceof IExplosiveData explosiveData)
        {
            return explosiveData;
        }
        else if (value instanceof ResourceLocation name)
        {
            return ICBMClassicAPI.EXPLOSIVE_REGISTRY.getExplosiveData(name, true);
        }
        else if (value instanceof Integer id)
        {
            return ICBMClassicAPI.EXPLOSIVE_REGISTRY.getExplosiveData(id);
        }
        return null;
    }

    @Override
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, RandomSource rand, ModelData extraData, @Nullable RenderType renderType)
    {
        return getModel(state, extraData).getQuads(state, side, rand, extraData, renderType);
    }

    @Override
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, RandomSource rand)
    {
        return getQuads(state, side, rand, ModelData.EMPTY, null);
    }

    @Override
    public ChunkRenderTypeSet getRenderTypes(BlockState state, RandomSource rand, ModelData data)
    {
        return getModel(state, data).getRenderTypes(state, rand, data);
    }

    @Override
    public TextureAtlasSprite getParticleIcon(ModelData data)
    {
        return getModel(null, data).getParticleIcon(data);
    }
}
// PORTED

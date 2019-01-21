package icbm.classic.client.mapper;

import icbm.classic.api.reg.IExplosiveData;
import icbm.classic.content.blocks.explosive.BlockExplosive;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.block.statemap.DefaultStateMapper;

import java.util.Map;

/**
 * Created by Dark(DarkGuardsman, Robert) on 1/21/2019.
 */
public class BlockModelMapperExplosive extends DefaultStateMapper
{
    private final Map<IExplosiveData, ModelResourceLocation> models;
    private final ModelResourceLocation fallBack;

    public BlockModelMapperExplosive(Map<IExplosiveData, ModelResourceLocation> models, ModelResourceLocation fallBack)
    {
        this.models = models;
        this.fallBack = fallBack;
    }

    @Override
    protected ModelResourceLocation getModelResourceLocation(IBlockState state)
    {
        if(state.getPropertyKeys().contains(BlockExplosive.EX_PROP))
        {
            final IExplosiveData explosiveData = state.getValue(BlockExplosive.EX_PROP);
            if (explosiveData != null)
            {
                final ModelResourceLocation modelResourceLocation = models.get(explosiveData);
                if (modelResourceLocation != null)
                {
                    return modelResourceLocation;
                }
            }
        }
        return fallBack;
    }
}

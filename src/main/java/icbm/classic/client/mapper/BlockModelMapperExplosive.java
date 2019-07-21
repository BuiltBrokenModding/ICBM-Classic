package icbm.classic.client.mapper;

import icbm.classic.api.reg.IExplosiveData;
import icbm.classic.content.blocks.explosive.BlockExplosive;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.block.statemap.DefaultStateMapper;
import net.minecraft.util.EnumFacing;

import java.util.Map;

/**
 * Created by Dark(DarkGuardsman, Robert) on 1/21/2019.
 */
public class BlockModelMapperExplosive extends DefaultStateMapper
{
    private final Map<IExplosiveData, Map<EnumFacing,ModelResourceLocation>> models;
    private final ModelResourceLocation fallBack;

    public BlockModelMapperExplosive(Map<IExplosiveData, Map<EnumFacing,ModelResourceLocation>> models, ModelResourceLocation fallBack)
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
                final Map<EnumFacing,ModelResourceLocation> facingModelMap = models.get(explosiveData);
                if (facingModelMap != null)
                {
                    final ModelResourceLocation modelResourceLocation = facingModelMap.get(state.getValue(BlockExplosive.ROTATION_PROP));
                    if (modelResourceLocation != null)
                    {
                        return modelResourceLocation;
                    }
                }
            }
        }
        return fallBack;
    }
}

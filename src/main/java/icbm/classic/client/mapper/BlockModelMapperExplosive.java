package icbm.classic.client.mapper;

import icbm.classic.api.reg.ExplosiveType;
import icbm.classic.world.block.explosive.ExplosiveBlock;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.block.statemap.DefaultStateMapper;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Map;

/**
 * Created by Dark(DarkGuardsman, Robert) on 1/21/2019.
 */
public class BlockModelMapperExplosive extends DefaultStateMapper {
    private final Map<ExplosiveType, Map<Direction, ModelResourceLocation>> models;
    private final ModelResourceLocation fallBack;

    public BlockModelMapperExplosive(Map<ExplosiveType, Map<Direction, ModelResourceLocation>> models, ModelResourceLocation fallBack) {
        this.models = models;
        this.fallBack = fallBack;
    }

    @Override
    protected ModelResourceLocation getModelResourceLocation(BlockState state) {
        if (state.getPropertyKeys().contains(ExplosiveBlock.EX_PROP)) {
            final ExplosiveType explosiveData = state.getValue(ExplosiveBlock.EX_PROP);
            if (explosiveData != null) {
                final Map<Direction, ModelResourceLocation> facingModelMap = models.get(explosiveData);
                if (facingModelMap != null) {
                    final ModelResourceLocation modelResourceLocation = facingModelMap.get(state.getValue(ExplosiveBlock.ROTATION_PROP));
                    if (modelResourceLocation != null) {
                        return modelResourceLocation;
                    }
                }
            }
        }
        return fallBack;
    }
}

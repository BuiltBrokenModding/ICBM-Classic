package icbm.classic.client.mapper;

import icbm.classic.api.ICBMClassicHelpers;
import icbm.classic.api.caps.IExplosive;
import icbm.classic.api.reg.ExplosiveType;
import net.minecraft.client.renderer.ItemMeshDefinition;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.Map;

/**
 * Created by Dark(DarkGuardsman, Robert) on 1/21/2019.
 */
public class ItemModelMapperExplosive implements ItemMeshDefinition {
    private final Map<ExplosiveType, ModelResourceLocation> models;
    private final ModelResourceLocation fallBack;

    public ItemModelMapperExplosive(Map<ExplosiveType, ModelResourceLocation> models, ModelResourceLocation fallBack) {
        this.models = models;
        this.fallBack = fallBack;
    }

    @Override
    public ModelResourceLocation getModelLocation(ItemStack stack) {
        final IExplosive explosive = ICBMClassicHelpers.getExplosive(stack);
        if (explosive != null && explosive.getExplosiveData() != null) {
            final ModelResourceLocation modelResourceLocation = models.get(explosive.getExplosiveData());
            if (modelResourceLocation != null) {
                return modelResourceLocation;
            }
        }
        return fallBack;
    }
}

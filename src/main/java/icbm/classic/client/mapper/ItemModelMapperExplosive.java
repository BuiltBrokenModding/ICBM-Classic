package icbm.classic.client.mapper;

import icbm.classic.api.reg.IExplosiveData;
import icbm.classic.content.items.ExplosiveItemHelper;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.model.BakedModelWrapper;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * Picks the item model of an explosive variant stack (grenade, bomb cart, missile, explosive block).
 * <p>
 * PORT: 1.12 used an ItemMeshDefinition per item. This wraps the item's base model and swaps it through
 * {@link ItemOverrides} for the standalone model {@code <ns>:<folder>/<explosive path>}, see {@link ExplosiveModels}.
 * <p>
 * Created by Dark(DarkGuardsman, Robin) on 1/21/2019.
 */
public class ItemModelMapperExplosive extends BakedModelWrapper<BakedModel>
{
    private final String folder;
    private final ResourceLocation fallBack;
    private final List<Special> specials = new ArrayList<>();
    private final ItemOverrides overrides = new ExplosiveOverrides();

    public ItemModelMapperExplosive(BakedModel base, String folder, ResourceLocation fallBack)
    {
        super(base);
        this.folder = folder;
        this.fallBack = fallBack;
    }

    /** Uses a fixed model for one explosive, like the 1.12 missile module entry */
    public ItemModelMapperExplosive withSpecial(Supplier<IExplosiveData> explosive, ResourceLocation model)
    {
        specials.add(new Special(explosive, model));
        return this;
    }

    @Override
    public ItemOverrides getOverrides()
    {
        return overrides;
    }

    /** @return model for the stack, null to keep the base model */
    @Nullable
    public BakedModel getModelLocation(ItemStack stack)
    {
        final IExplosiveData data = ExplosiveItemHelper.getExplosiveData(stack);
        if (data != null)
        {
            for (Special special : specials)
            {
                if (special.explosive.get() == data)
                {
                    return ExplosiveModels.getModel(special.model);
                }
            }
        }
        return ExplosiveModels.getModel(folder, data, fallBack);
    }

    private record Special(Supplier<IExplosiveData> explosive, ResourceLocation model)
    {
    }

    private class ExplosiveOverrides extends ItemOverrides
    {
        @Nullable
        @Override
        public BakedModel resolve(BakedModel model, ItemStack stack, @Nullable ClientLevel level, @Nullable LivingEntity entity, int seed)
        {
            final BakedModel variant = getModelLocation(stack);
            if (variant != null && variant != ItemModelMapperExplosive.this)
            {
                // Let the variant apply its own overrides, if any
                return variant.getOverrides().resolve(variant, stack, level, entity, seed);
            }
            return originalModel.getOverrides().resolve(originalModel, stack, level, entity, seed);
        }
    }
}
// PORTED

package icbm.classic.client.mapper;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import icbm.classic.ICBMClassic;
import icbm.classic.ICBMConstants;
import icbm.classic.api.refs.ICBMExplosives;
import icbm.classic.api.reg.IExplosiveData;
import icbm.classic.content.reg.BlockReg;
import icbm.classic.content.reg.ItemReg;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.BlockModelShaper;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.event.ModelEvent;

import javax.annotation.Nullable;
import java.io.Reader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

/**
 * PORT: replaces the 1.12 ItemMeshDefinition / StateMapper setup of ClientReg for explosive variants.
 * <p>
 * Every per explosive model is loaded as a standalone model ({@link ModelEvent.RegisterAdditional}), found by
 * listing the model folders below instead of reading the explosive registry, as that registry is filled during
 * common setup which may still be running when models load. The base models of the explosive items and block
 * are then wrapped ({@link ModelEvent.ModifyBakingResult}) with {@link ItemModelMapperExplosive} /
 * {@link BlockModelMapperExplosive} which pick the variant per stack / per block entity at render time.
 * <p>
 * Model locations, {@code <ns>:<path>} being the explosive registry name:
 * <ul>
 *     <li>block and block item: {@code <ns>:block/explosives/<path>}, models with {@code "icbmclassic:rotatable": true}
 *     rotate with the block facing (1.12 blockstate rotation variants)</li>
 *     <li>grenades: {@code <ns>:item/grenades/<path>}</li>
 *     <li>bomb carts: {@code <ns>:item/bombcarts/<path>}</li>
 *     <li>missiles: {@code <ns>:item/missiles/<path>}, missile module and fallback {@code icbmclassic:item/missiles/missile}</li>
 *     <li>bomblets: always {@code icbmclassic:item/explosive_bomblet} (1.12 had no per explosive models), plain item model</li>
 * </ul>
 */
public final class ExplosiveModels
{
    public static final String BLOCK_FOLDER = "block/explosives";
    public static final String GRENADE_FOLDER = "item/grenades";
    public static final String CART_FOLDER = "item/bombcarts";
    public static final String MISSILE_FOLDER = "item/missiles";

    /** JSON flag in block models that should follow the block facing */
    public static final String ROTATABLE_KEY = ICBMConstants.DOMAIN + ":rotatable";

    public static final ResourceLocation MISSILE_FALLBACK = ResourceLocation.fromNamespaceAndPath(ICBMConstants.DOMAIN, MISSILE_FOLDER + "/missile");
    public static final ResourceLocation GRENADE_FALLBACK = ResourceLocation.fromNamespaceAndPath(ICBMConstants.DOMAIN, GRENADE_FOLDER + "/condensed");
    public static final ResourceLocation CART_FALLBACK = ResourceLocation.fromNamespaceAndPath(ICBMConstants.DOMAIN, CART_FOLDER + "/condensed");
    public static final ResourceLocation BLOCK_FALLBACK = ResourceLocation.fromNamespaceAndPath(ICBMConstants.DOMAIN, BLOCK_FOLDER + "/condensed");

    /** Extra standalone models used by renderers (3D balloon) */
    public static final ResourceLocation BALLOON_3D = ResourceLocation.fromNamespaceAndPath(ICBMConstants.DOMAIN, "item/balloon_3d");

    /** Model ids found during the last model load */
    private static final Set<ResourceLocation> standaloneModels = new HashSet<>();
    /** Block models flagged as rotatable */
    private static final Set<ResourceLocation> rotatableModels = new HashSet<>();
    /** Baked standalone models of the last model bake */
    private static volatile Map<ResourceLocation, BakedModel> bakedModels = Map.of();

    private ExplosiveModels()
    {
    }

    // ------------------------------------------------------------------
    // Events
    // ------------------------------------------------------------------

    /** Mod bus, registers every explosive variant model as a standalone model */
    public static void onRegisterAdditional(ModelEvent.RegisterAdditional event)
    {
        synchronized (standaloneModels)
        {
            standaloneModels.clear();
            rotatableModels.clear();

            final ResourceManager resourceManager = Minecraft.getInstance().getResourceManager();
            collect(resourceManager, BLOCK_FOLDER, true);
            collect(resourceManager, GRENADE_FOLDER, false);
            collect(resourceManager, CART_FOLDER, false);
            collect(resourceManager, MISSILE_FOLDER, false);
            standaloneModels.add(BALLOON_3D);

            standaloneModels.forEach(id -> event.register(ModelResourceLocation.standalone(id)));
        }
    }

    private static void collect(ResourceManager resourceManager, String folder, boolean checkRotation)
    {
        final Map<ResourceLocation, Resource> found = resourceManager.listResources("models/" + folder, file -> file.getPath().endsWith(".json"));
        found.forEach((file, resource) -> {
            // icbmclassic:models/item/grenades/condensed.json -> icbmclassic:item/grenades/condensed
            final String path = file.getPath().substring("models/".length(), file.getPath().length() - ".json".length());
            final ResourceLocation id = ResourceLocation.fromNamespaceAndPath(file.getNamespace(), path);
            standaloneModels.add(id);

            if (checkRotation && isRotatable(resource))
            {
                rotatableModels.add(id);
            }
        });
    }

    private static boolean isRotatable(Resource resource)
    {
        try (Reader reader = resource.openAsReader())
        {
            final JsonElement json = JsonParser.parseReader(reader);
            if (json instanceof JsonObject object && object.has(ROTATABLE_KEY))
            {
                return object.get(ROTATABLE_KEY).getAsBoolean();
            }
        }
        catch (Exception e)
        {
            ICBMClassic.logger().error("ExplosiveModels: failed to read model json " + resource.sourcePackId(), e);
        }
        return false;
    }

    /** Mod bus, wraps the base explosive item and block models */
    public static void onModifyBakingResult(ModelEvent.ModifyBakingResult event)
    {
        final Map<ModelResourceLocation, BakedModel> models = event.getModels();

        // Collect baked standalone models
        final Map<ResourceLocation, BakedModel> baked = new HashMap<>();
        synchronized (standaloneModels)
        {
            for (ResourceLocation id : standaloneModels)
            {
                final BakedModel model = models.get(ModelResourceLocation.standalone(id));
                if (model != null)
                {
                    BakedModel finalModel = model;
                    if (rotatableModels.contains(id))
                    {
                        finalModel = new RotatableModel(model);
                    }
                    baked.put(id, finalModel);
                }
            }
        }
        bakedModels = baked;

        // Items
        wrapItem(models, ItemReg.itemGrenade, (base) -> new ItemModelMapperExplosive(base, GRENADE_FOLDER, GRENADE_FALLBACK));
        wrapItem(models, ItemReg.itemBombCart, (base) -> new ItemModelMapperExplosive(base, CART_FOLDER, CART_FALLBACK));
        wrapItem(models, ItemReg.itemExplosiveMissile, (base) -> new ItemModelMapperExplosive(base, MISSILE_FOLDER, MISSILE_FALLBACK)
            .withSpecial(() -> ICBMExplosives.MISSILEMODULE, MISSILE_FALLBACK));
        wrapItem(models, ItemReg.itemBlockExplosive, (base) -> new ItemModelMapperExplosive(base, BLOCK_FOLDER, BLOCK_FALLBACK));

        // Placed block, every state shares the wrapper logic
        for (BlockState state : BlockReg.blockExplosive.get().getStateDefinition().getPossibleStates())
        {
            final ModelResourceLocation location = BlockModelShaper.stateToModelLocation(state);
            final BakedModel base = models.get(location);
            if (base != null)
            {
                models.put(location, new BlockModelMapperExplosive(base));
            }
        }
    }

    private static void wrapItem(Map<ModelResourceLocation, BakedModel> models, @Nullable Supplier<? extends Item> item, java.util.function.Function<BakedModel, BakedModel> wrapper)
    {
        if (item == null)
        {
            return;
        }
        final ModelResourceLocation location = ModelResourceLocation.inventory(BuiltInRegistries.ITEM.getKey(item.get()));
        final BakedModel base = models.get(location);
        if (base != null)
        {
            models.put(location, wrapper.apply(base));
        }
        else
        {
            ICBMClassic.logger().warn("ExplosiveModels: no base model found for " + location);
        }
    }

    // ------------------------------------------------------------------
    // Lookups
    // ------------------------------------------------------------------

    /** @return baked standalone model, null if missing */
    @Nullable
    public static BakedModel getModel(ResourceLocation id)
    {
        return bakedModels.get(id);
    }

    /** @return model id for the explosive inside the folder, e.g. icbmclassic:item/grenades/condensed */
    public static ResourceLocation modelId(String folder, IExplosiveData data)
    {
        final ResourceLocation key = data.getRegistryKey();
        return ResourceLocation.fromNamespaceAndPath(key.getNamespace(), folder + "/" + key.getPath());
    }

    /** @return model for the explosive in the folder, or the fallback model, or null if neither exist */
    @Nullable
    public static BakedModel getModel(String folder, @Nullable IExplosiveData data, ResourceLocation fallback)
    {
        if (data != null && data.getRegistryKey() != null)
        {
            final BakedModel model = getModel(modelId(folder, data));
            if (model != null)
            {
                return model;
            }
        }
        return getModel(fallback);
    }

    /**
     * Block model for the explosive, rotated for the facing if the model is rotatable.
     * Used by the placed block, the primed explosive entity and bomb carts.
     *
     * @return model, falls back to condensed then to the vanilla missing model
     */
    public static BakedModel getBlockModel(@Nullable IExplosiveData data, Direction facing)
    {
        BakedModel model = getModel(BLOCK_FOLDER, data, BLOCK_FALLBACK);
        if (model == null)
        {
            return Minecraft.getInstance().getModelManager().getMissingModel();
        }
        if (model instanceof RotatableModel rotatable)
        {
            return rotatable.forFacing(facing);
        }
        return model;
    }
}
// PORTED

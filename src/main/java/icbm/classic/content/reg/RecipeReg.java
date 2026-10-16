package icbm.classic.content.reg;

import icbm.classic.ICBMConstants;
import icbm.classic.content.cargo.RecipeCargoData;
import icbm.classic.content.cargo.RecipeCargoDataDecraft;
import icbm.classic.content.cargo.balloon.BalloonProjectileData;
import icbm.classic.content.cargo.parachute.ParachuteProjectileData;
import icbm.classic.content.cluster.missile.RecipeCluster;
import icbm.classic.content.missile.entity.itemstack.item.RecipeHeldItemMissile;
import icbm.classic.content.missile.entity.itemstack.item.RecipeHeldItemMissileDecraft;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SimpleCraftingRecipeSerializer;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Serializers for the dynamic (code driven) crafting recipes. Each has a matching
 * {@code data/icbmclassic/recipe/<name>.json} of {@code {"type": "icbmclassic:<name>"}}.
 * <p>
 * Recipe classes extend {@link net.minecraft.world.item.crafting.CustomRecipe} and take the
 * {@link net.minecraft.world.item.crafting.CraftingBookCategory} as first constructor argument.
 */
public final class RecipeReg
{
    public static final DeferredRegister<RecipeSerializer<?>> SERIALIZERS = DeferredRegister.create(Registries.RECIPE_SERIALIZER, ICBMConstants.DOMAIN);

    public static final DeferredHolder<RecipeSerializer<?>, SimpleCraftingRecipeSerializer<RecipeCargoData>> BALLOON_CARGO = SERIALIZERS.register("balloon_cargo",
        () -> new SimpleCraftingRecipeSerializer<>(cat -> new RecipeCargoData(cat, () -> new ItemStack(ItemReg.itemBalloon.get()), BalloonProjectileData::new, RecipeReg.BALLOON_CARGO)));
    public static final DeferredHolder<RecipeSerializer<?>, SimpleCraftingRecipeSerializer<RecipeCargoDataDecraft>> BALLOON_CARGO_DECRAFT = SERIALIZERS.register("balloon_cargo_decraft",
        () -> new SimpleCraftingRecipeSerializer<>(cat -> new RecipeCargoDataDecraft(cat, ItemReg.itemBalloon, RecipeReg.BALLOON_CARGO_DECRAFT)));

    public static final DeferredHolder<RecipeSerializer<?>, SimpleCraftingRecipeSerializer<RecipeCargoData>> PARACHUTE_CARGO = SERIALIZERS.register("parachute_cargo",
        () -> new SimpleCraftingRecipeSerializer<>(cat -> new RecipeCargoData(cat, () -> new ItemStack(ItemReg.itemParachute.get()), ParachuteProjectileData::new, RecipeReg.PARACHUTE_CARGO)));
    public static final DeferredHolder<RecipeSerializer<?>, SimpleCraftingRecipeSerializer<RecipeCargoDataDecraft>> PARACHUTE_CARGO_DECRAFT = SERIALIZERS.register("parachute_cargo_decraft",
        () -> new SimpleCraftingRecipeSerializer<>(cat -> new RecipeCargoDataDecraft(cat, ItemReg.itemParachute, RecipeReg.PARACHUTE_CARGO_DECRAFT)));

    public static final DeferredHolder<RecipeSerializer<?>, SimpleCraftingRecipeSerializer<RecipeCluster>> CLUSTER_MISSILE = SERIALIZERS.register("cluster_missile",
        () -> new SimpleCraftingRecipeSerializer<>(cat -> new RecipeCluster(cat, () -> new ItemStack(ItemReg.itemClusterMissile.get()))));

    public static final DeferredHolder<RecipeSerializer<?>, SimpleCraftingRecipeSerializer<RecipeHeldItemMissile>> HELD_ITEM_MISSILE = SERIALIZERS.register("held_item_missile",
        () -> new SimpleCraftingRecipeSerializer<>(cat -> new RecipeHeldItemMissile(cat, () -> new ItemStack(ItemReg.heldItemMissile.get()))));
    public static final DeferredHolder<RecipeSerializer<?>, SimpleCraftingRecipeSerializer<RecipeHeldItemMissileDecraft>> HELD_ITEM_MISSILE_DECRAFT = SERIALIZERS.register("held_item_missile_decraft",
        () -> new SimpleCraftingRecipeSerializer<>(cat -> new RecipeHeldItemMissileDecraft(cat, ItemReg.heldItemMissile)));

    private RecipeReg()
    {
    }

    public static void register(IEventBus modBus)
    {
        SERIALIZERS.register(modBus);
    }
}

package icbm.classic.mods.jei;

import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.ShapelessRecipe;
import net.neoforged.neoforge.common.crafting.DataComponentIngredient;

import java.util.List;

/**
 * PORT: JEI 19 has no recipe wrappers for display only recipes. The 1.12 wrappers now build vanilla
 * {@link ShapelessRecipe}s that are only handed to JEI (never registered with the recipe manager).
 */
public final class JeiRecipeHelper
{
    private JeiRecipeHelper()
    {
    }

    /** Ingredient matching the exact stack including its components, so JEI displays the stack data */
    public static Ingredient exact(ItemStack stack)
    {
        return DataComponentIngredient.of(false, stack); // PORT-CHECK: NeoForge 21.1 DataComponentIngredient.of(boolean, ItemStack)
    }

    /** Ingredient cycling through the given stacks (display only) */
    public static Ingredient anyOf(List<ItemStack> stacks)
    {
        return Ingredient.of(stacks.stream());
    }

    public static RecipeHolder<CraftingRecipe> shapeless(ResourceLocation id, ItemStack output, Ingredient... inputs)
    {
        final NonNullList<Ingredient> ingredients = NonNullList.create();
        for (Ingredient ingredient : inputs)
        {
            ingredients.add(ingredient);
        }
        return new RecipeHolder<>(id, new ShapelessRecipe("", CraftingBookCategory.MISC, output, ingredients));
    }
}
// PORTED

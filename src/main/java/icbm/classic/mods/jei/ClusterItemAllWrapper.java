package icbm.classic.mods.jei;

import lombok.AllArgsConstructor;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Cluster missile + 8 slots of any allowed item. Unused in 1.12 as well (commented out in {@link JEIProxy}).
 * PORT: 1.12 ICraftingRecipeWrapper, now builds a display only shapeless recipe for JEI 19.
 */
@AllArgsConstructor
public class ClusterItemAllWrapper {

    private final ItemStack output;
    private final List<ItemStack> inputs;
    private boolean random;
    private final ResourceLocation regName;

    private List<ItemStack> copyShuffle(List<ItemStack> stacks) {
        List<ItemStack> copy = new ArrayList<>(stacks);
        Collections.shuffle(copy);
        return copy;
    }

    public RecipeHolder<CraftingRecipe> build() {
        final Ingredient[] ingredients = new Ingredient[9];
        ingredients[0] = JeiRecipeHelper.exact(output);
        for (int i = 1; i < 9; i++) {
            ingredients[i] = JeiRecipeHelper.anyOf(random && i > 1 ? copyShuffle(inputs) : inputs);
        }
        return JeiRecipeHelper.shapeless(regName, output.copy(), ingredients);
    }
}
// PORTED

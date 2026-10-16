package icbm.classic.mods.jei;

import lombok.AllArgsConstructor;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;

/**
 * Holder with cargo -> cargo.
 * PORT: 1.12 ICraftingRecipeWrapper, now builds a display only shapeless recipe for JEI 19.
 */
@AllArgsConstructor
public class DecraftCargoItemWrapper {

    private final ItemStack holder;
    private final ItemStack cargo;
    private final ResourceLocation regName;

    public RecipeHolder<CraftingRecipe> build() {
        return JeiRecipeHelper.shapeless(regName, cargo.copy(),
            JeiRecipeHelper.exact(CargoItemWrapper.createWithCargo(holder, cargo)));
    }
}
// PORTED

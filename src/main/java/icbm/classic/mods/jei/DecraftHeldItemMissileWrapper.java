package icbm.classic.mods.jei;

import lombok.AllArgsConstructor;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;

/**
 * Missile holding an item -> item.
 * PORT: 1.12 ICraftingRecipeWrapper, now builds a display only shapeless recipe for JEI 19.
 */
@AllArgsConstructor
public class DecraftHeldItemMissileWrapper {

    private final ItemStack holder;
    private final ItemStack cargo;
    private final ResourceLocation regName;

    public RecipeHolder<CraftingRecipe> build() {
        return JeiRecipeHelper.shapeless(regName, cargo.copy(),
            JeiRecipeHelper.exact(HeldItemMissileWrapper.createMissileWithItem(holder, cargo)));
    }
}
// PORTED

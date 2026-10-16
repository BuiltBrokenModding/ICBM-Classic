package icbm.classic.mods.jei;

import icbm.classic.api.ICBMClassicAPI;
import icbm.classic.api.missiles.ICapabilityMissileStack;
import icbm.classic.content.missile.entity.itemstack.item.CapabilityHeldItemMissile;
import lombok.AllArgsConstructor;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;

/**
 * Held item missile + item -> missile holding the item.
 * PORT: 1.12 ICraftingRecipeWrapper, now builds a display only shapeless recipe for JEI 19.
 */
@AllArgsConstructor
public class HeldItemMissileWrapper {

    private final ItemStack output;
    private final ItemStack input;
    private final ResourceLocation regName;

    public RecipeHolder<CraftingRecipe> build() {
        return JeiRecipeHelper.shapeless(regName, createMissileWithItem(output, input),
            JeiRecipeHelper.exact(output), JeiRecipeHelper.exact(input));
    }

    /** Shared with {@link DecraftHeldItemMissileWrapper} */
    static ItemStack createMissileWithItem(ItemStack missile, ItemStack held) {
        final ItemStack out = missile.copy();
        final ICapabilityMissileStack missileStack = out.getCapability(ICBMClassicAPI.MISSILE_STACK_CAPABILITY);
        if(missileStack instanceof CapabilityHeldItemMissile) {
            ((CapabilityHeldItemMissile) missileStack).setHeldItem(held.copy());
        }
        return out;
    }
}
// PORTED

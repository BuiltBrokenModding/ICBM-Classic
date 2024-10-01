package icbm.classic.mods.jei;

import icbm.classic.api.ICBMClassicAPI;
import icbm.classic.api.missiles.ICapabilityMissileStack;
import icbm.classic.content.missile.entity.itemstack.item.CapabilityHeldItemMissile;
import lombok.AllArgsConstructor;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.wrapper.ICraftingRecipeWrapper;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Collections;

@AllArgsConstructor
public class DecraftHeldItemMissileWrapper implements ICraftingRecipeWrapper {

    private final ItemStack holder;
    private final ItemStack cargo;
    private final ResourceLocation regName;

    @Override
    public void getIngredients(IIngredients iIngredients) {
        iIngredients.setInputs(ItemStack.class, Arrays.asList(createMissileWithCargo()));
        iIngredients.setOutput(ItemStack.class, Collections.singletonList(cargo));
    }

    private ItemStack createMissileWithCargo() {
        final ItemStack out = holder.copy();
        final ICapabilityMissileStack missileStack = out.getCapability(ICBMClassicAPI.MISSILE_STACK_CAPABILITY, null);
        if(missileStack instanceof CapabilityHeldItemMissile) {
            ((CapabilityHeldItemMissile) missileStack).setHeldItem(cargo.copy());
        }
        return out;
    }

    @Override
    @Nullable
    public ResourceLocation getRegistryName() {
        return regName;
    }
}

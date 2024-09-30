package icbm.classic.mods.jei;

import icbm.classic.api.ICBMClassicAPI;
import icbm.classic.api.missiles.ICapabilityMissileStack;
import icbm.classic.content.cluster.missile.CapabilityClusterMissileStack;
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
public class HeldItemMissileWrapper implements ICraftingRecipeWrapper {

    private final ItemStack output;
    private final ItemStack input;
    private final ResourceLocation regName;

    @Override
    public void getIngredients(IIngredients iIngredients) {

        iIngredients.setInputs(ItemStack.class, Arrays.asList(output, input));

        final ItemStack out = output.copy();
        final ICapabilityMissileStack missileStack = out.getCapability(ICBMClassicAPI.MISSILE_STACK_CAPABILITY, null);
        if(missileStack instanceof CapabilityHeldItemMissile) {
            ((CapabilityHeldItemMissile) missileStack).setHeldItem(input.copy());
        }
        iIngredients.setOutput(ItemStack.class, Collections.singletonList(out));
    }

    @Override
    @Nullable
    public ResourceLocation getRegistryName() {
        return regName;
    }
}

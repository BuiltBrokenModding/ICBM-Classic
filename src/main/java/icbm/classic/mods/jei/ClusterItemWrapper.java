package icbm.classic.mods.jei;

import icbm.classic.api.ICBMClassicAPI;
import icbm.classic.api.missiles.ICapabilityMissileStack;
import icbm.classic.content.cluster.missile.CapabilityClusterMissileStack;
import lombok.AllArgsConstructor;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;

/**
 * Cluster missile + item -> cluster missile containing the item.
 * PORT: 1.12 ICraftingRecipeWrapper, now builds a display only shapeless recipe for JEI 19.
 */
@AllArgsConstructor
public class ClusterItemWrapper {

    private final ItemStack output;
    private final ItemStack input;
    private final ResourceLocation regName;

    public RecipeHolder<CraftingRecipe> build() {
        final ItemStack out = output.copy();
        final ICapabilityMissileStack missileStack = out.getCapability(ICBMClassicAPI.MISSILE_STACK_CAPABILITY);
        if(missileStack instanceof CapabilityClusterMissileStack cluster) {
            cluster.getActionDataCluster().getClusterSpawnEntries().add(input.copy());
            cluster.saveToStack(); // PORT: caps are rebuilt per lookup, write back
        }
        return JeiRecipeHelper.shapeless(regName, out, JeiRecipeHelper.exact(output), JeiRecipeHelper.exact(input));
    }
}
// PORTED

package icbm.classic.mods.jei;

import icbm.classic.api.ICBMClassicAPI;
import icbm.classic.api.missiles.projectile.IProjectileStack;
import icbm.classic.content.cargo.CargoProjectileData;
import icbm.classic.content.cargo.balloon.BalloonProjectileData;
import icbm.classic.content.cargo.parachute.ParachuteProjectileData;
import icbm.classic.content.reg.ItemReg;
import icbm.classic.lib.projectile.ProjectileStack;
import lombok.AllArgsConstructor;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;

/**
 * Holder (balloon/parachute) + cargo -> holder with cargo.
 * PORT: 1.12 ICraftingRecipeWrapper, now builds a display only shapeless recipe for JEI 19.
 */
@AllArgsConstructor
public class CargoItemWrapper {

    private final ItemStack output;
    private final ItemStack input;
    private final ResourceLocation regName;

    public RecipeHolder<CraftingRecipe> build() {
        return JeiRecipeHelper.shapeless(regName, createWithCargo(output, input),
            JeiRecipeHelper.exact(output), JeiRecipeHelper.exact(input));
    }

    /** Shared with {@link DecraftCargoItemWrapper} */
    static ItemStack createWithCargo(ItemStack holder, ItemStack cargo) {
        final ItemStack out = holder.copy();
        final IProjectileStack projectileStack = out.getCapability(ICBMClassicAPI.PROJECTILE_STACK_CAPABILITY);
        if(projectileStack instanceof ProjectileStack) {
            CargoProjectileData<?, ?> data;
            if(holder.getItem() == ItemReg.itemBalloon.get()) {
                data = new BalloonProjectileData();
            }
            else {
                data = new ParachuteProjectileData();
            }
            // PORT: held item set before setProjectileData as the latter writes the cap back to the stack
            data.setHeldItem(cargo.copy());
            ((ProjectileStack) projectileStack).setProjectileData(data);
        }
        return out;
    }
}
// PORTED

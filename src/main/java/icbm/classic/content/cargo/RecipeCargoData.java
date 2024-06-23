package icbm.classic.content.cargo;

import icbm.classic.api.ICBMClassicAPI;
import icbm.classic.api.missiles.projectile.IProjectileStack;
import icbm.classic.content.cluster.missile.ClusterMissileHandler;
import icbm.classic.content.reg.ItemReg;
import icbm.classic.lib.projectile.ProjectileStack;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.util.NonNullList;
import net.minecraft.world.World;

import java.util.function.Supplier;


/**
 * Recipe for adding cargo to cargo projectile item
 */
@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
@Data
public class RecipeCargoData extends net.minecraftforge.registries.IForgeRegistryEntry.Impl<IRecipe> implements IRecipe {

    private final ItemStack recipeOutput;
    private final Supplier<CargoProjectileData> dataBuilder;

    @Override
    public boolean isDynamic()
    {
        return true;
    }

    @Override
    public NonNullList<ItemStack> getRemainingItems(InventoryCrafting inv)
    {
        // Don't leave container items, since we store the entire item
        return NonNullList.withSize(inv.getSizeInventory(), ItemStack.EMPTY);
    }

    @Override
    public boolean matches(InventoryCrafting inv, World worldIn) {
        int itemCount = 0;
        boolean hasCargoItem = false;
        for(int slot = 0; slot < inv.getSizeInventory(); slot++) {
            final ItemStack slotStack = inv.getStackInSlot(slot);
            if(!slotStack.isEmpty()) {
                itemCount++;


                if(ItemStack.areItemsEqual(slotStack, recipeOutput)) {
                    hasCargoItem = isValidProjectileStack(getProjectileStack(slotStack));
                }
                // Cluster takes priority as it is before parachute/balloon
                else if((slotStack.getItem() == ItemReg.itemClusterMissile
                    || slotStack.getItem() == ItemReg.heldItemMissile) && !hasCargoItem) {
                    return false;
                }
                else if(!CargoHolderHandler.isAllowed(slotStack)) {
                    return false;
                }
            }
        }
        return itemCount == 2 && hasCargoItem;
    }

    private ProjectileStack getProjectileStack(ItemStack stack) {

        if (!stack.hasCapability(ICBMClassicAPI.PROJECTILE_STACK_CAPABILITY, null)) {
            return null;
        }
        final IProjectileStack iProjectileStack = stack.getCapability(ICBMClassicAPI.PROJECTILE_STACK_CAPABILITY, null);
        if(iProjectileStack instanceof ProjectileStack) {
            return (ProjectileStack) iProjectileStack;
        }
        return null;
    }

    private boolean isValidProjectileStack(IProjectileStack projectileStack) {
        return projectileStack != null && (projectileStack.getProjectileData() == null || projectileStack.getProjectileData() instanceof CargoProjectileData);
    }

    @Override
    public ItemStack getCraftingResult(InventoryCrafting inv) {
        ItemStack cargoHolder = null;
        ItemStack cargo = null;
        for(int slot = 0; slot < inv.getSizeInventory() && (cargo == null || cargoHolder == null); slot++) {
            final ItemStack slotStack = inv.getStackInSlot(slot);
            if (!slotStack.isEmpty()) {
                if (ItemStack.areItemsEqual(slotStack, recipeOutput)) {
                    final IProjectileStack projectileStack = getProjectileStack(slotStack);
                    if (cargoHolder == null && isValidProjectileStack(projectileStack)) {
                        cargoHolder = slotStack;
                    } else {
                        cargo = slotStack;
                    }
                } else {
                    cargo = slotStack;
                }
            }
        }

        // Should not happen but IDE is annoying
        if(cargoHolder == null || cargo == null) {
            return null;
        }

        final ItemStack output = cargoHolder.copy();
        output.setCount(1);

        final ProjectileStack projectileStack = getProjectileStack(output);

        final CargoProjectileData projectileData = projectileStack.getProjectileData() instanceof CargoProjectileData ? (CargoProjectileData) projectileStack.getProjectileData() : dataBuilder.get();

        final ItemStack insert = cargo.copy();
        insert.setCount(1);
        projectileData.setHeldItem(insert);

        projectileStack.setProjectileData(projectileData);

        return output;
    }

    @Override
    public boolean canFit(int width, int height) {
        return width * height >= 2;
    }
}

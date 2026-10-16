package icbm.classic.content.cargo;

import icbm.classic.api.ICBMClassicAPI;
import icbm.classic.api.missiles.projectile.IProjectileData;
import icbm.classic.api.missiles.projectile.IProjectileStack;
import icbm.classic.content.reg.ItemReg;
import icbm.classic.lib.projectile.ProjectileStack;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;

import java.util.function.Supplier;


/**
 * Recipe for adding cargo to cargo projectile item
 */
public class RecipeCargoData extends CustomRecipe {

    private final Supplier<ItemStack> recipeOutput;
    private final Supplier<IProjectileData> dataBuilder;
    private final Supplier<? extends RecipeSerializer<?>> serializer;

    public RecipeCargoData(CraftingBookCategory category, Supplier<ItemStack> recipeOutput, Supplier<IProjectileData> dataBuilder, Supplier<? extends RecipeSerializer<?>> serializer) {
        super(category);
        this.recipeOutput = recipeOutput;
        this.dataBuilder = dataBuilder;
        this.serializer = serializer;
    }

    @Override
    public NonNullList<ItemStack> getRemainingItems(CraftingInput inv)
    {
        // Don't leave container items, since we store the entire item
        return NonNullList.withSize(inv.size(), ItemStack.EMPTY);
    }

    @Override
    public boolean matches(CraftingInput inv, Level worldIn) {
        final ItemStack output = recipeOutput.get();
        int itemCount = 0;
        boolean hasCargoItem = false;
        for(int slot = 0; slot < inv.size(); slot++) {
            final ItemStack slotStack = inv.getItem(slot);
            if(!slotStack.isEmpty()) {
                itemCount++;


                if(ItemStack.isSameItem(slotStack, output)) {
                    hasCargoItem = isValidProjectileStack(getProjectileStack(slotStack));
                }
                // Cluster takes priority as it is before parachute/balloon
                else if((slotStack.getItem() == ItemReg.itemClusterMissile.get()
                    || slotStack.getItem() == ItemReg.heldItemMissile.get()) && !hasCargoItem) {
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
        final IProjectileStack iProjectileStack = stack.getCapability(ICBMClassicAPI.PROJECTILE_STACK_CAPABILITY);
        if(iProjectileStack instanceof ProjectileStack) {
            return (ProjectileStack) iProjectileStack;
        }
        return null;
    }

    private boolean isValidProjectileStack(IProjectileStack projectileStack) {
        return projectileStack != null && (projectileStack.getProjectileData() == null || projectileStack.getProjectileData() instanceof CargoProjectileData);
    }

    @Override
    public ItemStack assemble(CraftingInput inv, HolderLookup.Provider registries) {
        final ItemStack recipeStack = recipeOutput.get();
        ItemStack cargoHolder = null;
        ItemStack cargo = null;
        for(int slot = 0; slot < inv.size() && (cargo == null || cargoHolder == null); slot++) {
            final ItemStack slotStack = inv.getItem(slot);
            if (!slotStack.isEmpty()) {
                if (ItemStack.isSameItem(slotStack, recipeStack)) {
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
            return ItemStack.EMPTY;
        }

        final ItemStack output = cargoHolder.copy();
        output.setCount(1);

        final ProjectileStack projectileStack = getProjectileStack(output);
        if(projectileStack == null) {
            return ItemStack.EMPTY;
        }

        final CargoProjectileData projectileData = projectileStack.getProjectileData() instanceof CargoProjectileData ? (CargoProjectileData) projectileStack.getProjectileData() : (CargoProjectileData) dataBuilder.get();

        final ItemStack insert = cargo.copy();
        insert.setCount(1);
        projectileData.setHeldItem(insert);

        projectileStack.setProjectileData(projectileData); // writes back to output stack

        return output;
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width * height >= 2;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return serializer.get();
    }
}
// PORTED

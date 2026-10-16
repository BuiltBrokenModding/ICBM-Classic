package icbm.classic.content.cargo;

import icbm.classic.api.ICBMClassicAPI;
import icbm.classic.api.missiles.projectile.IProjectileStack;
import icbm.classic.lib.projectile.ProjectileStack;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.registries.DeferredItem;

import java.util.function.Supplier;


/**
 * Recipe for removing held item from cargo holder
 */
public class RecipeCargoDataDecraft extends CustomRecipe {

    private final DeferredItem<?> targetItem;
    private final Supplier<? extends RecipeSerializer<?>> serializer;

    public RecipeCargoDataDecraft(CraftingBookCategory category, DeferredItem<?> targetItem, Supplier<? extends RecipeSerializer<?>> serializer) {
        super(category);
        this.targetItem = targetItem;
        this.serializer = serializer;
    }

    @Override
    public NonNullList<ItemStack> getRemainingItems(CraftingInput inv) {
        final NonNullList<ItemStack> list = NonNullList.withSize(inv.size(), ItemStack.EMPTY);
        for (int slot = 0; slot < inv.size(); slot++) {
            if (inv.getItem(slot).getItem() == targetItem.get()) {
                final ItemStack slotStack = inv.getItem(slot).copy();
                final ProjectileStack cap = getProjectileStack(slotStack);
                if (cap != null && cap.getProjectileData() instanceof CargoProjectileData) {
                    ((CargoProjectileData<?, ?>) cap.getProjectileData()).setHeldItem(ItemStack.EMPTY);
                    cap.saveToHost(); // PORT: item caps are not stored, write the change back to the stack
                }
                list.set(slot, slotStack);
            }
        }
        return list;
    }

    @Override
    public boolean matches(CraftingInput inv, Level worldIn) {
        boolean hasItem = false;
        for (int slot = 0; slot < inv.size(); slot++) {
            final ItemStack slotStack = inv.getItem(slot);
            if (!slotStack.isEmpty()) {
                if (!hasItem && slotStack.getItem() == targetItem.get() && slotStack.getCount() == 1) {
                    final IProjectileStack cap = getProjectileStack(slotStack);
                    if (cap != null && cap.getProjectileData() instanceof CargoProjectileData) {
                        hasItem = true;
                    } else {
                        return false;
                    }
                } else {
                    return false;
                }
            }
        }
        return hasItem;
    }

    private ProjectileStack getProjectileStack(ItemStack stack) {
        if (stack == null) {
            return null;
        }
        final IProjectileStack iProjectileStack = stack.getCapability(ICBMClassicAPI.PROJECTILE_STACK_CAPABILITY);
        if (iProjectileStack instanceof ProjectileStack) {
            return (ProjectileStack) iProjectileStack;
        }
        return null;
    }

    @Override
    public ItemStack assemble(CraftingInput inv, HolderLookup.Provider registries) {

        // Find missile
        ItemStack cargoHolderStack = null;
        for (int slot = 0; slot < inv.size(); slot++) {
            final ItemStack slotStack = inv.getItem(slot);
            if (!slotStack.isEmpty()) {
                if (slotStack.getItem() == targetItem.get()) {
                    cargoHolderStack = slotStack;
                    break;
                }
            }
        }

        // get item from cap
        final IProjectileStack cap = getProjectileStack(cargoHolderStack);
        if (cap != null && cap.getProjectileData() instanceof CargoProjectileData) {
            return ((CargoProjectileData<?, ?>) cap.getProjectileData()).getHeldItem().copy();
        }

        return ItemStack.EMPTY;
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width * height >= 1;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return serializer.get();
    }
}
// PORTED

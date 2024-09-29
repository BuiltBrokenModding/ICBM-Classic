package icbm.classic.content.cargo;

import icbm.classic.api.ICBMClassicAPI;
import icbm.classic.api.missiles.projectile.IProjectileStack;
import icbm.classic.lib.projectile.ProjectileStack;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.util.NonNullList;
import net.minecraft.world.World;


/**
 * Recipe for removing held item from cargo holder
 */
@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
@Data
public class RecipeCargoDataDecraft extends net.minecraftforge.registries.IForgeRegistryEntry.Impl<IRecipe> implements IRecipe {

    private final Item targetItem;

    @Override
    public boolean isDynamic() {
        return true;
    }

    @Override
    public NonNullList<ItemStack> getRemainingItems(InventoryCrafting inv) {
        final NonNullList<ItemStack> list = NonNullList.withSize(inv.getSizeInventory(), ItemStack.EMPTY);
        for (int slot = 0; slot < inv.getSizeInventory(); slot++) {
            if (inv.getStackInSlot(slot).getItem() == targetItem) {
                final ItemStack slotStack = inv.getStackInSlot(slot).copy();
                final IProjectileStack cap = getProjectileStack(slotStack);
                if (cap != null && cap.getProjectileData() instanceof CargoProjectileData) {
                    ((CargoProjectileData<?, ?>) cap.getProjectileData()).setHeldItem(ItemStack.EMPTY);
                }
                list.set(slot, slotStack);
            }
        }
        return list;
    }

    @Override
    public boolean matches(InventoryCrafting inv, World worldIn) {
        boolean hasItem = false;
        for (int slot = 0; slot < inv.getSizeInventory(); slot++) {
            final ItemStack slotStack = inv.getStackInSlot(slot);
            if (!slotStack.isEmpty()) {
                if (!hasItem && slotStack.getItem() == targetItem && slotStack.getCount() == 1) {
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

        if (!stack.hasCapability(ICBMClassicAPI.PROJECTILE_STACK_CAPABILITY, null)) {
            return null;
        }
        final IProjectileStack iProjectileStack = stack.getCapability(ICBMClassicAPI.PROJECTILE_STACK_CAPABILITY, null);
        if (iProjectileStack instanceof ProjectileStack) {
            return (ProjectileStack) iProjectileStack;
        }
        return null;
    }

    @Override
    public ItemStack getCraftingResult(InventoryCrafting inv) {

        // Find missile
        ItemStack cargoHolderStack = null;
        for (int slot = 0; slot < inv.getSizeInventory(); slot++) {
            final ItemStack slotStack = inv.getStackInSlot(slot);
            if (!slotStack.isEmpty()) {
                if (slotStack.getItem() == targetItem) {
                    cargoHolderStack = slotStack;
                    break;
                }
            }
        }

        // get item from cap
        final IProjectileStack cap = getProjectileStack(cargoHolderStack);
        if (cap != null && cap.getProjectileData() instanceof CargoProjectileData) {
            return ((CargoProjectileData<?, ?>) cap.getProjectileData()).getHeldItem();
        }

        return null;
    }

    @Override
    public boolean canFit(int width, int height) {
        return width * height >= 1;
    }

    @Override
    public ItemStack getRecipeOutput() {
        return ItemStack.EMPTY;
    }
}

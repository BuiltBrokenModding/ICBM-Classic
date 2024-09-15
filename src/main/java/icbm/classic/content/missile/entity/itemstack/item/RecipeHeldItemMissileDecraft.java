package icbm.classic.content.missile.entity.itemstack.item;

import icbm.classic.api.ICBMClassicAPI;
import icbm.classic.api.missiles.ICapabilityMissileStack;
import icbm.classic.content.cargo.CargoHolderHandler;
import icbm.classic.content.reg.ItemReg;
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
 * Recipe for adding cargo to cargo projectile item
 */
@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
@Data
public class RecipeHeldItemMissileDecraft extends net.minecraftforge.registries.IForgeRegistryEntry.Impl<IRecipe> implements IRecipe {

    private final Item targetItem;

    @Override
    public boolean isDynamic() {
        return true;
    }

    @Override
    public NonNullList<ItemStack> getRemainingItems(InventoryCrafting inv) {
        final NonNullList<ItemStack> list = NonNullList.withSize(inv.getSizeInventory(), ItemStack.EMPTY);
        for (int slot = 0; slot < inv.getSizeInventory(); slot++) {
            if(inv.getStackInSlot(slot).getItem() == targetItem) {
                final ItemStack slotStack = inv.getStackInSlot(slot).copy();
                final CapabilityHeldItemMissile cap = getCapability(slotStack);
                if (cap != null) {
                    cap.setHeldItem(ItemStack.EMPTY);
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
            if(!slotStack.isEmpty()) {
            if (!hasItem && slotStack.getItem() == targetItem && slotStack.getCount() == 1) {
                final CapabilityHeldItemMissile cap = getCapability(slotStack);
                if(cap != null && !cap.getHeldItem().isEmpty()) {
                    hasItem = true;
                }
                else {
                    return false;
                }
            } else {
                return false;
            }
            }
        }
        return hasItem;
    }

    private CapabilityHeldItemMissile getCapability(ItemStack stack) {
        final ICapabilityMissileStack cap = stack.getCapability(ICBMClassicAPI.MISSILE_STACK_CAPABILITY, null);
        if (cap instanceof CapabilityHeldItemMissile) {
            return (CapabilityHeldItemMissile) cap;
        }
        return null;
    }

    @Override
    public ItemStack getCraftingResult(InventoryCrafting inv) {

        // Find missile
        ItemStack missileIn = null;
        for (int slot = 0; slot < inv.getSizeInventory(); slot++) {
            final ItemStack slotStack = inv.getStackInSlot(slot);
            if (!slotStack.isEmpty()) {
                if (slotStack.getItem() == targetItem) {
                    missileIn = slotStack;
                    break;
                }
            }
        }

        // get item from cap
        final CapabilityHeldItemMissile cap = getCapability(missileIn);
        if(cap != null && !cap.getHeldItem().isEmpty()) {
            return cap.getHeldItem().copy();
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

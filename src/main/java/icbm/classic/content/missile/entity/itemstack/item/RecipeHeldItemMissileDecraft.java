package icbm.classic.content.missile.entity.itemstack.item;

import icbm.classic.api.ICBMClassicAPI;
import icbm.classic.api.missiles.ICapabilityMissileStack;
import icbm.classic.content.reg.RecipeReg;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.registries.DeferredItem;


/**
 * Recipe for removing the held item from a held item missile
 */
public class RecipeHeldItemMissileDecraft extends CustomRecipe {

    private final DeferredItem<?> targetItem;

    public RecipeHeldItemMissileDecraft(CraftingBookCategory category, DeferredItem<?> targetItem) {
        super(category);
        this.targetItem = targetItem;
    }

    @Override
    public NonNullList<ItemStack> getRemainingItems(CraftingInput inv) {
        final NonNullList<ItemStack> list = NonNullList.withSize(inv.size(), ItemStack.EMPTY);
        for (int slot = 0; slot < inv.size(); slot++) {
            if(inv.getItem(slot).getItem() == targetItem.get()) {
                final ItemStack slotStack = inv.getItem(slot).copy();
                final CapabilityHeldItemMissile cap = getCapability(slotStack);
                if (cap != null) {
                    cap.setHeldItem(ItemStack.EMPTY); // writes back to slotStack
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
            if(!slotStack.isEmpty()) {
                if (!hasItem && slotStack.getItem() == targetItem.get() && slotStack.getCount() == 1) {
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
        if (stack == null) {
            return null;
        }
        final ICapabilityMissileStack cap = stack.getCapability(ICBMClassicAPI.MISSILE_STACK_CAPABILITY);
        if (cap instanceof CapabilityHeldItemMissile) {
            return (CapabilityHeldItemMissile) cap;
        }
        return null;
    }

    @Override
    public ItemStack assemble(CraftingInput inv, HolderLookup.Provider registries) {

        // Find missile
        ItemStack missileIn = null;
        for (int slot = 0; slot < inv.size(); slot++) {
            final ItemStack slotStack = inv.getItem(slot);
            if (!slotStack.isEmpty()) {
                if (slotStack.getItem() == targetItem.get()) {
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

        return ItemStack.EMPTY;
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width * height >= 1;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return RecipeReg.HELD_ITEM_MISSILE_DECRAFT.get();
    }
}
// PORTED

package icbm.classic.content.missile.entity.itemstack.item;

import icbm.classic.api.ICBMClassicAPI;
import icbm.classic.api.missiles.ICapabilityMissileStack;
import icbm.classic.content.missile.entity.itemstack.HeldActionMode;
import icbm.classic.content.reg.RecipeReg;
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
public class RecipeHeldItemMissile extends CustomRecipe {

    private final Supplier<ItemStack> recipeOutput;

    public RecipeHeldItemMissile(CraftingBookCategory category, Supplier<ItemStack> recipeOutput) {
        super(category);
        this.recipeOutput = recipeOutput;
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
        boolean hasMissile = false;
        for(int slot = 0; slot < inv.size(); slot++) {
            final ItemStack slotStack = inv.getItem(slot);
            if(!slotStack.isEmpty()) {
                itemCount++;

                if(slotStack.getItem() == output.getItem()) {
                    if(hasMissile) {
                        return false;
                    }
                    hasMissile = true;
                }
                else if(!isAllowedItem(slotStack)) {
                    return false;
                }
            }
        }
        return itemCount == 2 && hasMissile;
    }

    private boolean isAllowedItem(ItemStack slotStack) {
        return HeldItemMissileHandler.isAllowed(slotStack);
    }

    @Override
    public ItemStack assemble(CraftingInput inv, HolderLookup.Provider registries) {
        // TODO in future version redo recipe to use a data-pad to set the interaction mode
        //       said data pad can be generic use for all crafting to encode settings for missile automation

        final ItemStack output = recipeOutput.get();
        ItemStack missileIn = null;
        ItemStack cargo = null;
        boolean primaryAction = true;
        for(int slot = 0; slot < inv.size() && (cargo == null || missileIn == null); slot++) {
            final ItemStack slotStack = inv.getItem(slot);
            if (!slotStack.isEmpty()) {
                if (slotStack.getItem() == output.getItem()) {
                    missileIn = output; // PORT: kept 1.12 behaviour, output starts from a fresh missile
                } else {
                    cargo = slotStack;
                    // If cargo comes second then secondary action (right click)
                    if(missileIn != null) {
                        primaryAction = false;
                    }
                }
            }
        }

        // Should not happen but IDE is annoying
        if(missileIn == null || cargo == null) {
            return ItemStack.EMPTY;
        }

        final ItemStack missileOut = missileIn.copy();
        missileOut.setCount(1);

        final ICapabilityMissileStack cap = missileOut.getCapability(ICBMClassicAPI.MISSILE_STACK_CAPABILITY);
        if(cap instanceof CapabilityHeldItemMissile heldCap) {
            if(!heldCap.getHeldItem().isEmpty()) {
                return ItemStack.EMPTY;
            }
            final ItemStack insert = cargo.copy();
            insert.setCount(1);

            heldCap.setHeldItem(insert);
            heldCap.setActionMode(primaryAction ? HeldActionMode.PRIMARY : HeldActionMode.SECONDARY);

            return missileOut;
        }

        return ItemStack.EMPTY;
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width * height >= 2;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return RecipeReg.HELD_ITEM_MISSILE.get();
    }
}
// PORTED

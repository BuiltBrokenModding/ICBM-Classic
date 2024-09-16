package icbm.classic.content.missile.entity.itemstack.item;

import icbm.classic.api.ICBMClassicAPI;
import icbm.classic.api.missiles.ICapabilityMissileStack;
import icbm.classic.content.cargo.CargoHolderHandler;
import icbm.classic.content.reg.ItemReg;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.minecraft.inventory.InventoryCrafting;
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
public class RecipeHeldItemMissile extends net.minecraftforge.registries.IForgeRegistryEntry.Impl<IRecipe> implements IRecipe {

    private final ItemStack recipeOutput;

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
        boolean hasMissile = false;
        for(int slot = 0; slot < inv.getSizeInventory(); slot++) {
            final ItemStack slotStack = inv.getStackInSlot(slot);
            if(!slotStack.isEmpty()) {
                itemCount++;

                if(slotStack.getItem() == recipeOutput.getItem()) {
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
        return slotStack.getItem() != ItemReg.itemClusterMissile
            && CargoHolderHandler.isAllowed(slotStack);
    }

    @Override
    public ItemStack getCraftingResult(InventoryCrafting inv) {
        // TODO in future version redo recipe to use a data-pad to set the interaction mode
        //       said data pad can be generic use for all crafting to encode settings for missile automation

        ItemStack missileIn = null;
        ItemStack cargo = null;
        boolean primaryAction = true;
        for(int slot = 0; slot < inv.getSizeInventory() && (cargo == null || missileIn == null); slot++) {
            final ItemStack slotStack = inv.getStackInSlot(slot);
            if (!slotStack.isEmpty()) {
                if (slotStack.getItem() == recipeOutput.getItem()) {
                    missileIn = recipeOutput;
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
            return null;
        }

        final ItemStack missileOut = missileIn.copy();
        missileOut.setCount(1);

        final ICapabilityMissileStack cap = missileOut.getCapability(ICBMClassicAPI.MISSILE_STACK_CAPABILITY, null);
        if(cap instanceof CapabilityHeldItemMissile) {
            if(!((CapabilityHeldItemMissile) cap).getHeldItem().isEmpty()) {
                return null;
            }
            final ItemStack insert = cargo.copy();
            insert.setCount(1);

            ((CapabilityHeldItemMissile) cap).setHeldItem(insert);
            ((CapabilityHeldItemMissile) cap).setPrimaryAction(primaryAction);

            return missileOut;
        }

        return null;
    }

    @Override
    public boolean canFit(int width, int height) {
        return width * height >= 2;
    }
}

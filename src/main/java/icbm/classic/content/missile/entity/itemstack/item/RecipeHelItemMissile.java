package icbm.classic.content.missile.entity.itemstack.item;

import icbm.classic.api.ICBMClassicAPI;
import icbm.classic.api.missiles.ICapabilityMissileStack;
import icbm.classic.api.missiles.projectile.IProjectileStack;
import icbm.classic.content.cargo.CargoHolderHandler;
import icbm.classic.content.cargo.CargoProjectileData;
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
public class RecipeHelItemMissile extends net.minecraftforge.registries.IForgeRegistryEntry.Impl<IRecipe> implements IRecipe {

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

                if(ItemStack.areItemsEqual(slotStack, recipeOutput)) {
                    hasMissile = true;
                }
                // Cluster takes priority as it is before parachute/balloon
                else if((slotStack.getItem() == ItemReg.itemClusterMissile) && !hasMissile) {
                    return false;
                }
                else if(!CargoHolderHandler.isAllowed(slotStack)) {
                    return false;
                }
            }
        }
        return itemCount == 2 && hasMissile;
    }

    @Override
    public ItemStack getCraftingResult(InventoryCrafting inv) {
        ItemStack missileIn = null;
        ItemStack cargo = null;
        for(int slot = 0; slot < inv.getSizeInventory() && (cargo == null || missileIn == null); slot++) {
            final ItemStack slotStack = inv.getStackInSlot(slot);
            if (!slotStack.isEmpty()) {
                if (ItemStack.areItemsEqual(slotStack, recipeOutput)) {
                    missileIn = recipeOutput;
                } else {
                    cargo = slotStack;
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

            return missileOut;
        }

        return null;
    }

    @Override
    public boolean canFit(int width, int height) {
        return width * height >= 2;
    }
}

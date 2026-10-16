package icbm.classic.content.cluster.missile;

import icbm.classic.api.ICBMClassicAPI;
import icbm.classic.api.missiles.ICapabilityMissileStack;
import icbm.classic.config.missile.ConfigMissile;
import icbm.classic.content.reg.RecipeReg;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;


/**
 * Recipe for adding cargo to cargo projectile item
 */
public class RecipeCluster extends CustomRecipe {

    private final Supplier<ItemStack> recipeOutput;

    public RecipeCluster(CraftingBookCategory category, Supplier<ItemStack> recipeOutput) {
        super(category);
        this.recipeOutput = recipeOutput;
    }

    @Override
    public NonNullList<ItemStack> getRemainingItems(CraftingInput inv) {
        // Don't leave container items, since we store the entire item
        return NonNullList.withSize(inv.size(), ItemStack.EMPTY);
    }

    @Override
    public boolean matches(CraftingInput inv, Level worldIn) {
        final ItemStack output = recipeOutput.get();
        ItemStack cluster = ItemStack.EMPTY;
        int newItemSize = 0;

        for (int slot = 0; slot < inv.size(); slot++) {
            final ItemStack slotStack = inv.getItem(slot);
            if(slotStack.isEmpty()) {
                continue;
            }
            else if (cluster.isEmpty() && slotStack.is(output.getItem())) {
                cluster = slotStack;
            }
            else if(!ClusterMissileHandler.isAllowed(slotStack)) {
                return false;
            }

            newItemSize += ClusterMissileHandler.sizeOf(slotStack);
        }

        final CapabilityClusterMissileStack cap = getCap(cluster);
        if(cap == null) {
            return false;
        }

        int currentSize = cap.getActionDataCluster().getClusterSpawnEntries().stream().mapToInt(ClusterMissileHandler::sizeOf).sum();
        return newItemSize >= 1 && (currentSize + newItemSize) <= ConfigMissile.CLUSTER_MISSILE.ITEM_SIZES.MAX_SIZE && !cluster.isEmpty();
    }

    private CapabilityClusterMissileStack getCap(ItemStack itemStack) {
        if (itemStack.isEmpty()) {
            return null;
        }
        ICapabilityMissileStack cap = itemStack.getCapability(ICBMClassicAPI.MISSILE_STACK_CAPABILITY);
        return cap instanceof CapabilityClusterMissileStack ? (CapabilityClusterMissileStack) cap : null;
    }

    @Override
    public ItemStack assemble(CraftingInput inv, HolderLookup.Provider registries) {
        final ItemStack output = recipeOutput.get();
        ItemStack cluster = ItemStack.EMPTY;
        List<ItemStack> newItems = new ArrayList<>();

        for (int slot = 0; slot < inv.size(); slot++) {
            final ItemStack slotStack = inv.getItem(slot);
            if (!slotStack.isEmpty() && slotStack.is(output.getItem())) {
                if (!cluster.isEmpty()) {
                    return ItemStack.EMPTY; // Can't nest clusters
                }
                cluster = slotStack.copy();
                cluster.setCount(1);
            } else if (!slotStack.isEmpty()) {
                ItemStack s = slotStack.copy();
                s.setCount(1);
                newItems.add(s);
            }
        }

        CapabilityClusterMissileStack cap = getCap(cluster);
        if (cap == null) {
            return ItemStack.EMPTY;
        }
        cap.getActionDataCluster().getClusterSpawnEntries().addAll(newItems);
        cap.saveToStack();

        return cluster;
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width * height >= 2;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return RecipeReg.CLUSTER_MISSILE.get();
    }
}
// PORTED

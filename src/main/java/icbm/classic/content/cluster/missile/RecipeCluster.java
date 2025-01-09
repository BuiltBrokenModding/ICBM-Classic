package icbm.classic.content.cluster.missile;

/**
 * Recipe for adding cargo to cargo projectile item
 */
//@EqualsAndHashCode(callSuper = true)
//@AllArgsConstructor
//@Data
public class RecipeCluster { //extends IForgeRegistryEntry.Impl<IRecipe> implements IRecipe {

    /*private final ItemStack recipeOutput;

    @Override
    public boolean isDynamic() {
        return true;
    }

    @Override
    public NonNullList<ItemStack> getRemainingItems(CraftingInventory inv) {
        // Don't leave container items, since we store the entire item
        return NonNullList.withSize(inv.getSizeInventory(), ItemStack.EMPTY);
    }

    @Override
    public boolean matches(CraftingInventory inv, World worldIn) {
        ItemStack cluster = ItemStack.EMPTY;
        int newItemSize = 0;

        for (int slot = 0; slot < inv.getSizeInventory(); slot++) {
            final ItemStack slotStack = inv.getStackInSlot(slot);
            if(slotStack.isEmpty()) {
                continue;
            }
            else if (cluster.isEmpty() && slotStack.isItemEqual(recipeOutput)) {
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
        if (!itemStack.hasCapability(ICBMClassicAPI.MISSILE_STACK_CAPABILITY, null)) {
            return null;
        }
        ICapabilityMissileStack cap = itemStack.getCapability(ICBMClassicAPI.MISSILE_STACK_CAPABILITY, null);
        return cap instanceof CapabilityClusterMissileStack ? (CapabilityClusterMissileStack) cap : null;
    }

    @Override
    public ItemStack getCraftingResult(CraftingInventory inv) {
        ItemStack cluster = ItemStack.EMPTY;
        List<ItemStack> newItems = new ArrayList<>();

        for (int slot = 0; slot < inv.getSizeInventory(); slot++) {
            final ItemStack slotStack = inv.getStackInSlot(slot);
            if (slotStack.isItemEqual(recipeOutput)) {
                if (!cluster.isEmpty()) {
                    return null; // Can't nest clusters
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
            return null;
        }
        cap.getActionDataCluster().getClusterSpawnEntries().addAll(newItems);

        return cluster;
    }

    @Override
    public boolean canFit(int width, int height) {
        return width * height >= 2;
    }*/
}

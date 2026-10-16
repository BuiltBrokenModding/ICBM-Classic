package icbm.classic.content.items;

import icbm.classic.api.ICBMClassicAPI;
import icbm.classic.api.reg.IExplosiveData;
import icbm.classic.api.reg.content.IExplosiveContentRegistry;
import icbm.classic.content.reg.ComponentReg;
import icbm.classic.lib.explosive.ExplosiveStacks;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;

/**
 * PORT: shared logic for the explosive variant items (grenade, bomb cart, missile). Replaces the 1.12 metadata
 * handling, the explosive now lives in {@link ComponentReg#EXPLOSIVE}.
 */
public final class ExplosiveItemHelper
{
    private ExplosiveItemHelper()
    {
    }

    /**
     * Explosive of a stack. A stack without the component acts like 1.12 metadata 0.
     *
     * @return explosive data, or null if the registry isn't ready or the id is unknown
     */
    @Nullable
    public static IExplosiveData getExplosiveData(ItemStack stack)
    {
        if (ICBMClassicAPI.EXPLOSIVE_REGISTRY == null || stack.isEmpty())
        {
            return null;
        }
        final ResourceLocation name = ExplosiveStacks.getExplosiveName(stack);
        if (name != null)
        {
            return ICBMClassicAPI.EXPLOSIVE_REGISTRY.getExplosiveData(name, true);
        }
        return ICBMClassicAPI.EXPLOSIVE_REGISTRY.getExplosiveData(0);
    }

    /** @return translation key prefix + "." + explosive registry name, or the prefix alone */
    public static String getVariantKey(String prefix, ItemStack stack)
    {
        final IExplosiveData data = getExplosiveData(stack);
        if (data != null)
        {
            return prefix + "." + data.getRegistryKey();
        }
        return prefix;
    }

    /** Creates a stack of the item holding the explosive */
    public static ItemStack create(Item item, IExplosiveData data)
    {
        return ExplosiveStacks.create(item, data);
    }

    /** Creates a stack of the item holding the explosive with the given 1.12 id */
    public static ItemStack create(Item item, int explosiveId)
    {
        final IExplosiveData data = ICBMClassicAPI.EXPLOSIVE_REGISTRY != null ? ICBMClassicAPI.EXPLOSIVE_REGISTRY.getExplosiveData(explosiveId) : null;
        if (data != null)
        {
            return create(item, data);
        }
        return new ItemStack(item);
    }

    /** Adds one stack per explosive enabled in the content registry, in id order like 1.12 */
    public static void addSubItems(Item item, @Nullable IExplosiveContentRegistry contentRegistry, NonNullList<ItemStack> items)
    {
        if (contentRegistry == null || contentRegistry.getExplosivesIDs() == null)
        {
            return;
        }
        contentRegistry.getExplosivesIDs().stream().sorted().forEach(id -> {
            final ItemStack stack = create(item, id);
            if (stack.has(ComponentReg.EXPLOSIVE.get()))
            {
                items.add(stack);
            }
        });
    }
}
// PORTED

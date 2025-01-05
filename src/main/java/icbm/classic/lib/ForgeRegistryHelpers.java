package icbm.classic.lib;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.IForgeRegistryEntry;

public class ForgeRegistryHelpers {

    /**
     * Checks if a forge registry contains the value.
     *
     * @param registry to check
     * @param targetKey to look for, supports resource path fuzzy logic (contains, start, end)
     * @return true if at least one entry matches
     */
    public static <V extends IForgeRegistryEntry<V>> boolean contains(IForgeRegistry<V> registry, ResourceLocation targetKey) {
        // Contains
        if(targetKey.getPath().startsWith("~") && targetKey.getPath().endsWith("~")) {
            final String checkStr = targetKey.getPath().substring(1, targetKey.getPath().length() - 1);
            return pathContains(registry, targetKey.getNamespace(), checkStr);
        }
        // Ends
        else if(targetKey.getPath().startsWith("~")) {
            final String checkStr = targetKey.getPath().substring(1);
            return pathEndsWith(registry, targetKey.getNamespace(), checkStr);
        }
        // Starts
        else if(targetKey.getPath().endsWith("~")) {
            final String checkStr = targetKey.getPath().substring(0, targetKey.getPath().length() - 1);
            return pathStartsWith(registry, targetKey.getNamespace(), checkStr);
        }
        // exact match
        return registry.containsKey(targetKey) && registry.getValue(targetKey) != null;
    }

    private static <V extends IForgeRegistryEntry<V>> boolean pathContains(IForgeRegistry<V> registry, String domain, String checkStr) {
        return registry.getKeys()
            .stream()
            .anyMatch(contentKey -> contentKey.getNamespace().equalsIgnoreCase(domain)
                && contentKey.getPath().contains(checkStr)
                && registry.getValue(contentKey) != null);
    }

    private static <V extends IForgeRegistryEntry<V>> boolean pathEndsWith(IForgeRegistry<V> registry, String domain, String checkStr) {
        return registry.getKeys()
            .stream()
            .anyMatch(contentKey -> contentKey.getNamespace().equalsIgnoreCase(domain)
                && contentKey.getPath().endsWith(checkStr)
                && registry.getValue(contentKey) != null);
    }

    private static <V extends IForgeRegistryEntry<V>> boolean pathStartsWith(IForgeRegistry<V> registry, String domain, String checkStr) {
        return registry.getKeys()
            .stream()
            .anyMatch(contentKey -> contentKey.getNamespace().equalsIgnoreCase(domain)
                && contentKey.getPath().startsWith(checkStr)
                && registry.getValue(contentKey) != null);
    }
}

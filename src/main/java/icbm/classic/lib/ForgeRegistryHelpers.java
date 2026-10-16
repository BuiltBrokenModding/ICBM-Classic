package icbm.classic.lib;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.Registry;

public class ForgeRegistryHelpers {

    /**
     * Checks if a registry contains the value. PORT: takes a vanilla {@link Registry} (ex: BuiltInRegistries.ITEM)
     * instead of a Forge IForgeRegistry.
     *
     * @param registry to check
     * @param targetKey to look for, supports resource path fuzzy logic (contains, start, end)
     * @return true if at least one entry matches
     */
    public static <V> boolean contains(Registry<V> registry, ResourceLocation targetKey) {
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
        return registry.containsKey(targetKey) && registry.get(targetKey) != null;
    }

    private static <V> boolean pathContains(Registry<V> registry, String domain, String checkStr) {
        return registry.keySet()
            .stream()
            .anyMatch(contentKey -> contentKey.getNamespace().equalsIgnoreCase(domain)
                && contentKey.getPath().contains(checkStr)
                && registry.get(contentKey) != null);
    }

    private static <V> boolean pathEndsWith(Registry<V> registry, String domain, String checkStr) {
        return registry.keySet()
            .stream()
            .anyMatch(contentKey -> contentKey.getNamespace().equalsIgnoreCase(domain)
                && contentKey.getPath().endsWith(checkStr)
                && registry.get(contentKey) != null);
    }

    private static <V> boolean pathStartsWith(Registry<V> registry, String domain, String checkStr) {
        return registry.keySet()
            .stream()
            .anyMatch(contentKey -> contentKey.getNamespace().equalsIgnoreCase(domain)
                && contentKey.getPath().startsWith(checkStr)
                && registry.get(contentKey) != null);
    }
}
// PORTED

package icbm.classic.lib;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.IForgeRegistryEntry;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mockito;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ForgeRegistryHelpersTests {

    @ParameterizedTest
    @MethodSource("containsData")
    void contains(List<String> keys, String input, boolean expected) {
        // Arrange
        final IForgeRegistry forgeRegistry = Mockito.mock(IForgeRegistry.class);

        final Set<ResourceLocation> resourceLocations = keys.stream().map(ResourceLocation::new).collect(Collectors.toSet());
        Mockito.when(forgeRegistry.getKeys()).thenReturn(resourceLocations);
        Mockito.when(forgeRegistry.containsKey(Mockito.any())).thenAnswer(invocation -> {
            final ResourceLocation in = invocation.getArgument(0);
            return resourceLocations.contains(in);
        });
        Mockito.when(forgeRegistry.getValue(Mockito.any())).thenAnswer(invocation -> {
            final ResourceLocation in = invocation.getArgument(0);
            if(!resourceLocations.contains(in) || in.getResourceDomain().equalsIgnoreCase("nil")) {
                return null;
            }
            final IForgeRegistryEntry entry = Mockito.mock(IForgeRegistryEntry.class);
            Mockito.when(entry.getRegistryName()).thenReturn(in);
            return entry;
        });

        final ResourceLocation targetKey = new ResourceLocation(input);

        // Act
        final boolean result = ForgeRegistryHelpers.contains(forgeRegistry, targetKey);

        // Assert
        Assertions.assertEquals(expected, result);
    }

    private static Stream<Arguments> containsData() {
        List<String> keys = Arrays.asList(
            "minecraft:stone",
            "minecraft:a_stone_1",
            "minecraft:log",
            "minecraft:dark_stone",
            "minecraft:stone_tube",
            "mod:cake",
            "nil:iron"
        );
        return Stream.of(
            // exact checks
            Arguments.of(keys, "minecraft:stone", true),
            Arguments.of(keys, "mod:stone", false),
            Arguments.of(keys, "minecraft:stone_1", false),

            // fuzzy
            Arguments.of(keys, "minecraft:stone~", true),
            Arguments.of(keys, "minecraft:~stone", true),
            Arguments.of(keys, "minecraft:~stone~", true),

            // Key doesn't exist for vanilla
            Arguments.of(keys, "minecraft:~iron~", false),
            Arguments.of(keys, "minecraft:~iron", false),
            Arguments.of(keys, "minecraft:iron~", false),
            Arguments.of(keys, "minecraft:iron", false),

            // Key doesn't exist for mod
            Arguments.of(keys, "mod:~iron~", false),
            Arguments.of(keys, "mod:~iron", false),
            Arguments.of(keys, "mod:iron~", false),
            Arguments.of(keys, "mod:iron", false),

            // Missing content but has a key, think some mods can do this to disable content
            Arguments.of(keys, "nil:~iron~", false),
            Arguments.of(keys, "nil:~iron", false),
            Arguments.of(keys, "nil:iron~", false),
            Arguments.of(keys, "nil:iron", false)

        );
    }
}

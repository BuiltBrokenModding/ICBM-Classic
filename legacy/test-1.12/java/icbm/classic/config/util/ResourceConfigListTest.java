package icbm.classic.config.util;


import icbm.classic.ICBMClassic;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.Loader;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Answers;
import org.mockito.Mockito;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;

class ResourceConfigListTest {

    @ParameterizedTest
    @MethodSource("compareData")
    void compare(ResourceConfigEntry a, ResourceConfigEntry b, int result) {

        final ResourceConfigList configList = new StubbedList("stub", (c) -> {});
        Assertions.assertEquals(result, configList.compare(a, b));
    }

    private static Stream<Arguments> compareData() {
        return Stream.of(
            Arguments.of(new ResourceConfigEntry("test", 1, null), new ResourceConfigEntry("test", 0, null), -1),
            Arguments.of(new ResourceConfigEntry("test", 0, null), new ResourceConfigEntry("test", 1, null), 1),
            Arguments.of(new ResourceConfigEntry("test", 0, null), new ResourceConfigEntry("test", 0, null), 0)
        );
    }

    @Test
    void sort() {

        final ResourceConfigList configList = new StubbedList("stub", (c) -> {});

        final List<ResourceConfigEntry> list = new ArrayList();
        list.add(new ResourceConfigEntry("test", 3, null));
        list.add(new ResourceConfigEntry("test", 17, null));
        list.add(new ResourceConfigEntry("test", 8, null));
        list.add(new ResourceConfigEntry("test", null, null));
        list.add(new ResourceConfigEntry("test", 1, null));
        list.add(new ResourceConfigEntry("test", -1, null));
        list.add(new ResourceConfigEntry("test", null, null));

        configList.sort(list);

        final List<ResourceConfigEntry> expected = new ArrayList();
        expected.add(new ResourceConfigEntry("test", 19, null));
        expected.add(new ResourceConfigEntry("test", 18, null));
        expected.add(new ResourceConfigEntry("test", 17, null));
        expected.add(new ResourceConfigEntry("test", 8, null));
        expected.add(new ResourceConfigEntry("test", 3, null));
        expected.add(new ResourceConfigEntry("test", 1, null));
        expected.add(new ResourceConfigEntry("test", -1, null));
        Assertions.assertEquals(expected, list);
    }

    @ParameterizedTest
    @MethodSource("getDomainValueData")
    void getDomainValue(String domain, ResourceLocation input, boolean expected) {
        final String content = "tree_cat";
        final ResourceConfigList configList = new StubbedList("stub", (c) -> {}) {
            @Override
            protected ResourceLocation getContentKey(Object o) {
                Assertions.assertEquals(content, o);
                return input;
            }
        };
        final Function matcher = configList.getDomainValue(domain, 3);
        if(expected) {
            Assertions.assertEquals(3, matcher.apply(content));
        }
        else {
            Assertions.assertNull(matcher.apply(content));
        }
    }

    private static Stream<Arguments> getDomainValueData() {
        return Stream.of(
            Arguments.of("minecraft", new ResourceLocation("minecraft", "stone"), true),
            Arguments.of("minecraft", new ResourceLocation("minecraft", "tree"), true),
            Arguments.of("minecraft", new ResourceLocation("Minecraft", "tree"), true),
            Arguments.of("minecraft", new ResourceLocation("MiNecRaft", "tree"), true),
            Arguments.of("minecraft", new ResourceLocation("minecraf", "tree"), false),
            Arguments.of("minecraft", new ResourceLocation("inecraft", "tree"), false)
        );
    }

    @ParameterizedTest
    @MethodSource("getSimpleValueData")
    void getSimpleValue(ResourceLocation key, ResourceLocation input, boolean expected) {
        final String content = "tree_cat";
        final ResourceConfigList configList = new StubbedList("stub", (c) -> {}) {
            @Override
            protected ResourceLocation getContentKey(Object o) {
                Assertions.assertEquals(content, o);
                return input;
            }
        };
        final Function matcher = configList.getSimpleValue(key, 3);
        if(expected) {
            Assertions.assertEquals(3, matcher.apply(content));
        }
        else {
            Assertions.assertNull(matcher.apply(content));
        }
    }

    private static Stream<Arguments> getSimpleValueData() {
        return Stream.of(
            Arguments.of(new ResourceLocation("minecraft", "stone"), new ResourceLocation("minecraft", "stone"), true),
            Arguments.of(new ResourceLocation("minecraft", "stone"), new ResourceLocation("minecraft", "log"), false),
            Arguments.of(new ResourceLocation("minecraft", "stone"), new ResourceLocation("mod", "stone"), false),
            Arguments.of(new ResourceLocation("minecraft", "stone"), new ResourceLocation("minecraft", "stone1"), false)
        );
    }

    @Nested
    class HandleEntryTests {
        @Test
        void noMatches() {

            // Arrange
            final String entry = "@tree(minecraft:stone)";
            final boolean[] wasCalled = new boolean[]{false};
            final ResourceConfigList configList = new StubbedList("stub", (c) -> {}) {
                @Override
                protected void issue(String source, String ent, String error, boolean isError) {
                    wasCalled[0] = true;
                    Assertions.assertEquals("test", source);
                    Assertions.assertEquals(entry, ent);
                    Assertions.assertEquals("Unknown format", error);
                    Assertions.assertTrue(isError);
                }
            };

            // Act
            final boolean result = configList.handleEntry("test", entry, 0);

            // Assert
            Assertions.assertFalse(result);

            final Map<ResourceLocation, List<ResourceConfigEntry>> expected = new HashMap();
            Assertions.assertEquals(expected, configList.contentMatchers);
        }

        @Test
        void keyValueRegex() {

            // Arrange
            final Function fun = (v) -> true;
            final ResourceLocation key = new ResourceLocation("minecraft", "stone");

            final boolean[] wasCalled = {false};
            final ResourceConfigList configList = new StubbedList("stub", (c) -> {}) {
                //Work around as Mockito.spy is breaking code-coverage
                @Override
                protected Function getSimpleValue(ResourceLocation targetKey, @Nullable Object o) {
                    wasCalled[0] = true;
                    Assertions.assertEquals(key, targetKey);
                    Assertions.assertNull(o);
                    return fun;
                }
            };

            final String entry = "minecraft:stone";

            // Act
            final boolean result = configList.handleEntry("test", entry, 0);

            // Assert
            Assertions.assertTrue(wasCalled[0]);
            Assertions.assertTrue(result);
            final Map<ResourceLocation, List<ResourceConfigEntry>> expected = new HashMap();
            expected.put(key, Collections.singletonList(new ResourceConfigEntry("resource_simple",0, fun).setKey(key)));
            Assertions.assertEquals(expected, configList.contentMatchers);
        }

        @Test
        void keyValueRegex_withValue() {

            // Arrange
            final String entry = "minecraft:stone=578";
            final Function fun = (v) -> true;
            final ResourceLocation key = new ResourceLocation("minecraft", "stone");

            final boolean[] wasCalled = {false};
            final ResourceConfigList configList = new StubbedList("stub", (c) -> {}) {
                //Work around as Mockito.spy is breaking code-coverage
                @Override
                protected Function getSimpleValue(ResourceLocation targetKey, @Nullable Object o) {
                    wasCalled[0] = true;
                    Assertions.assertEquals(key, targetKey);
                    Assertions.assertEquals(578, o);
                    return fun;
                }

                @Override
                protected Object parseValue(String source, String entry, String value) {
                    assert value != null;
                    return Integer.parseInt(value);
                }
            };

            // Act
            final boolean result = configList.handleEntry("test", entry, 0);

            // Assert
            Assertions.assertTrue(wasCalled[0]);
            Assertions.assertTrue(result);
            final Map<ResourceLocation, List<ResourceConfigEntry>> expected = new HashMap();
            expected.put(key, Collections.singletonList(new ResourceConfigEntry("resource_simple", 0, fun).setKey(key)));
            Assertions.assertEquals(expected, configList.contentMatchers);
        }

        @Test
        void keyValueRegex_rejected() {
            // Arrange
            final String entry = "minecraft:stone";
            final ResourceLocation key = new ResourceLocation("minecraft", "stone");

            final boolean[] wasCalled = {false};
            final ResourceConfigList configList = new StubbedList("stub", (c) -> {}) {
                @Override
                protected Function getSimpleValue(ResourceLocation targetKey, @Nullable Object o) {
                    wasCalled[0] = true;
                    Assertions.assertEquals(key, targetKey);
                    Assertions.assertNull(o);
                    return null; // null is rejected, usually means key is not found
                }
            };

            // Act
            final boolean result = configList.handleEntry("test", entry, 0);

            // Assert
            Assertions.assertTrue(wasCalled[0]);
            Assertions.assertTrue(result);
            final Map<ResourceLocation, List<ResourceConfigEntry>> expected = new HashMap();
            Assertions.assertEquals(expected, configList.contentMatchers);
        }

        @Test
        void metadataRegex() {
            // Arrange
            final String entry = "minecraft:stone@1";
            final Function fun = (v) -> true;
            final ResourceLocation key = new ResourceLocation("minecraft", "stone");

            final boolean[] wasCalled = {false};
            final ResourceConfigList configList = new StubbedList("stub", (c) -> {}) {
                @Override
                protected Function getMetaValue(ResourceLocation k, int metadata, @Nullable Object o) {
                    wasCalled[0] = true;
                    Assertions.assertEquals(key, k);
                    Assertions.assertEquals(1, metadata);
                    Assertions.assertNull(o);
                    return fun;
                }
            };

            // Act
            final boolean result = configList.handleEntry("test", entry, 0);

            // Assert
            Assertions.assertTrue(wasCalled[0]);
            Assertions.assertTrue(result);
            final Map<ResourceLocation, List<ResourceConfigEntry>> expected = new HashMap();
            expected.put(key, Collections.singletonList(new ResourceConfigEntry("resource_metadata",0, fun).setKey(key)));

            Assertions.assertEquals(expected, configList.contentMatchers);
        }

        @Test
        void metadataRegex_withValue() {
            // Arrange
            final String entry = "minecraft:stone@1=10";
            final Function fun = (v) -> true;
            final ResourceLocation key = new ResourceLocation("minecraft", "stone");

            final boolean[] wasCalled = {false};
            final ResourceConfigList configList = new StubbedList("stub", (c) -> {}) {
                @Override
                protected Function getMetaValue(ResourceLocation k, int metadata, @Nullable Object o) {
                    wasCalled[0] = true;
                    Assertions.assertEquals(key, k);
                    Assertions.assertEquals(1, metadata);
                    Assertions.assertEquals(10, o);
                    return fun;
                }

                @Override
                protected Object parseValue(String source, String entry, @Nullable String value) {
                    assert value != null;
                    return Integer.parseInt(value);
                }
            };

            // Act
            final boolean result = configList.handleEntry("test", entry, 0);

            // Assert
            Assertions.assertTrue(wasCalled[0]);
            Assertions.assertTrue(result);
            final Map<ResourceLocation, List<ResourceConfigEntry>> expected = new HashMap();
            expected.put(key, Collections.singletonList(new ResourceConfigEntry("resource_metadata",0, fun).setKey(key)));

            Assertions.assertEquals(expected, configList.contentMatchers);
        }

        @Test
        void metadataRegex_rejected() {
            // Arrange
            final String entry = "minecraft:stone@1";
            final ResourceLocation key = new ResourceLocation("minecraft", "stone");

            final boolean[] wasCalled = {false};
            final ResourceConfigList configList = new StubbedList("stub", (c) -> {}) {
                @Override
                protected Function getMetaValue(ResourceLocation k, int metadata, @Nullable Object o) {
                    wasCalled[0] = true;
                    Assertions.assertEquals(key, k);
                    Assertions.assertEquals(1, metadata);
                    Assertions.assertNull(o);
                    return null;
                }
            };

            // Act
            final boolean result = configList.handleEntry("test", entry, 0);

            // Assert
            Assertions.assertTrue(wasCalled[0]);
            Assertions.assertTrue(result);
            final Map<ResourceLocation, List<ResourceConfigEntry>> expected = new HashMap();
            Assertions.assertEquals(expected, configList.contentMatchers);
        }

        @Test
        void sortingRegex() {
            // Arrange
            final String entry = "@sort(3,minecraft:stone)";
            final Function fun = (v) -> true;
            final ResourceLocation key = new ResourceLocation("minecraft", "stone");

            final boolean[] wasCalled = {false};
            final ResourceConfigList configList = new StubbedList("stub", (c) -> {}) {
                @Override
                protected Function getSimpleValue(ResourceLocation targetKey, @Nullable Object o) {
                    wasCalled[0] = true;
                    Assertions.assertEquals(key, targetKey);
                    Assertions.assertNull(o);
                    return fun;
                }
            };

            // Act
            final boolean result = configList.handleEntry("test", entry, 33);

            // Assert
            Assertions.assertTrue(wasCalled[0]);
            Assertions.assertTrue(result);

            final Map<ResourceLocation, List<ResourceConfigEntry>> expected = new HashMap();
            expected.put(key, Collections.singletonList(new ResourceConfigEntry("resource_simple", 3, fun).setKey(key)));

            Assertions.assertEquals(expected, configList.contentMatchers);
        }

        @Test
        void domainRegex() {
            // Arrange
            final String entry = "@domain:minecraft";
            final Function fun = (v) -> true;

            final boolean[] wasCalled = {false};
            final ResourceConfigList configList = new StubbedList("stub", (c) -> {}) {
                @Override
                protected Function getDomainValue(String domain, @Nullable Object o) {
                    wasCalled[0] = true;
                    Assertions.assertEquals("minecraft", domain);
                    Assertions.assertNull(o);
                    return fun;
                }
            };

            // Act
            final boolean result = configList.handleEntry("test", entry, 3);

            // Assert
            Assertions.assertTrue(wasCalled[0]);
            Assertions.assertTrue(result);
            Assertions.assertEquals(Collections.singletonList(new ResourceConfigEntry("resource_domain", 3, fun)), configList.generalMatchers);
        }

        @Test
        void domainRegex_rejected() {
            final String entry = "@domain:minecraft";

            final boolean[] wasCalled = {false};
            final ResourceConfigList configList = new StubbedList("stub", (c) -> {}) {
                @Override
                protected Function getDomainValue(String domain, @Nullable Object o) {
                    wasCalled[0] = true;
                    Assertions.assertEquals("minecraft", domain);
                    Assertions.assertNull(o);
                    return null; // null is rejected
                }
            };

            // Act
            final boolean result = configList.handleEntry("test", entry, 30);

            // Assert
            Assertions.assertTrue(wasCalled[0]);
            Assertions.assertTrue(result);
            Assertions.assertEquals(Collections.emptyList(), configList.generalMatchers);
        }
    }

    public static class StubbedList extends ResourceConfigList {

        public StubbedList(String name, Consumer reloadCallback) {
            super(name, "www.config.url", reloadCallback);
            addMatcher(META_KEY_REGEX, this::handleMeta);
            addMatcher(KEY_VALUE_REGEX, this::handleSimple);
        }

        @Override
        protected boolean isDomainValid(String domain) {
            return true;
        }

        @Override
        protected ResourceLocation getContentKey(Object o) {
            return null;
        }

        @Override
        protected Object parseValue(String source, String entry, String value) {
            return null;
        }
    }
}
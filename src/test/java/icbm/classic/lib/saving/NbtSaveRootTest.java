package icbm.classic.lib.saving;

import net.minecraft.nbt.CompoundNBT;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

public class NbtSaveRootTest
{
    @Test
    void init_saveKey() {
        final NbtSaveRoot root = new NbtSaveRoot("thing", null, null);
        Assertions.assertEquals("thing", root.getSaveKey());
    }

    @Test
    void init_handler() {
        final NbtSaveHandler handler = new NbtSaveHandler();
        final NbtSaveRoot root = new NbtSaveRoot("thing", handler, null);
        Assertions.assertSame(handler, root.base());
    }

    @Test
    void init_parent() {
        final NbtSaveRoot parent = new NbtSaveRoot("duck", null, null);
        final NbtSaveRoot root = new NbtSaveRoot("thing", null, parent);
        Assertions.assertSame(parent, root.parent());
    }

    @Test
    void init_nullSaveKey_throwsError() {
        Assertions.assertThrows(IllegalArgumentException.class,
            () -> new NbtSaveRoot(null, null, null),
            "save key can't be null"
        );
    }

    @Nested
    class Save {

        @Test
        void noNodes() {
            final RandomThingMany thing = new RandomThingMany();
            final NbtSaveRoot root = new NbtSaveRoot("thing", null, null);

            final CompoundNBT save = root.save(thing);

            Assertions.assertTrue(save.isEmpty());
        }

        @Test
        void withNode() {
            final RandomThingMany thing = new RandomThingMany();
            thing.field1 = 4567;

            final NbtSaveRoot<RandomThingMany> root = new NbtSaveRoot<RandomThingMany>("thing", null, null)
                .nodeInteger("i", (t) -> t.field1, (t, i) -> t.field1 = i);

            final CompoundNBT expectedSave = new CompoundNBT();
            expectedSave.putInt("i", 4567);

            final CompoundNBT save = root.save(thing);

            Assertions.assertEquals(expectedSave, save);
        }

        @Test
        @DisplayName("Validate nodes can return null to skip saving")
        void withNodeButNullSaveTag() {
            final RandomThingMany thing = new RandomThingMany();
            thing.field1 = 4567;

            final NbtSaveRoot<RandomThingMany> root = new NbtSaveRoot<RandomThingMany>("thing", null, null)
                .node(new NbtSaveNode("i", (t) -> null, null));

            final CompoundNBT expectedSave = new CompoundNBT();

            final CompoundNBT save = root.save(thing);

            Assertions.assertEquals(expectedSave, save);
        }

        @Test
        @DisplayName("Validate nodes can return empty to skip saving")
        void withNodeButEmptySaveTag() {
            final RandomThingMany thing = new RandomThingMany();
            thing.field1 = 4567;

            final NbtSaveRoot<RandomThingMany> root = new NbtSaveRoot<RandomThingMany>("thing", null, null)
                .node(new NbtSaveNode("i", (t) -> new CompoundNBT(), null));

            final CompoundNBT expectedSave = new CompoundNBT();

            final CompoundNBT save = root.save(thing);

            Assertions.assertEquals(expectedSave, save);
        }

        @Test
        void nestedNodes() {
            final RandomThingMany thing = new RandomThingMany();
            thing.field1 = 4567;

            final NbtSaveRoot<RandomThingMany> root = new NbtSaveRoot<RandomThingMany>("thing", null, null)
                .nodeInteger("i", (t) -> t.field1, null)
                .addRoot("dave")
                /* */.nodeBoolean("b", (t) -> t.field1 > 0, null)
                .parent();

            final CompoundNBT expectedSave = new CompoundNBT();
            expectedSave.putInt("i", 4567);
            final CompoundNBT dave = new CompoundNBT();
            dave.putBoolean("b", true);
            expectedSave.put("dave", dave);

            final CompoundNBT save = root.save(thing);

            Assertions.assertEquals(expectedSave, save);
        }
    }

    @Nested
    class Load {
        @Test
        void nothingToLoad() {
            final RandomThingMany thing = new RandomThingMany();

            final NbtSaveRoot<RandomThingMany> root = new NbtSaveRoot<RandomThingMany>("thing", null, null)
                .nodeInteger("i", null, null);

            final CompoundNBT saveToLoad = new CompoundNBT();

            root.load(thing, saveToLoad);

            Assertions.assertEquals(1, thing.field1);
        }

        @Test
        void noNodes() {
            final RandomThingMany thing = new RandomThingMany();

            final NbtSaveRoot<RandomThingMany> root = new NbtSaveRoot<RandomThingMany>("thing", null, null);

            final CompoundNBT saveToLoad = new CompoundNBT();
            saveToLoad.putInt("i", 12345);

            root.load(thing, saveToLoad);

            Assertions.assertEquals(1, thing.field1);
        }

        @Test
        void nodesButEmptySave() {
            final RandomThingMany thing = new RandomThingMany();

            final NbtSaveRoot<RandomThingMany> root = new NbtSaveRoot<RandomThingMany>("thing", null, null)
                .nodeInteger("i", null, (t, i) -> t.field1 = i);

            final CompoundNBT saveToLoad = new CompoundNBT();

            root.load(thing, saveToLoad);

            Assertions.assertEquals(1, thing.field1);
        }

        @Test
        void nodesNullSave() {
            final RandomThingMany thing = new RandomThingMany();

            final NbtSaveRoot<RandomThingMany> root = new NbtSaveRoot<RandomThingMany>("thing", null, null)
                .nodeInteger("i", null, (t, i) -> t.field1 = i);

            root.load(thing, null);

            Assertions.assertEquals(1, thing.field1);
        }

        @Test
        void nodesButNoMatchingData() {
            final RandomThingMany thing = new RandomThingMany();

            final NbtSaveRoot<RandomThingMany> root = new NbtSaveRoot<RandomThingMany>("thing", null, null)
                .nodeInteger("i", null, (t, i) -> t.field1 = i);

            final CompoundNBT saveToLoad = new CompoundNBT();
            saveToLoad.putInt("j", 456);

            root.load(thing, saveToLoad);

            Assertions.assertEquals(1, thing.field1);
        }

        @Test
        void nodes() {
            final RandomThingMany thing = new RandomThingMany();

            final NbtSaveRoot<RandomThingMany> root = new NbtSaveRoot<RandomThingMany>("thing", null, null)
                .nodeInteger("i", null, (t, i) -> t.field1 = i);

            final CompoundNBT saveToLoad = new CompoundNBT();
            saveToLoad.putInt("j", 456);
            saveToLoad.putInt("i", 123);

            root.load(thing, saveToLoad);

            Assertions.assertEquals(123, thing.field1);
        }

        @Test
        void nestedLoad() {
            final RandomThingMany thing = new RandomThingMany();

            final NbtSaveRoot<RandomThingMany> root = new NbtSaveRoot<RandomThingMany>("thing", null, null)
                .nodeInteger("i", null, (t, i) -> t.field1 = i)
                .addRoot("jim")
                /* */.nodeInteger("j", null, (t, i) -> t.field2 = i)
                .parent();

            final CompoundNBT saveToLoad = new CompoundNBT();
            saveToLoad.putInt("j", 456);
            saveToLoad.putInt("i", 123);

            final CompoundNBT jim = new CompoundNBT();
            jim.putInt("j", 789);
            saveToLoad.put("jim", jim);

            root.load(thing, saveToLoad);

            Assertions.assertEquals(123, thing.field1);
            Assertions.assertEquals(789, thing.field2);
        }
    }

    private class RandomThingMany {
        int field1 = 1;
        int field2 = 2;
    }
}

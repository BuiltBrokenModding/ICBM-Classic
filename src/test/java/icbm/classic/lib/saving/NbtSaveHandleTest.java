package icbm.classic.lib.saving;

import net.minecraft.nbt.CompoundTag;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

public class NbtSaveHandleTest
{
    @Test
    @DisplayName("Validate #mainRoot() returns root")
    void init_mainRoot() {
        final NbtSaveHandler saveHandler = new NbtSaveHandler();
        Assertions.assertNotNull(saveHandler.mainRoot());
        Assertions.assertEquals("root", saveHandler.mainRoot().getSaveKey());
    }

    @Nested
    @DisplayName("Validate save")
    class Save {
        @Test
        @DisplayName("Validate returns tag passed into method")
        void returnsOriginalTag() {
            final NbtSaveHandler<RandomSaveThing> saveHandler = new NbtSaveHandler<RandomSaveThing>();

            final CompoundTag originalSave = new CompoundTag();
            final CompoundTag save = saveHandler.save(new RandomSaveThing(), originalSave);

            Assertions.assertSame(originalSave, save);
        }

        @Test
        @DisplayName("Validate adds no tags if no roots or main root exists")
        void nothingToSave() {
            final NbtSaveHandler<RandomSaveThing> saveHandler = new NbtSaveHandler<RandomSaveThing>();

            final CompoundTag originalSave = new CompoundTag();
            final CompoundTag save = saveHandler.save(new RandomSaveThing(), originalSave);

            Assertions.assertTrue(save.isEmpty());
        }

        @Test
        @DisplayName("Validate handles root with no nodes")
        void onlySingleRootWithNoNodes() {
            //Setup
            final NbtSaveHandler<RandomSaveThing> saveHandler = new NbtSaveHandler<RandomSaveThing>()
                .addRoot("dave")
                .base();
            final CompoundTag expectedSave = new CompoundTag();
            expectedSave.put("dave", new CompoundTag());

            //Invoke
            final CompoundTag save = saveHandler.save(new RandomSaveThing(), new CompoundTag());

            //Check
            Assertions.assertEquals(expectedSave, save);
        }

        @Test
        @DisplayName("Validate handles root with some nodes")
        void singleRoot_withNodes() {
            //Setup
            final NbtSaveHandler<RandomSaveThing> saveHandler = new NbtSaveHandler<RandomSaveThing>()
                .addRoot("dave")
                .nodeInteger("i", (r) -> r.field1, (r, i) -> r.field1 = i)
                .base();

            final CompoundTag expectedSave = new CompoundTag();
            final CompoundTag dave = new CompoundTag();
            dave.setInteger("i", 23);
            expectedSave.put("dave", dave);

            final RandomSaveThing saveThing = new RandomSaveThing();
            saveThing.field1 = 23;

            //Invoke
            final CompoundTag save = saveHandler.save(saveThing, new CompoundTag());

            //Check
            Assertions.assertEquals(expectedSave, save);
        }

        @Test
        @DisplayName("Validate handles main root with some nodes")
        void mainRoot() {
            //Setup
            final NbtSaveHandler<RandomSaveThing> saveHandler = new NbtSaveHandler<RandomSaveThing>()
                .mainRoot()
                .nodeInteger("i", (r) -> r.field1, (r, i) -> r.field1 = i)
                .base();

            final CompoundTag expectedSave = new CompoundTag();
            expectedSave.setInteger("i", 23);

            final RandomSaveThing saveThing = new RandomSaveThing();
            saveThing.field1 = 23;

            //Invoke
            final CompoundTag save = saveHandler.save(saveThing, new CompoundTag());

            //Check
            Assertions.assertEquals(expectedSave, save);
        }

        @Test
        @DisplayName("Validate handles main root with a node that returns null for data")
        void mainRoot_nullTagData() {
            //Setup
            final NbtSaveHandler<RandomSaveThing> saveHandler = new NbtSaveHandler<RandomSaveThing>()
                .mainRoot()
                .node(new NbtSaveNode<>("i", (r) -> null, null))
                .base();

            final CompoundTag expectedSave = new CompoundTag();

            final RandomSaveThing saveThing = new RandomSaveThing();

            //Invoke
            final CompoundTag save = saveHandler.save(saveThing, new CompoundTag());

            //Check
            Assertions.assertEquals(expectedSave, save);
        }

        @Test
        @DisplayName("Validate handles main root with a node that returns empty data")
        void mainRoot_emptyTagData() {
            //Setup
            final NbtSaveHandler<RandomSaveThing> saveHandler = new NbtSaveHandler<RandomSaveThing>()
                .mainRoot()
                .node(new NbtSaveNode<>("i", (r) -> new CompoundTag(), null))
                .base();

            final CompoundTag expectedSave = new CompoundTag();

            final RandomSaveThing saveThing = new RandomSaveThing();

            //Invoke
            final CompoundTag save = saveHandler.save(saveThing, new CompoundTag());

            //Check
            Assertions.assertEquals(expectedSave, save);
        }
    }

    @Nested
    @DisplayName("Validate load")
    class Load {

        @Test
        @DisplayName("Validate loading with nothing saved")
        void load_noData() {
            //Setup
            final NbtSaveHandler<RandomSaveThing> saveHandler = new NbtSaveHandler<RandomSaveThing>()
                .addRoot("dave")
                .nodeInteger("i", (r) -> r.field1, (r, i) -> r.field1 = i)
                .base();

            final CompoundTag saveToLoad = new CompoundTag();
            final RandomSaveThing saveThing = new RandomSaveThing();

            //Invoke
            saveHandler.load(saveThing, saveToLoad);

            //Check nothing was loaded
            Assertions.assertEquals(1, saveThing.field1);
            Assertions.assertEquals("", saveThing.field2);
        }

        @Test
        @DisplayName("Validate loading with no nodes to accept the save data")
        void load_dataNoNodes() {
            //Setup
            final NbtSaveHandler<RandomSaveThing> saveHandler = new NbtSaveHandler<RandomSaveThing>();

            final CompoundTag saveToLoad = new CompoundTag();
            saveToLoad.setInteger("i", 3);
            saveToLoad.putString("bob", "dave");

            final RandomSaveThing saveThing = new RandomSaveThing();

            //Invoke
            saveHandler.load(saveThing, saveToLoad);

            //Check nothing was loaded
            Assertions.assertEquals(1, saveThing.field1);
            Assertions.assertEquals("", saveThing.field2);
        }

        @Test
        @DisplayName("Validate loading with a root but no nodes to accept save data")
        void load_rootNoNodes() {
            //Setup
            final NbtSaveHandler<RandomSaveThing> saveHandler = new NbtSaveHandler<RandomSaveThing>()
                .addRoot("bob")
                .base();

            final CompoundTag saveToLoad = new CompoundTag();
            saveToLoad.setInteger("i", 3);
            saveToLoad.putString("bob", "dave");

            final RandomSaveThing saveThing = new RandomSaveThing();

            //Invoke
            saveHandler.load(saveThing, saveToLoad);

            //Check nothing was loaded
            Assertions.assertEquals(1, saveThing.field1);
            Assertions.assertEquals("", saveThing.field2);
        }

        @Test
        @DisplayName("Validate loading with a root but no matching data for nodes to use")
        void load_rootWithNodesButNoMatchingData() {
            //Setup
            final NbtSaveHandler<RandomSaveThing> saveHandler = new NbtSaveHandler<RandomSaveThing>()
                .addRoot("bob")
                .nodeInteger("j", null, (t, i) -> t.field1 = i)
                .base();

            final CompoundTag saveToLoad = new CompoundTag();
            saveToLoad.setInteger("i", 3);
            saveToLoad.putString("bob", "dave");

            final RandomSaveThing saveThing = new RandomSaveThing();

            //Invoke
            saveHandler.load(saveThing, saveToLoad);

            //Check nothing was loaded
            Assertions.assertEquals(1, saveThing.field1);
            Assertions.assertEquals("", saveThing.field2);
        }

        @Test
        @DisplayName("Validate loading with a root with nodes to accept save data")
        void load_rootWithNodes() {
            //Setup
            final NbtSaveHandler<RandomSaveThing> saveHandler = new NbtSaveHandler<RandomSaveThing>()
                .addRoot("bob")
                .nodeInteger("i", (r) -> r.field1, (r, i) -> r.field1 = i)
                .base();

            final CompoundTag saveToLoad = new CompoundTag();
            final CompoundTag bob = new CompoundTag();
            bob.setInteger("i", 3);
            saveToLoad.put("bob", bob);
            saveToLoad.putString("f", "dave");

            final RandomSaveThing saveThing = new RandomSaveThing();

            //Invoke
            saveHandler.load(saveThing, saveToLoad);

            //Check that something was loaded
            Assertions.assertEquals(3, saveThing.field1);

            //Check that nothing was loaded
            Assertions.assertEquals("", saveThing.field2);
        }


        @Test
        @DisplayName("Validate loading with a main save data")
        void load_main() {
            //Setup
            final NbtSaveHandler<RandomSaveThing> saveHandler = new NbtSaveHandler<RandomSaveThing>()
                .mainRoot()
                .nodeInteger("i", (r) -> r.field1, (r, i) -> r.field1 = i)
                .base();

            final CompoundTag saveToLoad = new CompoundTag();
            saveToLoad.setInteger("i", 3);
            saveToLoad.putString("bob", "dave");

            final RandomSaveThing saveThing = new RandomSaveThing();

            //Invoke
            saveHandler.load(saveThing, saveToLoad);

            //Check that something was loaded
            Assertions.assertEquals(3, saveThing.field1);

            //Check that nothing was loaded
            Assertions.assertEquals("", saveThing.field2);
        }
    }



    private class RandomSaveThing {
        public int field1 = 1;
        public String field2 = "";
    }
}

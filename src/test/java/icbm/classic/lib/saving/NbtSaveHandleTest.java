package icbm.classic.lib.saving;

import net.minecraft.nbt.CompoundNBT;
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

            final CompoundNBT originalSave = new CompoundNBT();
            final CompoundNBT save = saveHandler.save(new RandomSaveThing(), originalSave);

            Assertions.assertSame(originalSave, save);
        }

        @Test
        @DisplayName("Validate adds no tags if no roots or main root exists")
        void nothingToSave() {
            final NbtSaveHandler<RandomSaveThing> saveHandler = new NbtSaveHandler<RandomSaveThing>();

            final CompoundNBT originalSave = new CompoundNBT();
            final CompoundNBT save = saveHandler.save(new RandomSaveThing(), originalSave);

            Assertions.assertTrue(save.isEmpty());
        }

        @Test
        @DisplayName("Validate handles root with no nodes")
        void onlySingleRootWithNoNodes() {
            //Setup
            final NbtSaveHandler<RandomSaveThing> saveHandler = new NbtSaveHandler<RandomSaveThing>()
                .addRoot("dave")
                .base();
            final CompoundNBT expectedSave = new CompoundNBT();
            expectedSave.put("dave", new CompoundNBT());

            //Invoke
            final CompoundNBT save = saveHandler.save(new RandomSaveThing(), new CompoundNBT());

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

            final CompoundNBT expectedSave = new CompoundNBT();
            final CompoundNBT dave = new CompoundNBT();
            dave.putInt("i", 23);
            expectedSave.put("dave", dave);

            final RandomSaveThing saveThing = new RandomSaveThing();
            saveThing.field1 = 23;

            //Invoke
            final CompoundNBT save = saveHandler.save(saveThing, new CompoundNBT());

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

            final CompoundNBT expectedSave = new CompoundNBT();
            expectedSave.putInt("i", 23);

            final RandomSaveThing saveThing = new RandomSaveThing();
            saveThing.field1 = 23;

            //Invoke
            final CompoundNBT save = saveHandler.save(saveThing, new CompoundNBT());

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

            final CompoundNBT expectedSave = new CompoundNBT();

            final RandomSaveThing saveThing = new RandomSaveThing();

            //Invoke
            final CompoundNBT save = saveHandler.save(saveThing, new CompoundNBT());

            //Check
            Assertions.assertEquals(expectedSave, save);
        }

        @Test
        @DisplayName("Validate handles main root with a node that returns empty data")
        void mainRoot_emptyTagData() {
            //Setup
            final NbtSaveHandler<RandomSaveThing> saveHandler = new NbtSaveHandler<RandomSaveThing>()
                .mainRoot()
                .node(new NbtSaveNode<>("i", (r) -> new CompoundNBT(), null))
                .base();

            final CompoundNBT expectedSave = new CompoundNBT();

            final RandomSaveThing saveThing = new RandomSaveThing();

            //Invoke
            final CompoundNBT save = saveHandler.save(saveThing, new CompoundNBT());

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

            final CompoundNBT saveToLoad = new CompoundNBT();
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

            final CompoundNBT saveToLoad = new CompoundNBT();
            saveToLoad.putInt("i", 3);
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

            final CompoundNBT saveToLoad = new CompoundNBT();
            saveToLoad.putInt("i", 3);
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

            final CompoundNBT saveToLoad = new CompoundNBT();
            saveToLoad.putInt("i", 3);
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

            final CompoundNBT saveToLoad = new CompoundNBT();
            final CompoundNBT bob = new CompoundNBT();
            bob.putInt("i", 3);
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

            final CompoundNBT saveToLoad = new CompoundNBT();
            saveToLoad.putInt("i", 3);
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

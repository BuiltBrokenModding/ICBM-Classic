package icbm.classic.lib.saving;

import net.minecraft.nbt.NBTTagString;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class NbtSaveNodeTest
{
    @Test
    void init_checkSaveKey() {
        final NbtSaveNode node = new NbtSaveNode("tree", null, null);
        Assertions.assertEquals("tree", node.getSaveKey());
    }

    @Test
    void init_nullSaveKey_throwsError() {
        Assertions.assertThrows(IllegalArgumentException.class,
            () -> new NbtSaveNode(null, null, null),
            "save key can't be null");
    }

    @Test
    void save() {
        final SimpleTestObject simpleTestObject = new SimpleTestObject();
        simpleTestObject.someField = "123454";
        final NbtSaveNode<SimpleTestObject, NBTTagString> node
            = new NbtSaveNode<SimpleTestObject, NBTTagString>("test", (obj) -> new NBTTagString(obj.someField), null);

        Assertions.assertEquals(new NBTTagString("123454"), node.save(simpleTestObject));
    }

    @Test
    void load() {
        final SimpleTestObject simpleTestObject = new SimpleTestObject();
        final NBTTagString saveData = new NBTTagString("56789");

        final NbtSaveNode<SimpleTestObject, NBTTagString> node
            = new NbtSaveNode<SimpleTestObject, NBTTagString>("test", null, (obj, data) -> simpleTestObject.someField = data.getString());

        node.load(simpleTestObject, saveData);
        Assertions.assertEquals("56789", simpleTestObject.someField);
    }

    private class SimpleTestObject {
        public String someField = "abcd";
    }
}

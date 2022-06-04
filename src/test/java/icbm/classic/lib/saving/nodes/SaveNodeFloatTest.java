package icbm.classic.lib.saving.nodes;

import net.minecraft.nbt.NBTTagFloat;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class SaveNodeFloatTest
{
    final SaveNodeFloat<FloatSaveThing> node = new SaveNodeFloat<>("f",
        (o) -> o.f,
        (o, i) -> o.f = i
    );

    @Test
    void save() {
        final FloatSaveThing thing = new FloatSaveThing();
        thing.f = 245.23f;

        final NBTTagFloat save = node.save(thing);

        Assertions.assertEquals(245.23f, save.getFloat());
    }

    @Test
    void load() {
        final FloatSaveThing thing = new FloatSaveThing();
        final NBTTagFloat save = new NBTTagFloat(123.23f);

        node.load(thing, save);

        Assertions.assertEquals(123.23f, thing.f);
    }

    static class FloatSaveThing {
        public float f;
    }
}
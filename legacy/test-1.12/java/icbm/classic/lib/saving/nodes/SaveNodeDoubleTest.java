package icbm.classic.lib.saving.nodes;

import net.minecraft.nbt.NBTTagDouble;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;


class SaveNodeDoubleTest
{
    final SaveNodeDouble<DoubleSaveThing> node = new SaveNodeDouble<DoubleSaveThing>("d",
        (o) -> o.d,
        (o, i) -> o.d = i
    );

    @Test
    void save() {
        final DoubleSaveThing thing = new DoubleSaveThing();
        thing.d = 245.23;

        final NBTTagDouble save = node.save(thing);

        Assertions.assertEquals(245.23, save.getDouble());
    }

    @Test
    void load() {
        final DoubleSaveThing thing = new DoubleSaveThing();
        final NBTTagDouble save = new NBTTagDouble(123.23);

        node.load(thing, save);

        Assertions.assertEquals(123.23, thing.d);
    }

    static class DoubleSaveThing {
        public double d;
    }
}
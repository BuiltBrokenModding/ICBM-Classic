package icbm.classic.lib.saving.nodes;

import net.minecraft.nbt.NBTTagByte;
import net.minecraft.core.Direction;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class SaveNodeFacingTest
{
    final SaveNodeFacing<SideSaveThing> node = new SaveNodeFacing<>("side",
        (o) -> o.facing,
        (o, side) -> o.facing = side
    );

    @Test
    void save() {
        final SideSaveThing thing = new SideSaveThing();
        thing.facing = Direction.EAST;

        final NBTTagByte save = node.save(thing);

        Assertions.assertEquals((byte)Direction.EAST.ordinal(), save.getByte());
    }

    @Test
    void load() {
        final SideSaveThing thing = new SideSaveThing();
        final NBTTagByte save = new NBTTagByte((byte)Direction.DOWN.ordinal());

        node.load(thing, save);

        Assertions.assertEquals(Direction.DOWN, thing.facing);
    }

    static class SideSaveThing {
        public Direction facing;
    }
}
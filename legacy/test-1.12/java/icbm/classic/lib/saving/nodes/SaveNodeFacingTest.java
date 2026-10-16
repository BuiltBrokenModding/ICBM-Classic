package icbm.classic.lib.saving.nodes;

import net.minecraft.nbt.NBTTagByte;
import net.minecraft.util.EnumFacing;
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
        thing.facing = EnumFacing.EAST;

        final NBTTagByte save = node.save(thing);

        Assertions.assertEquals((byte)EnumFacing.EAST.ordinal(), save.getByte());
    }

    @Test
    void load() {
        final SideSaveThing thing = new SideSaveThing();
        final NBTTagByte save = new NBTTagByte((byte)EnumFacing.DOWN.ordinal());

        node.load(thing, save);

        Assertions.assertEquals(EnumFacing.DOWN, thing.facing);
    }

    static class SideSaveThing {
        public EnumFacing facing;
    }
}
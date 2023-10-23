package icbm.classic.lib.tracker;

import icbm.classic.content.missile.entity.EntityMissile;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

class EventTrackerDataTest {

    static Stream<Arguments> testData_isImmutable() {
        return Stream.of(
            Arguments.of(World.class, false),
            Arguments.of(EntityPlayer.class, false),
            Arguments.of(EntityZombie.class, false),
            Arguments.of(EntityMissile.class, false),
            Arguments.of(NBTTagCompound.class, false),
            Arguments.of(BlockPos.MutableBlockPos.class, false),

            Arguments.of(Integer.class, true),
            Arguments.of(RuntimeException.class, true),
            Arguments.of(NullPointerException.class, true)
        );
    }

    @ParameterizedTest
    @MethodSource("testData_isImmutable")
    void isImmutable(Class clzz, boolean expect) {
        Assertions.assertEquals(expect, EventTrackerData.isValidType(clzz), "Failed on " + clzz);
    }
}
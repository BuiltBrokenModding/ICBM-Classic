package icbm.classic.lib.tracker;

import icbm.classic.world.missile.entity.EntityMissile;
import net.minecraft.world.entity.monster.EntityZombie;
import net.minecraft.world.entity.player.Player;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

class EventTrackerDataTest {

    static Stream<Arguments> testData_isImmutable() {
        return Stream.of(
            Arguments.of(World.class, false),
            Arguments.of(Player.class, false),
            Arguments.of(EntityZombie.class, false),
            Arguments.of(EntityMissile.class, false),
            Arguments.of(CompoundTag.class, false),
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
package icbm.classic;

import icbm.classic.world.missile.entity.explosive.ExplosiveMissileEntity;
import net.minecraft.world.entity.passive.EntitySheep;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import org.junit.jupiter.api.Assertions;

/**
 * Created by Dark(DarkGuardsman, Robert) on 1/20/2020.
 */
public class TestUtils
{
    public static EntitySheep sheep(Level level, int x, int y, int z)
    {
        final EntitySheep sheep = new EntitySheep(world);
        sheep.forceSpawn = true;
        sheep.setPosition(x, y, z);
        world.spawnEntity(sheep);
        return sheep;
    }

    public static ExplosiveMissileEntity missile(Level level, int x, int y, int z)
    {
        final ExplosiveMissileEntity missile = new ExplosiveMissileEntity(world);
        missile.forceSpawn = true;
        missile.setPosition(x, y, z);
        world.spawnEntity(missile);
        return missile;
    }

    public static void assertContains(CompoundTag expected, CompoundTag actual) { //TODO move to testing library
        expected.getKeySet().forEach(key -> {
            Assertions.assertTrue(actual.contains(key), String.format("Missing key '%s'", key));
            Assertions.assertEquals(expected.getTagId(key), actual.getTagId(key), String.format("Tag type for key '%s' didn't match", key));
            Assertions.assertEquals(expected.getTag(key), actual.getTag(key), String.format("Tag didn't match for key '%s'", key));
        });
    }
}

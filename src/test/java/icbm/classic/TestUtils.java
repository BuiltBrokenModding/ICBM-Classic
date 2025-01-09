package icbm.classic;

/**
 * Created by Dark(DarkGuardsman, Robert) on 1/20/2020.
 */
public class TestUtils
{
    /*public static SheepEntity sheep(World world, int x, int y, int z)
    {
        final SheepEntity sheep = new SheepEntity(world);
        sheep.forceSpawn = true;
        sheep.setPosition(x, y, z);
        world.addEntity(sheep);
        return sheep;
    }

    public static EntityExplosiveMissile missile(World world, int x, int y, int z)
    {
        final EntityExplosiveMissile missile = new EntityExplosiveMissile(world);
        missile.forceSpawn = true;
        missile.setPosition(x, y, z);
        world.addEntity(missile);
        return missile;
    }*/

    /*public static void assertContains(CompoundNBT expected, CompoundNBT actual) { //TODO move to testing library
        expected.getKeySet().forEach(key -> {
            Assertions.assertTrue(actual.hasKey(key), String.format("Missing key '%s'", key));
            Assertions.assertEquals(expected.getTagId(key), actual.getTagId(key), String.format("Tag type for key '%s' didn't match", key));
            Assertions.assertEquals(expected.getTag(key), actual.getTag(key), String.format("Tag didn't match for key '%s'", key));
        });
    }*/
}

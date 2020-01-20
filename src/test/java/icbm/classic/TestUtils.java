package icbm.classic;

import icbm.classic.content.entity.missile.EntityMissile;
import net.minecraft.entity.passive.EntitySheep;
import net.minecraft.world.World;

/**
 * Created by Dark(DarkGuardsman, Robert) on 1/20/2020.
 */
public class TestUtils
{
    public static EntitySheep sheep(World world, int x, int y, int z)
    {
        final EntitySheep sheep = new EntitySheep(world);
        sheep.forceSpawn = true;
        sheep.setPosition(x, y, z);
        world.spawnEntity(sheep);
        return sheep;
    }

    public static EntityMissile missile(World world, int x, int y, int z)
    {
        final EntityMissile missile = new EntityMissile(world);
        missile.forceSpawn = true;
        missile.setPosition(x, y, z);
        world.spawnEntity(missile);
        return missile;
    }
}

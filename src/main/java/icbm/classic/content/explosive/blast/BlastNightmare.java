package icbm.classic.content.explosive.blast;

import com.builtbroken.mc.imp.transform.vector.Pos;
import net.minecraft.entity.Entity;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.entity.passive.EntityBat;
import net.minecraft.world.World;

/**
 * @see <a href="https://github.com/BuiltBrokenModding/VoltzEngine/blob/development/license.md">License</a> for what you can and can't do with the code.
 * Created by Dark(DarkGuardsman, Robert) on 10/2/2017.
 */
public class BlastNightmare extends Blast
{
    public BlastNightmare(World world, Entity entity, double x, double y, double z, float size)
    {
        super(world, entity, x, y, z, size);
    }

    @Override
    protected void doExplode()
    {
        if(!oldWorld().isRemote)
        {
            final Pos center = new Pos(this);
            final int size = (int) this.size;

            //TODO cache delays created by this blast to allow for /lag command to clear

            //Spawn bats
            final int batCount = (size / 10) + oldWorld().rand.nextInt(size / 10);
            for (int i = 0; i < batCount; i++)
            {
                EntityBat bat = new EntityBat(oldWorld());
                //DelayedActionHandler.add(new DelayedSpawn(oldWorld(), center, bat, 10, (i + oldWorld().rand.nextInt(size)) * 20));
            }

            //Spawn monsters
            final int monsterCount = (size / 10) + oldWorld().rand.nextInt(size / 10);
            for (int i = 0; i < monsterCount; i++)
            {
                //TODO distribute using missile miss spread code
                //TODO materialize zombies as ghosts so they can walk through walls in order to find an air pocket to spawn
                EntityZombie zombie = new EntityZombie(oldWorld());
                //DelayedActionHandler.add(new DelayedSpawn(oldWorld(), center, zombie, 10, (i + oldWorld().rand.nextInt(size * 2)) * 20));
            }

            //TODO play deathly scream
            //TODO replace torches with bone torch set for random halloween colors and low light levels
            //TODO have monsters move towards players
        }
    }
}

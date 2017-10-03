package icbm.classic.content.explosive.blast;

import com.builtbroken.mc.framework.thread.delay.DelayedAction;
import com.builtbroken.mc.framework.thread.delay.DelayedActionHandler;
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
        final Pos center = new Pos(this);
        final int size = (int) this.explosionSize;

        //Spawn bats
        final int batCount = (size / 2) + oldWorld().rand.nextInt(size);
        for (int i = 0; i < batCount; i++)
        {
            //TODO make new bat mob that can attack players and has a pumpkin for a head, have it shoot flames out behind it as well
            //TODO distribute using missile miss spread code
            EntityBat bat = new EntityBat(oldWorld());
            DelayedSpawn delay = new DelayedSpawn(oldWorld(), center, bat, 10, (i + oldWorld().rand.nextInt(size)) * 20); //reduce bats spawning at same time
            DelayedActionHandler.add(delay);
        }

        //Spawn monsters
        final int monsterCount = (size / 2) + oldWorld().rand.nextInt(size);
        for (int i = 0; i < monsterCount; i++)
        {
            //TODO distribute using missile miss spread code
            //TODO materialize zombies as ghosts so they can walk through walls in order to find an air pocket to spawn
            EntityZombie zombie = new EntityZombie(oldWorld());
            DelayedSpawn delay = new DelayedSpawn(oldWorld(), center, zombie, 10, (i + oldWorld().rand.nextInt(size * 2)) * 20); //reduce bats spawning at same time
            DelayedActionHandler.add(delay);
        }

        //TODO play deathly scream
        //TODO replace torches with bone torch set for random halloween colors and low light levels
        //TODO have monsters move towards players
    }

    public static class DelayedSpawn extends DelayedAction
    {
        Pos center;
        Entity entity;
        int range;

        int spawnAttemptLimit = 10;
        private int spawnAttempts;

        public DelayedSpawn(World world, Pos center, Entity entity, int range, int ticks)
        {
            super(world, ticks);
            this.center = center;
            this.entity = entity;
            this.range = range;
        }

        @Override
        public boolean trigger()
        {
            if (entity != null)
            {
                final float width = entity.width;
                final float height = entity.height;

                //Code can only handle entities roughly 1 block wide
                if (width <= 1.1)
                {
                    spawnAttempts++;

                    //Get random spawn point
                    Pos spawnPoint = center.addRandom(world.rand, range);
                    if (!spawnPoint.isAirBlock(world))
                    {
                        spawnPoint = spawnPoint.add(0, 1, 0);
                    }

                    //Ensure point is in air and is on ground
                    if (spawnPoint.isAirBlock(world) && !spawnPoint.sub(0, 1, 0).isAirBlock(world))
                    {
                        //Do height check
                        if (height > 1)
                        {
                            int heightChecks = (int) Math.ceil(height - 1);
                            for (int i = 0; i < heightChecks; i++)
                            {
                                if (!spawnPoint.add(0, i, 0).isAirBlock(world))
                                {
                                    return spawnAttempts >= spawnAttemptLimit;
                                }
                            }
                        }
                        //Set data
                        entity.setPosition(spawnPoint.xi() + 0.5, spawnPoint.yi() + 0.5, spawnPoint.zi() + 0.5);
                        //TODO rotate to face closest player

                        //Spawn
                        world.spawnEntityInWorld(entity);
                        return true; //done
                    }

                    return spawnAttempts >= spawnAttemptLimit;
                }
            }

            return true; //done due to bad data
        }
    }
}

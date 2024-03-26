package icbm.classic.world.blast;

/**
 * Created by Dark(DarkGuardsman, Robert) on 10/2/2017.
 */
//unused at the moment
public class BlastNightmare extends Blast {
    @Override
    protected boolean doExplode(int callCount) {
        if (!level().isClientSide()) {
            //final Pos center = new Pos(this);
            final int size = (int) this.size;

            //TODO cache delays created by this blast to allow for /lag command to clear

            //Spawn bats
            final int batCount = (size / 10) + level().rand.nextInt(size / 10);
            for (int i = 0; i < batCount; i++) {
                //EntityBat bat = new EntityBat(world());
                //DelayedActionHandler.add(new DelayedSpawn(oldLevel(), center, bat, 10, (i + oldLevel().rand.nextInt(size)) * 20));
            }

            //Spawn monsters
            final int monsterCount = (size / 10) + level().rand.nextInt(size / 10);
            for (int i = 0; i < monsterCount; i++) {
                //TODO distribute using missile miss spread code
                //TODO materialize zombies as ghosts so they can walk through walls in order to find an air pocket to spawn
                //EntityZombie zombie = new EntityZombie(world());
                //DelayedActionHandler.add(new DelayedSpawn(oldLevel(), center, zombie, 10, (i + oldLevel().rand.nextInt(size * 2)) * 20));
            }

            //TODO play deathly scream
            //TODO replace torches with bone torch set for random halloween colors and low light levels
            //TODO have monsters move towards players
        }
        return true;
    }
}

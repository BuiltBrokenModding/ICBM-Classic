package icbm.classic.content.missile.tracker;

import icbm.classic.ICBMClassic;
import icbm.classic.ICBMConstants;
import icbm.classic.config.ConfigDebug;
import icbm.classic.config.missile.ConfigMissile;
import icbm.classic.content.missile.entity.EntityMissile;
import icbm.classic.content.missile.entity.explosive.EntityExplosiveMissile;
import icbm.classic.content.missile.logic.flight.DeadFlightLogic;
import icbm.classic.lib.NBTConstants;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.server.TicketType;
import net.minecraft.world.storage.WorldSavedData;
import org.apache.logging.log4j.Level;

import java.util.Iterator;
import java.util.LinkedList;

/**
 * Per world handler for tracking and simulating missiles
 *
 * Created by GHXX on 7/31/2018.
 */
public class MissileTrackerWorld extends WorldSavedData
{
    public static final TicketType<Integer> MISSILE_ENTER_WORLD = TicketType.create(ICBMConstants.DOMAIN + ":post_simulate", Integer::compareTo, 60);

    //Constants
    private final int speedPerSecond = 10; //10 blocks per second TODO config

    //Missile lists
    private final LinkedList<MissileTrackerData> missileList;         //Stores missiles which are being simulated right now
    private final LinkedList<MissileTrackerData> missileSpawnList;    //Stores missiles which are awaiting to be spawned in world


    //Tick counter for reducing the simulation speed
    private int ticks = 0;

    //Constructor has to be (String) or it will break
    public MissileTrackerWorld(String identifier)
    {
        super(identifier);
        missileList = new LinkedList<>();
        missileSpawnList = new LinkedList<>();
    }

    /**
     * Called to simulate the missile
     * @param missile
     */
    void simulateMissile(EntityExplosiveMissile missile)
    {
        if(ConfigDebug.DEBUG_MISSILE_TRACKER) {
            final String formatted = String.format("MissileTracker[%s]: Simulating missile: %s",
                missile.world.getDimension().getType().getId(),
                missile
            );
            ICBMClassic.logger().info(formatted);
        }

        //Only run on server
        if (!missile.world.isRemote && missile.getMissileCapability().getTargetData()  != null)
        {
            //Clear flight logic, once we are out of simulation the computer is dead
            missile.getMissileCapability().setFlightLogic(null); //TODO create custom sim-logic flight logic to better handle re-entry

            final MissileTrackerData mtd = new MissileTrackerData(missile);

            //Calculate distance
            double dx = missile.getMissileCapability().getTargetData().getX() - missile.posX;
            double dz = missile.getMissileCapability().getTargetData().getZ() - missile.posZ;
            double dst = Math.sqrt(dx * dx + dz * dz);

            //Calculate duration and queue up
            mtd.ticksLeftToTarget = (int) Math.round(dst / ConfigMissile.SIMULATION_FLIGHT_SPEED);
            missileList.add(mtd);

            //Destroys the entity and marks it for removal from world
            missile.remove();

            //Mark that we need to save
            this.markDirty();
        }
    }

    /**
     * Called each time the world ticks
     */
    public void onWorldTick(final ServerWorld world)
    {
        if (ticks++ >= 20)  //Run every 20 ticks = 1 second
        {
            ticks = 0;
            int mIndex = 0;
            //Loop through all simulated missiles
            final Iterator<MissileTrackerData> missileIterator = missileList.iterator();
            while(missileIterator.hasNext())
            {
                //Get current missile
                MissileTrackerData missile = missileIterator.next();
                if (missile.ticksLeftToTarget <= 0) //If missile is at the target location
                {

                    ChunkPos currentLoadedChunk = new ChunkPos((int) Math.floor(missile.targetPos.x) >> 4, (int) Math.floor(missile.targetPos.z) >> 4);
                    world.getChunkProvider().func_217228_a(MISSILE_ENTER_WORLD, currentLoadedChunk, world.getDimension().getType().getId(), 0);

                    currentLoadedChunk = new ChunkPos(1 + ((int) Math.floor(missile.targetPos.x) >> 4), (int) Math.floor(missile.targetPos.z) >> 4);
                    world.getChunkProvider().func_217228_a(MISSILE_ENTER_WORLD, currentLoadedChunk, world.getDimension().getType().getId(), 0);

                    currentLoadedChunk = new ChunkPos(-1 + ((int) Math.floor(missile.targetPos.x) >> 4), (int) Math.floor(missile.targetPos.z) >> 4);
                    world.getChunkProvider().func_217228_a(MISSILE_ENTER_WORLD, currentLoadedChunk, world.getDimension().getType().getId(), 0);

                    currentLoadedChunk = new ChunkPos((int) Math.floor(missile.targetPos.x) >> 4, 1 + ((int) Math.floor(missile.targetPos.z) >> 4));
                    world.getChunkProvider().func_217228_a(MISSILE_ENTER_WORLD, currentLoadedChunk, world.getDimension().getType().getId(), 0);

                    currentLoadedChunk = new ChunkPos((int) Math.floor(missile.targetPos.x) >> 4, -1 + ((int) Math.floor(missile.targetPos.z) >> 4));
                    world.getChunkProvider().func_217228_a(MISSILE_ENTER_WORLD, currentLoadedChunk, world.getDimension().getType().getId(), 0);

                    missile.preLoadChunkTimer = 0;
                    missileSpawnList.add(missile);
                    missileIterator.remove();
                }
                else //If we aren't at the target location then simulate it for another tick
                {
                    missile.ticksLeftToTarget--;
                    if(ConfigDebug.DEBUG_MISSILE_TRACKER)
                        ICBMClassic.logger().log(Level.INFO,"MissileTracker Missile ["+mIndex+"]: Simulation ticks left: "+missile.ticksLeftToTarget);
                }
                mIndex++;
            }

            //Check missile spawn-queue
            final Iterator<MissileTrackerData> spawnIterator = missileSpawnList.iterator();
            while(spawnIterator.hasNext()) // TODO wait for callback maybe instead of waiting a set amount of time
            {
                final MissileTrackerData mtd = spawnIterator.next();

                //Decrease timer
                mtd.preLoadChunkTimer--;

                //Check if ready to launch
                if (mtd.preLoadChunkTimer <= 0)
                {
                    //Load missile into world
                    spawnMissileOnDestination(world, mtd);

                    //Remove
                    spawnIterator.remove();
                }
            }

            //Mark so we save
            this.markDirty();
        }
    }

    private void spawnMissileOnDestination(final ServerWorld world, MissileTrackerData mtd)
    {
        if(mtd.entityType == null) {
            return;
        }
        //Create entity
        Entity missile = mtd.entityType.create(world);

        //Set data
        missile.read(mtd.missileData);
        missile.setPosition(mtd.targetPos.x, ConfigMissile.SIMULATION_ENTER_HEIGHT, mtd.targetPos.z); //TODO calculate arc position so we don't come in on top of the target
        missile.setMotion(0, -ConfigMissile.SIMULATION_ENTER_SPEED, 0); //TODO get speed it would have been at the given time

        if(missile instanceof EntityMissile) {
            ((EntityMissile)missile).rotateTowardsMotion(1);

            // Change over to dead aim if we have no custom flight system
            if ( ((EntityMissile)missile).getMissileCapability().getFlightLogic() == null) {
                ((EntityMissile)missile).getMissileCapability().setFlightLogic(new DeadFlightLogic(100));
            }

            //Trigger launch event
            ((EntityMissile)missile).getMissileCapability().launch();
        }

        //Spawn entity
        missile.world.addEntity(missile);

        if(ConfigDebug.DEBUG_MISSILE_TRACKER)
            ICBMClassic.logger().info("MissileTracker[{}]: Missile spawned by missile tracker: {}", missile.world.getDimension().toString(), missile);
    }

    @Override
    public void read(CompoundNBT nbt)
    {
        //Load missiles
        ListNBT list = nbt.getList(NBTConstants.MISSILES, 10);
        for (int i = 0; i < list.size(); i++)
        {
            CompoundNBT missileSave = list.getCompound(i);
            MissileTrackerData mtd = new MissileTrackerData(missileSave);
            missileList.add(mtd);
        }

        //Load missiles that will spawn
        list = nbt.getList(NBTConstants.SPAWNS, 10);
        for (int i = 0; i < list.size(); i++)
        {
            CompoundNBT missileSave = list.getCompound(i);
            MissileTrackerData mtd = new MissileTrackerData(missileSave);
            missileSpawnList.add(mtd);
        }
    }

    @Override
    public CompoundNBT write(CompoundNBT nbt)
    {
        //Save missiles
        ListNBT list = new ListNBT();
        for (MissileTrackerData mtd : missileList)
        {
            CompoundNBT compound = new CompoundNBT();
            mtd.writeToNBT(compound);
            list.add(compound);
        }
        nbt.put(NBTConstants.MISSILES, list);

        //Save missiles that will spawn
        list = new ListNBT();
        for (MissileTrackerData mtd : missileSpawnList)
        {
            CompoundNBT compound = new CompoundNBT();
            mtd.writeToNBT(compound);
            list.add(compound);
        }
        nbt.put(NBTConstants.SPAWNS, list);

        return nbt;
    }

    // clear buffers
    public void destroy()
    {
        this.missileList.clear();
        this.missileSpawnList.clear();
    }
}

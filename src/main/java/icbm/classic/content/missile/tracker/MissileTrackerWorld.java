package icbm.classic.content.missile.tracker;

import icbm.classic.ICBMClassic;
import icbm.classic.api.events.MissileChunkEvent;
import icbm.classic.config.ConfigDebug;
import icbm.classic.config.missile.ConfigMissile;
import icbm.classic.content.missile.entity.explosive.EntityExplosiveMissile;
import icbm.classic.content.missile.logic.flight.DeadFlightLogic;
import icbm.classic.lib.NBTConstants;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.storage.WorldSavedData;
import net.minecraftforge.common.ForgeChunkManager;
import net.minecraftforge.common.MinecraftForge;
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
    //Constants
    private static final int unloadChunkCooldown = 60; //1 minute
    private static final int preLoadChunkTimer = 5; //5 update ticks (5 seconds) / the time that we wait before spawning the missile in a force-loaded chunk

    //Missile lists
    private final LinkedList<MissileTrackerData> missileList;         //Stores missiles which are being simulated right now
    private final LinkedList<MissileTrackerData> missileSpawnList;    //Stores missiles which are awaiting to be spawned in world

    //Chunk stuff
    private ForgeChunkManager.Ticket chunkLoadTicket;   //The chunkloading ticket used for loading chunks
    private final LinkedList<LoadedChunkPair> currentLoadedChunks;    //Stores the currently loaded chunks along with a timer how long they will be loaded for

    //Tick counter for reducing the simulation speed
    private int ticks = 0;

    //Constructor has to be (String) or it will break
    public MissileTrackerWorld(String identifier)
    {
        super(identifier);
        missileList = new LinkedList<>();
        currentLoadedChunks = new LinkedList<>();
        missileSpawnList = new LinkedList<>();
        ForgeChunkManager.setForcedChunkLoadingCallback(ICBMClassic.INSTANCE, (tickets, world) -> {});
    }

    /**
     * Called to simulate the missile
     * @param missile
     */
    void simulateMissile(EntityExplosiveMissile missile)
    {
        if(ConfigDebug.DEBUG_MISSILE_TRACKER) {
            final String formatted = String.format("MissileTracker[%s]: Simulating missile: %s",
                missile.world.provider.getDimension(),
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
            missile.setDead();

            //Mark that we need to save
            this.markDirty();
        }
    }

    /**
     * Called each time the world ticks
     */
    public void onWorldTick(final World world)
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
                    //If we haven't got a chunkload ticket then create one
                    if (chunkLoadTicket == null)
                    {
                        chunkLoadTicket = ForgeChunkManager.requestTicket(ICBMClassic.INSTANCE, world, ForgeChunkManager.Type.NORMAL);

                        if(chunkLoadTicket != null) //If we just created a ticket then we queue up all chunks which may have been kept forced to unforce them later
                        {
                            for(ChunkPos cp : chunkLoadTicket.getChunkList())
                            {
                                this.currentLoadedChunks.add(new LoadedChunkPair(cp, unloadChunkCooldown));
                            }
                        }
                    }

                    if (chunkLoadTicket != null) //If we are allowed to load chunks, lets load the chunk the target location is in and the adjacent ones
                    {
                        ChunkPos currentLoadedChunk = new ChunkPos((int) missile.targetPos.x() >> 4, (int) missile.targetPos.z() >> 4);
                        forceChunk(currentLoadedChunk, chunkLoadTicket);

                        currentLoadedChunk = new ChunkPos(1 + ((int) missile.targetPos.x() >> 4), (int) missile.targetPos.z() >> 4);
                        forceChunk(currentLoadedChunk, chunkLoadTicket);

                        currentLoadedChunk = new ChunkPos(-1 + ((int) missile.targetPos.x() >> 4), (int) missile.targetPos.z() >> 4);
                        forceChunk(currentLoadedChunk, chunkLoadTicket);

                        currentLoadedChunk = new ChunkPos((int) missile.targetPos.x() >> 4, 1 + ((int) missile.targetPos.z() >> 4));
                        forceChunk(currentLoadedChunk, chunkLoadTicket);

                        currentLoadedChunk = new ChunkPos((int) missile.targetPos.x() >> 4, -1 + ((int) missile.targetPos.z() >> 4));
                        forceChunk(currentLoadedChunk, chunkLoadTicket);

                    }
                    else
                    {
                        ICBMClassic.logger().warn("Unable to receive chunkloading ticket. You could try to increase the maximum loaded chunks for ICBM.");
                    }

                    missile.preLoadChunkTimer = preLoadChunkTimer;
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

            //Check forced chunks, decrease lifetime and maybe unforce
            for (int i = 0; i < currentLoadedChunks.size(); i++) //TODO replace with while loop
            {
                ChunkPos chunkPos = currentLoadedChunks.get(i).chunkPos;
                int waitTime = currentLoadedChunks.get(i).timeLeft - 1;
                if (waitTime <= 0)
                {
                    if(!MinecraftForge.EVENT_BUS.post(new MissileChunkEvent.Unload(new LoadedChunkPair(chunkPos, waitTime), chunkLoadTicket)))
                    {
                        ForgeChunkManager.unforceChunk(chunkLoadTicket, chunkPos);
                        currentLoadedChunks.remove(i);
                    }
                }
                else
                {
                    currentLoadedChunks.set(i, new LoadedChunkPair(chunkPos, waitTime));
                }
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

    private void spawnMissileOnDestination(final World world, MissileTrackerData mtd)
    {
        //Create entity
        EntityExplosiveMissile missile = new EntityExplosiveMissile(world);

        //Set data
        missile.readEntityFromNBT(mtd.missileData);
        missile.posY = ConfigMissile.SIMULATION_ENTER_HEIGHT;
        missile.posX = mtd.targetPos.x(); //TODO calculate arc position so we don't come in on top of the target
        missile.posZ = mtd.targetPos.z();
        missile.motionY = -ConfigMissile.SIMULATION_ENTER_SPEED; //TODO get speed it would have been at the given time
        missile.motionZ = 0;
        missile.motionX = 0;
        missile.rotateTowardsMotion(1);

        // Change over to dead aim if we have no custom flight system
        if(missile.getMissileCapability().getFlightLogic() == null) {
            missile.getMissileCapability().setFlightLogic(new DeadFlightLogic(100));
        }

        //Trigger launch event
        missile.getMissileCapability().launch();

        //Spawn entity
        missile.world().spawnEntity(missile);

        if(ConfigDebug.DEBUG_MISSILE_TRACKER)
            ICBMClassic.logger().info("MissileTracker[" + missile.world.provider.getDimension() + "]: Missile spawned by missile tracker: " + missile);
    }

    //Helper method for forcing a chunk (chunkloading)
    private void forceChunk(ChunkPos chunkPos, ForgeChunkManager.Ticket ticket)
    {
        for (int i = 0; i < currentLoadedChunks.size(); i++) // check if the chunk that should be loaded is loaded already. If so then just reset the remaining time.
        {
            if (currentLoadedChunks.get(i).chunkPos == chunkPos)
            {
                currentLoadedChunks.set(i, new LoadedChunkPair(chunkPos, unloadChunkCooldown));
                return;
            }
        }

        LoadedChunkPair pair = new LoadedChunkPair(chunkPos, unloadChunkCooldown);

        if(!MinecraftForge.EVENT_BUS.post(new MissileChunkEvent.Load(pair, ticket)))
        {
            currentLoadedChunks.add(pair);
            ForgeChunkManager.forceChunk(ticket, chunkPos);
        }
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt)
    {
        //Load missiles
        NBTTagList list = nbt.getTagList(NBTConstants.MISSILES, 10);
        for (int i = 0; i < list.tagCount(); i++)
        {
            NBTTagCompound missileSave = list.getCompoundTagAt(i);
            MissileTrackerData mtd = new MissileTrackerData(missileSave);
            missileList.add(mtd);
        }

        //Load missiles that will spawn
        list = nbt.getTagList(NBTConstants.SPAWNS, 10);
        for (int i = 0; i < list.tagCount(); i++)
        {
            NBTTagCompound missileSave = list.getCompoundTagAt(i);
            MissileTrackerData mtd = new MissileTrackerData(missileSave);
            missileSpawnList.add(mtd);
        }
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbt)
    {
        //Save missiles
        NBTTagList list = new NBTTagList();
        for (MissileTrackerData mtd : missileList)
        {
            NBTTagCompound compound = new NBTTagCompound();
            mtd.writeToNBT(compound);
            list.appendTag(compound);
        }
        nbt.setTag(NBTConstants.MISSILES, list);

        //Save missiles that will spawn
        list = new NBTTagList();
        for (MissileTrackerData mtd : missileSpawnList)
        {
            NBTTagCompound compound = new NBTTagCompound();
            mtd.writeToNBT(compound);
            list.appendTag(compound);
        }
        nbt.setTag(NBTConstants.SPAWNS, list);

        return nbt;
    }

    // clear buffers
    public void destroy()
    {
        this.missileList.clear();
        this.missileSpawnList.clear();

        chunkLoadTicket = null;
        currentLoadedChunks.clear();
    }
}

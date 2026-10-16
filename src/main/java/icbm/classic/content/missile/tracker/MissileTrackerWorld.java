package icbm.classic.content.missile.tracker;

import icbm.classic.ICBMClassic;
import icbm.classic.api.events.MissileChunkEvent;
import icbm.classic.config.ConfigDebug;
import icbm.classic.config.missile.ConfigMissile;
import icbm.classic.content.missile.entity.explosive.EntityExplosiveMissile;
import icbm.classic.content.missile.logic.flight.DeadFlightLogic;
import icbm.classic.lib.NBTConstants;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.world.chunk.TicketController;

import java.util.UUID;

import java.util.Iterator;
import java.util.LinkedList;

/**
 * Per world handler for tracking and simulating missiles
 *
 * Created by GHXX on 7/31/2018.
 */
public class MissileTrackerWorld extends SavedData
{
    //Constants
    private static final int unloadChunkCooldown = 60; //1 minute
    private static final int preLoadChunkTimer = 5; //5 update ticks (5 seconds) / the time that we wait before spawning the missile in a force-loaded chunk
    private static final int MAX_SPAWN_RETRIES = 6; //Extra preload waits if the target chunk is still not ticking entities

    //Missile lists
    private final LinkedList<MissileTrackerData> missileList;         //Stores missiles which are being simulated right now
    private final LinkedList<MissileTrackerData> missileSpawnList;    //Stores missiles which are awaiting to be spawned in world

    /** PORT: owner id used with {@link MissileTrackerHandler#CHUNK_TICKETS}, replaces the 1.12 ForgeChunkManager ticket */
    private static final UUID TICKET_OWNER = UUID.nameUUIDFromBytes("icbmclassic:missile_tracker".getBytes(java.nio.charset.StandardCharsets.UTF_8));

    //Chunk stuff
    private TicketController chunkLoadTicket;   //The chunkloading ticket used for loading chunks
    private final LinkedList<LoadedChunkPair> currentLoadedChunks;    //Stores the currently loaded chunks along with a timer how long they will be loaded for

    //Tick counter for reducing the simulation speed
    private int ticks = 0;

    public MissileTrackerWorld()
    {
        missileList = new LinkedList<>();
        currentLoadedChunks = new LinkedList<>();
        missileSpawnList = new LinkedList<>();
    }

    /** PORT: SavedData factory loader, 1.12 readFromNBT */
    public static MissileTrackerWorld load(CompoundTag nbt, HolderLookup.Provider provider)
    {
        final MissileTrackerWorld tracker = new MissileTrackerWorld();
        tracker.readFromNBT(nbt);
        return tracker;
    }

    /**
     * Called to simulate the missile
     * @param missile
     */
    void simulateMissile(EntityExplosiveMissile missile)
    {
        if(ConfigDebug.DEBUG_MISSILE_TRACKER) {
            final String formatted = String.format("MissileTracker[%s]: Simulating missile: %s",
                missile.level().dimension().location(),
                missile
            );
            ICBMClassic.logger().info(formatted);
        }

        //Only run on server
        if (!missile.level().isClientSide() && missile.getMissileCapability().getTargetData()  != null)
        {
            //Clear flight logic, once we are out of simulation the computer is dead
            missile.getMissileCapability().setFlightLogic(null); //TODO create custom sim-logic flight logic to better handle re-entry

            final MissileTrackerData mtd = new MissileTrackerData(missile);

            //Calculate distance
            double dx = missile.getMissileCapability().getTargetData().getX() - missile.getX();
            double dz = missile.getMissileCapability().getTargetData().getZ() - missile.getZ();
            double dst = Math.sqrt(dx * dx + dz * dz);

            //Calculate duration and queue up
            mtd.ticksLeftToTarget = (int) Math.round(dst / ConfigMissile.SIMULATION_FLIGHT_SPEED);
            missileList.add(mtd);

            //Destroys the entity and marks it for removal from world
            missile.discard();

            //Mark that we need to save
            this.setDirty();
        }
    }

    /**
     * Called each time the world ticks
     */
    public void onWorldTick(final Level world)
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
                    forceTargetChunks(world, missile);

                    missile.preLoadChunkTimer = preLoadChunkTimer;
                    missileSpawnList.add(missile);
                    missileIterator.remove();
                }
                else //If we aren't at the target location then simulate it for another tick
                {
                    missile.ticksLeftToTarget--;
                    if(ConfigDebug.DEBUG_MISSILE_TRACKER)
                        ICBMClassic.logger().info("MissileTracker Missile ["+mIndex+"]: Simulation ticks left: "+missile.ticksLeftToTarget);
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
                    if(!NeoForge.EVENT_BUS.post(new MissileChunkEvent.Unload(new LoadedChunkPair(chunkPos, waitTime), chunkLoadTicket)).isCanceled())
                    {
                        unforceChunk(world, chunkPos, chunkLoadTicket);
                        currentLoadedChunks.remove(i);
                        i--; // PORT: 1.12 skipped the entry after a removed one for this update
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
                    // PORT: 1.12 spawnEntity refused unloaded chunks, 1.21 addFreshEntity puts the missile into a frozen
                    // hidden section instead. Wait for the forced target chunk to tick entities, a few retries at most
                    // so a cancelled MissileChunkEvent.Load can not hold the missile forever
                    if (mtd.spawnRetries < MAX_SPAWN_RETRIES && world instanceof ServerLevel serverLevel
                        && !MissileTrackerHandler.canEntitiesTick(serverLevel, targetChunkX(mtd), targetChunkZ(mtd)))
                    {
                        mtd.spawnRetries++;
                        forceTargetChunks(world, mtd);
                        mtd.preLoadChunkTimer = preLoadChunkTimer;
                        continue;
                    }

                    //Load missile into world
                    spawnMissileOnDestination(world, mtd);

                    //Remove
                    spawnIterator.remove();
                }
            }

            //Mark so we save
            this.setDirty();
        }
    }

    private void spawnMissileOnDestination(final Level world, MissileTrackerData mtd)
    {
        //Create entity
        EntityExplosiveMissile missile = new EntityExplosiveMissile(world);

        //Set data
        missile.load(mtd.missileData); // PORT-CHECK: 1.12 readEntityFromNBT is protected in 1.21, full load used instead (Pos/UUID stripped on save)
        //TODO calculate arc position so we don't come in on top of the target
        missile.setPos(mtd.targetPos.x(), ConfigMissile.SIMULATION_ENTER_HEIGHT, mtd.targetPos.z());
        missile.setMotionVector(0, -ConfigMissile.SIMULATION_ENTER_SPEED, 0); //TODO get speed it would have been at the given time
        missile.rotateTowardsMotion(1);

        // Change over to dead aim if we have no custom flight system
        if(missile.getMissileCapability().getFlightLogic() == null) {
            missile.getMissileCapability().setFlightLogic(new DeadFlightLogic(100));
        }

        //Trigger launch event
        missile.getMissileCapability().launch();

        //Spawn entity
        missile.world().addFreshEntity(missile);

        if(ConfigDebug.DEBUG_MISSILE_TRACKER)
            ICBMClassic.logger().info("MissileTracker[" + missile.level().dimension().location() + "]: Missile spawned by missile tracker: " + missile);
    }

    private static int targetChunkX(MissileTrackerData mtd)
    {
        return Mth.floor(mtd.targetPos.x()) >> 4; // PORT: 1.12 cast to int, which rounds negative coordinates the wrong way
    }

    private static int targetChunkZ(MissileTrackerData mtd)
    {
        return Mth.floor(mtd.targetPos.z()) >> 4;
    }

    /** Forces the chunk the target location is in and the adjacent ones */
    private void forceTargetChunks(Level world, MissileTrackerData mtd)
    {
        //If we haven't got a chunkload ticket then create one
        // PORT: NeoForge ticket controllers have no per-mod limit, chunks left forced by a previous run are released by the controller's load callback
        if (chunkLoadTicket == null)
        {
            chunkLoadTicket = MissileTrackerHandler.CHUNK_TICKETS;
        }

        if (chunkLoadTicket != null) //If we are allowed to load chunks, lets load the chunk the target location is in and the adjacent ones
        {
            final int chunkX = targetChunkX(mtd);
            final int chunkZ = targetChunkZ(mtd);
            forceChunk(world, new ChunkPos(chunkX, chunkZ), chunkLoadTicket);
            forceChunk(world, new ChunkPos(chunkX + 1, chunkZ), chunkLoadTicket);
            forceChunk(world, new ChunkPos(chunkX - 1, chunkZ), chunkLoadTicket);
            forceChunk(world, new ChunkPos(chunkX, chunkZ + 1), chunkLoadTicket);
            forceChunk(world, new ChunkPos(chunkX, chunkZ - 1), chunkLoadTicket);
        }
        else
        {
            ICBMClassic.logger().warn("Unable to receive chunkloading ticket. You could try to increase the maximum loaded chunks for ICBM.");
        }
    }

    //Helper method for forcing a chunk (chunkloading)
    private void forceChunk(Level world, ChunkPos chunkPos, TicketController ticket)
    {
        for (int i = 0; i < currentLoadedChunks.size(); i++) // check if the chunk that should be loaded is loaded already. If so then just reset the remaining time.
        {
            // PORT: 1.12 compared with ==, which never matched a new ChunkPos and stacked duplicate entries
            if (currentLoadedChunks.get(i).chunkPos.equals(chunkPos))
            {
                currentLoadedChunks.set(i, new LoadedChunkPair(chunkPos, unloadChunkCooldown));
                return;
            }
        }

        LoadedChunkPair pair = new LoadedChunkPair(chunkPos, unloadChunkCooldown);

        if(!NeoForge.EVENT_BUS.post(new MissileChunkEvent.Load(pair, ticket)).isCanceled())
        {
            currentLoadedChunks.add(pair);
            if (world instanceof ServerLevel serverLevel) {
                ticket.forceChunk(serverLevel, TICKET_OWNER, chunkPos.x, chunkPos.z, true, true);
            }
        }
    }

    private void unforceChunk(Level world, ChunkPos chunkPos, TicketController ticket)
    {
        if (ticket != null && world instanceof ServerLevel serverLevel) {
            ticket.forceChunk(serverLevel, TICKET_OWNER, chunkPos.x, chunkPos.z, false, true);
        }
    }

    public void readFromNBT(CompoundTag nbt)
    {
        //Load missiles
        ListTag list = nbt.getList(NBTConstants.MISSILES, 10);
        for (int i = 0; i < list.size(); i++)
        {
            CompoundTag missileSave = list.getCompound(i);
            MissileTrackerData mtd = new MissileTrackerData(missileSave);
            missileList.add(mtd);
        }

        //Load missiles that will spawn
        // PORT: forced chunks are released on world load, so missiles that were waiting to spawn go back through
        // the arrival step which forces the target chunks and waits the preload time again (1.12 spawned them right away)
        list = nbt.getList(NBTConstants.SPAWNS, 10);
        for (int i = 0; i < list.size(); i++)
        {
            CompoundTag missileSave = list.getCompound(i);
            MissileTrackerData mtd = new MissileTrackerData(missileSave);
            mtd.ticksLeftToTarget = 0;
            missileList.add(mtd);
        }
    }

    @Override
    public CompoundTag save(CompoundTag nbt, HolderLookup.Provider provider)
    {
        //Save missiles
        ListTag list = new ListTag();
        for (MissileTrackerData mtd : missileList)
        {
            CompoundTag compound = new CompoundTag();
            mtd.writeToNBT(compound);
            list.add(compound);
        }
        nbt.put(NBTConstants.MISSILES, list);

        //Save missiles that will spawn
        list = new ListTag();
        for (MissileTrackerData mtd : missileSpawnList)
        {
            CompoundTag compound = new CompoundTag();
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

        chunkLoadTicket = null;
        currentLoadedChunks.clear();
    }
}
// PORTED

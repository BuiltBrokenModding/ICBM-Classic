package icbm.classic.content.missile;

import icbm.classic.ICBMClassic;
import javafx.util.Pair;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.storage.WorldSavedData;
import net.minecraftforge.common.ForgeChunkManager;

import java.util.Iterator;
import java.util.LinkedList;

/**
 * Per world handler for tracking and simulating missiles
 */
public class MissileTrackerWorld extends WorldSavedData
{
    //NBT keys
    private static final String NBT_MISSILE = "missiles";
    private static final String NBT_MISSILE_SPAWN = "spawns";

    //Constants
    private final int speedPerSecond = 20; // 20 blocks per second // TODO set to 10
    private final int unloadChunkCooldown = 60 * 20; // 1 minute
    private final int preLoadChunkTimer = 5; // 5 update ticks (5 seconds) / the time that we wait before spawning the missile in a force-loaded chunk

    //Missile lists
    private LinkedList<MissileTrackerData> missileList;
    private LinkedList<MissileTrackerData> missileSpawnList;

    //Chunk stuff
    private ForgeChunkManager.Ticket chunkLoadTicket;
    private LinkedList<Pair<ChunkPos, Integer>> currentLoadedChunks;

    //Ticks
    private int ticks = 0;

    //Constructor has to be (String) or it will break
    public MissileTrackerWorld(String identifier)
    {
        super(identifier);
        missileList = new LinkedList<>();
        currentLoadedChunks = new LinkedList<>();
        missileSpawnList = new LinkedList<>();
        ForgeChunkManager.setForcedChunkLoadingCallback(ICBMClassic.INSTANCE, null);
    }

    /**
     * Called to simulate the missile
     * @param missile
     */
    protected void simulateMissile(EntityMissile missile)
    {
        //Only run on server
        if (!missile.world.isRemote)
        {
            final MissileTrackerData mtd = new MissileTrackerData(missile);

            double dx = speedPerSecond * Math.signum(missile.targetPos.x() - missile.posX);
            double dz = speedPerSecond * Math.signum(missile.targetPos.z() - missile.posZ);
            double dst = Math.sqrt(dx * dx + dz * dz);

            mtd.ticksLeftToTarget = (int) Math.round(dst / speedPerSecond);

            /*
            // create copy
            EntityMissile newMissile = new EntityMissile(missile.world());

            // copy data

            newMissile.explosiveID =
            newMissile.launcherPos = missile.launcherPos;
            newMissile.sourceOfProjectile = missile.sourceOfProjectile;
            newMissile.setPosition(missile.posX, missile.posY, missile.posZ);
            newMissile.targetPos = missile.targetPos;
            newMissile.world = missile.world;
            newMissile.ticksInAir = (int) missile.missileFlightTime - 60;
            newMissile.wasSimulated = true;
            newMissile.motionX =
            newMissile.motionZ = ;*/

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
        if (ticks++ >= 20)
        {
            ticks = 0;

            final Iterator<MissileTrackerData> missileIterator = missileList.iterator();
            while(missileIterator.hasNext())
            {
                MissileTrackerData missile = missileIterator.next();
                if (missile.ticksLeftToTarget <= 0) // if missile is at the target location
                {
                    if (chunkLoadTicket == null)
                    {
                        chunkLoadTicket = ForgeChunkManager.requestTicket(ICBMClassic.INSTANCE, world, ForgeChunkManager.Type.NORMAL);
                    }

                    if (chunkLoadTicket != null) // if we are allowed to load chunks
                    {
                        ChunkPos currentLoadedChunk = new ChunkPos((int) missile.targetPos.x() >> 4, (int) missile.targetPos.z() >> 4);
                        forceChunk(currentLoadedChunk, unloadChunkCooldown, chunkLoadTicket);

                        currentLoadedChunk = new ChunkPos(1 + ((int) missile.targetPos.x() >> 4), (int) missile.targetPos.z() >> 4);
                        forceChunk(currentLoadedChunk, unloadChunkCooldown, chunkLoadTicket);

                        currentLoadedChunk = new ChunkPos(-1 + ((int) missile.targetPos.x() >> 4), (int) missile.targetPos.z() >> 4);
                        forceChunk(currentLoadedChunk, unloadChunkCooldown, chunkLoadTicket);

                        currentLoadedChunk = new ChunkPos((int) missile.targetPos.x() >> 4, 1 + ((int) missile.targetPos.z() >> 4));
                        forceChunk(currentLoadedChunk, unloadChunkCooldown, chunkLoadTicket);

                        currentLoadedChunk = new ChunkPos((int) missile.targetPos.x() >> 4, -1 + ((int) missile.targetPos.z() >> 4));
                        forceChunk(currentLoadedChunk, unloadChunkCooldown, chunkLoadTicket);

                    }
                    else
                    {
                        ICBMClassic.logger().warn("Unable to receive chunkloading ticket. You could try to increase the maximum loaded chunks for ICBM.");
                    }

                    missile.preLoadChunkTimer = preLoadChunkTimer;
                    missileSpawnList.add(missile);
                    missileIterator.remove();
                }
                else
                {
                    ICBMClassic.logger().warn("Seconds left to target: "+ missile.ticksLeftToTarget);
                    missile.ticksLeftToTarget--;
                }
            }

            for (int i = 0; i < currentLoadedChunks.size(); i++) //TODO replace with while loop
            {
                ChunkPos chunkPos = currentLoadedChunks.get(i).getKey();
                int waitTime = currentLoadedChunks.get(i).getValue() - 1;
                if (waitTime <= 0)
                {
                    ForgeChunkManager.unforceChunk(chunkLoadTicket, chunkPos);
                    currentLoadedChunks.remove(i);
                    ICBMClassic.logger().info("Unforced chunk");
                }
                else
                {
                    currentLoadedChunks.set(i, new Pair(chunkPos, waitTime));
                }
            }

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
                    Launch(world, mtd);

                    //Remove
                    spawnIterator.remove();
                }
            }

            //Mark so we save
            this.markDirty();
        }
    }

    private void Launch(final World world, MissileTrackerData mtd)
    {
        EntityMissile missile = new EntityMissile(world);
        missile.readEntityFromNBT(mtd.missileData);

        // create entity
        missile.missileType = MissileFlightType.PAD_LAUNCHER;
        missile.posY = 250;
        missile.posX = mtd.targetPos.x();
        missile.posZ = mtd.targetPos.z();
        missile.motionY = -1;
        missile.motionZ = 0;
        missile.motionX = 0;

        missile.lockHeight = 0;
        missile.acceleration = 5;
        missile.preLaunchSmokeTimer = 0;
        missile.targetPos = mtd.targetPos;
        missile.wasSimulated = true;

        //Trigger launch event
        missile.launch(missile.targetPos, (int) missile.lockHeight);

        //Spawn entity
        missile.world().spawnEntity(missile);

        /*
        newMissile.explosiveID = missile.explosiveID;
        newMissile.launcherPos = missile.launcherPos;
        newMissile.sourceOfProjectile = missile.sourceOfProjectile;
        newMissile.setPosition(missile.posX, missile.posY, missile.posZ);
        newMissile.lockHeight = missile.lockHeight;
        newMissile.targetPos = missile.targetPos;
        newMissile.world = missile.world;
        newMissile.ticksInAir = (int) missile.missileFlightTime - 60;
        newMissile
        newMissile.motionX = speedPerSecond * Math.signum(missile.targetPos.x() - missile.posX);
        newMissile.motionZ = speedPerSecond * Math.signum(missile.targetPos.z() - missile.posZ);
        */

    }

    private void forceChunk(ChunkPos chunkPos, Integer forceTime, ForgeChunkManager.Ticket ticket)
    {
        for (int i = 0; i < currentLoadedChunks.size(); i++)
        {
            if (currentLoadedChunks.get(i).getKey() == chunkPos)
            {
                currentLoadedChunks.set(i, new Pair(chunkPos, forceTime));
                ICBMClassic.logger().warn("(Init) Re-Forced chunk at: " + chunkPos.toString());
                return;
            }
        }
        currentLoadedChunks.add(new Pair(chunkPos, forceTime));
        ForgeChunkManager.forceChunk(ticket, chunkPos);
        ICBMClassic.logger().warn("(Init) Forced chunk at: " + chunkPos.toString());
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt)
    {
        //Load missiles
        NBTTagList list = nbt.getTagList(NBT_MISSILE, 10);
        for (int i = 0; i < list.tagCount(); i++)
        {
            NBTTagCompound missileSave = list.getCompoundTagAt(i);
            MissileTrackerData mtd = new MissileTrackerData(missileSave);
            missileList.add(mtd);
        }

        //Load missiles that will spawn
        list = nbt.getTagList(NBT_MISSILE_SPAWN, 10);
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
        nbt.setTag(NBT_MISSILE, list);

        //Save missiles that will spawn
        list = new NBTTagList();
        for (MissileTrackerData mtd : missileSpawnList)
        {
            NBTTagCompound compound = new NBTTagCompound();
            mtd.writeToNBT(compound);
            list.appendTag(compound);
        }
        nbt.setTag(NBT_MISSILE_SPAWN, list);

        return nbt;
    }

    // save data and then clear buffers
    public void destroy()
    {
        this.missileList.clear();
        this.missileSpawnList.clear();

        if(chunkLoadTicket != null)
        {
            ForgeChunkManager.releaseTicket(chunkLoadTicket);
            chunkLoadTicket = null;
        }
        currentLoadedChunks.clear();
    }
}

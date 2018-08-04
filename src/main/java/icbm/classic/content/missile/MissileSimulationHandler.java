package icbm.classic.content.missile;

import com.builtbroken.jlib.data.vector.IPos3D;
import com.builtbroken.jlib.data.vector.Pos3D;
import com.google.common.util.concurrent.UncheckedExecutionException;
import icbm.classic.ICBMClassic;
import icbm.classic.lib.transform.vector.Pos;
import javafx.util.Pair;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.EntityTrackerEntry;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.storage.MapStorage;
import net.minecraft.world.storage.WorldSavedData;
import net.minecraftforge.common.ForgeChunkManager;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.io.InvalidObjectException;
import java.net.Inet4Address;
import java.util.*;


public class MissileSimulationHandler extends WorldSavedData {

    private LinkedList<MissileTrackerData> missileBuffer;
    private ForgeChunkManager.Ticket chunkLoadTicket;
    private LinkedList<Pair<ChunkPos, Integer>> currentLoadedChunks;
    private LinkedList<Pair<MissileTrackerData, Integer>> queuedMissileSpawns;
    private Integer simTick = 0;
    private String identifier;
    private World world;
    private MapStorage mStorage;

    public MissileSimulationHandler(String identifier, World world)
    {
        super(identifier);
        this.identifier = identifier;
        this.world = world;
        missileBuffer = new LinkedList<>();
        currentLoadedChunks = new LinkedList<>();
        queuedMissileSpawns = new LinkedList<>();
        this.mStorage = world.getPerWorldStorage();
        ForgeChunkManager.setForcedChunkLoadingCallback(ICBMClassic.INSTANCE, null);
    }

    protected void AddMissile(EntityMissile missile) {
        if (missile.world.isRemote) // TODO add turn into log with stacktrace
        {
            throw new UncheckedExecutionException(new InvalidObjectException("Missile handler cannot be constructed Clientside!"));
        }

        MissileTrackerData mtd = new MissileTrackerData();
        mtd.explosiveID = missile.explosiveID;
        mtd.targetPos = missile.targetPos;

        double dx = speedPerSecond * Math.signum(missile.targetPos.x() - missile.posX);
        double dz = speedPerSecond * Math.signum(missile.targetPos.z() - missile.posZ);
        double dst = Math.sqrt(dx * dx + dz * dz);

        mtd.ticksLeftToTarget =  (int)Math.round(dst / (speedPerSecond / 20d));

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

        missileBuffer.add(mtd);
        this.markDirty();
    }

    private final int speedPerSecond = 20; // 20 blocks per second // TODO set to 10
    private final int unloadChunkCooldown = 60 * 20; // 1 minute
    private final int preLoadChunkTimer = 5; // 5 update ticks (5 seconds) / the time that we wait before spawning the missile in a force-loaded chunk

    public void Simulate() {
        if(simTick >= 20)
        {
            simTick = 0;

            for (int i = 0; i < missileBuffer.size(); i++)
            {

                MissileTrackerData missile = missileBuffer.get(i);
                if (missile.ticksLeftToTarget <= 0) // if missile is at the target location
                {
                    ICBMClassic.logger().info("[" + i + "] Reached target location");

                    if (chunkLoadTicket == null)
                    {
                        chunkLoadTicket = ForgeChunkManager.requestTicket(ICBMClassic.INSTANCE, this.world, ForgeChunkManager.Type.NORMAL);
                    }

                    if (chunkLoadTicket != null) // if we are allowed to load chunks
                    {
                        ChunkPos currentLoadedChunk = new ChunkPos((int) missile.targetPos.x()>> 4, (int) missile.targetPos.z() >> 4);
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

                    queuedMissileSpawns.add(new Pair(missile,preLoadChunkTimer));
                    missileBuffer.remove(i);
                }
                else
                {
                    missile.ticksLeftToTarget--;
                }
            }

            for (int i = 0; i < currentLoadedChunks.size(); i++)
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

            for (int i = 0; i < queuedMissileSpawns.size(); i++) // TODO wait for callback maybe instead of waiting a set amount of time
            {
                MissileTrackerData mtd = queuedMissileSpawns.get(i).getKey();
                int waitTime = queuedMissileSpawns.get(i).getValue() - 1;
                if (waitTime <= 0)
                {
                    Launch(mtd);
                    queuedMissileSpawns.remove(i); // TODO maybe do i--?
                }
                else
                {
                    queuedMissileSpawns.set(i, new Pair(mtd, waitTime));
                }
            }
            this.markDirty();
        }
        simTick++;
    }

    private void Launch(MissileTrackerData mtd)
    {
        EntityMissile missile = new EntityMissile(this.world);

        // create entity
        missile.missileType = MissileFlightType.PAD_LAUNCHER;
        missile.posY = 256;
        missile.motionY = -1;
        missile.motionZ = 0;
        missile.motionX = 0;

        missile.lockHeight = 0;
        missile.acceleration = 5;
        missile.preLaunchSmokeTimer = 0;
        missile.targetPos = (Pos)mtd.targetPos;
        missile.explosiveID = mtd.explosiveID;
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
            if(currentLoadedChunks.get(i).getKey() == chunkPos)
            {
                currentLoadedChunks.set(i, new Pair(chunkPos, forceTime));
                ICBMClassic.logger().warn("(Init) Forced chunk at: " + chunkPos.toString());
                return;
            }
        }
        currentLoadedChunks.add(new Pair(chunkPos, forceTime));
        ForgeChunkManager.forceChunk(ticket, chunkPos);
        ICBMClassic.logger().warn("(Init) Re-forced chunk at: " + chunkPos.toString());
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt)
    {
        NBTTagList list = nbt.getTagList("missiles", 10);
        for ( int i = 0; i<list.tagCount(); i++)
        {
            MissileTrackerData mtd = new MissileTrackerData();
            mtd.readFromNBT(list.getCompoundTagAt(i));
        }
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbt)
    {
        NBTTagList list = new NBTTagList();
        for (MissileTrackerData mtd : missileBuffer)
        {
            NBTTagCompound compound = new NBTTagCompound();
            mtd.writeToNBT(compound);
            list.appendTag(compound);
        }
        nbt.setTag("missiles",list);
        return nbt;
    }

    // save data and then clear buffers
    public void destroy() {
        save();
        this.missileBuffer.clear();

        for (Pair<ChunkPos,Integer> chunkPair : currentLoadedChunks) {  // unforca all chunks
            ForgeChunkManager.unforceChunk(chunkLoadTicket,chunkPair.getKey());
        }
        currentLoadedChunks.clear();
        chunkLoadTicket = null;
    }


    public void save() {
        // TODO add saving
    }
}

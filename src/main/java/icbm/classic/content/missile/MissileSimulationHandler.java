package icbm.classic.content.missile;

import com.builtbroken.jlib.data.vector.IPos3D;
import com.builtbroken.jlib.data.vector.Pos3D;
import com.google.common.util.concurrent.UncheckedExecutionException;
import icbm.classic.ICBMClassic;
import icbm.classic.lib.transform.vector.Pos;
import javafx.util.Pair;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.storage.WorldSavedData;
import net.minecraftforge.common.ForgeChunkManager;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.io.InvalidObjectException;
import java.util.*;


public class MissileSimulationHandler extends WorldSavedData {

    private LinkedList<EntityMissile> missileBuffer;
    private ForgeChunkManager.Ticket chunkLoadTicket;
    private LinkedList<Pair<ChunkPos, Integer>> currentLoadedChunks;
    private LinkedList<Pair<EntityMissile, Integer>> queuedMissileSpawns;
    private Integer simTick = 0;

    public MissileSimulationHandler(String mapName) {
        super(mapName);
        missileBuffer = new LinkedList<>();
        currentLoadedChunks = new LinkedList<>();
        queuedMissileSpawns = new LinkedList<>();
        ForgeChunkManager.setForcedChunkLoadingCallback(ICBMClassic.INSTANCE, null);
    }

    protected void AddMissile(EntityMissile missile) {
        if (missile.world.isRemote) // TODO add turn into log with stacktrace
        {
            throw new UncheckedExecutionException(new InvalidObjectException("Missile handler cannot be constructed Clientside!"));
        }

        // create copy
        EntityMissile newMissile = new EntityMissile(missile.world());

        // copy data
        newMissile.explosiveID = missile.explosiveID;
        newMissile.launcherPos = missile.launcherPos;
        newMissile.sourceOfProjectile = missile.sourceOfProjectile;
        newMissile.setPosition(missile.posX, missile.posY, missile.posZ);
        newMissile.lockHeight = missile.lockHeight;
        newMissile.targetPos = missile.targetPos;
        newMissile.world = missile.world;
        newMissile.ticksInAir = (int) missile.missileFlightTime - 60;
        newMissile.wasSimulated = true;
        newMissile.motionX = speedPerSecond * Math.signum(missile.targetPos.x() - missile.posX);
        newMissile.motionZ = speedPerSecond * Math.signum(missile.targetPos.z() - missile.posZ);

        missileBuffer.add(newMissile);
    }

    private final int speedPerSecond = 20; // 20 blocks per second // TODO set to 10
    private final int unloadChunkCooldown = 60 * 20; // 1 minute
    private final int preLoadChunkTimer = 5; // 5 update ticks (5 seconds) / the time that we wait before spawning the missile in a force-loaded chunk

    public void Simulate() {
        if(simTick >= 20)
        {
            simTick = 0;

            for (int i = 0; i < missileBuffer.size(); i++) {
                EntityMissile missile = missileBuffer.get(i);
                if (missile.posX == missile.targetPos.x() && missile.posZ == missile.targetPos.z()) // if missile is at the target location
                {
                    //missile.missileType = MissileFlightType.DEAD_AIM;
                    missile.missileType = MissileFlightType.PAD_LAUNCHER;
                    ICBMClassic.logger().info("[" + i + "] Reached target location");

                    if (chunkLoadTicket == null) {
                        chunkLoadTicket = ForgeChunkManager.requestTicket(ICBMClassic.INSTANCE, missile.world, ForgeChunkManager.Type.NORMAL);
                    }
                    if (chunkLoadTicket != null) // if we are allowed to load chunks
                    {
                        ChunkPos currentLoadedChunk = new ChunkPos((int) missile.posX >> 4, (int) missile.posZ >> 4);
                        forceChunk(currentLoadedChunk, unloadChunkCooldown, chunkLoadTicket);
                        ICBMClassic.logger().warn("(Init) Forced chunk at: " + currentLoadedChunk.toString());

                        currentLoadedChunk = new ChunkPos(1 + ((int) missile.posX >> 4), (int) missile.posZ >> 4);
                        forceChunk(currentLoadedChunk, unloadChunkCooldown, chunkLoadTicket);
                        ICBMClassic.logger().warn("(Init) Forced chunk at: " + currentLoadedChunk.toString());

                        currentLoadedChunk = new ChunkPos(-1 + ((int) missile.posX >> 4), (int) missile.posZ >> 4);
                        forceChunk(currentLoadedChunk, unloadChunkCooldown, chunkLoadTicket);
                        ICBMClassic.logger().warn("(Init) Forced chunk at: " + currentLoadedChunk.toString());

                        currentLoadedChunk = new ChunkPos((int) missile.posX >> 4, 1 + ((int) missile.posZ >> 4));
                        forceChunk(currentLoadedChunk, unloadChunkCooldown, chunkLoadTicket);
                        ICBMClassic.logger().warn("(Init) Forced chunk at: " + currentLoadedChunk.toString());

                        currentLoadedChunk = new ChunkPos((int) missile.posX >> 4, -1 + ((int) missile.posZ >> 4));
                        forceChunk(currentLoadedChunk, unloadChunkCooldown, chunkLoadTicket);
                        ICBMClassic.logger().warn("(Init) Forced chunk at: " + currentLoadedChunk.toString());

                    } else {
                        ICBMClassic.logger().warn("Unable to receive chunkloading ticket. You could try to increase the maximum loaded chunks for ICBM.");
                    }

                    missile.posY = 256;
                    missile.motionY = -1;
                    missile.motionZ = 0;
                    missile.motionX = 0;

                    missile.lockHeight = 0;
                    missile.acceleration = 5;
                    missile.preLaunchSmokeTimer = 0;
                    //missile.targetPos = null;
                    queuedMissileSpawns.add(new Pair(missile,preLoadChunkTimer));
                    missileBuffer.remove(i);
                } else {
                    ICBMClassic.logger().info("[" + i + "] Adjusting target x, z. Current Delta: " + (missile.targetPos.x() - missile.posX) + ", " + (missile.targetPos.z() - missile.posZ));
                    double currDeltaX = Math.abs(missile.targetPos.x() - missile.posX);
                    double nextDeltaX = Math.abs(currDeltaX - missile.motionX);
                    double currDeltaZ = Math.abs(missile.targetPos.z() - missile.posZ);
                    double nextDeltaZ = Math.abs(currDeltaZ - missile.motionZ);

                    if (nextDeltaX < currDeltaX) // lets tro to move the missile closer. if we cant then we are at the target pos.
                    {
                        missile.posX += missile.motionX;
                    } else {
                        missile.posX = missile.targetPos.x();
                        ICBMClassic.logger().info("[" + i + "] Reached target x");
                    }
                    if (nextDeltaZ < currDeltaZ) {
                        missile.posZ += missile.motionZ;
                    } else {
                        missile.posZ = missile.targetPos.z();
                        ICBMClassic.logger().info("[" + i + "] Reached target z");
                    }
                }
            }

            for (int i = 0; i < currentLoadedChunks.size(); i++) {
                ChunkPos chunkPos = currentLoadedChunks.get(i).getKey();
                int waitTime = currentLoadedChunks.get(i).getValue() - 1;
                if (waitTime <= 0) {
                    ForgeChunkManager.unforceChunk(chunkLoadTicket, chunkPos);
                    currentLoadedChunks.remove(i);
                    ICBMClassic.logger().info("Unforced chunk");
                } else {
                    currentLoadedChunks.set(i, new Pair(chunkPos, waitTime));
                }
            }

            for (int i = 0; i < queuedMissileSpawns.size(); i++) { // TODO wait for callback maybe instead of waiting a set amount of time
                EntityMissile missile = queuedMissileSpawns.get(i).getKey();
                int waitTime = queuedMissileSpawns.get(i).getValue() - 1;
                if (waitTime <= 0) {
                    Launch(missile);
                    queuedMissileSpawns.remove(i); // TODO maybe do i--?
                } else {
                    queuedMissileSpawns.set(i, new Pair(missile, waitTime));
                }
            }
        }
        simTick++;
    }

    private void Launch(EntityMissile missile)
    {
        //Trigger launch event
        missile.launch(missile.targetPos, (int) missile.lockHeight);

        //Spawn entity
        missile.world().spawnEntity(missile);
    }

    private void forceChunk(ChunkPos chunkPos, Integer forceTime, ForgeChunkManager.Ticket ticket)
    {
        for (int i = 0; i < currentLoadedChunks.size(); i++) {
            if(currentLoadedChunks.get(i).getKey() == chunkPos)
            {
                currentLoadedChunks.set(i, new Pair(chunkPos, forceTime));
                return;
            }
        }
        currentLoadedChunks.add(new Pair(chunkPos, forceTime));
        ForgeChunkManager.forceChunk(ticket, chunkPos);
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt)
    {
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbt)
    {
        return null;
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

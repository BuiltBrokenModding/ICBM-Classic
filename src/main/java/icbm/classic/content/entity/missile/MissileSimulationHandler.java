package icbm.classic.content.entity.missile;

import com.builtbroken.jlib.data.vector.IPos3D;
import com.builtbroken.jlib.data.vector.Pos3D;
import com.google.common.util.concurrent.UncheckedExecutionException;
import icbm.classic.ICBMClassic;
import icbm.classic.lib.transform.vector.Pos;
import javafx.util.Pair;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.storage.WorldSavedData;
import net.minecraftforge.common.ForgeChunkManager;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.io.InvalidObjectException;
import java.util.*;


public class MissileSimulationHandler extends WorldSavedData{

    private LinkedList<EntityMissile> missileBuffer;
    private ForgeChunkManager.Ticket chunkLoadTicket;
    private LinkedList<Pair<ChunkPos, Integer>> currentLoadedChunks;
    private Integer simTick = 0;

    public MissileSimulationHandler(String mapName)
    {
        super(mapName);
        missileBuffer = new LinkedList<>();
        currentLoadedChunks = new LinkedList<>();
        ForgeChunkManager.setForcedChunkLoadingCallback(ICBMClassic.INSTANCE,null);
    }

    protected void AddMissile(EntityMissile missile)
    {
        if(missile.world.isRemote) // TODO add turn into log with stacktrace
        {
            throw new UncheckedExecutionException(new InvalidObjectException("Missile handler cannot be constructed Clientside!"));
        }

        // create copy
        EntityMissile newMissile = new EntityMissile(missile.world());

        // copy data
        newMissile.explosiveID = missile.explosiveID;
        newMissile.launcherPos = missile.launcherPos;
        newMissile.sourceOfProjectile = missile.sourceOfProjectile;
        newMissile.setPosition(missile.posX,missile.posY,missile.posZ);
        newMissile.lockHeight = missile.lockHeight;
        newMissile.targetPos = missile.targetPos;
        newMissile.world = missile.world;
        newMissile.ticksInAir = (int)missile.missileFlightTime - 20;
        newMissile.wasSimulated = true;
        newMissile.motionX = speedPerSecond *Math.signum(missile.targetPos.x() - missile.posX);
        newMissile.motionZ = speedPerSecond *Math.signum(missile.targetPos.z() - missile.posZ);

        missileBuffer.add(newMissile);
    }

    private final int speedPerSecond = 20; // 20 blocks per second // TODO set to 10
    private final int unloadChunkCooldown = 60*20; // 1 minute

    public void Simulate()
    {
        if(simTick >= 20)
        {
            simTick = 0;

            for (int i = 0; i<missileBuffer.size();i++)
            {
                EntityMissile missile = missileBuffer.get(i);
                if (missile.posX == missile.targetPos.x() && missile.posZ == missile.targetPos.z()) // if missile is at the target location
                {
                    //missile.missileType = MissileFlightType.DEAD_AIM;
                    missile.missileType = MissileFlightType.PAD_LAUNCHER;
                    ICBMClassic.logger().info("["+i+"] Reached target location");

                    if (chunkLoadTicket == null) {
                        chunkLoadTicket = ForgeChunkManager.requestTicket(ICBMClassic.INSTANCE, missile.world, ForgeChunkManager.Type.NORMAL);
                    }
                    if (chunkLoadTicket != null) // if we are allowed to load chunks
                    {
                        ChunkPos currentLoadedChunk = new ChunkPos((int) missile.posX >> 4, (int) missile.posZ >> 4);
                        currentLoadedChunks.add(new Pair(currentLoadedChunk, unloadChunkCooldown));
                        ForgeChunkManager.forceChunk(chunkLoadTicket, currentLoadedChunk);
                        ICBMClassic.logger().warn("(Init) Forced chunk at: " + currentLoadedChunk.toString());
                        currentLoadedChunk = new ChunkPos(1+((int) missile.posX >> 4), (int) missile.posZ >> 4);
                        currentLoadedChunks.add(new Pair(currentLoadedChunk, unloadChunkCooldown));
                        ForgeChunkManager.forceChunk(chunkLoadTicket, currentLoadedChunk);
                        ICBMClassic.logger().warn("(Init) Forced chunk at: " + currentLoadedChunk.toString());
                        currentLoadedChunk = new ChunkPos(-1+((int) missile.posX >> 4), (int) missile.posZ >> 4);
                        currentLoadedChunks.add(new Pair(currentLoadedChunk, unloadChunkCooldown));
                        ForgeChunkManager.forceChunk(chunkLoadTicket, currentLoadedChunk);
                        ICBMClassic.logger().warn("(Init) Forced chunk at: " + currentLoadedChunk.toString());
                        currentLoadedChunk = new ChunkPos((int) missile.posX >> 4, 1+((int) missile.posZ >> 4));
                        currentLoadedChunks.add(new Pair(currentLoadedChunk, unloadChunkCooldown));
                        ForgeChunkManager.forceChunk(chunkLoadTicket, currentLoadedChunk);
                        ICBMClassic.logger().warn("(Init) Forced chunk at: " + currentLoadedChunk.toString());
                        currentLoadedChunk = new ChunkPos((int) missile.posX >> 4 , -1+((int) missile.posZ >> 4));
                        currentLoadedChunks.add(new Pair(currentLoadedChunk, unloadChunkCooldown));
                        ForgeChunkManager.forceChunk(chunkLoadTicket, currentLoadedChunk);

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
                    //missile.targetPos = null;
                    Launch(missile);
                    missileBuffer.remove(i);
                }
                else
                {
                    ICBMClassic.logger().info("["+i+"] Adjusting target x, z. Current Delta: "+(missile.targetPos.x() - missile.posX)+", "+(missile.targetPos.z() - missile.posZ));
                    double currDeltaX = Math.abs(missile.targetPos.x() - missile.posX);
                    double nextDeltaX = Math.abs(currDeltaX - missile.motionX);
                    double currDeltaZ = Math.abs(missile.targetPos.z() - missile.posZ);
                    double nextDeltaZ = Math.abs(currDeltaZ - missile.motionZ);

                    if (nextDeltaX < currDeltaX) // lets tro to move the missile closer. if we cant then we are at the target pos.
                    {
                        missile.posX += missile.motionX;
                    }
                    else
                    {
                        missile.posX = missile.targetPos.x();
                        ICBMClassic.logger().info("["+i+"] Reached target x");
                    }
                    if (nextDeltaZ < currDeltaZ)
                    {
                        missile.posZ += missile.motionZ;
                    }
                    else
                    {
                        missile.posZ = missile.targetPos.z();
                        ICBMClassic.logger().info("["+i+"] Reached target z");
                    }
                }
            }

            for (int i = 0; i<currentLoadedChunks.size();i++) {
                ChunkPos chunkPos = currentLoadedChunks.get(i).getKey();
                int waitTime = currentLoadedChunks.get(i).getValue() - 1;
                if (waitTime <= 0)
                {
                    ForgeChunkManager.unforceChunk(chunkLoadTicket,chunkPos);
                    currentLoadedChunks.remove(i);
                    ICBMClassic.logger().info("Unforced chunk");
                }
                else
                {
                    currentLoadedChunks.set(i, new Pair(chunkPos,waitTime));
                }
            }
        }
        simTick++;
    }

    private void Launch(EntityMissile missile)
    {
        //Spawn entity
        missile.world().spawnEntity(missile);

        //Trigger launch event
        missile.launch(missile.targetPos, (int)missile.lockHeight);
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
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

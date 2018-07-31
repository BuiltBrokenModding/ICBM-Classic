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

import java.io.InvalidObjectException;
import java.util.*;


public class MissileSimulationHandler extends WorldSavedData{

    public Thread handlerThread = null;
    private Stack<EntityMissile> missileBuffer;
    private ForgeChunkManager.Ticket chunkLoadTicket;
    private Stack<Pair<ChunkPos, Integer>> currentLoadedChunks;

    public MissileSimulationHandler(String mapName)
    {
        super(mapName);
        missileBuffer = new Stack<EntityMissile>();
        handlerThread = new Thread(new Runnable() {
            @Override
            public void run() {
                SimulationLoop();
            }
        }, "ICBM-MissileSimThread");
        currentLoadedChunks = new Stack<>();
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
        newMissile.motionX = speedPerTick *Math.signum(missile.targetPos.x() - missile.posX);
        newMissile.motionZ = speedPerTick *Math.signum(missile.targetPos.z() - missile.posZ);

        missileBuffer.add(newMissile);
    }

    private final int speedPerTick = 20;
    private final int unloadChunkCooldown = 60;

    private void SimulationLoop()
    {
        boolean doRun = true;
        while (doRun)
        {
            for (int i = 0; i<missileBuffer.size();i++)
            {
                EntityMissile missile = missileBuffer.get(i);
                if (missile.posX == missile.targetPos.x() && missile.posZ == missile.targetPos.z()) // if missile is at the target location
                {
                    //missile.missileType = MissileFlightType.DEAD_AIM;
                    missile.missileType = MissileFlightType.PAD_LAUNCHER;
                    ICBMClassic.logger().info("["+i+"] Reached target location");
                    try {
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
                    }
                    catch (Exception e)
                    {
                        ICBMClassic.logger().warn("Exception!");
                    }

                    missile.posY = 256;
                    missile.motionY = -1;
                    missile.motionZ = 0;
                    missile.motionX = 0;

                    missile.lockHeight = 0;
                    missile.acceleration = 1;
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
                ChunkPos chunkPos = currentLoadedChunks.elementAt(i).getKey();
                int waitTime = currentLoadedChunks.elementAt(i).getValue() - 1;
                if (waitTime <= 0)
                {
                    ForgeChunkManager.unforceChunk(chunkLoadTicket,chunkPos);
                    currentLoadedChunks.removeElementAt(i);
                    ICBMClassic.logger().info("Unforced chunk");
                }
                else
                {
                    currentLoadedChunks.setElementAt(new Pair(chunkPos,waitTime),i);
                }

            }

            try {
                Thread.sleep(200);
            }
            catch (InterruptedException e)
            {
                doRun = false;
            }
        }
    }

    private void Launch(EntityMissile missile)
    {
        //Trigger launch event
        missile.launch(missile.targetPos, (int)missile.lockHeight);

        //Spawn entity
        missile.world().spawnEntity(missile);
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        handlerThread.interrupt();
        missileBuffer.clear();
        return null;
    }

}

package icbm.classic.content.explosive.ex.missiles;

import com.builtbroken.mc.api.edit.IWorldChangeAction;
import com.builtbroken.mc.api.event.TriggerCause;
import com.builtbroken.mc.lib.transform.vector.Pos;
import icbm.classic.content.entity.EntityMissile;
import icbm.classic.content.entity.EntityMissile.MissileType;
import icbm.classic.content.explosive.blast.BlastRepulsive;
import icbm.classic.content.items.ItemTracker;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ChatComponentText;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;

public class MissileHoming extends Missile
{
    public MissileHoming()
    {
        super("homing", 1);
        this.hasBlock = false;
        this.modelName = "missile_homing.tcn";
    }

    @Override
    public void launch(EntityMissile missileObj)
    {
        if (!missileObj.worldObj.isRemote)
        {
            WorldServer worldServer = (WorldServer) missileObj.worldObj;
            Entity trackingEntity = worldServer.getEntityByID(missileObj.trackingVar);

            if (trackingEntity != null)
            {
                if (trackingEntity == missileObj)
                {
                    missileObj.setExplode();
                }

                missileObj.targetVector = new Pos(trackingEntity);
            }
        }
    }

    @Override
    public void update(EntityMissile missileObj)
    {
        if (missileObj.getTicksInAir() > missileObj.missileFlightTime / 2 && missileObj.missileType == MissileType.MISSILE)
        {
            WorldServer worldServer = (WorldServer) missileObj.worldObj;
            Entity trackingEntity = worldServer.getEntityByID(missileObj.trackingVar);

            if (trackingEntity != null)
            {
                if (trackingEntity.equals(missileObj))
                {
                    missileObj.setExplode();
                }

                missileObj.targetVector = new Pos(trackingEntity);

                missileObj.missileType = MissileType.CruiseMissile;

                missileObj.deltaPathX = missileObj.targetVector.x() - missileObj.posX;
                missileObj.deltaPathY = missileObj.targetVector.y() - missileObj.posY;
                missileObj.deltaPathZ = missileObj.targetVector.z() - missileObj.posZ;

                missileObj.flatDistance = missileObj.sourceOfProjectile.toVector2().distance(missileObj.targetVector.toVector2());
                missileObj.maxHeight = 150 + (int) (missileObj.flatDistance * 1.8);
                missileObj.missileFlightTime = (float) Math.max(100, 2.4 * missileObj.flatDistance);
                missileObj.acceleration = (float) missileObj.maxHeight * 2 / (missileObj.missileFlightTime * missileObj.missileFlightTime);

                if (missileObj.xiaoDanMotion.equals(new Pos()) || missileObj.xiaoDanMotion == null)
                {
                    float suDu = 0.3f;
                    missileObj.xiaoDanMotion = new Pos(missileObj.deltaPathX / (missileObj.missileFlightTime * suDu)
                            ,missileObj.deltaPathY / (missileObj.missileFlightTime * suDu),
                            missileObj.deltaPathZ / (missileObj.missileFlightTime * suDu));
                }
            }
        }
    }

    @Override
    public boolean onInteract(EntityMissile missileObj, EntityPlayer entityPlayer)
    {
        if (!missileObj.worldObj.isRemote && missileObj.getTicksInAir() <= 0)
        {
            if (entityPlayer.getCurrentEquippedItem() != null)
            {
                if (entityPlayer.getCurrentEquippedItem().getItem() instanceof ItemTracker)
                {
                    Entity trackingEntity = ((ItemTracker) entityPlayer.getCurrentEquippedItem().getItem()).getTrackingEntity(missileObj.worldObj, entityPlayer.getCurrentEquippedItem());

                    if (trackingEntity != null)
                    {
                        if (missileObj.trackingVar != trackingEntity.getEntityId())
                        {
                            missileObj.trackingVar = trackingEntity.getEntityId();
                            entityPlayer.addChatMessage(new ChatComponentText("Missile target locked to: " + trackingEntity.getCommandSenderName()));

                            if (missileObj.getLauncher() != null && missileObj.getLauncher().getController() != null)
                            {
                                Pos newTarget = new Pos(trackingEntity.posX, 0, trackingEntity.posZ);
                                missileObj.getLauncher().getController().setTarget(newTarget);
                            }

                            return true;
                        }
                    }
                }
            }
        }

        return false;
    }

    @Override
    public boolean isCruise()
    {
        return false;
    }

    @Override
    public void doCreateExplosion(World world, double x, double y, double z, Entity entity)
    {
        new BlastRepulsive(world, entity, x, y, z, 4).setDestroyItems().explode();
    }

    @Override
    public IWorldChangeAction createBlastForTrigger(World world, double x, double y, double z, TriggerCause triggerCause, double size, NBTTagCompound tag)
    {
        return null;
    }
}

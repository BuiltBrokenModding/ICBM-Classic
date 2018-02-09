package icbm.classic.content.explosive.ex.missiles;

import com.builtbroken.mc.imp.transform.vector.Pos;
import icbm.classic.content.entity.EntityMissile;
import icbm.classic.content.entity.EntityMissile.MissileType;
import icbm.classic.content.explosive.blast.BlastTNT;
import icbm.classic.content.items.ItemTracker;
import icbm.classic.prefab.EnumTier;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;

public class MissileHoming extends Missile
{
    public MissileHoming()
    {
        super("homing", EnumTier.ONE);
        this.hasBlock = false;
        //this.missileModelPath = "missiles/tier1/missile_head_homing.obj";
    }

    @Override
    public void launch(EntityMissile missileObj)
    {
        if (!missileObj.world.isRemote)
        {
            WorldServer worldServer = (WorldServer) missileObj.world;
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
            World world = missileObj.world;
            Entity trackingEntity = world.getEntityByID(missileObj.trackingVar);

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
                            , missileObj.deltaPathY / (missileObj.missileFlightTime * suDu),
                            missileObj.deltaPathZ / (missileObj.missileFlightTime * suDu));
                }
            }
        }
    }

    @Override
    public boolean onInteract(EntityMissile missileObj, EntityPlayer entityPlayer, EnumHand hand)
    {
        if (!missileObj.world.isRemote && missileObj.getTicksInAir() <= 0)
        {
            final ItemStack heldItem = entityPlayer.getHeldItem(hand);
            if (heldItem != null && heldItem.getItem() instanceof ItemTracker)
            {
                Entity trackingEntity = ((ItemTracker) heldItem.getItem()).getTrackingEntity(missileObj.world, heldItem);

                if (trackingEntity != null)
                {
                    if (missileObj.trackingVar != trackingEntity.getEntityId())
                    {
                        missileObj.trackingVar = trackingEntity.getEntityId();
                        entityPlayer.sendMessage(new TextComponentString("Missile target locked to: " + trackingEntity.getName()));

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

        return false;
    }

    @Override
    public boolean isCruise()
    {
        return false;
    }

    @Override
    public void doCreateExplosion(World world, BlockPos pos, Entity entity)
    {
        new BlastTNT(world, entity, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 4).setDestroyItems().explode();
    }
}

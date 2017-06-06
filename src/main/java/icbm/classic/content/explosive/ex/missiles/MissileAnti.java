package icbm.classic.content.explosive.ex.missiles;

import com.builtbroken.mc.api.edit.IWorldChangeAction;
import com.builtbroken.mc.api.event.TriggerCause;
import com.builtbroken.mc.imp.transform.vector.Pos;
import icbm.classic.ICBMClassic;
import icbm.classic.content.entity.EntityMissile;
import icbm.classic.content.explosive.blast.BlastTNT;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.World;

/**
 * Antiballistic missile.
 *
 * @author Calclavia
 */
public class MissileAnti extends Missile
{
    public static final int ABMRange = 30;

    public MissileAnti()
    {
        super("antiBallistic", 2);
        this.hasBlock = false;
        this.missileModelPath = "missiles/tier2/missile_head_antballistic.obj";
    }

    @Override
    public void update(EntityMissile missileObj)
    {
        if (missileObj.lockedTarget != null)
        {
            Pos target = new Pos(missileObj.lockedTarget);

            if (missileObj.lockedTarget.isDead)
            {
                missileObj.explode();
                return;
            }

            if (missileObj.lockedTarget instanceof EntityMissile)
            {
                target = ((EntityMissile) missileObj.lockedTarget).getPredictedPosition(4);
            }

            missileObj.motionX = (target.x() - missileObj.posX) * (0.3F);
            missileObj.motionY = (target.y() - missileObj.posY) * (0.3F);
            missileObj.motionZ = (target.z() - missileObj.posZ) * (0.3F);

            return;
        }

        AxisAlignedBB bounds = AxisAlignedBB.getBoundingBox(missileObj.posX - ABMRange, missileObj.posY - ABMRange, missileObj.posZ - ABMRange, missileObj.posX + ABMRange, missileObj.posY + ABMRange, missileObj.posZ + ABMRange);
        // TODO: Check if this works.
        Entity nearestEntity = missileObj.worldObj.findNearestEntityWithinAABB(EntityMissile.class, bounds, missileObj);

        if (nearestEntity instanceof EntityMissile)
        {
            // Lock target onto missileObj missile
            missileObj.lockedTarget = nearestEntity;
            missileObj.didTargetLockBefore = true;
            missileObj.worldObj.playSoundAtEntity(missileObj, ICBMClassic.PREFIX + "targetlocked", 5F, 0.9F);
        }
        else
        {
            missileObj.motionX = missileObj.deltaPathX / missileObj.missileFlightTime;
            missileObj.motionZ = missileObj.deltaPathZ / missileObj.missileFlightTime;

            if (missileObj.didTargetLockBefore == true)
            {
                missileObj.explode();
            }
        }
    }

    @Override
    public boolean isCruise()
    {
        return true;
    }

    @Override
    public void doCreateExplosion(World world, double x, double y, double z, Entity entity)
    {
        new BlastTNT(world, entity, x, y, z, 6).setDestroyItems().explode();
    }

    @Override
    public IWorldChangeAction createBlastForTrigger(World world, double x, double y, double z, TriggerCause triggerCause, double size, NBTTagCompound tag)
    {
        return null;
    }
}

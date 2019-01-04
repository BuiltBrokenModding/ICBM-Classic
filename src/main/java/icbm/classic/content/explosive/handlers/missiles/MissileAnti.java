package icbm.classic.content.explosive.handlers.missiles;

import com.builtbroken.jlib.data.vector.IPos3D;
import icbm.classic.client.ICBMSounds;
import icbm.classic.content.missile.EntityMissile;
import icbm.classic.lib.transform.vector.Pos;
import icbm.classic.api.EnumTier;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.List;

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
        super("antiBallistic", EnumTier.TWO);
        this.hasBlock = false;
    }

    @Override
    public void update(EntityMissile missileAnti)
    {
        //if target is valid, move towards target
        if (missileAnti.lockedTarget instanceof EntityMissile && !missileAnti.lockedTarget.isDead)
        {
            Pos target = new Pos(missileAnti.lockedTarget);

            //If close enough destroy
            if (target.distance((IPos3D) missileAnti) < 5)
            {
                missileAnti.lockedTarget.setDead();
                missileAnti.setDead();
                missileAnti.world.createExplosion(missileAnti,
                        missileAnti.lockedTarget.posX, missileAnti.lockedTarget.posY, missileAnti.lockedTarget.posZ,
                        1F, true);
                return;
            }

            //Get next movement point
            if (missileAnti.lockedTarget instanceof EntityMissile)
            {
                target = ((EntityMissile) missileAnti.lockedTarget).getPredictedPosition(4);
            }

            //Update motion
            missileAnti.motionX = (target.x() - missileAnti.posX) * (0.3F);
            missileAnti.motionY = (target.y() - missileAnti.posY) * (0.3F);
            missileAnti.motionZ = (target.z() - missileAnti.posZ) * (0.3F);

        }
        //If not valid, reset target so we can find another
        else
        {
            missileAnti.lockedTarget = null;
        }

        //Find target, only run every 3 ticks to reduce lag TODO add config for how often to run
        if (missileAnti.lockedTarget == null && missileAnti.ticksInAir % 3 == 0)
        {
            //TODO find a way to prevent more than 1 missile from targeting the same missile
            //Build bounds
            AxisAlignedBB bounds = new AxisAlignedBB(
                    missileAnti.posX - ABMRange, missileAnti.posY - ABMRange, missileAnti.posZ - ABMRange,
                    missileAnti.posX + ABMRange, missileAnti.posY + ABMRange, missileAnti.posZ + ABMRange);

            //Get all missiles in range
            List<EntityMissile> missiles = missileAnti.world.getEntitiesWithinAABB(EntityMissile.class, bounds, null);

            //Loop to find best target
            for (EntityMissile missile : missiles)
            {
                //Do not target other anti-missile missiles
                if (missile.getExplosiveType() != this)
                {
                    // Lock target onto missileObj missile
                    missileAnti.lockedTarget = missile;
                    missileAnti.didTargetLockBefore = true;
                    ICBMSounds.TARGET_LOCKED.play(missileAnti, 5F, 0.9F, true);
                    break; //TODO setup simple logic to find best target (closest and highest point value)
                }
            }
        }
    }

    @Override
    public boolean isCruise()
    {
        return true;
    }

    @Override
    public void doCreateExplosion(World world, BlockPos pos, Entity entity, float scale)
    {
        //new BlastTNT(world, entity, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 6).setDestroyItems().explode();
        //TODO drop item
    }
}

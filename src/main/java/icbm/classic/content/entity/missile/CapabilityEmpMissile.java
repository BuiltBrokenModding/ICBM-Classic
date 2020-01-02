package icbm.classic.content.entity.missile;

import icbm.classic.api.explosion.IBlast;
import icbm.classic.lib.capability.emp.CapabilityEmpKill;
import icbm.classic.config.ConfigEMP;

/**
 *
 * Created by Dark(DarkGuardsman, Robert) on 3/12/2018.
 */
public class CapabilityEmpMissile extends CapabilityEmpKill<EntityMissile>
{
    public CapabilityEmpMissile(EntityMissile entity)
    {
        super(entity);
    }

    @Override
    protected void setDeadEmp(IBlast emp_blast, float power)
    {
        if (ConfigEMP.ALLOW_MISSILE_DESTROY && entity.isEntityAlive() && !entity.capabilityMissile.hasExploded())
        {
            //Drop missile items
            if (ConfigEMP.ALLOW_MISSILE_DROPS)
            {
                entity.capabilityMissile.dropMissileAsItem();
            }

            //Kill missile
            entity.setDead();
        }
    }
}

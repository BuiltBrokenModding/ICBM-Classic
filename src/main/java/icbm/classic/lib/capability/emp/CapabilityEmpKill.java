package icbm.classic.lib.capability.emp;

import icbm.classic.api.actions.IAction;
import icbm.classic.api.caps.IEMPReceiver;
import net.minecraft.entity.Entity;
import net.minecraft.world.World;

/**
 * Capability that destroys the host
 *
 *
 * Created by Dark(DarkGuardsman, Robin) on 3/12/2018.
 */
public class CapabilityEmpKill<E extends Entity> implements IEMPReceiver
{
    public final E entity;

    public CapabilityEmpKill(E entity)
    {
        this.entity = entity;
    }

    @Override
    public float applyEmpAction(World world, double x, double y, double z, IAction emp_blast, float power, boolean doAction)
    {
        if (doAction)
        {
            setDeadEmp(emp_blast, power); //TODO drop item?
        }
        return power;
    }

    protected void setDeadEmp(IAction emp_blast, float power)
    {
        entity.remove();
    }
}

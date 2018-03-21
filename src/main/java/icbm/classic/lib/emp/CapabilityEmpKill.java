package icbm.classic.lib.emp;

import icbm.classic.api.caps.IEMPReceiver;
import icbm.classic.api.explosion.IBlast;
import net.minecraft.entity.Entity;
import net.minecraft.world.World;

/**
 * Capability that destroys the host
 *
 * @see <a href="https://github.com/BuiltBrokenModding/VoltzEngine/blob/development/license.md">License</a> for what you can and can't do with the code.
 * Created by Dark(DarkGuardsman, Robert) on 3/12/2018.
 */
public class CapabilityEmpKill<E extends Entity> implements IEMPReceiver
{
    public final E entity;

    public CapabilityEmpKill(E entity)
    {
        this.entity = entity;
    }

    @Override
    public float applyEmpAction(World world, double x, double y, double z, IBlast emp_blast, float power, boolean doAction)
    {
        if (doAction)
        {
            setDeadEmp(emp_blast, power);
        }
        return power;
    }

    protected void setDeadEmp(IBlast emp_blast, float power)
    {
        entity.setDead();
    }
}

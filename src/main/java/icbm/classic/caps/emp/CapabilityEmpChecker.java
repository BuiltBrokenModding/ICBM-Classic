package icbm.classic.caps.emp;

import icbm.classic.api.caps.IEMPReceiver;
import icbm.classic.api.explosion.IBlast;
import net.minecraft.world.World;

/**
 * Basic version of the capability that acts as a placeholder
 *
 * @see <a href="https://github.com/BuiltBrokenModding/VoltzEngine/blob/development/license.md">License</a> for what you can and can't do with the code.
 * Created by Dark(DarkGuardsman, Robert) on 3/12/2018.
 */
public class CapabilityEmpChecker implements IEMPReceiver
{
    public int timesHitByEMP = 0;

    @Override
    public float applyEmpAction(World world, double x, double y, double z, IBlast emp_blast, float power, boolean doAction)
    {
        if (doAction)
        {
            timesHitByEMP++;
        }
        return power;
    }
}

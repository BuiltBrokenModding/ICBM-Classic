package icbm.classic.content.machines.emptower;

import com.builtbroken.mc.framework.energy.data.AbstractEnergyBuffer;

/**
 * @see <a href="https://github.com/BuiltBrokenModding/VoltzEngine/blob/development/license.md">License</a> for what you can and can't do with the code.
 * Created by Dark(DarkGuardsman, Robert) on 6/17/2018.
 */
public class EnergyBufferEMPTower extends AbstractEnergyBuffer
{
    public final TileEMPTower tower;

    public EnergyBufferEMPTower(TileEMPTower tower)
    {
        this.tower = tower;
    }

    @Override
    public int getMaxBufferSize()
    {
        return tower.getEnergyBufferSize();
    }
}

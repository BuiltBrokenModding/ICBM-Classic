package icbm.classic.world.missile.entity;

import icbm.classic.api.caps.IEMPReceiver;
import icbm.classic.api.explosion.IBlast;
import icbm.classic.api.missiles.IMissile;
import icbm.classic.config.ConfigEMP;
import icbm.classic.world.missile.logic.flight.DeadFlightLogic;
import net.minecraft.world.level.Level;

/**
 * Created by Dark(DarkGuardsman, Robert) on 3/12/2018.
 */
public class CapabilityEmpMissile implements IEMPReceiver {
    final IMissile missile;

    public CapabilityEmpMissile(IMissile missile) {
        this.missile = missile;
    }

    @Override
    public float applyEmpAction(Level level, double x, double y, double z, IBlast emp_blast, float power, boolean doAction) {
        if (ConfigEMP.ALLOW_MISSILES && missile.getMissileEntity() != null && missile.getMissileEntity().isAlive()) {
            if (doAction) {
                //Kill guidance and start falling out of the sky
                missile.setFlightLogic(new DeadFlightLogic(0));
                //TODO add random chance to disable fuse and have the missile dud on impact
            }
        }
        return power;
    }
}

package icbm.classic.lib.radio.messages;

import icbm.classic.api.actions.status.IActionStatus;
import icbm.classic.api.missiles.IMissile;
import icbm.classic.api.radio.messages.IIncomingMissileMessage;
import icbm.classic.content.blocks.launcher.status.LaunchedWithMissile;
import icbm.classic.content.missile.entity.anti.EntitySurfaceToAirMissile;
import lombok.Data;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;

@Data
public class IncomingMissileMessage implements IIncomingMissileMessage {
    private final String channel;
    private final IMissile missile;
    private final boolean trigger;

    @Override
    public Vec3d getIntercept(double x, double y, double z, double velocity) {
        if(missile != null && missile.getMissileEntity() != null) {
            final Entity entity = missile.getMissileEntity();
            final int maxTime = 20;
            for(int time = 1; time <= maxTime; time++) { //TODO replace with alg equation that can calculate fn(t)
                final double tX = entity.posX + entity.getMotion().x * time;
                final double tY = entity.posY + entity.getMotion().y * time;
                final double tZ = entity.posZ + entity.getMotion().z * time;

                final double mag = Math.sqrt(tX * tX + tY * tY + tZ * tZ); //TODO remove need for sqrt

                if(mag < velocity * time || time == maxTime) {
                    return missile.getVec3d();
                }
            }
            return missile.getVec3d();
        }
        return null;
    }

    @Override
    public Vec3d getTarget() {
        if(missile != null) {
            return missile.getVec3d();
        }
        return null;
    }

    @Override
    public boolean shouldTrigger() {
        return trigger;
    }

    @Override
    public void onTriggerCallback(IActionStatus status) {
        if(status instanceof LaunchedWithMissile) {
            final IMissile firedMissile = ((LaunchedWithMissile) status).getMissile();
            if(firedMissile != null && firedMissile.getMissileEntity() instanceof EntitySurfaceToAirMissile) {
               final EntitySurfaceToAirMissile sam = (EntitySurfaceToAirMissile) firedMissile.getMissileEntity();
               sam.scanLogic.setCurrentTarget(missile.getMissileEntity());
            }
        }
    }
}

package icbm.classic.content.missile.entity.anti;

import icbm.classic.config.ConfigAntiMissile;
import icbm.classic.content.missile.entity.EntityMissile;
import icbm.classic.content.missile.logic.flight.FollowTargetLogic;
import net.minecraft.entity.Entity;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EntityDamageSource;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;

import javax.annotation.Nullable;

/**
 * Created by Robin Seifert on 11/30/2021.
 */
public class EntityAntiMissile extends EntityMissile<EntityAntiMissile> {
    protected final AntiMissileTarget scanLogic = new AntiMissileTarget(this);

    private boolean hasStartedFollowing = false;

    public EntityAntiMissile(World world) {
        super(world);
        this.getMissileCapability().setTargetData(scanLogic); //TODO create custom missileCap to force getTarget()
    }

    @Override
    public void onUpdate() {
        //Scan for targets
        scanLogic.tick();

        final Entity currentTarget = scanLogic.getTarget();

        //Switch to follow logic once we have a target in range, launcher will set initial flight logic to get it out of the tube
        if(!hasStartedFollowing && currentTarget != null && this.getMissileCapability().getFlightLogic().canSafelyExitLogic()) {
            hasStartedFollowing = true;
            //TODO play missile lock sound effect
            this.getMissileCapability().setFlightLogic(new FollowTargetLogic(ConfigAntiMissile.FUEL));
        }

        //Handle kill target logic
        if(currentTarget != null) {
            final double distance = this.getDistance(currentTarget);

            if(distance <= ConfigAntiMissile.ATTACK_DISTANCE) {
                //TODO add custom damage source that reflects owner of the AB missile
                currentTarget.attackEntityFrom(new EntityDamageSource("missile", this), ConfigAntiMissile.ATTACK_DAMAGE);
                //TODO play sound effect of missile exploding
                this.setDead();
            }
        }

        //Normal update logic
        super.onUpdate();
    }

    @Override
    public <T> T getCapability(Capability<T> capability, @Nullable EnumFacing facing) {
        //TODO add AB capability so radars can redirect targets
        return super.getCapability(capability, facing);
    }

    @Override
    public boolean hasCapability(Capability<?> capability, @Nullable EnumFacing facing) {
        //TODO add AB capability so radars can redirect targets
        return super.hasCapability(capability, facing);
    }
}

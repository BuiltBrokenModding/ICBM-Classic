package icbm.classic.world.missile.entity.anti;

import icbm.classic.config.missile.ConfigMissile;
import icbm.classic.world.IcbmItems;
import icbm.classic.world.missile.entity.EntityMissile;
import icbm.classic.world.missile.logic.flight.FollowTargetLogic;
import net.minecraft.core.Direction;
import net.minecraft.util.EntityDamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.common.capabilities.Capability;

import javax.annotation.Nullable;

/**
 * Created by Robin Seifert on 11/30/2021.
 */
public class SurfaceToAirMissileEntity extends EntityMissile<SurfaceToAirMissileEntity> {

    private ItemStack renderStackCache;
    protected final SAMTargetData scanLogic = new SAMTargetData(this);

    private boolean hasStartedFollowing = false;

    public SurfaceToAirMissileEntity(EntityType<? extends EntityMissile<?>> type, Level level) {
        super(type, level);
        this.getMissileCapability().setTargetData(scanLogic); //TODO create custom missileCap to force getTarget()
    }

    @Override
    public float getMaxHealth() {
        return ConfigMissile.TIER_2_HEALTH;
    }

    @Override
    public void onUpdate() {

        if (!world.isClientSide()) {

            //Scan for targets
            scanLogic.tick();

            final Entity currentTarget = scanLogic.getTarget();

            //TODO code version of ballistic flight logic that switches for us without manually checking
            //Switch to follow logic once we have a target in range, launcher will set initial flight logic to get it out of the tube
            if (!hasStartedFollowing && currentTarget != null && this.getMissileCapability().getFlightLogic().canSafelyExitLogic()) {
                hasStartedFollowing = true;
                //TODO play missile lock sound effect

                // Update our targeting system to track sam targets, some flight systems will use their own targeting logic
                this.getMissileCapability().setTargetData(scanLogic);

                // Update out flight logic to follow our sam target
                this.getMissileCapability().switchFlightLogic(new FollowTargetLogic(ConfigMissile.SAM_MISSILE.FUEL));
            }

            //TODO move to object that gets a tick() invoke `ProximityKillHandler`
            //Handle kill target logic
            if (currentTarget != null) {
                final double distance = this.getDistance(currentTarget);

                if (distance <= ConfigMissile.SAM_MISSILE.FLIGHT_SPEED) {
                    //TODO add custom damage source that reflects owner of the AB missile, damage is impact-blunt
                    currentTarget.attackEntityFrom(new EntityDamageSource("missile", this), ConfigMissile.SAM_MISSILE.ATTACK_DAMAGE);
                    //TODO play sound effect of missile exploding
                    this.setDead();
                }
            }
        }

        //Normal update logic
        super.onUpdate();
    }

    @Override
    public <T> T getCapability(Capability<T> capability, @Nullable Direction facing) {
        //TODO add AB capability so radars can redirect targets
        return super.getCapability(capability, facing);
    }

    @Override
    public boolean hasCapability(Capability<?> capability, @Nullable Direction facing) {
        //TODO add AB capability so radars can redirect targets
        return super.hasCapability(capability, facing);
    }

    @Override
    public ItemStack toStack() {
        if (world.isClientSide()) {
            if (renderStackCache == null) {
                renderStackCache = new ItemStack(IcbmItems.itemSAM);
            }
            return renderStackCache;
        }
        return new ItemStack(IcbmItems.itemSAM);
    }
}

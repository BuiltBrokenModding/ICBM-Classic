package icbm.classic.content.missile.entity.anti;

import icbm.classic.config.missile.ConfigSAMMissile;
import icbm.classic.config.missile.ConfigMissile;
import icbm.classic.content.missile.entity.EntityMissile;
import icbm.classic.content.missile.logic.flight.FollowTargetLogic;
import icbm.classic.content.reg.ItemReg;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EntityDamageSource;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;

import javax.annotation.Nullable;

/**
 * Created by Robin Seifert on 11/30/2021.
 */
public class EntitySurfaceToAirMissile extends EntityMissile<EntitySurfaceToAirMissile> {

    private ItemStack renderStackCache;
    protected final SAMTargetData scanLogic = new SAMTargetData(this);

    private boolean hasStartedFollowing = false;

    public EntitySurfaceToAirMissile(World world) {
        super(world);
        this.getMissileCapability().setTargetData(scanLogic); //TODO create custom missileCap to force getTarget()
    }

    @Override
    public float getMaxHealth()
    {
        return ConfigMissile.TIER_2_HEALTH;
    }

    @Override
    public void onUpdate() {

        if(!world.isRemote) {

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
                this.getMissileCapability().setFlightLogic(new FollowTargetLogic(ConfigMissile.SAM_MISSILE.FUEL));
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
    public <T> T getCapability(Capability<T> capability, @Nullable EnumFacing facing) {
        //TODO add AB capability so radars can redirect targets
        return super.getCapability(capability, facing);
    }

    @Override
    public boolean hasCapability(Capability<?> capability, @Nullable EnumFacing facing) {
        //TODO add AB capability so radars can redirect targets
        return super.hasCapability(capability, facing);
    }

    @Override
    public ItemStack toStack() {
        if(world.isRemote) {
            if(renderStackCache == null) {
                renderStackCache = new ItemStack(ItemReg.itemSAM);
            }
            return renderStackCache;
        }
        return new ItemStack(ItemReg.itemSAM);
    }
}

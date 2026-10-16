package icbm.classic.content.missile.entity.anti;

import icbm.classic.api.missiles.parts.IMissileFlightLogic;
import icbm.classic.config.missile.ConfigMissile;
import icbm.classic.content.blast.ICBMDamageTypes;
import icbm.classic.content.missile.entity.EntityMissile;
import icbm.classic.content.missile.logic.flight.FollowTargetLogic;
import icbm.classic.content.reg.EntityReg;
import icbm.classic.content.reg.ItemReg;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.capabilities.EntityCapability;

import javax.annotation.Nullable;

/**
 * Created by Robin Seifert on 11/30/2021.
 */
public class EntitySurfaceToAirMissile extends EntityMissile<EntitySurfaceToAirMissile> {

    private ItemStack renderStackCache;
    public final SAMTargetData scanLogic = new SAMTargetData(this);

    private boolean hasStartedFollowing = false;

    public EntitySurfaceToAirMissile(EntityType<? extends EntitySurfaceToAirMissile> type, Level world) {
        super(type, world);
        this.getMissileCapability().setTargetData(scanLogic); //TODO create custom missileCap to force getTarget()
        // PORT: 1.12 only set the max health, leaving the synced health at its default of 1
        this.initHealth(ConfigMissile.TIER_2_HEALTH);
    }

    public EntitySurfaceToAirMissile(Level world) {
        this(EntityReg.MISSILE_SAM.get(), world);
    }

    @Override
    public void tick() {

        if(!level().isClientSide()) {

            //Scan for targets
            scanLogic.tick();

            final Entity currentTarget = scanLogic.getTarget();

            //TODO code version of ballistic flight logic that switches for us without manually checking
            //Switch to follow logic once we have a target in range, launcher will set initial flight logic to get it out of the tube
            // PORT: null check added, SAMs without a flight plan (/summon, API, finished step chain) crashed the server tick in 1.12
            final IMissileFlightLogic flightLogic = this.getMissileCapability().getFlightLogic();
            if (!hasStartedFollowing && currentTarget != null && (flightLogic == null || flightLogic.canSafelyExitLogic())) {
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
                final double distance = this.distanceTo(currentTarget);

                if (distance <= ConfigMissile.SAM_MISSILE.FLIGHT_SPEED) {
                    //TODO add custom damage source that reflects owner of the AB missile, damage is impact-blunt
                    // PORT: 1.12 EntityDamageSource("missile", this)
                    currentTarget.hurt(ICBMDamageTypes.source(level(), ICBMDamageTypes.MISSILE, this, this), ConfigMissile.SAM_MISSILE.ATTACK_DAMAGE);
                    //TODO play sound effect of missile exploding
                    this.discard();
                }
            }
        }

        //Normal update logic
        super.tick();
    }

    @Nullable
    @Override
    public <T> T getEntityCapability(EntityCapability<T, Void> capability) {
        //TODO add AB capability so radars can redirect targets
        return super.getEntityCapability(capability);
    }

    @Override
    public ItemStack toStack() {
        if(level().isClientSide()) {
            if(renderStackCache == null) {
                renderStackCache = new ItemStack(ItemReg.itemSAM.get());
            }
            return renderStackCache;
        }
        return new ItemStack(ItemReg.itemSAM.get());
    }
}
// PORTED

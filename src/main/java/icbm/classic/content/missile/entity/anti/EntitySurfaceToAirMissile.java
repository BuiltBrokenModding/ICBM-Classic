package icbm.classic.content.missile.entity.anti;

import icbm.classic.config.missile.ConfigMissile;
import icbm.classic.content.missile.entity.EntityMissile;
import icbm.classic.content.missile.logic.flight.FollowTargetLogic;
import icbm.classic.content.reg.ItemReg;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EntityDamageSource;
import net.minecraft.util.Direction;
import net.minecraft.util.NonNullList;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.common.util.NonNullSupplier;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Created by Robin Seifert on 11/30/2021.
 */
public class EntitySurfaceToAirMissile extends EntityMissile<EntitySurfaceToAirMissile> {

    private ItemStack renderStackCache;
    public final SAMTargetData scanLogic = new SAMTargetData(this);

    private boolean hasStartedFollowing = false;

    private final LazyOptional<ItemStack> itemstack;

    public EntitySurfaceToAirMissile(EntityType<EntitySurfaceToAirMissile> type, World world, NonNullSupplier<ItemStack> itemstack) {
        super(type, world);
        this.itemstack = LazyOptional.of(itemstack);
        this.getMissileCapability().setTargetData(scanLogic); //TODO create custom missileCap to force getTarget()
        this.setMaxHealth(ConfigMissile.TIER_2_HEALTH);
    }

    @Override
    public void tick() {

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
                    this.remove();
                }
            }
        }

        //Normal update logic
        super.tick();
    }

    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> capability, @Nullable Direction facing) {
        //TODO add AB capability so radars can redirect targets
        return super.getCapability(capability, facing);
    }

    @Override
    public ItemStack toStack() {
        return this.itemstack.orElse(ItemStack.EMPTY);
    }
}

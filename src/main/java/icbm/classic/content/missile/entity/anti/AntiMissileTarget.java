package icbm.classic.content.missile.entity.anti;

import icbm.classic.ICBMConstants;
import icbm.classic.api.missiles.IMissileTarget;
import icbm.classic.content.missile.entity.explosive.EntityExplosiveMissile;
import net.minecraft.entity.Entity;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.Vec3d;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Queue;

/**
 * Handles scanning for targets
 */
public class AntiMissileTarget implements IMissileTarget {

    public static final ResourceLocation REG_NAME = new ResourceLocation(ICBMConstants.DOMAIN, "anti.missile");

    private static final int MAX_TARGETS = 5;
    private static final int SCAN_DELAY = 10;
    private static final int MAX_RANGE = 30;

    private final Queue<Entity> targets = new LinkedList();

    private Entity currentTarget;

    private final EntityAntiMissile host;

    private int scanDelayTick = 0;

    public AntiMissileTarget(EntityAntiMissile host) {
        this.host = host;
    }

    public void tick() {
        if (scanDelayTick < SCAN_DELAY) {
            scanDelayTick++;
        } else if (targets.isEmpty()) {
            scanDelayTick = 0;
            refreshTargets();
        }
    }

    public void refreshTargets() {
        //FInd new targets
        List<EntityExplosiveMissile> missiles = getValidTargets();

        //Sort so we get more priority targets
        missiles.sort((a, b) -> {
            // we want to avoid comparing on tier as this can create balance issues

            //Compare with distance from self TODO add radar priority settings
            final double distanceA = host.getDistanceSq(a);
            final double distanceB = host.getDistanceSq(b);
            return Double.compare(distanceA, distanceB);
        });

        //Only track a few targets at a time
        targets.addAll(missiles.subList(0, Math.min(MAX_TARGETS, missiles.size())));
    }

    private List<EntityExplosiveMissile> getValidTargets() {
        return host.world()
            .getEntitiesWithinAABB(EntityExplosiveMissile.class, targetArea(), this::isValid);
    }

    private AxisAlignedBB targetArea() {
        return new AxisAlignedBB(
            host.x() - MAX_RANGE,
            host.y() - MAX_RANGE,
            host.z() - MAX_RANGE,
            host.x() + MAX_RANGE,
            host.y() + MAX_RANGE,
            host.z() + MAX_RANGE
        );
    }

    private boolean isValid(Entity entity) {
        return entity instanceof EntityExplosiveMissile
            && entity.isEntityAlive();
        //TODO setup a FoF system to prevent targeting friendly missiles
        //TODO link to radar system so we can prioritize targets
        //TODO create missile that can fake out ABs
    }

    public Entity getTarget() {

        //Invalidate target if it is no longer valid (likely dead)
        if(!isValid(currentTarget)) {
            currentTarget = null;
        }

        //Loop until we find a good target or run out of targets
        while (currentTarget == null && targets.peek() != null) {
            currentTarget = targets.poll();
            if(!isValid(currentTarget)) {
                currentTarget = null;
            }
        }

        return currentTarget;
    }

    @Override
    public Vec3d getPosition() {
        return getTarget() != null ? getTarget().getPositionVector() : null;
    }

    @Override
    public boolean isValid() {
        return isValid(getTarget());
    }

    @Override
    public double getX() {
        return Optional.ofNullable(getTarget()).map((entity) -> entity.posX).orElse(0.0);
    }

    @Override
    public double getY() {
        return Optional.ofNullable(getTarget()).map((entity) -> entity.posY).orElse(0.0);
    }

    @Override
    public double getZ() {
        return Optional.ofNullable(getTarget()).map((entity) -> entity.posZ).orElse(0.0);
    }

    @Override
    public ResourceLocation getRegistryName() {
        return REG_NAME;
    }

    @Override
    public NBTTagCompound serializeNBT() {
        //TODO find a way to save current target
        return null;
    }

    @Override
    public void deserializeNBT(NBTTagCompound nbt) {

    }
}

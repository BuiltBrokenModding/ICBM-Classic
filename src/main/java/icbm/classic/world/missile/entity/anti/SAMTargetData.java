package icbm.classic.world.missile.entity.anti;

import icbm.classic.IcbmConstants;
import icbm.classic.api.missiles.parts.IMissileTarget;
import icbm.classic.world.missile.entity.explosive.ExplosiveMissileEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Queue;

/**
 * Handles scanning for targets
 */
public class SAMTargetData implements IMissileTarget {

    public static final ResourceLocation REG_NAME = new ResourceLocation(IcbmConstants.MOD_ID, "anti.missile");

    private static final int MAX_TARGETS = 5;
    private static final int SCAN_DELAY = 10;
    private static final int MAX_RANGE = 30;

    private final Queue<Entity> targets = new LinkedList();

    private Entity currentTarget;

    private final SurfaceToAirMissileEntity host;

    private int scanDelayTick = 0;

    public SAMTargetData(SurfaceToAirMissileEntity host) {
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
        List<ExplosiveMissileEntity> missiles = getValidTargets();

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

    private List<ExplosiveMissileEntity> getValidTargets() {
        return host.world()
            .getEntitiesWithinAABB(ExplosiveMissileEntity.class, targetArea(), this::isValid);
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
        return entity instanceof ExplosiveMissileEntity
            && entity.isAlive();
        //TODO setup a FoF system to prevent targeting friendly missiles
        //TODO link to radar system so we can prioritize targets
        //TODO create missile that can fake out ABs
    }

    public Entity getTarget() {

        //Invalidate target if it is no longer valid (likely dead)
        if (!isValid(currentTarget)) {
            currentTarget = null;
        }

        //Loop until we find a good target or run out of targets
        while (currentTarget == null && targets.peek() != null) {
            currentTarget = targets.poll();
            if (!isValid(currentTarget)) {
                currentTarget = null;
            }
        }

        return currentTarget;
    }

    @Override
    public Vec3 getPosition() {
        return getTarget() != null ? getTarget().getPositionVector() : null;
    }

    @Override
    public boolean isValid() {
        return isValid(getTarget());
    }

    @Override
    public double getX() {
        return Optional.ofNullable(getTarget()).map((entity) -> entity.getX()).orElse(0.0);
    }

    @Override
    public double getY() {
        return Optional.ofNullable(getTarget()).map((entity) -> entity.getY()).orElse(0.0);
    }

    @Override
    public double getZ() {
        return Optional.ofNullable(getTarget()).map((entity) -> entity.getZ()).orElse(0.0);
    }

    @Override
    public ResourceLocation getRegistryName() {
        return REG_NAME;
    }

    @Override
    public CompoundTag serializeNBT() {
        //TODO find a way to save current target so we don't change targets after save/load
        return null;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {

    }
}

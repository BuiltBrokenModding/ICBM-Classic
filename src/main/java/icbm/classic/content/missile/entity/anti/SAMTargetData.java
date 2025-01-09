package icbm.classic.content.missile.entity.anti;

import icbm.classic.ICBMConstants;
import icbm.classic.api.ICBMClassicAPI;
import icbm.classic.api.missiles.parts.IMissileTarget;
import icbm.classic.api.reg.obj.IBuilderRegistry;
import icbm.classic.config.missile.ConfigMissile;
import icbm.classic.content.missile.entity.EntityMissile;
import icbm.classic.lib.buildable.BuildableObject;
import icbm.classic.lib.radar.RadarEntity;
import icbm.classic.lib.radar.RadarRegistry;
import lombok.Setter;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.Vec3d;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Queue;
import java.util.stream.Collectors;

/**
 * Handles scanning for targets
 */
public class SAMTargetData extends BuildableObject<SAMTargetData, IBuilderRegistry<IMissileTarget>> implements IMissileTarget {

    public static final ResourceLocation REG_NAME = new ResourceLocation(ICBMConstants.DOMAIN, "anti.missile");

    private static final int MAX_TARGETS = 5;
    private static final int SCAN_DELAY = 10;

    private final Queue<Entity> targets = new LinkedList();

    @Setter
    private Entity currentTarget;

    private final EntitySurfaceToAirMissile host;

    private int scanDelayTick = 0;

    public SAMTargetData(EntitySurfaceToAirMissile host) {
        super(REG_NAME, ICBMClassicAPI.MISSILE_TARGET_DATA_REGISTRY, null);
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

        if(ConfigMissile.SAM_MISSILE.RADAR_MAP_ONLY) {
            seekByRadar();
        }
        else {
            seekByAABB();
        }

    }

    private void seekByRadar() {
        final List<RadarEntity> entries = RadarRegistry.getRadarMapForWorld(host.world).getRadarObjects(host.posX, host.posY, ConfigMissile.SAM_MISSILE.TARGET_RANGE);
        final List<Entity> valid = entries.stream().filter(RadarEntity::isValid).map(e -> e.entity).filter(this::isValid).collect(Collectors.toList());

        //Sort so we get more priority targets
         entries.sort((a, b) -> {
            // we want to avoid comparing on tier as this can create balance issues

            //Compare with distance from self TODO add radar priority settings
            final double distanceA = host.getDistanceSq(a.entity);
            final double distanceB = host.getDistanceSq(b.entity);
            return Double.compare(distanceA, distanceB);
        });

        //Only track a few targets at a time
        targets.addAll(valid.subList(0, Math.min(MAX_TARGETS, valid.size())));
    }

    private void seekByAABB() {
        //FInd new targets
        final List<Entity> missiles = getValidTargets();

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

    private List<Entity> getValidTargets() {
        return host.world
            .getEntitiesWithinAABB(EntityMissile.class, targetArea(), this::isValid);
    }

    private AxisAlignedBB targetArea() {
        return new AxisAlignedBB(
            host.posX - ConfigMissile.SAM_MISSILE.TARGET_RANGE,
            host.posY - ConfigMissile.SAM_MISSILE.TARGET_RANGE,
            host.posZ - ConfigMissile.SAM_MISSILE.TARGET_RANGE,
            host.posX + ConfigMissile.SAM_MISSILE.TARGET_RANGE,
            host.posY + ConfigMissile.SAM_MISSILE.TARGET_RANGE,
            host.posZ + ConfigMissile.SAM_MISSILE.TARGET_RANGE
        );
    }

    private boolean isValid(Entity entity) {
        return entity instanceof EntityMissile
            && !(entity instanceof EntitySurfaceToAirMissile)
            && entity.isAlive();
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
}

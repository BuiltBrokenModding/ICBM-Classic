package icbm.classic.lib.explosive;

import icbm.classic.ICBMClassic;
import icbm.classic.IcbmConstants;
import icbm.classic.api.caps.IExplosive;
import icbm.classic.api.explosion.IBlast;
import icbm.classic.api.explosion.IBlastInit;
import icbm.classic.api.explosion.responses.BlastErrorResponses;
import icbm.classic.api.explosion.responses.BlastNullResponses;
import icbm.classic.api.explosion.responses.BlastResponse;
import icbm.classic.api.reg.ExplosiveType;
import icbm.classic.core.registries.IcbmBuiltinRegistries;
import icbm.classic.world.blast.Blast;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.event.level.LevelEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Simple handler to track blasts in order to disable or remove
 * <p>
 * <p>
 * Created by Dark(DarkGuardsman, Robert) on 4/9/2018.
 */
@Mod.EventBusSubscriber(modid = IcbmConstants.MOD_ID)
public class ExplosiveHandler {
    public static final ArrayList<IBlast> activeBlasts = new ArrayList();

    public static void add(Blast blast) {
        activeBlasts.add(blast);
    }

    public static void remove(Blast blast) {
        activeBlasts.remove(blast);
    }

    @SubscribeEvent
    public static void worldUnload(LevelEvent.Unload event) {
        if (!event.getLevel().isClientSide()) {
            final DimensionType dim = event.getLevel().dimensionType();
            activeBlasts.stream()
                .filter(blast -> !blast.hasLevel() || blast.level().dimensionType() == dim)
                .forEach(IBlast::clearBlast);
        }
    }

    /**
     * Called to remove blasts near the location
     *
     * @param level = position
     * @param pos   - position
     * @param range - distance from position, less than zero will turn into global
     * @return number of blasts removed
     */
    public static int removeNear(Level level, Vec3 pos, double range) {
        //Collect blasts marked for removal
        final List<IBlast> toRemove = ExplosiveHandler.activeBlasts.stream()
            .filter(blast -> blast.level() == level)
            .filter(blast -> range < 0 || range > 0 && range > pos.distanceTo(blast.getPos()))
            .collect(Collectors.toList());

        //Do removals
        activeBlasts.removeAll(toRemove);
        toRemove.forEach(IBlast::clearBlast);

        return toRemove.size();
    }

    public static BlastResponse createExplosion(Entity cause, Level level, double x, double y, double z, IExplosive explosive) {
        if (explosive == null) {
            return logEventThenRespond(cause, level, x, y, z, null, 1, BlastNullResponses.EXPLOSIVE_CAPABILITY.get());
        }
        return createExplosion(cause, level, x, y, z, explosive.getExplosiveData(), 1, explosive.getCustomBlastData());
    }

    public static BlastResponse createExplosion(Entity cause, Level level, double x, double y, double z, ResourceKey<ExplosiveType> key, double scale, CompoundTag customData) {
        ExplosiveType explosiveData = IcbmBuiltinRegistries.EXPLOSIVES.get(key);
        if (explosiveData == null) {
            // Log missing registry
            ICBMClassic.logger().error("Missing explosive data in registry for blastID({})", key);

            return logEventThenRespond(cause, level, x, y, z, null, 1, BlastErrorResponses.MISSING_BLAST_REGISTRY.get());
        }
        return createExplosion(cause, level, x, y, z, explosiveData, scale, customData);
    }

    public static BlastResponse createExplosion(Entity cause, Level level, double x, double y, double z, ExplosiveType explosiveData, double scale, CompoundTag customData) {
        BlastResponse response = BlastNullResponses.BLAST_CREATION.get();
        if (explosiveData == null) {
            response = BlastNullResponses.EXPLOSIVE_DATA.get();
        } else if (explosiveData.getBlastFactory() != null) {
            // TODO: add way to hook blast builder to add custom blasts
            final IBlastInit factoryBlast = explosiveData.getBlastFactory().create();

            if (factoryBlast != null) {
                //Setup blast using factory
                factoryBlast.setBlastLevel(level);
                factoryBlast.setBlastPosition(x, y, z);
                factoryBlast.scaleBlast(scale);
                factoryBlast.setBlastSource(cause);
                factoryBlast.setExplosiveData(explosiveData);
                factoryBlast.setCustomData(customData);
                factoryBlast.buildBlast();

                //run blast
                response = factoryBlast.runBlast();
            }
        } else {
            response = BlastNullResponses.BLAST_FACTORY.get();
        }
        return logEventThenRespond(cause, level, x, y, z, explosiveData, scale, response);
    }

    private static BlastResponse logEventThenRespond(Entity cause, Level level, double x, double y, double z, ExplosiveType explosiveData, double scale, BlastResponse blastResponse) {
        String explosiveName = explosiveData == null ? "null" : explosiveData.getRegistryName().toString();
        String entitySource = Objects.toString(BuiltInRegistries.ENTITY_TYPE.getKey(cause.getType()));

        if (blastResponse.errorMessage != null) {
            String formatString = "Explosion[%s] | Scale(x%,.1f) | BlastState(%s) | EntitySource(%s) | Impacted (%,.1fx %,.1fy %,.1fz %sd) | ErrorMessage: %s";
            String formattedMessage = String.format(formatString,
                explosiveName,
                scale,
                blastResponse.state,
                entitySource,
                x,
                y,
                z,
                level.dimension(),
                blastResponse.errorMessage
            );
            ICBMClassic.logger().error(formattedMessage, blastResponse.error);
        } else {
            // TODO: make optional via config
            // TODO: log to ICBM file separated from main config
            // TODO: offer hook for database logging
            String formatString = "Explosion[%s] | Scale(x%,.1f) | BlastState(%s) | EntitySource(%s) | Impacted (%,.1fx %,.1fy %,.1fz %sd)";
            String formattedMessage = String.format(formatString,
                explosiveName,
                scale,
                blastResponse.state,
                entitySource,
                x,
                y,
                z,
                level.dimension()
            );

            ICBMClassic.logger().info(formattedMessage);
        }
        return blastResponse;
    }
}

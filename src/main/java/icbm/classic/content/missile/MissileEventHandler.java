package icbm.classic.content.missile;

import icbm.classic.ICBMConstants;
import icbm.classic.api.ICBMClassicAPI;
import icbm.classic.api.events.MissileRideEvent;
import icbm.classic.api.missiles.IMissile;
import icbm.classic.content.missile.entity.explosive.EntityExplosiveMissile;
import icbm.classic.content.missile.logic.flight.BallisticFlightLogicOld;
import icbm.classic.content.missile.tracker.MissileTrackerHandler;
import icbm.classic.lib.radar.RadarMap;
import icbm.classic.lib.radar.RadarRegistry;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ChunkPos;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.EntityMountEvent;
import net.neoforged.neoforge.event.level.ChunkEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.bus.api.SubscribeEvent;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by Dark(DarkGuardsman, Robin) on 8/4/2019.
 */
@EventBusSubscriber(modid = ICBMConstants.DOMAIN)
public class MissileEventHandler
{
    @SubscribeEvent
    public static void onEntityMount(EntityMountEvent event)
    {
        if (event.isDismounting()
                && event.getEntityBeingMounted() != null
                && event.getEntityMounting() instanceof Player)
        {
            IMissile missile = event.getEntityBeingMounted().getCapability(ICBMClassicAPI.MISSILE_CAPABILITY);
            if(missile != null)
            {
                event.setCanceled(NeoForge.EVENT_BUS.post(new MissileRideEvent.Stop(missile, (Player) event.getEntityMounting())).isCanceled());
            }
        }
    }

    // PORT: kept as a fallback only. In 1.21 entities are unloaded (setRemoved UNLOADED_TO_CHUNK) well before
    // ChunkEvent.Unload fires, so missiles found here are usually no longer alive and simulateMissile skips them.
    // Flight logics now simulate before entering a chunk where entities do not tick, see MissileTrackerHandler#canEntitiesTick
    @SubscribeEvent
    public static void chunkUnload(ChunkEvent.Unload event)
    {
        if (event.getLevel() instanceof Level world && !world.isClientSide())
        {
            final ChunkPos chunk = event.getChunk().getPos();
            final RadarMap map = RadarRegistry.getRadarMapForWorld(world);
            if (map != null)
            {
                // Collect missiles we are about to unload, using list to avoid concurrent mod from radar remove TODO have radar system track removals in list and apply next tick
                final List<EntityExplosiveMissile> unloading = new LinkedList();
                map.collectEntitiesInChunk(chunk.x, chunk.z, (radarEntity -> {
                    if (radarEntity.entity instanceof EntityExplosiveMissile) //TODO rewrite to work on any missile via capability system
                    {
                        unloading.add((EntityExplosiveMissile) radarEntity.entity);
                    }
                }));

                unloading.stream()
                    .filter(missile -> missile.getMissileCapability().getFlightLogic() instanceof BallisticFlightLogicOld)
                    .forEach(MissileTrackerHandler::simulateMissile);
            }
        }
    }
}
// PORTED

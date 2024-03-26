package icbm.classic.world.missile;

import icbm.classic.IcbmConstants;
import icbm.classic.api.ICBMClassicAPI;
import icbm.classic.api.events.MissileRideEvent;
import icbm.classic.api.missiles.IMissile;
import icbm.classic.lib.radar.RadarMap;
import icbm.classic.lib.radar.RadarRegistry;
import icbm.classic.world.missile.entity.explosive.ExplosiveMissileEntity;
import icbm.classic.world.missile.logic.flight.BallisticFlightLogicOld;
import icbm.classic.world.missile.tracker.MissileTrackerHandler;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.neoforged.common.MinecraftForge;
import net.neoforged.event.entity.EntityMountEvent;
import net.neoforged.event.world.ChunkEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.common.eventhandler.SubscribeEvent;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by Dark(DarkGuardsman, Robert) on 8/4/2019.
 */
@Mod.EventBusSubscriber(modid = IcbmConstants.MOD_ID)
public class MissileEventHandler {
    @SubscribeEvent
    public static void onEntityMount(EntityMountEvent event) {
        if (event.isDismounting()
            && event.getEntityBeingMounted().hasCapability(ICBMClassicAPI.MISSILE_CAPABILITY, null)
            && event.getEntityMounting() instanceof Player) {
            IMissile missile = event.getEntityBeingMounted().getCapability(ICBMClassicAPI.MISSILE_CAPABILITY, null);
            if (missile != null) {
                event.setCanceled(MinecraftForge.EVENT_BUS.post(new MissileRideEvent.Stop(missile, (Player) event.getEntityMounting())));
            }
        }
    }

    @SubscribeEvent
    public static void chunkUnload(ChunkEvent.Unload event) {
        final Level level = event.getLevel();
        if (!world.isClientSide()) {
            final Chunk chunk = event.getChunk();
            final RadarMap map = RadarRegistry.getRadarMapForLevel(world);
            if (map != null) {
                // Collect missiles we are about to unload, using list to avoid concurrent mod from radar remove TODO have radar system track removals in list and apply next tick
                final List<ExplosiveMissileEntity> unloading = new LinkedList();
                map.collectEntitiesInChunk(chunk.x, chunk.z, (radarEntity -> {
                    if (radarEntity.entity instanceof ExplosiveMissileEntity) //TODO rewrite to work on any missile via capability system
                    {
                        unloading.add((ExplosiveMissileEntity) radarEntity.entity);
                    }
                }));

                unloading.stream()
                    .filter(missile -> missile.getMissileCapability().getFlightLogic() instanceof BallisticFlightLogicOld)
                    .forEach(MissileTrackerHandler::simulateMissile);
            }
        }
    }
}

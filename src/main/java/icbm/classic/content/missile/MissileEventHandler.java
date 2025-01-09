package icbm.classic.content.missile;

import icbm.classic.ICBMConstants;
import icbm.classic.api.ICBMClassicAPI;
import icbm.classic.api.events.MissileRideEvent;
import icbm.classic.content.missile.entity.explosive.EntityExplosiveMissile;
import icbm.classic.content.missile.logic.flight.ArcFlightLogic;
import icbm.classic.content.missile.tracker.MissileTrackerHandler;
import icbm.classic.lib.radar.RadarMap;
import icbm.classic.lib.radar.RadarRegistry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.IWorld;
import net.minecraft.world.chunk.IChunk;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.EntityMountEvent;
import net.minecraftforge.event.world.ChunkEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by Dark(DarkGuardsman, Robin) on 8/4/2019.
 */
@Mod.EventBusSubscriber(modid = ICBMConstants.DOMAIN)
public class MissileEventHandler
{
    @SubscribeEvent
    public static void onEntityMount(EntityMountEvent event)
    {
        if (event.isDismounting() && event.getEntityMounting() instanceof PlayerEntity)
        {
            event.getEntityBeingMounted().getCapability(ICBMClassicAPI.MISSILE_CAPABILITY, null).ifPresent(missile -> {
                event.setCanceled(MinecraftForge.EVENT_BUS.post(new MissileRideEvent.Stop(missile, (PlayerEntity) event.getEntityMounting())));
            });
        }
    }

    @SubscribeEvent
    public static void chunkUnload(ChunkEvent.Unload event)
    {
        final IWorld world = event.getWorld();
        if (!world.isRemote())
        {
            final IChunk chunk = event.getChunk();
            final RadarMap map = RadarRegistry.getRadarMapForWorld(world);
            if (map != null)
            {
                // Collect missiles we are about to unload, using list to avoid concurrent mod from radar remove TODO have radar system track removals in list and apply next tick
                final List<EntityExplosiveMissile> unloading = new LinkedList();
                map.collectEntitiesInChunk(chunk.getPos().x, chunk.getPos().z, (radarEntity -> {
                    if (radarEntity.entity instanceof EntityExplosiveMissile) //TODO rewrite to work on any missile via capability system
                    {
                        unloading.add((EntityExplosiveMissile) radarEntity.entity);
                    }
                }));

                unloading.stream()
                    .filter(missile -> missile.getMissileCapability().getFlightLogic() instanceof ArcFlightLogic)
                    .forEach(MissileTrackerHandler::simulateMissile);
            }
        }
    }
}

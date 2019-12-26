package icbm.classic.content.entity.missile;

import icbm.classic.ICBMConstants;
import icbm.classic.api.events.MissileRideEvent;
import icbm.classic.lib.radar.RadarMap;
import icbm.classic.lib.radar.RadarRegistry;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.EntityMountEvent;
import net.minecraftforge.event.world.ChunkEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/**
 * Created by Dark(DarkGuardsman, Robert) on 8/4/2019.
 */
@Mod.EventBusSubscriber(modid = ICBMConstants.DOMAIN)
public class MissileEventHandler
{
    @SubscribeEvent
    public static void onEntityMount(EntityMountEvent event)
    {
        if (event.isDismounting()
                && event.getEntityBeingMounted() instanceof EntityMissile
                && event.getEntityMounting() instanceof EntityPlayer)
        {
            event.setCanceled(MinecraftForge.EVENT_BUS.post(new MissileRideEvent.Stop((EntityMissile) event.getEntityBeingMounted(), (EntityPlayer) event.getEntityMounting())));
        }
    }

    @SubscribeEvent
    public static void chunkUnload(ChunkEvent.Unload event)
    {
        final World world = event.getWorld();
        if (!world.isRemote)
        {
            final Chunk chunk = event.getChunk();
            final RadarMap map = RadarRegistry.getRadarMapForWorld(world);
            if (map != null)
            {
                map.collectEntitiesInChunk(chunk.x, chunk.z, (radarEntity -> {
                    if (radarEntity.entity instanceof EntityMissile)
                    {
                        final EntityMissile missile = (EntityMissile) radarEntity.entity;
                        if(!missile.wasSimulated && missile.missileType == MissileFlightType.PAD_LAUNCHER)
                        {
                            MissileTrackerHandler.simulateMissile((EntityMissile) radarEntity.entity);
                        }
                    }
                }));
            }
        }
    }
}

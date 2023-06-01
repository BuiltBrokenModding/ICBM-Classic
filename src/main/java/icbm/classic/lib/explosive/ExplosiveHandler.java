package icbm.classic.lib.explosive;

import icbm.classic.ICBMClassic;
import icbm.classic.ICBMConstants;
import icbm.classic.api.ICBMClassicAPI;
import icbm.classic.api.caps.IExplosive;
import icbm.classic.api.explosion.IBlast;
import icbm.classic.api.explosion.IBlastInit;
import icbm.classic.api.explosion.responses.BlastErrorResponses;
import icbm.classic.api.explosion.responses.BlastNullResponses;
import icbm.classic.api.explosion.responses.BlastResponse;
import icbm.classic.api.reg.IExplosiveData;
import icbm.classic.content.blast.Blast;
import icbm.classic.lib.transform.vector.Pos;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * Simple handler to track blasts in order to disable or remove
 * <p>
 * <p>
 * Created by Dark(DarkGuardsman, Robert) on 4/9/2018.
 */
@Mod.EventBusSubscriber(modid = ICBMConstants.DOMAIN)
public class ExplosiveHandler
{
    public static final ArrayList<IBlast> activeBlasts = new ArrayList();

    public static void add(Blast blast)
    {
        activeBlasts.add(blast);
    }

    public static void remove(Blast blast)
    {
        activeBlasts.remove(blast);
    }

    @SubscribeEvent
    public static void worldUnload(WorldEvent.Unload event)
    {
        if (!event.getWorld().isRemote)
        {
            final int dim = event.getWorld().provider.getDimension();
            activeBlasts.stream()
                    .filter(blast -> !blast.hasWorld() || blast.world().provider.getDimension() == dim)
                    .forEach(IBlast::clearBlast);
        }
    }

    /**
     * Called to remove blasts near the location
     *
     * @param world = position
     * @param x     - position
     * @param y     - position
     * @param z     - position
     * @param range - distance from position, less than zero will turn into global
     * @return number of blasts removed
     */
    public static int removeNear(World world, double x, double y, double z, double range)
    {
        final Pos pos = new Pos(x, y, z);

        //Collect blasts marked for removal
        final List<IBlast> toRemove = ExplosiveHandler.activeBlasts.stream()
                .filter(blast -> blast.world() == world)
                .filter(blast -> range < 0 || range > 0 && range > pos.distance(blast))
                .collect(Collectors.toList());

        //Do removals
        activeBlasts.removeAll(toRemove);
        toRemove.forEach(IBlast::clearBlast);

        return toRemove.size();
    }

    public static BlastResponse createExplosion(Entity cause, World world, double x, double y, double z, IExplosive capabilityExplosive)
    {
        if (capabilityExplosive == null)
        {
            return logEventThenRespond(cause, world, x, y, z, null, 1, BlastNullResponses.EXPLOSIVE_CAPABILITY.get());
        }
        return createExplosion(cause, world, x, y, z, capabilityExplosive.getExplosiveData(), 1, capabilityExplosive::applyCustomizations);
    }

    public static BlastResponse createExplosion(Entity cause, World world, double x, double y, double z, int blastID, float scale, Consumer<IBlast> customizer)
    {
        final IExplosiveData explosiveData = ICBMClassicAPI.EXPLOSIVE_REGISTRY.getExplosiveData(blastID);
        if (explosiveData == null)
        {
            //Log missing registry
            ICBMClassic.logger().error("Missing explosive data in registry for blastID({})", blastID);

            return logEventThenRespond(cause, world, x, y, z, null, 1, BlastErrorResponses.MISSING_BLAST_REGISTRY.get());
        }
        return createExplosion(cause, world, x, y, z, explosiveData, scale, customizer);
    }

    public static BlastResponse createExplosion(Entity cause, World world, double x, double y, double z, IExplosiveData explosiveData, float scale, Consumer<IBlast> customizer)
    {
        BlastResponse response = BlastNullResponses.BLAST_CREATION.get();
        if (explosiveData == null)
        {
            response = BlastNullResponses.EXPLOSIVE_DATA.get();
        }
        else if (explosiveData.getBlastFactory() != null)
        {
            //TODO add way to hook blast builder to add custom blasts
            final IBlastInit factoryBlast = explosiveData.getBlastFactory().create();

            if (factoryBlast != null)
            {
                //Setup blast using factory
                factoryBlast.setBlastWorld(world);
                factoryBlast.setBlastPosition(x, y, z);
                factoryBlast.scaleBlast(scale);
                factoryBlast.setBlastSource(cause);
                factoryBlast.setExplosiveData(explosiveData);
                customizer.accept(factoryBlast);
                factoryBlast.buildBlast();

                //run blast
                response = factoryBlast.runBlast();
            }
        }
        else
        {
            response = BlastNullResponses.BLAST_FACTORY.get();
        }
        return logEventThenRespond(cause, world, x, y, z, explosiveData, scale, response);
    }

    private static BlastResponse logEventThenRespond(Entity cause, World world, double x, double y, double z, IExplosiveData explosiveData, float scale, BlastResponse blastResponse)
    {
        final String explosiveName = explosiveData == null ? "null" : explosiveData.getRegistryName().toString();
        final String entitySource = cause != null ? Integer.toString(cause.getEntityId()) : "null";

        if (blastResponse.errorMessage != null)
        {
            final String formatString = "Explosion[%s] | Scale(x%,.1f) | BlastState(%s) | EntitySource(%s) | Impacted (%,.1fx %,.1fy %,.1fz %sd) | ErrorMessage: %s";
            final String formattedMessage = String.format(formatString,
                    explosiveName,
                    scale,
                    blastResponse.state,
                    entitySource,
                    x,
                    y,
                    z,
                    world.provider.getDimension(),
                    blastResponse.errorMessage
            );
            ICBMClassic.logger().error(formattedMessage, blastResponse.error);
        }
        else
        {
            // TODO make optional via config
            // TODO log to ICBM file separated from main config
            // TODO offer hook for database logging
            final String formatString = "Explosion[%s] | Scale(x%,.1f) | BlastState(%s) | EntitySource(%s) | Impacted (%,.1fx %,.1fy %,.1fz %sd)";
            final String formattedMessage = String.format(formatString,
                    explosiveName,
                    scale,
                    blastResponse.state,
                    entitySource,
                    x,
                    y,
                    z,
                    world.provider.getDimension()
            );

            ICBMClassic.logger().info(formattedMessage);
        }
        return blastResponse;
    }
}

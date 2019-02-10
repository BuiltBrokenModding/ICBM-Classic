package icbm.classic.lib.explosive;

import icbm.classic.ICBMClassic;
import icbm.classic.api.ICBMClassicAPI;
import icbm.classic.api.explosion.BlastState;
import icbm.classic.api.explosion.IBlast;
import icbm.classic.api.explosion.IBlastInit;
import icbm.classic.api.reg.IExplosiveData;
import icbm.classic.content.blast.Blast;
import icbm.classic.lib.capability.ex.CapabilityExplosive;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * Simple handler to track blasts in order to disable or remove
 * <p>
 * <p>
 * Created by Dark(DarkGuardsman, Robert) on 4/9/2018.
 */
@Mod.EventBusSubscriber(modid = ICBMClassic.DOMAIN)
public class ExplosiveHandler
{

    public static final ArrayList<Blast> activeBlasts = new ArrayList();

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
            Iterator<Blast> it = activeBlasts.iterator();
            while (it.hasNext())
            {
                Blast next = it.next();
                if (next.world == null || next.world.provider.getDimension() == event.getWorld().provider.getDimension())
                {
                    onKill(next);
                    it.remove();
                }
            }
        }
    }

    /**
     * Runs kill logic on the blast, does not remove the blast
     *
     * @param blast
     */
    public static void onKill(Blast blast)
    {
        if (blast.getThread() != null)
        {
            blast.getThread().kill();
        }
        blast.isAlive = false; //TODO replace with method to allow blast to cleanup
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
        int removeCount = 0;
        Iterator<Blast> it = ExplosiveHandler.activeBlasts.iterator();
        while (it.hasNext())
        {
            Blast blast = it.next();
            if (blast.world == world && (range < 0 || range > 0 && range > blast.location.distance(x, y, z)))
            {
                ExplosiveHandler.onKill(blast);
                it.remove();
                removeCount++;
            }
        }
        return removeCount;
    }

    public static BlastState createExplosion(Entity cause, World world, double x, double y, double z, CapabilityExplosive capabilityExplosive)
    {
        if (capabilityExplosive == null || capabilityExplosive.getExplosiveData() == null)
        {
            ICBMClassic.logger().error("ExplosiveHandler: Missing capability for explosive, cap: " + capabilityExplosive + "  cause: " + cause, new RuntimeException());
            return BlastState.NULL;
        }
        return createExplosion(cause, world, x, y, z, capabilityExplosive.getExplosiveData().getRegistryID(), 1, capabilityExplosive.getCustomBlastData());
    }

    public static BlastState createExplosion(Entity cause, World world, double x, double y, double z, int blastID, float scale, NBTTagCompound customData)
    {
        IBlast blast = createNew(cause, world, x, y, z, blastID, scale, customData);
        if (blast != null)
        {
            return blast.runBlast();
        }
        return BlastState.NULL;
    }

    public static IBlast createNew(Entity cause, World world, double x, double y, double z, int blastID, float scale, NBTTagCompound customData)
    {
        final IExplosiveData explosiveData = ICBMClassicAPI.EXPLOSIVE_REGISTRY.getExplosiveData(blastID);
        return createNew(cause, world, x, y, z, explosiveData, scale, customData);
    }

    public static IBlast createNew(Entity cause, World world, double x, double y, double z, IExplosiveData data, float scale, NBTTagCompound customData)
    {
        if (data != null && data.getBlastFactory() != null) //TODO add way to hook blast builder to add custom blasts
        {
            IBlastInit blast = data.getBlastFactory().createNewBlast();
            blast.setBlastWorld(world);
            blast.setBlastPosition(x, y, z);
            blast.scaleBlast(scale);
            blast.setBlastSource(cause);
            blast.setExplosiveData(data);

            return blast.buildBlast();
        }

        ICBMClassic.logger().error("ExplosiveHandler: Failed to create blast, data: " + data + " factory: " + (data != null ? data.getBlastFactory() : " nil"), new RuntimeException());
        return null;
    }
}

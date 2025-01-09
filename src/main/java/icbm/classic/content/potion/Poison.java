package icbm.classic.content.potion;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Vec3d;

import java.util.HashMap;

/**
 * @deprecated remove at some point and apply potion effects directly
 */
@Deprecated
public abstract class Poison
{
    static HashMap<String, Poison> poisons = new HashMap();
    static BiMap<String, Integer> poisonIDs = HashBiMap.create();
    private static int maxID = 0;

    protected String name;

    public static Poison getPoison(String name)
    {
        return poisons.get(name);
    }

    public static Poison getPoison(int id)
    {
        return poisons.get(getName(id));
    }

    public static String getName(int fluidID)
    {
        return poisonIDs.inverse().get(fluidID);
    }

    public static int getID(String name)
    {
        return poisonIDs.get(name);
    }

    public Poison(String name)
    {
        this.name = name;
        poisons.put(name, this);
        poisonIDs.put(name, ++maxID);
    }

    public String getName()
    {
        return this.name;
    }

    public final int getID()
    {
        return getID(this.getName());
    }

    /**
     * Called to poison this specific entity with this specific type of poison.
     *
     * @param entity
     * @param amplifier
     * @param emitPosition
     */
    public void poisonEntity(Vec3d emitPosition, LivingEntity entity, int amplifier)
    {
        if (!(entity instanceof PlayerEntity && ((PlayerEntity) entity).isCreative()))
        {
            doPoisonEntity(emitPosition, entity, amplifier);
        }
    }

    public void poisonEntity(Vec3d emitPosition, LivingEntity entity)
    {
        this.poisonEntity(emitPosition, entity, 0);
    }

    protected abstract void doPoisonEntity(Vec3d emitPosition, LivingEntity entity, int amplifier);
}
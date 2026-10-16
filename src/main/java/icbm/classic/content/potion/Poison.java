package icbm.classic.content.potion;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import icbm.classic.lib.transform.vector.Pos;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

import java.util.HashMap;

/**
 * PORT: in 1.12 this was a plain object that applied vanilla potion effects. The 1.21 registry
 * contract registers the poisons as mob effects ({@code ICBMClassic.chemicalPotion}/{@code contagiousPotion}),
 * so it extends {@link MobEffect}. The effect itself is never applied to entities, {@link #poisonEntity}
 * still applies vanilla effects exactly as before.
 *
 * @deprecated remove at some point and apply potion effects directly
 */
@Deprecated
public abstract class Poison extends MobEffect
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

    public Poison(String name, int color)
    {
        super(MobEffectCategory.HARMFUL, color);
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
    public void poisonEntity(Pos emitPosition, LivingEntity entity, int amplifier)
    {
        if (!isEntityProtected(emitPosition, entity, amplifier))
        {
            doPoisonEntity(emitPosition, entity, amplifier);
        }
    }

    public void poisonEntity(Pos emitPosition, LivingEntity entity)
    {
        this.poisonEntity(emitPosition, entity, 0);
    }

    public boolean isEntityProtected(Pos emitPosition, LivingEntity entity, int amplifier)
    {
        if (entity instanceof Player && ((Player) entity).getAbilities().instabuild)
        {
            return true;
        }
        //1.12 had armor based protection commented out here, see git history
        return false;
    }

    public int getAntiPoisonBlockCount(Level world, Pos startingPosition, Pos endingPosition)
    {
        //1.12 had anti-poison block logic commented out here, see git history
        return 0;
    }

    protected abstract void doPoisonEntity(Pos emitPosition, LivingEntity entity, int amplifier);
}
// PORTED

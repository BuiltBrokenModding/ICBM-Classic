package icbm.classic.content.explosive;

import icbm.classic.ICBMClassic;
import icbm.classic.content.explosive.ex.*;
import icbm.classic.content.explosive.ex.missiles.*;
import net.minecraft.item.ItemStack;

/**
 * Enum of explosives for use a metadata in items and quick reference of values
 *
 * @see <a href="https://github.com/BuiltBrokenModding/VoltzEngine/blob/development/license.md">License</a> for what you can and can't do with the code.
 * Created by Dark(DarkGuardsman, Robert) on 1/7/2017.
 */
public enum Explosives
{
    CONDENSED(new ExCondensed("condensed", 1)),
    SHRAPNEL(new ExShrapnel("shrapnel", 1)),
    INCENDIARY(new ExIncendiary("incendiary", 1)),
    DEBLITATION(new ExDebilitation("debilitation", 1)),
    CHEMICAL(new ExChemical("chemical", 1)),
    ANVIL(new ExShrapnel("anvil", 1)),
    REPLUSIVE(new ExRepulsive("repulsive", 1)),
    ATTRACTIVE(new ExRepulsive("attractive", 1)),

    FRAGMENTATION(new ExShrapnel("fragmentation", 2)),
    CONTAGIOUS(new ExChemical("contagious", 2)),
    SONIC(new ExSonic("sonic", 2)),
    BREACHING(new ExBreaching()),
    REJUVENATION(new ExRejuvenation()),
    THERMOBARIC(new ExNuclear("thermobaric", 2)),
    SMINE(new ExSMine("sMine", 2)),

    NUCLEAR(new ExNuclear("nuclear", 3)),
    EMP(new ExEMP()),
    EXOTHERMIC(new ExExothermic()),
    ENDOTHERMIC(new ExEndothermic()),
    ANTI_GRAV(new ExAntiGravitational()),
    ENDER(new ExEnder()),
    HYPERSONIC(new ExSonic("hypersonic", 3)), //TODO find Missile model

    ANTIMATTER(new ExAntimatter()),
    REDMATTER(new ExRedMatter()),

    MISSILE(new MissileModule()),
    MISSILE_HOMING(new MissileHoming()),
    MISSILE_ANTI(new MissileAnti()),
    MISSILE_CLUSTER(new MissileCluster("cluster", 2)),
    MISSILE_CLUSTER_NUKE(new MissileNuclearCluster());

    public final Explosive handler;

    Explosives(Explosive handler)
    {
        this.handler = handler;
    }

    public ItemStack getItemStack()
    {
        return this.getItemStack(1);
    }

    public ItemStack getItemStack(int amount)
    {
        if (handler instanceof Missile)
        {
            return new ItemStack(ICBMClassic.itemMissile, amount, ordinal());
        }
        return new ItemStack(ICBMClassic.blockExplosive, amount, ordinal());
    }

    public static Explosives get(int itemDamage)
    {
        if (itemDamage >= 0 && itemDamage < values().length)
        {
            return values()[itemDamage];
        }
        return CONDENSED;
    }
}

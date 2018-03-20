package icbm.classic.content.explosive;

import icbm.classic.ICBMClassic;
import icbm.classic.content.explosive.handlers.*;
import icbm.classic.content.explosive.handlers.missiles.*;
import icbm.classic.prefab.tile.EnumTier;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IStringSerializable;

import java.util.ArrayList;
import java.util.List;

/**
 * Enum of explosives for use a metadata in items and quick reference of values
 *
 * @see <a href="https://github.com/BuiltBrokenModding/VoltzEngine/blob/development/license.md">License</a> for what you can and can't do with the code.
 * Created by Dark(DarkGuardsman, Robert) on 1/7/2017.
 */
public enum Explosives implements IStringSerializable
{
    /* 0  */CONDENSED(new ExCondensed("condensed", EnumTier.ONE)),
    /* 1  */SHRAPNEL(new ExShrapnel("shrapnel", EnumTier.ONE)),
    /* 2  */INCENDIARY(new ExIncendiary("incendiary", EnumTier.ONE)),
    /* 3  */DEBLITATION(new ExDebilitation("debilitation", EnumTier.ONE)),
    /* 4  */CHEMICAL(new ExChemical("chemical", EnumTier.ONE)),
    /* 5  */ANVIL(new ExShrapnel("anvil", EnumTier.ONE)),
    /* 6  */REPLUSIVE(new ExRepulsive("repulsive", EnumTier.ONE)),
    /* 7  */ATTRACTIVE(new ExRepulsive("attractive", EnumTier.ONE)),

    /* 8  */FRAGMENTATION(new ExShrapnel("fragmentation", EnumTier.TWO)),
    /* 9  */CONTAGIOUS(new ExChemical("contagious", EnumTier.TWO)),
    /* 10 */SONIC(new ExSonic("sonic", EnumTier.TWO)),
    /* 11 */BREACHING(new ExBreaching()),
    /* 12 */REJUVENATION(new ExRejuvenation()),
    /* 13 */THERMOBARIC(new ExNuclear("thermobaric", EnumTier.TWO)),
    /* 14 */SMINE(new ExSMine("sMine", EnumTier.TWO)),

    /* 15 */NUCLEAR(new ExNuclear("nuclear", EnumTier.THREE)),
    /* 16 */EMP(new ExEMP()),
    /* 17 */EXOTHERMIC(new ExExothermic()),
    /* 18 */ENDOTHERMIC(new ExEndothermic()),
    /* 19 */ANTI_GRAV(new ExAntiGravitational()),
    /* 20 */ENDER(new ExEnder()),
    /* 21 */HYPERSONIC(new ExSonic("hypersonic", EnumTier.THREE)), //TODO find Missile model

    /* 22 */ANTIMATTER(new ExAntimatter()),
    /* 23 */REDMATTER(new ExRedMatter()),

    /* 24 */MISSILE(new MissileModule()),
    /* 25 */MISSILE_HOMING(new MissileHoming()),
    /* 26 */MISSILE_ANTI(new MissileAnti()),
    /* 27 */MISSILE_CLUSTER(new MissileCluster("cluster", EnumTier.TWO)),
    /* 28 */MISSILE_CLUSTER_NUKE(new MissileNuclearCluster());

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

    @Override
    public String getName()
    {
        return super.name().toLowerCase();
    }

    public static List<Explosives> getBlocksOnly()
    {
        List<Explosives> list = new ArrayList();
        for (Explosives ex : values())
        {
            if (ex.handler.hasBlockForm())
            {
                list.add(ex);
            }
        }
        return list;
    }
}

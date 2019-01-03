package icbm.classic.content.explosive;

import icbm.classic.ICBMClassic;
import icbm.classic.api.explosion.IBlastFactory;
import icbm.classic.content.explosive.blast.BlastShrapnel;
import icbm.classic.content.explosive.blast.BlastTNT;
import icbm.classic.content.explosive.blast.threaded.BlastNuclear;
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
    /* 0  */CONDENSED(new Explosion("condensed", EnumTier.ONE, () -> new BlastTNT().setBlastSize(6)).setFuseTime(1)),
    /* 1  */SHRAPNEL("shrapnel", EnumTier.ONE, () -> new BlastShrapnel().setFlaming().setBlastSize(30)),
    /* 2  */INCENDIARY(new ExIncendiary("incendiary", EnumTier.ONE)),
    /* 3  */DEBLITATION(new ExDebilitation("debilitation", EnumTier.ONE)),
    /* 4  */CHEMICAL(new ExChemical("chemical", EnumTier.ONE)),
    /* 5  */ANVIL("anvil", EnumTier.ONE, () -> new BlastShrapnel().setAnvil().setBlastSize(25)),
    /* 6  */REPLUSIVE(new ExRepulsive("repulsive", EnumTier.ONE)),
    /* 7  */ATTRACTIVE(new ExRepulsive("attractive", EnumTier.ONE)),

    /* 8  */FRAGMENTATION("fragmentation", EnumTier.TWO, () -> new BlastShrapnel().setFlaming().setExplosive().setBlastSize(15)),
    /* 9  */CONTAGIOUS(new ExChemical("contagious", EnumTier.TWO)),
    /* 10 */SONIC(new ExSonic("sonic", EnumTier.TWO)),
    /* 11 */BREACHING(new ExBreaching()),
    /* 12 */REJUVENATION(new ExRejuvenation()),
    /* 13 */THERMOBARIC("thermobaric", EnumTier.TWO, () -> new BlastNuclear().setEnergy(45).setBlastSize(30)),
    /* 14 */SMINE(new ExSMine("sMine", EnumTier.TWO)),

    /* 15 */NUCLEAR("nuclear", EnumTier.THREE, () -> new BlastNuclear().setNuclear().setEnergy(80).setBlastSize(50)),
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
    /* 28 */MISSILE_CLUSTER_NUKE(new MissileNuclearCluster()),


    /* 29 */XMAS_ZOMBIE(new ExXMAS(true)),
    /* 30 */XMAS_SKELETON(new ExXMAS(false));

    public final Explosive handler;

    Explosives(Explosive handler)
    {
        this.handler = handler;
    }

    Explosives(String name, EnumTier tier, IBlastFactory factory)
    {
        this(new Explosion(name, tier, factory));
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

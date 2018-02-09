package icbm.classic.content.explosive;

import icbm.classic.ICBMClassic;
import icbm.classic.content.explosive.ex.*;
import icbm.classic.content.explosive.ex.missiles.*;
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
    CONDENSED(new ExCondensed("condensed", EnumTier.ONE)),
    SHRAPNEL(new ExShrapnel("shrapnel", EnumTier.ONE)),
    INCENDIARY(new ExIncendiary("incendiary", EnumTier.ONE)),
    DEBLITATION(new ExDebilitation("debilitation", EnumTier.ONE)),
    CHEMICAL(new ExChemical("chemical", EnumTier.ONE)),
    ANVIL(new ExShrapnel("anvil", EnumTier.ONE)),
    REPLUSIVE(new ExRepulsive("repulsive", EnumTier.ONE)),
    ATTRACTIVE(new ExRepulsive("attractive", EnumTier.ONE)),

    FRAGMENTATION(new ExShrapnel("fragmentation", EnumTier.TWO)),
    CONTAGIOUS(new ExChemical("contagious", EnumTier.TWO)),
    SONIC(new ExSonic("sonic", EnumTier.TWO)),
    BREACHING(new ExBreaching()),
    REJUVENATION(new ExRejuvenation()),
    THERMOBARIC(new ExNuclear("thermobaric", EnumTier.TWO)),
    SMINE(new ExSMine("sMine", EnumTier.TWO)),

    NUCLEAR(new ExNuclear("nuclear", EnumTier.THREE)),
    EMP(new ExEMP()),
    EXOTHERMIC(new ExExothermic()),
    ENDOTHERMIC(new ExEndothermic()),
    ANTI_GRAV(new ExAntiGravitational()),
    ENDER(new ExEnder()),
    HYPERSONIC(new ExSonic("hypersonic", EnumTier.THREE)), //TODO find Missile model

    ANTIMATTER(new ExAntimatter()),
    REDMATTER(new ExRedMatter()),

    MISSILE(new MissileModule()),
    MISSILE_HOMING(new MissileHoming()),
    MISSILE_ANTI(new MissileAnti()),
    MISSILE_CLUSTER(new MissileCluster("cluster", EnumTier.TWO)),
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

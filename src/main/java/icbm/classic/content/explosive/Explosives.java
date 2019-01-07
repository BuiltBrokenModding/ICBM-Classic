package icbm.classic.content.explosive;

import icbm.classic.ICBMClassic;
import icbm.classic.api.explosion.IBlastFactory;
import icbm.classic.config.ConfigBlast;
import icbm.classic.content.explosive.blast.*;
import icbm.classic.content.explosive.blast.threaded.BlastAntimatter;
import icbm.classic.content.explosive.blast.threaded.BlastNuclear;
import icbm.classic.content.explosive.handlers.*;
import icbm.classic.content.explosive.handlers.missiles.*;
import icbm.classic.api.EnumTier;
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
public enum Explosives
{
    /* 0  */CONDENSED(new Explosion("condensed", EnumTier.ONE,
            (nbt) -> new BlastTNT().setBlastSize(6)).setFuseTime(1)), //TODO convert fully

    /* 1  */SHRAPNEL("shrapnel", EnumTier.ONE,
            (nbt) -> new BlastShrapnel().setFlaming().setBlastSize(30)),

    /* 2  */INCENDIARY(new ExIncendiary("incendiary", EnumTier.ONE)), //TODO convert

    /* 3  */DEBLITATION("debilitation", EnumTier.ONE,
            (nbt) -> new BlastChemical(20 * 30, false)
                    .setConfuse().setBlastSize(20)),

    /* 4  */CHEMICAL("chemical", EnumTier.ONE,
            (nbt) -> new BlastChemical(20 * 30, false)
                    .setPoison().setRGB(0.8f, 0.8f, 0).setBlastSize(20)),

    /* 5  */ANVIL("anvil", EnumTier.ONE,
            (nbt) -> new BlastShrapnel().setAnvil().setBlastSize(25)),

    /* 6  */REPLUSIVE("repulsive", EnumTier.ONE,
            (nbt) -> new BlastTNT().setDestroyItems().setPushType(2).setBlastSize(2)), //TODO .setFuseTime(120)

    /* 7  */ATTRACTIVE("attractive", EnumTier.ONE,
            (nbt) -> new BlastTNT().setDestroyItems().setPushType(1).setBlastSize(2)), //TODO .setFuseTime(120)

    //=================== Tier 2
    /* 8  */FRAGMENTATION("fragmentation", EnumTier.TWO,
            (nbt) -> new BlastShrapnel().setFlaming().setExplosive().setBlastSize(15)),

    /* 9  */CONTAGIOUS("contagious", EnumTier.TWO,
            (nbt) -> new BlastChemical(20 * 30, false)
                    .setContagious().setRGB(0.3f, 0.8f, 0).setBlastSize(20)),

    /* 10 */SONIC("sonic", EnumTier.TWO,
            (nbt) -> new BlastSonic(30).setBlastSize(15)),

    /* 11 */BREACHING("breaching", EnumTier.TWO,
            (nbt) -> new BlastBreech(7).setBlastSize(2.5)), //TODO this.setFuseTime(40);

    /* 12 */REJUVENATION("rejuvenation", EnumTier.TWO,
            (nbt) -> new BlastRegen().setBlastSize(16)),

    /* 13 */THERMOBARIC("thermobaric", EnumTier.TWO,
            (nbt) -> new BlastNuclear().setEnergy(45).setBlastSize(30)),

    /* 14 */SMINE(new ExSMine("sMine", EnumTier.TWO)), //TODO convert, replace model with JSON and custom entity

    //=================== Tier 3
    /* 15 */NUCLEAR("nuclear", EnumTier.THREE,
            (nbt) -> new BlastNuclear().setNuclear().setEnergy(80).setBlastSize(50)),

    /* 16 */EMP("emp", EnumTier.THREE,
            (nbt) -> new BlastEMP().setEffectBlocks().setEffectEntities().setBlastSize(50)),

    /* 17 */EXOTHERMIC(new ExExothermic()), //TODO convert

    /* 18 */ENDOTHERMIC("endothermic", EnumTier.THREE,
            (nbt) -> new BlastEndothermic().setBlastSize(50)), //TODO add custom fuze, exo has one but not endo

    /* 19 */ANTI_GRAV("antiGravitational", EnumTier.THREE,
            (nbt) -> new BlastAntiGravitational().setBlastSize(30)),

    /* 20 */ENDER(new ExEnder()), //TODO convert, will need event handling... honestly should make a custom block/item for it

    /* 21 */HYPERSONIC("hypersonic", EnumTier.THREE,
            (nbt) -> new BlastSonic(35).setShockWave().setBlastSize(20)), //TODO find Missile model

    //=================== Tier 4
    /* 22 */ANTIMATTER("antimatter", EnumTier.FOUR,
            (nbt) -> new BlastAntimatter(ConfigBlast.ANTIMATTER_DESTROY_UNBREAKABLE_BLOCKS).setBlastSize(ConfigBlast.ANTIMATTER_SIZE)),
    //TODO this.setFuseTime(300);
    //TODO add config (disable by default) for alarm audio

    /* 23 */REDMATTER("redMatter", EnumTier.FOUR,
            (nbt) -> new BlastRedmatter().setBlastSize(BlastRedmatter.NORMAL_RADIUS)),


    //=================== Missiles
    /* 24 */MISSILE(new MissileModule()),
    /* 25 */MISSILE_HOMING(new MissileHoming()),
    /* 26 */MISSILE_ANTI(new MissileAnti()),
    /* 27 */MISSILE_CLUSTER(new MissileCluster("cluster", EnumTier.TWO)),
    /* 28 */MISSILE_CLUSTER_NUKE(new MissileNuclearCluster()),

    //=================== Special
    /* 29 */XMAS_ZOMBIE("xmas.zombie", EnumTier.TWO,
            (nbt) -> new BlastXmas(true).setBlastSize(5)),

    /* 30 */XMAS_SKELETON("xmas.skeleton", EnumTier.TWO,
            (nbt) -> new BlastXmas(false).setBlastSize(5));

    //TODO use a datafixer to flatten missiles into (ex, homing, anti, cluster)
    //https://github.com/RS485/LogisticsPipes/blob/dev-mc1122/common/logisticspipes/datafixer/DataFixerSolidBlockItems.java
    //https://github.com/RS485/LogisticsPipes/blob/dev-mc1122/common/logisticspipes/datafixer/LPDataFixer.java#L17-L19

    public final Explosive handler;

    Explosives(Explosive handler)
    {
        this.handler = handler;
    }

    Explosives(String name, EnumTier tier, IBlastFactory factory)
    {
        this(new Explosion(name, tier, factory));
    }

}

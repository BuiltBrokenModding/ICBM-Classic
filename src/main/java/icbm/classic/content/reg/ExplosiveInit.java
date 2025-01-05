package icbm.classic.content.reg;

import icbm.classic.ICBMConstants;
import icbm.classic.api.ICBMClassicAPI;
import icbm.classic.api.actions.data.ActionFields;
import icbm.classic.api.explosion.IBlastFactory;
import icbm.classic.api.refs.ICBMExplosives;
import icbm.classic.api.reg.IExplosiveData;
import icbm.classic.config.blast.ConfigBlast;
import icbm.classic.content.actions.emp.ActionDataEmpArea;
import icbm.classic.content.actions.entity.ActionSpawnEntity;
import icbm.classic.content.blast.*;
import icbm.classic.content.blast.BlastTNT.PushType;
import icbm.classic.content.blast.ender.BlastEnder;
import icbm.classic.content.blast.gas.BlastChemical;
import icbm.classic.content.blast.gas.BlastColor;
import icbm.classic.content.blast.gas.BlastDebilitation;
import icbm.classic.content.blast.gas.BlastContagious;
import icbm.classic.content.blast.redmatter.ActionSpawnRedmatter;
import icbm.classic.content.blast.threaded.BlastAntimatter;
import icbm.classic.content.blast.threaded.BlastNuclear;
import icbm.classic.lib.explosive.reg.ExplosiveRegistry;
import net.minecraft.entity.Entity;
import net.minecraft.util.*;
import net.minecraft.util.math.Vec3d;

/**
 * Created by Dark(DarkGuardsman, Robin) on 1/7/19.
 */
public class ExplosiveInit
{
    public static void init()
    {
        //=================== Tier 1
        ICBMExplosives.CONDENSED = newEx( "condensed",  (w, x, y, z, s) -> new BlastTNT()
            .setDamageToEntities(ConfigBlast.condensed.damage)
            .setBlastWorld(w).setBlastPosition(x, y, z)
            .setBlastSize(ConfigBlast.condensed.energyScale));

        ICBMExplosives.SHRAPNEL = newEx("shrapnel", (w, x, y, z, s) -> new BlastShrapnel()
            .setProjectile((world) -> {
                final Entity fragments = EntityReg.SHRAPNEL_GENERIC.get().create(world);
                fragments.setFire(100);
                return fragments;
            })
            .setBlastWorld(w).setBlastPosition(x, y, z)
            .setBlastSize(ConfigBlast.shrapnel.fragments));

        ICBMExplosives.INCENDIARY = newEx("incendiary", (w, x, y, z, s) -> new BlastFire().setBlastWorld(w).setBlastPosition(x, y, z).setBlastSize(ConfigBlast.incendiary.scale));

        ICBMExplosives.DEBILITATION = newEx( "debilitation",
                (w, x, y, z, s) -> new BlastDebilitation()
                    .setDuration(ConfigBlast.debilitation.duration)
                    .setBlastWorld(w).setBlastPosition(x, y, z)
                    .setBlastSize(ConfigBlast.debilitation.size)
        );

        ICBMExplosives.CHEMICAL = newEx( "chemical",
                (w, x, y, z, s) -> new BlastChemical()
                    .setToxicityBuildup(ConfigBlast.chemical.toxicityBuildup)
                    .setToxicityScale(ConfigBlast.chemical.toxicityScale)
                    .setToxicityMinDamage(ConfigBlast.chemical.toxicityMinDamage)
                    .setDuration(ConfigBlast.chemical.duration)
                    .setBlastWorld(w).setBlastPosition(x, y, z)
                    .setBlastSize(ConfigBlast.chemical.size)
        );

        ICBMExplosives.ANVIL = newEx("anvil",
                (w, x, y, z, s) -> new BlastShrapnel()
                    .setProjectile((world) -> EntityReg.ANVIL.get().create(world))
                    .setBlastWorld(w).setBlastPosition(x, y, z)
                    .setBlastSize(ConfigBlast.anvil.fragments));

        ICBMExplosives.REPULSIVE = newEx( "repulsive",
                (w, x, y, z, s) -> new BlastTNT().setDestroyItems().setPushType(PushType.REPEL).setBlastSize(ConfigBlast.repulsive.scale).setBlastWorld(w).setBlastPosition(x, y, z));

        ICBMExplosives.ATTRACTIVE = newEx( "attractive",
                (w, x, y, z, s) -> new BlastTNT().setDestroyItems().setPushType(PushType.ATTRACT).setBlastSize(ConfigBlast.attractive.scale).setBlastWorld(w).setBlastPosition(x, y, z));

        //=================== Tier 2
        ICBMExplosives.FRAGMENTATION = newEx( "fragmentation",
                (w, x, y, z, s) -> new BlastShrapnel()
                    .setProjectile((world) -> EntityReg.EXPLOSIVE_FRAGMENT.get().create(world))
                    .setBlastSize(ConfigBlast.fragmentation.fragments)
                    .setBlastWorld(w).setBlastPosition(x, y, z));

        ICBMExplosives.CONTAGIOUS = newEx("contagious",
                (w, x, y, z, s) -> new BlastContagious()
                    .setToxicityScale(ConfigBlast.contagious.toxicityScale)
                    .setToxicityBuildup(ConfigBlast.contagious.toxicityBuildup)
                    .setToxicityMinDamage(ConfigBlast.contagious.toxicityMinDamage)
                    .setDuration(ConfigBlast.contagious.duration)
                    .setBlastSize(ConfigBlast.contagious.size)
                    .setBlastWorld(w).setBlastPosition(x, y, z)
        );

        ICBMExplosives.SONIC = newEx( "sonic",
                (w, x, y, z, s) -> new BlastSonic().setBlastSize(ConfigBlast.sonic.scale).setBlastWorld(w).setBlastPosition(x, y, z));


        ICBMExplosives.BREACHING = newEx( "breaching",
                (w, x, y, z, s) -> new BlastBreach()
                    .setDepth(ConfigBlast.breaching.depth)
                    .setWidth(ConfigBlast.breaching.size)
                    .setEnergy(ConfigBlast.breaching.energy)
                    .setEnergyDistanceScale(ConfigBlast.breaching.energyDistanceScale)
                    .setEnergyCostDistance(ConfigBlast.breaching.energyCostDistance)
                    .setDamageToEntities(ConfigBlast.breaching.damage)
                    .setBlastWorld(w).setBlastPosition(x, y, z)
        );

        //12 -> Regen

        ICBMExplosives.THERMOBARIC = newEx("thermobaric",
                (w, x, y, z, s) -> new BlastNuclear().setEnergy(45).setBlastSize(ConfigBlast.thermobaric.scale).setBlastWorld(w).setBlastPosition(x, y, z));

        //14 -> S-Mine

        //=================== Tier 3
        ICBMExplosives.NUCLEAR = newEx( "nuclear",
                (w, x, y, z, s) -> new BlastNuclear().setEnergy(ConfigBlast.nuclear.energy).setBlastSize(ConfigBlast.nuclear.scale).setBlastWorld(w).setBlastPosition(x, y, z));


        ICBMExplosives.EMP = newEx( "emp", (w, x, y, z, s) -> ActionDataEmpArea.INSTANCE.create(w, x, y, z, s, null));


        ICBMExplosives.EXOTHERMIC = newEx("exothermic", (w, x, y, z, s) -> new BlastExothermic().setBlastSize(ConfigBlast.exothermic.scale).setBlastWorld(w).setBlastPosition(x, y, z));


        ICBMExplosives.ENDOTHERMIC = newEx("endothermic", (w, x, y, z, s) -> new BlastEndothermic().setBlastSize(ConfigBlast.endothermic.scale).setBlastWorld(w).setBlastPosition(x, y, z));


        ICBMExplosives.GRAVITY = newEx("antigravitational", (w, x, y, z, s) -> new BlastAntiGravitational().setBlastSize(ConfigBlast.antigravitational.scale).setBlastWorld(w).setBlastPosition(x, y, z));


        ICBMExplosives.ENDER = newEx("ender", (w, x, y, z, s) -> new BlastEnder().setBlastSize(ConfigBlast.ender.scale).setBlastWorld(w).setBlastPosition(x, y, z));

        //=================== Tier 4
        ICBMExplosives.ANTIMATTER = newEx("antimatter",
                (w, x, y, z, s) -> new BlastAntimatter()
                    .setBlastSize(ConfigBlast.antimatter.size)
                    .setBlastWorld(w).setBlastPosition(x, y, z)
        );

        ICBMExplosives.REDMATTER = newEx("redMatter", (w, x, y, z, s) -> new ActionSpawnRedmatter(w, new Vec3d(x, y, z), s, ICBMExplosives.REDMATTER));

        //=================== No content, only blast

        ICBMExplosives.ROT = newEx("rot", (w, x, y, z, s) -> new BlastRadioactiveBlockSwaps().setBlastWorld(w).setBlastPosition(x, y, z)); //TODO add item version
        ICBMExplosives.MUTATION = newEx("mutation", (w, x, y, z, s) -> new BlastMutation().setBlastWorld(w).setBlastPosition(x, y, z)); //TODO add item version

        //=================== New Explosives not part of classic original
        ICBMExplosives.COLOR = newEx("colors", (w, x, y, z, s) -> new BlastColor().setBlastSize(ConfigBlast.colorful.scale).setBlastWorld(w).setBlastPosition(x, y, z));
        ICBMExplosives.SMOKE = newEx("smoke", (w, x, y, z, s) -> {
            final ActionSpawnEntity actionSpawnEntity = new ActionSpawnEntity(w, new Vec3d(x, y, z), s, ICBMExplosives.SMOKE);
            actionSpawnEntity.setValue(ActionFields.ENTITY_REG_NAME, EntityReg.MARKING_SMOKE.getId());
            return actionSpawnEntity;
        });
    }

    private static IExplosiveData newEx(String name, IBlastFactory factory)
    {
        return ICBMClassicAPI.EXPLOSIVE_REGISTRY.register(new ResourceLocation(ICBMConstants.DOMAIN, name), factory);
    }
}

package icbm.classic.content.reg;

import icbm.classic.ICBMConstants;
import icbm.classic.api.EnumTier;
import icbm.classic.api.ICBMClassicAPI;
import icbm.classic.api.ICBMClassicHelpers;
import icbm.classic.api.actions.data.ActionFields;
import icbm.classic.api.caps.IExplosive;
import icbm.classic.api.caps.IGPSData;
import icbm.classic.api.explosion.IBlastFactory;
import icbm.classic.api.refs.ICBMEntities;
import icbm.classic.api.refs.ICBMExplosives;
import icbm.classic.api.reg.IExplosiveData;
import icbm.classic.config.blast.ConfigBlast;
import icbm.classic.content.actions.emp.ActionDataEmpArea;
import icbm.classic.content.actions.entity.ActionSpawnEntity;
import icbm.classic.content.blast.*;
import icbm.classic.content.blast.BlastTNT.PushType;
import icbm.classic.content.blast.ender.BlastEnder;
import icbm.classic.content.blast.ender.EnderBlastCustomization;
import icbm.classic.content.blast.gas.BlastChemical;
import icbm.classic.content.blast.gas.BlastColor;
import icbm.classic.content.blast.gas.BlastDebilitation;
import icbm.classic.content.blast.gas.BlastContagious;
import icbm.classic.content.blast.redmatter.ActionSpawnRedmatter;
import icbm.classic.content.blast.threaded.BlastAntimatter;
import icbm.classic.content.blast.threaded.BlastNuclear;
import icbm.classic.content.blast.threaded.BlastThermobaric;
import icbm.classic.content.entity.EntityFragments;
import icbm.classic.lib.LanguageUtility;
import icbm.classic.lib.explosive.reg.ExplosiveRegistry;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;

/**
 * Created by Dark(DarkGuardsman, Robin) on 1/7/19.
 */
public class ExplosiveInit
{
    //the fuse suppliers all return 100 (default in ExBlockContentReg#getFuseTime) when an unknown enum value is given as a type
    public static void init()
    {
        //=================== Tier 1
        ICBMExplosives.CONDENSED = newEx(0, "condensed", EnumTier.ONE, (w, x, y, z, s) -> new BlastTNT()
            .setDamageToEntities(ConfigBlast.condensed.damage)
            .setBlastWorld(w).setBlastPosition(x, y, z)
            .setBlastSize(ConfigBlast.condensed.energyScale));
        ICBMClassicAPI.EX_BLOCK_REGISTRY.setFuseSupplier(ICBMExplosives.CONDENSED.getRegistryKey(), (world, x, y, z) -> ConfigBlast.FUSE_TIMES.EXPLOSIVES.CONDENSED);
        ICBMClassicAPI.EX_GRENADE_REGISTRY.setFuseSupplier(ICBMExplosives.CONDENSED.getRegistryKey(), (entity) -> ConfigBlast.FUSE_TIMES.GRENADES.CONVENTIONAL);
        ICBMClassicAPI.EX_MINECART_REGISTRY.setFuseSupplier(ICBMExplosives.CONDENSED.getRegistryKey(), (entity) -> ConfigBlast.FUSE_TIMES.BOMB_CARTS.CONDENSED);


        ICBMExplosives.SHRAPNEL = newEx(1, "shrapnel", EnumTier.ONE, (w, x, y, z, s) -> new BlastShrapnel()
            .setProjectile((world) -> {
                final EntityFragments fragments = new EntityFragments(world);
                fragments.setArrowCritical(true);
                fragments.setFire(100);
                fragments.setDamage(ConfigBlast.shrapnel.damage);
                return fragments;
            })
            .setBlastWorld(w).setBlastPosition(x, y, z)
            .setBlastSize(ConfigBlast.shrapnel.fragments));
        ICBMClassicAPI.EX_BLOCK_REGISTRY.setFuseSupplier(ICBMExplosives.SHRAPNEL.getRegistryKey(), (world, x, y, z) -> ConfigBlast.FUSE_TIMES.EXPLOSIVES.SHRAPNEL);
        ICBMClassicAPI.EX_GRENADE_REGISTRY.setFuseSupplier(ICBMExplosives.SHRAPNEL.getRegistryKey(), (entity) -> ConfigBlast.FUSE_TIMES.GRENADES.SHRAPNEL);
        ICBMClassicAPI.EX_MINECART_REGISTRY.setFuseSupplier(ICBMExplosives.SHRAPNEL.getRegistryKey(), (entity) -> ConfigBlast.FUSE_TIMES.BOMB_CARTS.SHRAPNEL);


        ICBMExplosives.INCENDIARY = newEx(2, "incendiary", EnumTier.ONE, (w, x, y, z, s) -> new BlastFire().setBlastWorld(w).setBlastPosition(x, y, z).setBlastSize(ConfigBlast.incendiary.scale));
        ICBMClassicAPI.EX_BLOCK_REGISTRY.setFuseTickListener(ICBMExplosives.INCENDIARY.getRegistryKey(),
                (world, x, y, z, tick) -> world.spawnParticle(EnumParticleTypes.LAVA, x, y + 0.5D, z, 0.0D, 0.0D, 0.0D)
                );
        ICBMClassicAPI.EX_BLOCK_REGISTRY.setFuseSupplier(ICBMExplosives.INCENDIARY.getRegistryKey(), (world, x, y, z) -> ConfigBlast.FUSE_TIMES.EXPLOSIVES.INCENDIARY);
        ICBMClassicAPI.EX_GRENADE_REGISTRY.setFuseSupplier(ICBMExplosives.INCENDIARY.getRegistryKey(), (entity) -> ConfigBlast.FUSE_TIMES.GRENADES.INCENDIARY);
        ICBMClassicAPI.EX_MINECART_REGISTRY.setFuseSupplier(ICBMExplosives.INCENDIARY.getRegistryKey(), (entity) -> ConfigBlast.FUSE_TIMES.BOMB_CARTS.INCENDIARY);


        ICBMExplosives.DEBILITATION = newEx(3, "debilitation", EnumTier.ONE,
                (w, x, y, z, s) -> new BlastDebilitation()
                    .setDuration(ConfigBlast.debilitation.duration)
                    .setBlastWorld(w).setBlastPosition(x, y, z)
                    .setBlastSize(ConfigBlast.debilitation.size)
        );

        ICBMClassicAPI.EX_BLOCK_REGISTRY.setFuseSupplier(ICBMExplosives.DEBILITATION.getRegistryKey(), (world, x, y, z) -> ConfigBlast.FUSE_TIMES.EXPLOSIVES.DEBILITATION);
        ICBMClassicAPI.EX_GRENADE_REGISTRY.setFuseSupplier(ICBMExplosives.DEBILITATION.getRegistryKey(), (entity) -> ConfigBlast.FUSE_TIMES.GRENADES.DEBILITATION);
        ICBMClassicAPI.EX_MINECART_REGISTRY.setFuseSupplier(ICBMExplosives.DEBILITATION.getRegistryKey(), (entity) -> ConfigBlast.FUSE_TIMES.BOMB_CARTS.DEBILITATION);


        ICBMExplosives.CHEMICAL = newEx(4, "chemical", EnumTier.ONE,
                (w, x, y, z, s) -> new BlastChemical()
                    .setToxicityBuildup(ConfigBlast.chemical.toxicityBuildup)
                    .setToxicityScale(ConfigBlast.chemical.toxicityScale)
                    .setToxicityMinDamage(ConfigBlast.chemical.toxicityMinDamage)
                    .setDuration(ConfigBlast.chemical.duration)
                    .setBlastWorld(w).setBlastPosition(x, y, z)
                    .setBlastSize(ConfigBlast.chemical.size)
        );

        ICBMClassicAPI.EX_BLOCK_REGISTRY.setFuseSupplier(ICBMExplosives.CHEMICAL.getRegistryKey(), (world, x, y, z) -> ConfigBlast.FUSE_TIMES.EXPLOSIVES.CHEMICAL);
        ICBMClassicAPI.EX_GRENADE_REGISTRY.setFuseSupplier(ICBMExplosives.CHEMICAL.getRegistryKey(), (entity) -> ConfigBlast.FUSE_TIMES.GRENADES.CHEMICAL);
        ICBMClassicAPI.EX_MINECART_REGISTRY.setFuseSupplier(ICBMExplosives.CHEMICAL.getRegistryKey(), (entity) -> ConfigBlast.FUSE_TIMES.BOMB_CARTS.CHEMICAL);


        ICBMExplosives.ANVIL = newEx(5, "anvil", EnumTier.ONE,
                (w, x, y, z, s) -> new BlastShrapnel()
                    .setProjectile((world) -> {
                        final EntityFragments fragments = new EntityFragments(world);
                        fragments.setAnvil(true);
                        fragments.setDamage(ConfigBlast.anvil.damage);
                        return fragments;
                    })
                    .setBlastWorld(w).setBlastPosition(x, y, z)
                    .setBlastSize(ConfigBlast.anvil.fragments));

        ICBMClassicAPI.EX_BLOCK_REGISTRY.setFuseSupplier(ICBMExplosives.ANVIL.getRegistryKey(), (world, x, y, z) -> ConfigBlast.FUSE_TIMES.EXPLOSIVES.ANVIL);
        ICBMClassicAPI.EX_GRENADE_REGISTRY.setFuseSupplier(ICBMExplosives.ANVIL.getRegistryKey(), (entity) -> ConfigBlast.FUSE_TIMES.GRENADES.ANVIL);
        ICBMClassicAPI.EX_MINECART_REGISTRY.setFuseSupplier(ICBMExplosives.ANVIL.getRegistryKey(), (entity) -> ConfigBlast.FUSE_TIMES.BOMB_CARTS.ANVIL);


        ICBMExplosives.REPULSIVE = newEx(6, "repulsive", EnumTier.ONE,
                (w, x, y, z, s) -> new BlastTNT().setDestroyItems().setPushType(PushType.REPEL).setBlastSize(ConfigBlast.repulsive.scale).setBlastWorld(w).setBlastPosition(x, y, z));

        ICBMClassicAPI.EX_BLOCK_REGISTRY.setFuseSupplier(ICBMExplosives.REPULSIVE.getRegistryKey(), (world, x, y, z) -> ConfigBlast.FUSE_TIMES.EXPLOSIVES.REPULSIVE);
        ICBMClassicAPI.EX_GRENADE_REGISTRY.setFuseSupplier(ICBMExplosives.REPULSIVE.getRegistryKey(), (entity) -> ConfigBlast.FUSE_TIMES.GRENADES.REPULSIVE);
        ICBMClassicAPI.EX_MINECART_REGISTRY.setFuseSupplier(ICBMExplosives.REPULSIVE.getRegistryKey(), (entity) -> ConfigBlast.FUSE_TIMES.BOMB_CARTS.REPULSIVE);


        ICBMExplosives.ATTRACTIVE = newEx(7, "attractive", EnumTier.ONE,
                (w, x, y, z, s) -> new BlastTNT().setDestroyItems().setPushType(PushType.ATTRACT).setBlastSize(ConfigBlast.attractive.scale).setBlastWorld(w).setBlastPosition(x, y, z));

        ICBMClassicAPI.EX_BLOCK_REGISTRY.setFuseSupplier(ICBMExplosives.ATTRACTIVE.getRegistryKey(), (world, x, y, z) -> ConfigBlast.FUSE_TIMES.EXPLOSIVES.ATTRACTIVE);
        ICBMClassicAPI.EX_GRENADE_REGISTRY.setFuseSupplier(ICBMExplosives.ATTRACTIVE.getRegistryKey(), (entity) -> ConfigBlast.FUSE_TIMES.GRENADES.ATTRACTIVE);
        ICBMClassicAPI.EX_MINECART_REGISTRY.setFuseSupplier(ICBMExplosives.ATTRACTIVE.getRegistryKey(), (entity) -> ConfigBlast.FUSE_TIMES.BOMB_CARTS.ATTRACTIVE);


        //=================== Tier 2
        ICBMExplosives.FRAGMENTATION = newEx(8, "fragmentation", EnumTier.TWO,
                (w, x, y, z, s) -> new BlastShrapnel()
                    .setProjectile((world) -> {
                        final EntityFragments fragments =  new EntityFragments(world);
                        fragments.setExplosive(true);
                        fragments.setDamage(ConfigBlast.fragmentation.damage);
                        fragments.explosionSize = ConfigBlast.fragmentation.explosionSize;
                        return fragments;
                    })
                    .setBlastSize(ConfigBlast.fragmentation.fragments)
                    .setBlastWorld(w).setBlastPosition(x, y, z));
        ICBMClassicAPI.EX_BLOCK_REGISTRY.setFuseSupplier(ICBMExplosives.FRAGMENTATION.getRegistryKey(), (world, x, y, z) -> ConfigBlast.FUSE_TIMES.EXPLOSIVES.FRAGMENTATION);
        ICBMClassicAPI.EX_MINECART_REGISTRY.setFuseSupplier(ICBMExplosives.FRAGMENTATION.getRegistryKey(), (entity) -> ConfigBlast.FUSE_TIMES.BOMB_CARTS.FRAGMENTATION);
        //TODO add fire version of fragmentation with smaller animated flames

        ICBMExplosives.CONTAGIOUS = newEx(9, "contagious", EnumTier.TWO,
                (w, x, y, z, s) -> new BlastContagious()
                    .setToxicityScale(ConfigBlast.contagious.toxicityScale)
                    .setToxicityBuildup(ConfigBlast.contagious.toxicityBuildup)
                    .setToxicityMinDamage(ConfigBlast.contagious.toxicityMinDamage)
                    .setDuration(ConfigBlast.contagious.duration)
                    .setBlastSize(ConfigBlast.contagious.size)
                    .setBlastWorld(w).setBlastPosition(x, y, z)
        );
        ICBMClassicAPI.EX_BLOCK_REGISTRY.setFuseSupplier(ICBMExplosives.CONTAGIOUS.getRegistryKey(), (world, x, y, z) -> ConfigBlast.FUSE_TIMES.EXPLOSIVES.CONTAGIOUS);
        ICBMClassicAPI.EX_MINECART_REGISTRY.setFuseSupplier(ICBMExplosives.CONTAGIOUS.getRegistryKey(), (entity) -> ConfigBlast.FUSE_TIMES.BOMB_CARTS.CONTAGIOUS);


        ICBMExplosives.SONIC = newEx(10, "sonic", EnumTier.TWO,
                (w, x, y, z, s) -> new BlastSonic().setBlastSize(ConfigBlast.sonic.scale).setBlastWorld(w).setBlastPosition(x, y, z));
        ICBMClassicAPI.EX_BLOCK_REGISTRY.setFuseSupplier(ICBMExplosives.SONIC.getRegistryKey(), (world, x, y, z) -> ConfigBlast.FUSE_TIMES.EXPLOSIVES.SONIC);
        ICBMClassicAPI.EX_MINECART_REGISTRY.setFuseSupplier(ICBMExplosives.SONIC.getRegistryKey(), (entity) -> ConfigBlast.FUSE_TIMES.BOMB_CARTS.SONIC);


        ICBMExplosives.BREACHING = newEx(11, "breaching", EnumTier.TWO,
                (w, x, y, z, s) -> new BlastBreach()
                    .setDepth(ConfigBlast.breaching.depth)
                    .setWidth(ConfigBlast.breaching.size)
                    .setEnergy(ConfigBlast.breaching.energy)
                    .setEnergyDistanceScale(ConfigBlast.breaching.energyDistanceScale)
                    .setEnergyCostDistance(ConfigBlast.breaching.energyCostDistance)
                    .setDamageToEntities(ConfigBlast.breaching.damage)
                    .setBlastWorld(w).setBlastPosition(x, y, z)
        );
        ICBMClassicAPI.EX_BLOCK_REGISTRY.setFuseSupplier(ICBMExplosives.BREACHING.getRegistryKey(), (world, x, y, z) -> ConfigBlast.FUSE_TIMES.EXPLOSIVES.BREACHING);
        ICBMClassicAPI.EX_MINECART_REGISTRY.setFuseSupplier(ICBMExplosives.BREACHING.getRegistryKey(), (entity) -> ConfigBlast.FUSE_TIMES.BOMB_CARTS.BREACHING);


        //12 -> Regen

        ICBMExplosives.THERMOBARIC = newEx(13, "thermobaric", EnumTier.TWO,
                (w, x, y, z, s) -> new BlastThermobaric().setEnergy(ConfigBlast.thermobaric.energy).setBlastSize(ConfigBlast.thermobaric.scale).setBlastWorld(w).setBlastPosition(x, y, z));
        ICBMClassicAPI.EX_BLOCK_REGISTRY.setFuseSupplier(ICBMExplosives.THERMOBARIC.getRegistryKey(), (world, x, y, z) -> ConfigBlast.FUSE_TIMES.EXPLOSIVES.THERMOBARIC);
        ICBMClassicAPI.EX_MINECART_REGISTRY.setFuseSupplier(ICBMExplosives.THERMOBARIC.getRegistryKey(), (entity) -> ConfigBlast.FUSE_TIMES.BOMB_CARTS.THERMOBARIC);

        //14 -> S-Mine

        //=================== Tier 3
        ICBMExplosives.NUCLEAR = newEx(15, "nuclear", EnumTier.THREE,
                (w, x, y, z, s) -> new BlastNuclear().setEnergy(ConfigBlast.nuclear.energy).setBlastSize(ConfigBlast.nuclear.scale).setBlastWorld(w).setBlastPosition(x, y, z));
        ICBMClassicAPI.EX_BLOCK_REGISTRY.setFuseSupplier(ICBMExplosives.NUCLEAR.getRegistryKey(), (world, x, y, z) -> ConfigBlast.FUSE_TIMES.EXPLOSIVES.NUCLEAR);
        ICBMClassicAPI.EX_MINECART_REGISTRY.setFuseSupplier(ICBMExplosives.NUCLEAR.getRegistryKey(), (entity) -> ConfigBlast.FUSE_TIMES.BOMB_CARTS.NUCLEAR);


        ICBMExplosives.EMP = newEx(16, "emp", EnumTier.THREE, (w, x, y, z, s) -> ActionDataEmpArea.INSTANCE.create(w, x, y, z, s, null));
        ICBMClassicAPI.EX_BLOCK_REGISTRY.setFuseSupplier(ICBMExplosives.EMP.getRegistryKey(), (world, x, y, z) -> ConfigBlast.FUSE_TIMES.EXPLOSIVES.EMP);
        ICBMClassicAPI.EX_MINECART_REGISTRY.setFuseSupplier(ICBMExplosives.EMP.getRegistryKey(), (entity) -> ConfigBlast.FUSE_TIMES.BOMB_CARTS.EMP);


        ICBMExplosives.EXOTHERMIC = newEx(17, "exothermic", EnumTier.THREE, (w, x, y, z, s) -> new BlastExothermic().setBlastSize(ConfigBlast.exothermic.scale).setBlastWorld(w).setBlastPosition(x, y, z));
        ICBMClassicAPI.EX_BLOCK_REGISTRY.setFuseTickListener(ICBMExplosives.EXOTHERMIC.getRegistryKey(),
                (world, x, y, z, tick) -> world.spawnParticle(EnumParticleTypes.LAVA, x, y + 0.5D, z, 0.0D, 0.0D, 0.0D));
        ICBMClassicAPI.EX_BLOCK_REGISTRY.setFuseSupplier(ICBMExplosives.EXOTHERMIC.getRegistryKey(), (world, x, y, z) -> ConfigBlast.FUSE_TIMES.EXPLOSIVES.EXOTHERMIC);
        ICBMClassicAPI.EX_MINECART_REGISTRY.setFuseSupplier(ICBMExplosives.EXOTHERMIC.getRegistryKey(), (entity) -> ConfigBlast.FUSE_TIMES.BOMB_CARTS.EXOTHERMIC);


        ICBMExplosives.ENDOTHERMIC = newEx(18, "endothermic", EnumTier.THREE, (w, x, y, z, s) -> new BlastEndothermic().setBlastSize(ConfigBlast.endothermic.scale).setBlastWorld(w).setBlastPosition(x, y, z));
        //TODO add ice fuse animation
        ICBMClassicAPI.EX_BLOCK_REGISTRY.setFuseSupplier(ICBMExplosives.ENDOTHERMIC.getRegistryKey(), (world, x, y, z) -> ConfigBlast.FUSE_TIMES.EXPLOSIVES.ENDOTHERMIC);
        ICBMClassicAPI.EX_MINECART_REGISTRY.setFuseSupplier(ICBMExplosives.ENDOTHERMIC.getRegistryKey(), (entity) -> ConfigBlast.FUSE_TIMES.BOMB_CARTS.ENDOTHERMIC);


        ICBMExplosives.ANTI_GRAVITATIONAL = newEx(19, "antigravitational", EnumTier.THREE, (w, x, y, z, s) -> new BlastAntiGravitational().setBlastSize(ConfigBlast.antigravitational.scale).setBlastWorld(w).setBlastPosition(x, y, z));
        ICBMClassicAPI.EX_BLOCK_REGISTRY.setFuseSupplier(ICBMExplosives.ANTI_GRAVITATIONAL.getRegistryKey(), (world, x, y, z) -> ConfigBlast.FUSE_TIMES.EXPLOSIVES.ANTI_GRAVITATIONAL);
        ICBMClassicAPI.EX_MINECART_REGISTRY.setFuseSupplier(ICBMExplosives.ANTI_GRAVITATIONAL.getRegistryKey(), (entity) -> ConfigBlast.FUSE_TIMES.BOMB_CARTS.ANTI_GRAVITATIONAL);


        ICBMExplosives.ENDER = newEx(20, "ender", EnumTier.THREE, (w, x, y, z, s) -> new BlastEnder().setBlastSize(ConfigBlast.ender.scale).setBlastWorld(w).setBlastPosition(x, y, z));
        ICBMClassicAPI.EX_MISSILE_REGISTRY.setInteractionListener(ICBMExplosives.ENDER.getRegistryKey(), ExplosiveInit::enderMissileCoordSet);
        ICBMClassicAPI.EX_BLOCK_REGISTRY.setActivationListener(ICBMExplosives.ENDER.getRegistryKey(), ExplosiveInit::enderBlockCoordSet);
        ICBMClassicAPI.EX_BLOCK_REGISTRY.setFuseSupplier(ICBMExplosives.ENDER.getRegistryKey(), (world, x, y, z) -> ConfigBlast.FUSE_TIMES.EXPLOSIVES.ENDER);
        ICBMClassicAPI.EX_MINECART_REGISTRY.setFuseSupplier(ICBMExplosives.ENDER.getRegistryKey(), (entity) -> ConfigBlast.FUSE_TIMES.BOMB_CARTS.ENDER);

        // Hypersonic was removed in 4.3.0 and is kept as a numeric placeholder as a hardcoded id until next MC update
        ICBMExplosives.HYPERSONIC = newEx(21, "hypersonic", EnumTier.NONE, (w, x, y, z, s) -> new BlastTNT().setBlastSize(0).setBlastWorld(w).setBlastPosition(x, y, z));

        //=================== Tier 4
        ICBMExplosives.ANTIMATTER = newEx(22, "antimatter", EnumTier.FOUR,
                (w, x, y, z, s) -> new BlastAntimatter()
                    .setBlastSize(ConfigBlast.antimatter.size)
                    .setBlastWorld(w).setBlastPosition(x, y, z)
        );
        ICBMClassicAPI.EX_BLOCK_REGISTRY.setFuseSupplier(ICBMExplosives.ANTIMATTER.getRegistryKey(), (world, x, y, z) -> ConfigBlast.FUSE_TIMES.EXPLOSIVES.ANTIMATTER);
        ICBMClassicAPI.EX_MINECART_REGISTRY.setFuseSupplier(ICBMExplosives.ANTIMATTER.getRegistryKey(), (entity) -> ConfigBlast.FUSE_TIMES.BOMB_CARTS.ANTIMATTER);

        //TODO add config (disable by default) for alarm audio

        ICBMExplosives.REDMATTER = newEx(23, "redMatter", EnumTier.FOUR, (w, x, y, z, s) -> new ActionSpawnRedmatter(w, new Vec3d(x, y, z), s, ICBMExplosives.REDMATTER));
        ICBMClassicAPI.EX_BLOCK_REGISTRY.setFuseSupplier(ICBMExplosives.REDMATTER.getRegistryKey(), (world, x, y, z) -> ConfigBlast.FUSE_TIMES.EXPLOSIVES.REDMATTER);
        ICBMClassicAPI.EX_MINECART_REGISTRY.setFuseSupplier(ICBMExplosives.REDMATTER.getRegistryKey(), (entity) -> ConfigBlast.FUSE_TIMES.BOMB_CARTS.REDMATTER);

        //=================== No content, only blast

        // originally "mutation" had the id 24, but to keep metadata consistent, the missile module now has metadata 24
        ICBMExplosives.MISSILEMODULE = newEx(24, "missile",  EnumTier.NONE, (w, x, y, z, s) -> new BlastTNT().setBlastSize(0).setBlastWorld(w).setBlastPosition(x, y, z)); //TODO remove need

        ICBMExplosives.ROT = newEx(25, "rot", EnumTier.NONE, (w, x, y, z, s) -> new BlastRadioactiveBlockSwaps().setBlastWorld(w).setBlastPosition(x, y, z)); //TODO add item version
        ICBMExplosives.MUTATION = newEx(26, "mutation", EnumTier.NONE, (w, x, y, z, s) -> new BlastMutation().setBlastWorld(w).setBlastPosition(x, y, z)); //TODO add item version

        //=================== New Explosives not part of classic original
        ICBMExplosives.COLOR = newEx(-1, "colors", EnumTier.ONE, (w, x, y, z, s) -> new BlastColor().setBlastSize(ConfigBlast.colorful.scale).setBlastWorld(w).setBlastPosition(x, y, z));
        ICBMExplosives.SMOKE = newEx(-1, "smoke", EnumTier.ONE, (w, x, y, z, s) -> {
            final ActionSpawnEntity actionSpawnEntity = new ActionSpawnEntity(w, new Vec3d(x, y, z), s, ICBMExplosives.SMOKE);
            actionSpawnEntity.setValue(ActionFields.ENTITY_REG_NAME, ICBMEntities.SMOKE);
            return actionSpawnEntity;
        });


        ((ExplosiveRegistry) ICBMClassicAPI.EXPLOSIVE_REGISTRY).lockForce();


        //=================== Missiles
        ///* 24 */MISSILE(new MissileModule());
        ///* 25 */MISSILE_HOMING(new MissileHoming());
        ///* 26 */MISSILE_ANTI(new MissileAnti());
        ///* 27 */MISSILE_CLUSTER(new MissileCluster("cluster", EnumTier.TWO));
        ///* 28 */MISSILE_CLUSTER_NUKE(new MissileNuclearCluster())
    }

    /**
     * @deprecated removing id in 1.13 and need to register content manually without tier... though could make this data driven?
     */
    @Deprecated
    private static IExplosiveData newEx(int id, String name, EnumTier tier, IBlastFactory factory)
    {
        final ResourceLocation regName = new ResourceLocation(ICBMConstants.DOMAIN, name);
        if (id != -1)
        {
            //Setup old IDs so saves work
            ((ExplosiveRegistry) ICBMClassicAPI.EXPLOSIVE_REGISTRY).forceID(regName, id);
        }
        final IExplosiveData data = ICBMClassicAPI.EXPLOSIVE_REGISTRY.register(regName, tier, factory);

        //Do default content types per explosive
        if(tier != EnumTier.NONE) {
            ICBMClassicAPI.EX_BLOCK_REGISTRY.enableContent(regName);
            ICBMClassicAPI.EX_MISSILE_REGISTRY.enableContent(regName);
            ICBMClassicAPI.EX_MINECART_REGISTRY.enableContent(regName);
        }
        if(tier == EnumTier.ONE) {
            ICBMClassicAPI.EX_GRENADE_REGISTRY.enableContent(regName);
        }

        return data;
    }

    private static boolean enderMissileCoordSet(Entity entity, EntityPlayer player, EnumHand hand)
    {
        if (entity.hasCapability(ICBMClassicAPI.EXPLOSIVE_CAPABILITY, null))
        {
            return encodeEnderCoordSet(entity.getCapability(ICBMClassicAPI.EXPLOSIVE_CAPABILITY, null), player, hand);
        }
        return false;
    }

    private static boolean enderBlockCoordSet(World world, BlockPos pos, EntityPlayer player, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitz)
    {
        final TileEntity tileEntity = world.getTileEntity(pos);

        if (tileEntity != null && tileEntity.hasCapability(ICBMClassicAPI.EXPLOSIVE_CAPABILITY, facing))
        {
            return encodeEnderCoordSet(tileEntity.getCapability(ICBMClassicAPI.EXPLOSIVE_CAPABILITY, null), player, hand);
        }

        return false;
    }

    private static boolean encodeEnderCoordSet(IExplosive provider, EntityPlayer player, EnumHand hand) {
        if(provider == null) {
            return false;
        }
        final ItemStack stack = player.getHeldItem(hand);
        final IGPSData gpsData = ICBMClassicHelpers.getGPSData(stack);
        if (gpsData != null)
        {
            final Vec3d position = gpsData.getPosition();
            if (position != null)
            {
                provider.addCustomization(new EnderBlastCustomization(gpsData.getWorldId(), position));
                player.sendMessage(new TextComponentString(LanguageUtility.getLocal("chat.launcher.toolTargetSet")));
            }
            else
            {
                player.sendMessage(new TextComponentString(LanguageUtility.getLocal("chat.launcher.noTargetInTool")));
            }
            return true;
        }
        return false;
    }
}

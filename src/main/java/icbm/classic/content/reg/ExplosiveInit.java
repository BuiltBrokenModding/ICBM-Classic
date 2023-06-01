package icbm.classic.content.reg;

import icbm.classic.ICBMConstants;
import icbm.classic.api.EnumTier;
import icbm.classic.api.ICBMClassicAPI;
import icbm.classic.api.ICBMClassicHelpers;
import icbm.classic.api.caps.IExplosive;
import icbm.classic.api.caps.IGPSData;
import icbm.classic.api.explosion.IBlastFactory;
import icbm.classic.api.refs.ICBMExplosives;
import icbm.classic.api.reg.IExplosiveData;
import icbm.classic.config.blast.ConfigBlast;
import icbm.classic.content.blast.*;
import icbm.classic.content.blast.BlastTNT.PushType;
import icbm.classic.content.blast.cluster.BlastCluster;
import icbm.classic.content.blast.ender.BlastEnder;
import icbm.classic.content.blast.ender.EnderBlastCustomization;
import icbm.classic.content.blast.gas.BlastChemical;
import icbm.classic.content.blast.gas.BlastColor;
import icbm.classic.content.blast.gas.BlastConfusion;
import icbm.classic.content.blast.gas.BlastContagious;
import icbm.classic.content.blast.redmatter.BlastRedmatterSpawner;
import icbm.classic.content.blast.threaded.BlastAntimatter;
import icbm.classic.content.blast.threaded.BlastNuclear;
import icbm.classic.lib.LanguageUtility;
import icbm.classic.lib.NBTConstants;
import icbm.classic.lib.explosive.reg.ExplosiveRegistry;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
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
 * Created by Dark(DarkGuardsman, Robert) on 1/7/19.
 */
public class ExplosiveInit
{
    //the fuse suppliers all return 100 (default in ExBlockContentReg#getFuseTime) when an unknown enum value is given as a type
    public static void init()
    {
        //=================== Tier 1
        ICBMExplosives.CONDENSED = newEx(0, "condensed", EnumTier.ONE, () -> new BlastTNT().setBlastSize(ConfigBlast.condensed.scale));
        ICBMClassicAPI.EX_BLOCK_REGISTRY.setFuseSupplier(ICBMExplosives.CONDENSED.getRegistryName(), (world, x, y, z) -> ConfigBlast.FUSE_TIMES.EXPLOSIVES.CONDENSED);
        ICBMClassicAPI.EX_GRENADE_REGISTRY.setFuseSupplier(ICBMExplosives.CONDENSED.getRegistryName(), (entity) -> ConfigBlast.FUSE_TIMES.GRENADES.CONVENTIONAL);
        ICBMClassicAPI.EX_MINECART_REGISTRY.setFuseSupplier(ICBMExplosives.CONDENSED.getRegistryName(), (entity) -> ConfigBlast.FUSE_TIMES.BOMB_CARTS.CONDENSED);


        ICBMExplosives.SHRAPNEL = newEx(1, "shrapnel", EnumTier.ONE, () -> new BlastShrapnel().setFlaming().setBlastSize(ConfigBlast.shrapnel.scale));
        ICBMClassicAPI.EX_BLOCK_REGISTRY.setFuseSupplier(ICBMExplosives.SHRAPNEL.getRegistryName(), (world, x, y, z) -> ConfigBlast.FUSE_TIMES.EXPLOSIVES.SHRAPNEL);
        ICBMClassicAPI.EX_GRENADE_REGISTRY.setFuseSupplier(ICBMExplosives.SHRAPNEL.getRegistryName(), (entity) -> ConfigBlast.FUSE_TIMES.GRENADES.SHRAPNEL);
        ICBMClassicAPI.EX_MINECART_REGISTRY.setFuseSupplier(ICBMExplosives.SHRAPNEL.getRegistryName(), (entity) -> ConfigBlast.FUSE_TIMES.BOMB_CARTS.SHRAPNEL);


        ICBMExplosives.INCENDIARY = newEx(2, "incendiary", EnumTier.ONE, () -> new BlastFire().setBlastSize(ConfigBlast.incendiary.scale));
        ICBMClassicAPI.EX_BLOCK_REGISTRY.setFuseTickListener(ICBMExplosives.INCENDIARY.getRegistryName(),
                (world, x, y, z, tick) -> world.spawnParticle(EnumParticleTypes.LAVA, x, y + 0.5D, z, 0.0D, 0.0D, 0.0D)
                );
        ICBMClassicAPI.EX_BLOCK_REGISTRY.setFuseSupplier(ICBMExplosives.INCENDIARY.getRegistryName(), (world, x, y, z) -> ConfigBlast.FUSE_TIMES.EXPLOSIVES.INCENDIARY);
        ICBMClassicAPI.EX_GRENADE_REGISTRY.setFuseSupplier(ICBMExplosives.INCENDIARY.getRegistryName(), (entity) -> ConfigBlast.FUSE_TIMES.GRENADES.INCENDIARY);
        ICBMClassicAPI.EX_MINECART_REGISTRY.setFuseSupplier(ICBMExplosives.INCENDIARY.getRegistryName(), (entity) -> ConfigBlast.FUSE_TIMES.BOMB_CARTS.INCENDIARY);


        ICBMExplosives.DEBILITATION = newEx(3, "debilitation", EnumTier.ONE,
                () -> new BlastConfusion().setBlastSize(ConfigBlast.debilitation.scale));

        ICBMClassicAPI.EX_BLOCK_REGISTRY.setFuseSupplier(ICBMExplosives.DEBILITATION.getRegistryName(), (world, x, y, z) -> ConfigBlast.FUSE_TIMES.EXPLOSIVES.DEBILITATION);
        ICBMClassicAPI.EX_GRENADE_REGISTRY.setFuseSupplier(ICBMExplosives.DEBILITATION.getRegistryName(), (entity) -> ConfigBlast.FUSE_TIMES.GRENADES.DEBILITATION);
        ICBMClassicAPI.EX_MINECART_REGISTRY.setFuseSupplier(ICBMExplosives.DEBILITATION.getRegistryName(), (entity) -> ConfigBlast.FUSE_TIMES.BOMB_CARTS.DEBILITATION);


        ICBMExplosives.CHEMICAL = newEx(4, "chemical", EnumTier.ONE,
                () -> new BlastChemical().setBlastSize(ConfigBlast.chemical.scale));

        ICBMClassicAPI.EX_BLOCK_REGISTRY.setFuseSupplier(ICBMExplosives.CHEMICAL.getRegistryName(), (world, x, y, z) -> ConfigBlast.FUSE_TIMES.EXPLOSIVES.CHEMICAL);
        ICBMClassicAPI.EX_GRENADE_REGISTRY.setFuseSupplier(ICBMExplosives.CHEMICAL.getRegistryName(), (entity) -> ConfigBlast.FUSE_TIMES.GRENADES.CHEMICAL);
        ICBMClassicAPI.EX_MINECART_REGISTRY.setFuseSupplier(ICBMExplosives.CHEMICAL.getRegistryName(), (entity) -> ConfigBlast.FUSE_TIMES.BOMB_CARTS.CHEMICAL);


        ICBMExplosives.ANVIL = newEx(5, "anvil", EnumTier.ONE,
                () -> new BlastShrapnel().setAnvil().setBlastSize(ConfigBlast.anvil.scale));

        ICBMClassicAPI.EX_BLOCK_REGISTRY.setFuseSupplier(ICBMExplosives.ANVIL.getRegistryName(), (world, x, y, z) -> ConfigBlast.FUSE_TIMES.EXPLOSIVES.ANVIL);
        ICBMClassicAPI.EX_GRENADE_REGISTRY.setFuseSupplier(ICBMExplosives.ANVIL.getRegistryName(), (entity) -> ConfigBlast.FUSE_TIMES.GRENADES.ANVIL);
        ICBMClassicAPI.EX_MINECART_REGISTRY.setFuseSupplier(ICBMExplosives.ANVIL.getRegistryName(), (entity) -> ConfigBlast.FUSE_TIMES.BOMB_CARTS.ANVIL);


        ICBMExplosives.REPULSIVE = newEx(6, "repulsive", EnumTier.ONE,
                () -> new BlastTNT().setDestroyItems().setPushType(PushType.REPEL).setBlastSize(ConfigBlast.repulsive.scale));

        ICBMClassicAPI.EX_BLOCK_REGISTRY.setFuseSupplier(ICBMExplosives.REPULSIVE.getRegistryName(), (world, x, y, z) -> ConfigBlast.FUSE_TIMES.EXPLOSIVES.REPULSIVE);
        ICBMClassicAPI.EX_GRENADE_REGISTRY.setFuseSupplier(ICBMExplosives.REPULSIVE.getRegistryName(), (entity) -> ConfigBlast.FUSE_TIMES.GRENADES.REPULSIVE);
        ICBMClassicAPI.EX_MINECART_REGISTRY.setFuseSupplier(ICBMExplosives.REPULSIVE.getRegistryName(), (entity) -> ConfigBlast.FUSE_TIMES.BOMB_CARTS.REPULSIVE);


        ICBMExplosives.ATTRACTIVE = newEx(7, "attractive", EnumTier.ONE,
                () -> new BlastTNT().setDestroyItems().setPushType(PushType.ATTRACT).setBlastSize(ConfigBlast.attractive.scale));

        ICBMClassicAPI.EX_BLOCK_REGISTRY.setFuseSupplier(ICBMExplosives.ATTRACTIVE.getRegistryName(), (world, x, y, z) -> ConfigBlast.FUSE_TIMES.EXPLOSIVES.ATTRACTIVE);
        ICBMClassicAPI.EX_GRENADE_REGISTRY.setFuseSupplier(ICBMExplosives.ATTRACTIVE.getRegistryName(), (entity) -> ConfigBlast.FUSE_TIMES.GRENADES.ATTRACTIVE);
        ICBMClassicAPI.EX_MINECART_REGISTRY.setFuseSupplier(ICBMExplosives.ATTRACTIVE.getRegistryName(), (entity) -> ConfigBlast.FUSE_TIMES.BOMB_CARTS.ATTRACTIVE);


        //=================== Tier 2
        ICBMExplosives.FRAGMENTATION = newEx(8, "fragmentation", EnumTier.TWO,
                () -> new BlastShrapnel().setExplosive().setBlastSize(ConfigBlast.fragmentation.scale));
        ICBMClassicAPI.EX_BLOCK_REGISTRY.setFuseSupplier(ICBMExplosives.FRAGMENTATION.getRegistryName(), (world, x, y, z) -> ConfigBlast.FUSE_TIMES.EXPLOSIVES.FRAGMENTATION);
        ICBMClassicAPI.EX_MINECART_REGISTRY.setFuseSupplier(ICBMExplosives.FRAGMENTATION.getRegistryName(), (entity) -> ConfigBlast.FUSE_TIMES.BOMB_CARTS.FRAGMENTATION);
        //TODO add fire version of fragmentation with smaller animated flames

        ICBMExplosives.CONTAGIOUS = newEx(9, "contagious", EnumTier.TWO,
                () -> new BlastContagious().setBlastSize(ConfigBlast.contagious.scale));
        ICBMClassicAPI.EX_BLOCK_REGISTRY.setFuseSupplier(ICBMExplosives.CONTAGIOUS.getRegistryName(), (world, x, y, z) -> ConfigBlast.FUSE_TIMES.EXPLOSIVES.CONTAGIOUS);
        ICBMClassicAPI.EX_MINECART_REGISTRY.setFuseSupplier(ICBMExplosives.CONTAGIOUS.getRegistryName(), (entity) -> ConfigBlast.FUSE_TIMES.BOMB_CARTS.CONTAGIOUS);


        ICBMExplosives.SONIC = newEx(10, "sonic", EnumTier.TWO,
                () -> new BlastSonic().setBlastSize(ConfigBlast.sonic.scale));
        ICBMClassicAPI.EX_BLOCK_REGISTRY.setFuseSupplier(ICBMExplosives.SONIC.getRegistryName(), (world, x, y, z) -> ConfigBlast.FUSE_TIMES.EXPLOSIVES.SONIC);
        ICBMClassicAPI.EX_MINECART_REGISTRY.setFuseSupplier(ICBMExplosives.SONIC.getRegistryName(), (entity) -> ConfigBlast.FUSE_TIMES.BOMB_CARTS.SONIC);


        ICBMExplosives.BREACHING = newEx(11, "breaching", EnumTier.TWO,
                () -> new BlastBreach(7).setBlastSize(ConfigBlast.breaching.scale));
        ICBMClassicAPI.EX_BLOCK_REGISTRY.setFuseSupplier(ICBMExplosives.BREACHING.getRegistryName(), (world, x, y, z) -> ConfigBlast.FUSE_TIMES.EXPLOSIVES.BREACHING);
        ICBMClassicAPI.EX_MINECART_REGISTRY.setFuseSupplier(ICBMExplosives.BREACHING.getRegistryName(), (entity) -> ConfigBlast.FUSE_TIMES.BOMB_CARTS.BREACHING);


        //12 -> Regen

        ICBMExplosives.THERMOBARIC = newEx(13, "thermobaric", EnumTier.TWO,
                () -> new BlastNuclear().setEnergy(45).setBlastSize(ConfigBlast.thermobaric.scale));
        ICBMClassicAPI.EX_BLOCK_REGISTRY.setFuseSupplier(ICBMExplosives.THERMOBARIC.getRegistryName(), (world, x, y, z) -> ConfigBlast.FUSE_TIMES.EXPLOSIVES.THERMOBARIC);
        ICBMClassicAPI.EX_MINECART_REGISTRY.setFuseSupplier(ICBMExplosives.THERMOBARIC.getRegistryName(), (entity) -> ConfigBlast.FUSE_TIMES.BOMB_CARTS.THERMOBARIC);

        //14 -> S-Mine

        //=================== Tier 3
        ICBMExplosives.NUCLEAR = newEx(15, "nuclear", EnumTier.THREE,
                () -> new BlastNuclear().setEnergy(ConfigBlast.nuclear.energy).setBlastSize(ConfigBlast.nuclear.scale));
        ICBMClassicAPI.EX_BLOCK_REGISTRY.setFuseSupplier(ICBMExplosives.NUCLEAR.getRegistryName(), (world, x, y, z) -> ConfigBlast.FUSE_TIMES.EXPLOSIVES.NUCLEAR);
        ICBMClassicAPI.EX_MINECART_REGISTRY.setFuseSupplier(ICBMExplosives.NUCLEAR.getRegistryName(), (entity) -> ConfigBlast.FUSE_TIMES.BOMB_CARTS.NUCLEAR);


        ICBMExplosives.EMP = newEx(16, "emp", EnumTier.THREE,
                () -> new BlastEMP().setEffectBlocks().setEffectEntities().setBlastSize(ConfigBlast.emp.scale));
        ICBMClassicAPI.EX_BLOCK_REGISTRY.setFuseSupplier(ICBMExplosives.EMP.getRegistryName(), (world, x, y, z) -> ConfigBlast.FUSE_TIMES.EXPLOSIVES.EMP);
        ICBMClassicAPI.EX_MINECART_REGISTRY.setFuseSupplier(ICBMExplosives.EMP.getRegistryName(), (entity) -> ConfigBlast.FUSE_TIMES.BOMB_CARTS.EMP);


        ICBMExplosives.EXOTHERMIC = newEx(17, "exothermic", EnumTier.THREE, () -> new BlastExothermic().setBlastSize(ConfigBlast.exothermic.scale));
        ICBMClassicAPI.EX_BLOCK_REGISTRY.setFuseTickListener(ICBMExplosives.EXOTHERMIC.getRegistryName(),
                (world, x, y, z, tick) -> world.spawnParticle(EnumParticleTypes.LAVA, x, y + 0.5D, z, 0.0D, 0.0D, 0.0D));
        ICBMClassicAPI.EX_BLOCK_REGISTRY.setFuseSupplier(ICBMExplosives.EXOTHERMIC.getRegistryName(), (world, x, y, z) -> ConfigBlast.FUSE_TIMES.EXPLOSIVES.EXOTHERMIC);
        ICBMClassicAPI.EX_MINECART_REGISTRY.setFuseSupplier(ICBMExplosives.EXOTHERMIC.getRegistryName(), (entity) -> ConfigBlast.FUSE_TIMES.BOMB_CARTS.EXOTHERMIC);


        ICBMExplosives.ENDOTHERMIC = newEx(18, "endothermic", EnumTier.THREE, () -> new BlastEndothermic().setBlastSize(ConfigBlast.endothermic.scale));
        //TODO add ice fuse animation
        ICBMClassicAPI.EX_BLOCK_REGISTRY.setFuseSupplier(ICBMExplosives.ENDOTHERMIC.getRegistryName(), (world, x, y, z) -> ConfigBlast.FUSE_TIMES.EXPLOSIVES.ENDOTHERMIC);
        ICBMClassicAPI.EX_MINECART_REGISTRY.setFuseSupplier(ICBMExplosives.ENDOTHERMIC.getRegistryName(), (entity) -> ConfigBlast.FUSE_TIMES.BOMB_CARTS.ENDOTHERMIC);


        ICBMExplosives.ANTI_GRAVITATIONAL = newEx(19, "antigravitational", EnumTier.THREE, () -> new BlastAntiGravitational().setBlastSize(ConfigBlast.antigravitational.scale));
        ICBMClassicAPI.EX_BLOCK_REGISTRY.setFuseSupplier(ICBMExplosives.ANTI_GRAVITATIONAL.getRegistryName(), (world, x, y, z) -> ConfigBlast.FUSE_TIMES.EXPLOSIVES.ANTI_GRAVITATIONAL);
        ICBMClassicAPI.EX_MINECART_REGISTRY.setFuseSupplier(ICBMExplosives.ANTI_GRAVITATIONAL.getRegistryName(), (entity) -> ConfigBlast.FUSE_TIMES.BOMB_CARTS.ANTI_GRAVITATIONAL);


        ICBMExplosives.ENDER = newEx(20, "ender", EnumTier.THREE, () -> new BlastEnder().setBlastSize(ConfigBlast.ender.scale));
        ICBMClassicAPI.EX_MISSILE_REGISTRY.setInteractionListener(ICBMExplosives.ENDER.getRegistryName(), ExplosiveInit::enderMissileCoordSet);
        ICBMClassicAPI.EX_BLOCK_REGISTRY.setActivationListener(ICBMExplosives.ENDER.getRegistryName(), ExplosiveInit::enderBlockCoordSet);
        ICBMClassicAPI.EX_BLOCK_REGISTRY.setFuseSupplier(ICBMExplosives.ENDER.getRegistryName(), (world, x, y, z) -> ConfigBlast.FUSE_TIMES.EXPLOSIVES.ENDER);
        ICBMClassicAPI.EX_MINECART_REGISTRY.setFuseSupplier(ICBMExplosives.ENDER.getRegistryName(), (entity) -> ConfigBlast.FUSE_TIMES.BOMB_CARTS.ENDER);

        // Hypersonic was removed in 4.3.0 and is kept as a numeric placeholder as a hardcoded id until next MC update
        ICBMExplosives.HYPERSONIC = newEx(21, "hypersonic", EnumTier.NONE, null);

        //=================== Tier 4
        ICBMExplosives.ANTIMATTER = newEx(22, "antimatter", EnumTier.FOUR,
                () -> new BlastAntimatter().setBlastSize(ConfigBlast.antimatter.scale));
        ICBMClassicAPI.EX_BLOCK_REGISTRY.setFuseSupplier(ICBMExplosives.ANTIMATTER.getRegistryName(), (world, x, y, z) -> ConfigBlast.FUSE_TIMES.EXPLOSIVES.ANTIMATTER);
        ICBMClassicAPI.EX_MINECART_REGISTRY.setFuseSupplier(ICBMExplosives.ANTIMATTER.getRegistryName(), (entity) -> ConfigBlast.FUSE_TIMES.BOMB_CARTS.ANTIMATTER);

        //TODO add config (disable by default) for alarm audio

        ICBMExplosives.REDMATTER = newEx(23, "redMatter", EnumTier.FOUR, () -> new BlastRedmatterSpawner().setBlastSize(ConfigBlast.redmatter.DEFAULT_SIZE));
        ICBMClassicAPI.EX_BLOCK_REGISTRY.setFuseSupplier(ICBMExplosives.REDMATTER.getRegistryName(), (world, x, y, z) -> ConfigBlast.FUSE_TIMES.EXPLOSIVES.REDMATTER);
        ICBMClassicAPI.EX_MINECART_REGISTRY.setFuseSupplier(ICBMExplosives.REDMATTER.getRegistryName(), (entity) -> ConfigBlast.FUSE_TIMES.BOMB_CARTS.REDMATTER);

        //=================== No content, only blast

        // originally "mutation" had the id 24, but to keep metadata consistent, the missile module now has metadata 24
        ICBMExplosives.MISSILEMODULE = newEx(24, "missile",  EnumTier.NONE, null); //TODO remove need

        ICBMExplosives.ROT = newEx(25, "rot", EnumTier.NONE, BlastRot::new); //TODO add item version
        ICBMExplosives.MUTATION = newEx(26, "mutation", EnumTier.NONE, BlastMutation::new); //TODO add item version

        //=================== New Explosives not part of classic original
        ICBMExplosives.COLOR = newEx(-1, "colors", EnumTier.ONE, () -> new BlastColor().setBlastSize(ConfigBlast.colorful.scale));
        ICBMExplosives.SMOKE = newEx(-1, "smoke", EnumTier.ONE, BlastSmoke::new); //TODO add scale for smoke count, and ticks alive as NBT var

        ICBMExplosives.CLUSTER = newEx(-1, "cluster", EnumTier.NONE, BlastCluster::new);
        ICBMClassicAPI.EX_MISSILE_REGISTRY.enableContent(ICBMExplosives.CLUSTER.getRegistryName());


        ((ExplosiveRegistry) ICBMClassicAPI.EXPLOSIVE_REGISTRY).lockForce();


        //=================== Missiles
        ///* 24 */MISSILE(new MissileModule());
        ///* 25 */MISSILE_HOMING(new MissileHoming());
        ///* 26 */MISSILE_ANTI(new MissileAnti());
        ///* 27 */MISSILE_CLUSTER(new MissileCluster("cluster", EnumTier.TWO));
        ///* 28 */MISSILE_CLUSTER_NUKE(new MissileNuclearCluster())
    }

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

    private static boolean enderBlockCoordSet(World world, BlockPos pos, EntityPlayer player, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ)
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

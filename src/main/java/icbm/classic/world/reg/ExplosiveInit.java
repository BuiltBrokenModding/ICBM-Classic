package icbm.classic.world.reg;

import icbm.classic.IcbmConstants;
import icbm.classic.api.ICBMClassicAPI;
import icbm.classic.api.ICBMClassicHelpers;
import icbm.classic.api.WeaponTier;
import icbm.classic.api.caps.IExplosive;
import icbm.classic.api.caps.IGPSData;
import icbm.classic.api.explosion.IBlastFactory;
import icbm.classic.api.refs.ICBMExplosives;
import icbm.classic.api.reg.ExplosiveType;
import icbm.classic.config.blast.ConfigBlast;
import icbm.classic.lib.NBTConstants;
import icbm.classic.lib.explosive.reg.ExplosiveRegistry;
import icbm.classic.world.blast.*;
import icbm.classic.world.blast.redmatter.BlastRedmatterSpawner;
import icbm.classic.world.blast.threaded.BlastAntimatter;
import icbm.classic.world.blast.threaded.BlastNuclear;
import icbm.classic.world.effect.gas.BlastChemical;
import icbm.classic.world.effect.gas.BlastColor;
import icbm.classic.world.effect.gas.BlastConfusion;
import icbm.classic.world.effect.gas.BlastContagious;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;

/**
 * Created by Dark(DarkGuardsman, Robert) on 1/7/19.
 */
public class ExplosiveInit {
    //the fuse suppliers all return 100 (default in ExBlockContentReg#getFuseTime) when an unknown enum value is given as a type
    public static void init() {
        //=================== Tier 1
        ICBMExplosives.CONDENSED = newEx(0, "condensed", WeaponTier.ONE, () -> new BlastTNT().setBlastSize(ConfigBlast.condensed.scale));
        ICBMClassicAPI.EX_BLOCK_REGISTRY.setFuseSupplier(ICBMExplosives.CONDENSED.getRegistryName(), (world, x, y, z) -> ConfigBlast.FUSE_TIMES.EXPLOSIVES.CONDENSED);
        ICBMClassicAPI.EX_GRENADE_REGISTRY.setFuseSupplier(ICBMExplosives.CONDENSED.getRegistryName(), (entity) -> ConfigBlast.FUSE_TIMES.GRENADES.CONVENTIONAL);
        ICBMClassicAPI.EX_MINECART_REGISTRY.setFuseSupplier(ICBMExplosives.CONDENSED.getRegistryName(), (entity) -> ConfigBlast.FUSE_TIMES.BOMB_CARTS.CONDENSED);


        ICBMExplosives.SHRAPNEL = newEx(1, "shrapnel", WeaponTier.ONE, () -> new BlastShrapnel().setFlaming().setBlastSize(ConfigBlast.shrapnel.scale));
        ICBMClassicAPI.EX_BLOCK_REGISTRY.setFuseSupplier(ICBMExplosives.SHRAPNEL.getRegistryName(), (world, x, y, z) -> ConfigBlast.FUSE_TIMES.EXPLOSIVES.SHRAPNEL);
        ICBMClassicAPI.EX_GRENADE_REGISTRY.setFuseSupplier(ICBMExplosives.SHRAPNEL.getRegistryName(), (entity) -> ConfigBlast.FUSE_TIMES.GRENADES.SHRAPNEL);
        ICBMClassicAPI.EX_MINECART_REGISTRY.setFuseSupplier(ICBMExplosives.SHRAPNEL.getRegistryName(), (entity) -> ConfigBlast.FUSE_TIMES.BOMB_CARTS.SHRAPNEL);


        ICBMExplosives.INCENDIARY = newEx(2, "incendiary", WeaponTier.ONE, () -> new BlastFire().setBlastSize(ConfigBlast.incendiary.scale));
        ICBMClassicAPI.EX_BLOCK_REGISTRY.setFuseTickListener(ICBMExplosives.INCENDIARY.getRegistryName(),
            (world, x, y, z, tick) -> world.addParticle(ParticleTypes.LAVA, x, y + 0.5D, z, 0.0D, 0.0D, 0.0D)
        );
        ICBMClassicAPI.EX_BLOCK_REGISTRY.setFuseSupplier(ICBMExplosives.INCENDIARY.getRegistryName(), (world, x, y, z) -> ConfigBlast.FUSE_TIMES.EXPLOSIVES.INCENDIARY);
        ICBMClassicAPI.EX_GRENADE_REGISTRY.setFuseSupplier(ICBMExplosives.INCENDIARY.getRegistryName(), (entity) -> ConfigBlast.FUSE_TIMES.GRENADES.INCENDIARY);
        ICBMClassicAPI.EX_MINECART_REGISTRY.setFuseSupplier(ICBMExplosives.INCENDIARY.getRegistryName(), (entity) -> ConfigBlast.FUSE_TIMES.BOMB_CARTS.INCENDIARY);


        ICBMExplosives.DEBILITATION = newEx(3, "debilitation", WeaponTier.ONE,
            () -> new BlastConfusion().setBlastSize(ConfigBlast.debilitation.scale));

        ICBMClassicAPI.EX_BLOCK_REGISTRY.setFuseSupplier(ICBMExplosives.DEBILITATION.getRegistryName(), (world, x, y, z) -> ConfigBlast.FUSE_TIMES.EXPLOSIVES.DEBILITATION);
        ICBMClassicAPI.EX_GRENADE_REGISTRY.setFuseSupplier(ICBMExplosives.DEBILITATION.getRegistryName(), (entity) -> ConfigBlast.FUSE_TIMES.GRENADES.DEBILITATION);
        ICBMClassicAPI.EX_MINECART_REGISTRY.setFuseSupplier(ICBMExplosives.DEBILITATION.getRegistryName(), (entity) -> ConfigBlast.FUSE_TIMES.BOMB_CARTS.DEBILITATION);


        ICBMExplosives.CHEMICAL = newEx(4, "chemical", WeaponTier.ONE,
            () -> new BlastChemical().setBlastSize(ConfigBlast.chemical.scale));

        ICBMClassicAPI.EX_BLOCK_REGISTRY.setFuseSupplier(ICBMExplosives.CHEMICAL.getRegistryName(), (world, x, y, z) -> ConfigBlast.FUSE_TIMES.EXPLOSIVES.CHEMICAL);
        ICBMClassicAPI.EX_GRENADE_REGISTRY.setFuseSupplier(ICBMExplosives.CHEMICAL.getRegistryName(), (entity) -> ConfigBlast.FUSE_TIMES.GRENADES.CHEMICAL);
        ICBMClassicAPI.EX_MINECART_REGISTRY.setFuseSupplier(ICBMExplosives.CHEMICAL.getRegistryName(), (entity) -> ConfigBlast.FUSE_TIMES.BOMB_CARTS.CHEMICAL);


        ICBMExplosives.ANVIL = newEx(5, "anvil", WeaponTier.ONE,
            () -> new BlastShrapnel().setAnvil().setBlastSize(ConfigBlast.anvil.scale));

        ICBMClassicAPI.EX_BLOCK_REGISTRY.setFuseSupplier(ICBMExplosives.ANVIL.getRegistryName(), (world, x, y, z) -> ConfigBlast.FUSE_TIMES.EXPLOSIVES.ANVIL);
        ICBMClassicAPI.EX_GRENADE_REGISTRY.setFuseSupplier(ICBMExplosives.ANVIL.getRegistryName(), (entity) -> ConfigBlast.FUSE_TIMES.GRENADES.ANVIL);
        ICBMClassicAPI.EX_MINECART_REGISTRY.setFuseSupplier(ICBMExplosives.ANVIL.getRegistryName(), (entity) -> ConfigBlast.FUSE_TIMES.BOMB_CARTS.ANVIL);


        ICBMExplosives.REPULSIVE = newEx(6, "repulsive", WeaponTier.ONE,
            () -> new BlastTNT().setDestroyItems().setPushType(BlastTNT.PushType.REPEL).setBlastSize(ConfigBlast.repulsive.scale));

        ICBMClassicAPI.EX_BLOCK_REGISTRY.setFuseSupplier(ICBMExplosives.REPULSIVE.getRegistryName(), (world, x, y, z) -> ConfigBlast.FUSE_TIMES.EXPLOSIVES.REPULSIVE);
        ICBMClassicAPI.EX_GRENADE_REGISTRY.setFuseSupplier(ICBMExplosives.REPULSIVE.getRegistryName(), (entity) -> ConfigBlast.FUSE_TIMES.GRENADES.REPULSIVE);
        ICBMClassicAPI.EX_MINECART_REGISTRY.setFuseSupplier(ICBMExplosives.REPULSIVE.getRegistryName(), (entity) -> ConfigBlast.FUSE_TIMES.BOMB_CARTS.REPULSIVE);


        ICBMExplosives.ATTRACTIVE = newEx(7, "attractive", WeaponTier.ONE,
            () -> new BlastTNT().setDestroyItems().setPushType(BlastTNT.PushType.ATTRACT).setBlastSize(ConfigBlast.attractive.scale));

        ICBMClassicAPI.EX_BLOCK_REGISTRY.setFuseSupplier(ICBMExplosives.ATTRACTIVE.getRegistryName(), (world, x, y, z) -> ConfigBlast.FUSE_TIMES.EXPLOSIVES.ATTRACTIVE);
        ICBMClassicAPI.EX_GRENADE_REGISTRY.setFuseSupplier(ICBMExplosives.ATTRACTIVE.getRegistryName(), (entity) -> ConfigBlast.FUSE_TIMES.GRENADES.ATTRACTIVE);
        ICBMClassicAPI.EX_MINECART_REGISTRY.setFuseSupplier(ICBMExplosives.ATTRACTIVE.getRegistryName(), (entity) -> ConfigBlast.FUSE_TIMES.BOMB_CARTS.ATTRACTIVE);


        //=================== Tier 2
        ICBMExplosives.FRAGMENTATION = newEx(8, "fragmentation", WeaponTier.TWO,
            () -> new BlastShrapnel().setExplosive().setBlastSize(ConfigBlast.fragmentation.scale));
        ICBMClassicAPI.EX_BLOCK_REGISTRY.setFuseSupplier(ICBMExplosives.FRAGMENTATION.getRegistryName(), (world, x, y, z) -> ConfigBlast.FUSE_TIMES.EXPLOSIVES.FRAGMENTATION);
        ICBMClassicAPI.EX_MINECART_REGISTRY.setFuseSupplier(ICBMExplosives.FRAGMENTATION.getRegistryName(), (entity) -> ConfigBlast.FUSE_TIMES.BOMB_CARTS.FRAGMENTATION);
        //TODO add fire version of fragmentation with smaller animated flames

        ICBMExplosives.CONTAGIOUS = newEx(9, "contagious", WeaponTier.TWO,
            () -> new BlastContagious().setBlastSize(ConfigBlast.contagious.scale));
        ICBMClassicAPI.EX_BLOCK_REGISTRY.setFuseSupplier(ICBMExplosives.CONTAGIOUS.getRegistryName(), (world, x, y, z) -> ConfigBlast.FUSE_TIMES.EXPLOSIVES.CONTAGIOUS);
        ICBMClassicAPI.EX_MINECART_REGISTRY.setFuseSupplier(ICBMExplosives.CONTAGIOUS.getRegistryName(), (entity) -> ConfigBlast.FUSE_TIMES.BOMB_CARTS.CONTAGIOUS);


        ICBMExplosives.SONIC = newEx(10, "sonic", WeaponTier.TWO,
            () -> new BlastSonic().setBlastSize(ConfigBlast.sonic.scale));
        ICBMClassicAPI.EX_BLOCK_REGISTRY.setFuseSupplier(ICBMExplosives.SONIC.getRegistryName(), (world, x, y, z) -> ConfigBlast.FUSE_TIMES.EXPLOSIVES.SONIC);
        ICBMClassicAPI.EX_MINECART_REGISTRY.setFuseSupplier(ICBMExplosives.SONIC.getRegistryName(), (entity) -> ConfigBlast.FUSE_TIMES.BOMB_CARTS.SONIC);


        ICBMExplosives.BREACHING = newEx(11, "breaching", WeaponTier.TWO,
            () -> new BlastBreach(7).setBlastSize(ConfigBlast.breaching.scale));
        ICBMClassicAPI.EX_BLOCK_REGISTRY.setFuseSupplier(ICBMExplosives.BREACHING.getRegistryName(), (world, x, y, z) -> ConfigBlast.FUSE_TIMES.EXPLOSIVES.BREACHING);
        ICBMClassicAPI.EX_MINECART_REGISTRY.setFuseSupplier(ICBMExplosives.BREACHING.getRegistryName(), (entity) -> ConfigBlast.FUSE_TIMES.BOMB_CARTS.BREACHING);


        //12 -> Regen

        ICBMExplosives.THERMOBARIC = newEx(13, "thermobaric", WeaponTier.TWO,
            () -> new BlastNuclear().setEnergy(45).setBlastSize(ConfigBlast.thermobaric.scale));
        ICBMClassicAPI.EX_BLOCK_REGISTRY.setFuseSupplier(ICBMExplosives.THERMOBARIC.getRegistryName(), (world, x, y, z) -> ConfigBlast.FUSE_TIMES.EXPLOSIVES.THERMOBARIC);
        ICBMClassicAPI.EX_MINECART_REGISTRY.setFuseSupplier(ICBMExplosives.THERMOBARIC.getRegistryName(), (entity) -> ConfigBlast.FUSE_TIMES.BOMB_CARTS.THERMOBARIC);

        //14 -> S-Mine

        //=================== Tier 3
        ICBMExplosives.NUCLEAR = newEx(15, "nuclear", WeaponTier.THREE,
            () -> new BlastNuclear().setEnergy(ConfigBlast.nuclear.energy).setBlastSize(ConfigBlast.nuclear.scale));
        ICBMClassicAPI.EX_BLOCK_REGISTRY.setFuseSupplier(ICBMExplosives.NUCLEAR.getRegistryName(), (world, x, y, z) -> ConfigBlast.FUSE_TIMES.EXPLOSIVES.NUCLEAR);
        ICBMClassicAPI.EX_MINECART_REGISTRY.setFuseSupplier(ICBMExplosives.NUCLEAR.getRegistryName(), (entity) -> ConfigBlast.FUSE_TIMES.BOMB_CARTS.NUCLEAR);


        ICBMExplosives.EMP = newEx(16, "emp", WeaponTier.THREE,
            () -> new BlastEMP().setEffectBlocks().setEffectEntities().setBlastSize(ConfigBlast.emp.scale));
        ICBMClassicAPI.EX_BLOCK_REGISTRY.setFuseSupplier(ICBMExplosives.EMP.getRegistryName(), (world, x, y, z) -> ConfigBlast.FUSE_TIMES.EXPLOSIVES.EMP);
        ICBMClassicAPI.EX_MINECART_REGISTRY.setFuseSupplier(ICBMExplosives.EMP.getRegistryName(), (entity) -> ConfigBlast.FUSE_TIMES.BOMB_CARTS.EMP);


        ICBMExplosives.EXOTHERMIC = newEx(17, "exothermic", WeaponTier.THREE, () -> new BlastExothermic().setBlastSize(ConfigBlast.exothermic.scale));
        ICBMClassicAPI.EX_BLOCK_REGISTRY.setFuseTickListener(ICBMExplosives.EXOTHERMIC.getRegistryName(),
            (level, x, y, z, tick) -> level.addParticle(ParticleTypes.LAVA, x, y + 0.5D, z, 0.0D, 0.0D, 0.0D));
        ICBMClassicAPI.EX_BLOCK_REGISTRY.setFuseSupplier(ICBMExplosives.EXOTHERMIC.getRegistryName(), (world, x, y, z) -> ConfigBlast.FUSE_TIMES.EXPLOSIVES.EXOTHERMIC);
        ICBMClassicAPI.EX_MINECART_REGISTRY.setFuseSupplier(ICBMExplosives.EXOTHERMIC.getRegistryName(), (entity) -> ConfigBlast.FUSE_TIMES.BOMB_CARTS.EXOTHERMIC);


        ICBMExplosives.ENDOTHERMIC = newEx(18, "endothermic", WeaponTier.THREE, () -> new BlastEndothermic().setBlastSize(ConfigBlast.endothermic.scale));
        //TODO add ice fuse animation
        ICBMClassicAPI.EX_BLOCK_REGISTRY.setFuseSupplier(ICBMExplosives.ENDOTHERMIC.getRegistryName(), (world, x, y, z) -> ConfigBlast.FUSE_TIMES.EXPLOSIVES.ENDOTHERMIC);
        ICBMClassicAPI.EX_MINECART_REGISTRY.setFuseSupplier(ICBMExplosives.ENDOTHERMIC.getRegistryName(), (entity) -> ConfigBlast.FUSE_TIMES.BOMB_CARTS.ENDOTHERMIC);


        ICBMExplosives.ANTI_GRAVITATIONAL = newEx(19, "antigravitational", WeaponTier.THREE, () -> new BlastAntiGravitational().setBlastSize(ConfigBlast.antigravitational.scale));
        ICBMClassicAPI.EX_BLOCK_REGISTRY.setFuseSupplier(ICBMExplosives.ANTI_GRAVITATIONAL.getRegistryName(), (world, x, y, z) -> ConfigBlast.FUSE_TIMES.EXPLOSIVES.ANTI_GRAVITATIONAL);
        ICBMClassicAPI.EX_MINECART_REGISTRY.setFuseSupplier(ICBMExplosives.ANTI_GRAVITATIONAL.getRegistryName(), (entity) -> ConfigBlast.FUSE_TIMES.BOMB_CARTS.ANTI_GRAVITATIONAL);


        ICBMExplosives.ENDER = newEx(20, "ender", WeaponTier.THREE, () -> new BlastEnderman().setBlastSize(ConfigBlast.ender.scale));
        ICBMClassicAPI.EX_MISSILE_REGISTRY.setInteractionListener(ICBMExplosives.ENDER.getRegistryName(), ExplosiveInit::enderMissileCoordSet);
        ICBMClassicAPI.EX_BLOCK_REGISTRY.setActivationListener(ICBMExplosives.ENDER.getRegistryName(), ExplosiveInit::enderBlockCoordSet);
        ICBMClassicAPI.EX_BLOCK_REGISTRY.setFuseSupplier(ICBMExplosives.ENDER.getRegistryName(), (world, x, y, z) -> ConfigBlast.FUSE_TIMES.EXPLOSIVES.ENDER);
        ICBMClassicAPI.EX_MINECART_REGISTRY.setFuseSupplier(ICBMExplosives.ENDER.getRegistryName(), (entity) -> ConfigBlast.FUSE_TIMES.BOMB_CARTS.ENDER);

        // Hypersonic was removed in 4.3.0 and is kept as a numeric placeholder as a hardcoded id until next MC update
        ICBMExplosives.HYPERSONIC = newEx(21, "hypersonic", WeaponTier.NONE, null);

        //=================== Tier 4
        ICBMExplosives.ANTIMATTER = newEx(22, "antimatter", WeaponTier.FOUR,
            () -> new BlastAntimatter().setBlastSize(ConfigBlast.antimatter.scale));
        ICBMClassicAPI.EX_BLOCK_REGISTRY.setFuseSupplier(ICBMExplosives.ANTIMATTER.getRegistryName(), (world, x, y, z) -> ConfigBlast.FUSE_TIMES.EXPLOSIVES.ANTIMATTER);
        ICBMClassicAPI.EX_MINECART_REGISTRY.setFuseSupplier(ICBMExplosives.ANTIMATTER.getRegistryName(), (entity) -> ConfigBlast.FUSE_TIMES.BOMB_CARTS.ANTIMATTER);

        //TODO add config (disable by default) for alarm audio

        ICBMExplosives.REDMATTER = newEx(23, "redMatter", WeaponTier.FOUR, () -> new BlastRedmatterSpawner().setBlastSize(ConfigBlast.redmatter.DEFAULT_SIZE));
        ICBMClassicAPI.EX_BLOCK_REGISTRY.setFuseSupplier(ICBMExplosives.REDMATTER.getRegistryName(), (world, x, y, z) -> ConfigBlast.FUSE_TIMES.EXPLOSIVES.REDMATTER);
        ICBMClassicAPI.EX_MINECART_REGISTRY.setFuseSupplier(ICBMExplosives.REDMATTER.getRegistryName(), (entity) -> ConfigBlast.FUSE_TIMES.BOMB_CARTS.REDMATTER);

        //=================== No content, only blast

        // originally "mutation" had the id 24, but to keep metadata consistent, the missile module now has metadata 24
        ICBMExplosives.MISSILEMODULE = newEx(24, "missile", WeaponTier.NONE, null);

        ICBMExplosives.ROT = newEx(25, "rot", WeaponTier.NONE, BlastRot::new);
        ICBMExplosives.MUTATION = newEx(26, "mutation", WeaponTier.NONE, BlastMutation::new);

        //New Explosives not part of classic original
        ICBMExplosives.COLOR = newEx(-1, "colors", WeaponTier.ONE, () -> new BlastColor().setBlastSize(ConfigBlast.colorful.scale));
        ICBMExplosives.SMOKE = newEx(-1, "smoke", WeaponTier.ONE, BlastSmoke::new); //TODO add scale for smoke count, and ticks alive as NBT var

        ((ExplosiveRegistry) ICBMClassicAPI.EXPLOSIVE_REGISTRY).lockForce();

        //=================== Missiles
        ///* 24 */MISSILE(new MissileModule());
        ///* 25 */MISSILE_HOMING(new MissileHoming());
        ///* 26 */MISSILE_ANTI(new MissileAnti());
        ///* 27 */MISSILE_CLUSTER(new MissileCluster("cluster", EnumTier.TWO));
        ///* 28 */MISSILE_CLUSTER_NUKE(new MissileNuclearCluster())
    }

    private static ExplosiveType newEx(int id, String name, WeaponTier tier, IBlastFactory factory) {
        final ResourceLocation regName = new ResourceLocation(IcbmConstants.MOD_ID, name);
        if (id != -1) {
            //Setup old IDs so saves work
            ((ExplosiveRegistry) ICBMClassicAPI.EXPLOSIVE_REGISTRY).forceID(regName, id);
        }
        final ExplosiveType data = ICBMClassicAPI.EXPLOSIVE_REGISTRY.register(regName, tier, factory);

        //Do default content types per explosive
        if (tier != WeaponTier.NONE) {
            ICBMClassicAPI.EX_BLOCK_REGISTRY.enableContent(regName);
            ICBMClassicAPI.EX_MISSILE_REGISTRY.enableContent(regName);
            ICBMClassicAPI.EX_MINECART_REGISTRY.enableContent(regName);
        }
        if (tier == WeaponTier.ONE) {
            ICBMClassicAPI.EX_GRENADE_REGISTRY.enableContent(regName);
        }

        return data;
    }

    private static boolean enderMissileCoordSet(Entity entity, Player player, InteractionHand hand) {
        if (entity.getCapability(ICBMClassicAPI.EXPLOSIVE_CAPABILITY, null)) {
            return encodeEnderCoordSet(entity.getCapability(ICBMClassicAPI.EXPLOSIVE_CAPABILITY, null), player, hand);
        }
        return false;
    }

    private static boolean enderBlockCoordSet(Level level, BlockPos pos, Player player, InteractionHand hand, Direction direction, float hitX, float hitY, float hitZ) {
        final BlockEntity blockEntity = level.getBlockEntity(pos);

        IExplosive explosive;
        if (blockEntity != null && blockEntity.hasCapability(ICBMClassicAPI.EXPLOSIVE_CAPABILITY, direction)) {
            return encodeEnderCoordSet(blockEntity.getCapability(ICBMClassicAPI.EXPLOSIVE_CAPABILITY, null), player, hand);
        }

        return false;
    }

    private static boolean encodeEnderCoordSet(IExplosive provider, Player player, InteractionHand hand) {
        if (provider == null || provider.getCustomBlastData() == null) {
            return false;
        }
        CompoundTag tag = provider.getCustomBlastData();
        ItemStack stack = player.getItemInHand(hand);
        IGPSData gpsData = ICBMClassicHelpers.getGPSData(stack);
        if (gpsData != null) {
            final Vec3 position = gpsData.getPosition();
            if (position != null) {
                tag.putInt(NBTConstants.X, (int) Math.floor(position.x));
                tag.putInt(NBTConstants.Y, (int) Math.floor(position.y));
                tag.putInt(NBTConstants.Z, (int) Math.floor(position.z));
                player.sendSystemMessage(Component.translatable("chat.launcher.toolTargetSet"));
            } else {
                player.sendSystemMessage(Component.translatable("chat.launcher.noTargetInTool"));
            }
            return true;
        }
        return false;
    }
}

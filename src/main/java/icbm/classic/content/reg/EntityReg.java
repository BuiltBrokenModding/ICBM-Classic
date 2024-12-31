package icbm.classic.content.reg;

import icbm.classic.ICBMClassic;
import icbm.classic.ICBMConstants;
import icbm.classic.api.actions.IActionData;
import icbm.classic.api.refs.ICBMEntities;
import icbm.classic.api.refs.ICBMExplosives;
import icbm.classic.config.missile.ConfigMissile;
import icbm.classic.content.blast.redmatter.EntityRedmatter;
import icbm.classic.content.blocks.BlockSpikes;
import icbm.classic.content.cargo.balloon.EntityBalloon;
import icbm.classic.content.cargo.parachute.EntityParachute;
import icbm.classic.content.cluster.bomblet.EntityBombDroplet;
import icbm.classic.content.entity.*;
import icbm.classic.content.entity.flyingblock.EntityFlyingBlock;
import icbm.classic.content.items.ItemMissile;
import icbm.classic.content.missile.entity.anti.EntitySurfaceToAirMissile;
import icbm.classic.content.missile.entity.explosive.EntityExplosiveMissile;
import icbm.classic.content.missile.entity.explosive.EntityMissileActionable;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.EntityType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.util.NonNullSupplier;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

/**
 * Created by Dark(DarkGuardsman, Robin) on 1/7/19.
 */
@Mod.EventBusSubscriber(modid = ICBMConstants.DOMAIN)
public final class EntityReg
{

    public static final DeferredRegister<EntityType<?>> ENTITIES = new DeferredRegister<>(ForgeRegistries.ENTITIES, ICBMConstants.DOMAIN);

    // <editor-fold desc="missiles">
    // TODO make health per missile in the configs
    public static final RegistryObject<EntityType<EntityExplosiveMissile>> MISSILE_CONDENSED = explosiveMissile(
        "missile_explosive_condensed", ICBMExplosives.CONDENSED,
        () -> (float) ConfigMissile.TIER_1_HEALTH, () -> new ItemStack(ItemReg.MISSILE_CONDENSED.get()));

    public static final RegistryObject<EntityType<EntityExplosiveMissile>> MISSILE_SHRAPNEL = explosiveMissile(
        "missile_explosive_shrapnel",  ICBMExplosives.SHRAPNEL,
        () -> (float) ConfigMissile.TIER_1_HEALTH, () -> new ItemStack(ItemReg.MISSILE_SHRAPNEL.get()));

    public static final RegistryObject<EntityType<EntityExplosiveMissile>> MISSILE_INCENDIARY = explosiveMissile(
        "missile_explosive_incendiary", ICBMExplosives.INCENDIARY,
        () -> (float) ConfigMissile.TIER_1_HEALTH, () -> new ItemStack(ItemReg.MISSILE_INCENDIARY.get()));

    public static final RegistryObject<EntityType<EntityExplosiveMissile>> MISSILE_DEBILITATION = explosiveMissile(
        "missile_explosive_debilitation",  ICBMExplosives.DEBILITATION,
        () -> (float) ConfigMissile.TIER_1_HEALTH, () -> new ItemStack(ItemReg.MISSILE_DEBILITATION.get()));

    public static final RegistryObject<EntityType<EntityExplosiveMissile>> MISSILE_CHEMICAL = explosiveMissile(
        "missile_explosive_chemical",  ICBMExplosives.CHEMICAL,
        () -> (float) ConfigMissile.TIER_1_HEALTH, () -> new ItemStack(ItemReg.MISSILE_CHEMICAL.get()));

    public static final RegistryObject<EntityType<EntityExplosiveMissile>> MISSILE_ANVIL = explosiveMissile(
        "missile_explosive_anvil",  ICBMExplosives.ANVIL,
        () -> (float) ConfigMissile.TIER_1_HEALTH, () -> new ItemStack(ItemReg.MISSILE_ANVIL.get()));

    public static final RegistryObject<EntityType<EntityExplosiveMissile>> MISSILE_REPULSIVE = explosiveMissile(
        "missile_explosive_repulsive",  ICBMExplosives.REPULSIVE,
        () -> (float) ConfigMissile.TIER_1_HEALTH, () -> new ItemStack(ItemReg.MISSILE_REPULSIVE.get()));

    public static final RegistryObject<EntityType<EntityExplosiveMissile>> MISSILE_ATTRACTIVE = explosiveMissile(
        "missile_explosive_attractive",  ICBMExplosives.ATTRACTIVE,
        () -> (float) ConfigMissile.TIER_1_HEALTH, () -> new ItemStack(ItemReg.MISSILE_ATTRACTIVE.get()));

    public static final RegistryObject<EntityType<EntityExplosiveMissile>> MISSILE_COLOR = explosiveMissile(
        "missile_explosive_color",  ICBMExplosives.COLOR,
        () -> (float) ConfigMissile.TIER_1_HEALTH, () -> new ItemStack(ItemReg.MISSILE_COLOR.get()));

    public static final RegistryObject<EntityType<EntityExplosiveMissile>> MISSILE_SMOKE = explosiveMissile(
        "missile_explosive_smoke",  ICBMExplosives.SMOKE,
        () -> (float) ConfigMissile.TIER_1_HEALTH, () -> new ItemStack(ItemReg.MISSILE_SMOKE.get()));


    public static final RegistryObject<EntityType<EntityExplosiveMissile>> MISSILE_FRAGMENTATION = explosiveMissile(
        "missile_explosive_fragmentation",  ICBMExplosives.FRAGMENTATION,
        () -> (float) ConfigMissile.TIER_2_HEALTH, () -> new ItemStack(ItemReg.MISSILE_FRAGMENTATION.get()));

    public static final RegistryObject<EntityType<EntityExplosiveMissile>> MISSILE_CONTAGIOUS = explosiveMissile(
        "missile_explosive_contagious",  ICBMExplosives.CONTAGIOUS,
        () -> (float) ConfigMissile.TIER_2_HEALTH, () -> new ItemStack(ItemReg.MISSILE_CONTAGIOUS.get()));

    public static final RegistryObject<EntityType<EntityExplosiveMissile>> MISSILE_SONIC = explosiveMissile(
        "missile_explosive_sonic",  ICBMExplosives.SONIC,
        () -> (float) ConfigMissile.TIER_2_HEALTH, () -> new ItemStack(ItemReg.MISSILE_SONIC.get()));

    public static final RegistryObject<EntityType<EntityExplosiveMissile>> MISSILE_BREACHING = explosiveMissile(
        "missile_explosive_breaching",  ICBMExplosives.BREACHING,
        () -> (float) ConfigMissile.TIER_2_HEALTH, () -> new ItemStack(ItemReg.MISSILE_BREACHING.get()));

    public static final RegistryObject<EntityType<EntityExplosiveMissile>> MISSILE_THERMOBARIC = explosiveMissile(
        "missile_explosive_thermobaric",  ICBMExplosives.THERMOBARIC,
        () -> (float) ConfigMissile.TIER_2_HEALTH, () -> new ItemStack(ItemReg.MISSILE_THERMOBARIC.get()));


    public static final RegistryObject<EntityType<EntityExplosiveMissile>> MISSILE_NUCLEAR = explosiveMissile(
        "missile_explosive_nuclear",  ICBMExplosives.NUCLEAR,
        () -> (float) ConfigMissile.TIER_3_HEALTH, () -> new ItemStack(ItemReg.MISSILE_NUCLEAR.get()));

    public static final RegistryObject<EntityType<EntityExplosiveMissile>> MISSILE_EMP = explosiveMissile(
        "missile_explosive_emp", ICBMExplosives.EMP,
        () -> (float) ConfigMissile.TIER_3_HEALTH, () -> new ItemStack(ItemReg.MISSILE_EMP.get()));

    public static final RegistryObject<EntityType<EntityExplosiveMissile>> MISSILE_EXOTHERMIC = explosiveMissile(
        "missile_explosive_exothermic",  ICBMExplosives.EXOTHERMIC,
        () -> (float) ConfigMissile.TIER_3_HEALTH, () -> new ItemStack(ItemReg.MISSILE_EXOTHERMIC.get()));

    public static final RegistryObject<EntityType<EntityExplosiveMissile>> MISSILE_ENDOTHERMIC = explosiveMissile(
        "missile_explosive_endothermic",  ICBMExplosives.ENDOTHERMIC,
        () -> (float) ConfigMissile.TIER_3_HEALTH, () -> new ItemStack(ItemReg.MISSILE_ENDOTHERMIC.get()));

    public static final RegistryObject<EntityType<EntityExplosiveMissile>> MISSILE_GRAVITY = explosiveMissile(
        "missile_explosive_gravity",  ICBMExplosives.GRAVITY,
        () -> (float) ConfigMissile.TIER_3_HEALTH, () -> new ItemStack(ItemReg.MISSILE_GRAVITY.get()));

    public static final RegistryObject<EntityType<EntityExplosiveMissile>> MISSILE_ENDER = explosiveMissile(
        "missile_explosive_ender",  ICBMExplosives.ENDER,
        () -> (float) ConfigMissile.TIER_3_HEALTH, () -> new ItemStack(ItemReg.MISSILE_ENDER.get()));


    public static final RegistryObject<EntityType<EntityExplosiveMissile>> MISSILE_ANTIMATTER = explosiveMissile(
        "missile_explosive_antimatter",  ICBMExplosives.ANTIMATTER,
        () -> (float) ConfigMissile.TIER_4_HEALTH, () -> new ItemStack(ItemReg.MISSILE_ANTIMATTER.get()));

    public static final RegistryObject<EntityType<EntityExplosiveMissile>> MISSILE_REDMATTER = explosiveMissile(
        "missile_explosive_redmatter",  ICBMExplosives.REDMATTER,
        () -> (float) ConfigMissile.TIER_4_HEALTH, () -> new ItemStack(ItemReg.MISSILE_REDMATTER.get()));
    // </editor-fold>

    private static RegistryObject<EntityType<EntityExplosiveMissile>> explosiveMissile(String name, IActionData action, NonNullSupplier<Float> maxHealth, NonNullSupplier<ItemStack> itemstack) {
        return ENTITIES.register(name, () -> EntityType.Builder.<EntityExplosiveMissile>create(
            (t, w) -> new EntityExplosiveMissile(t, w, action, maxHealth, itemstack), EntityClassification.MISC)
            .setTrackingRange(500)
            .setUpdateInterval(1)
            .size(0.5f, 0.5f)
            .immuneToFire()
            .build(ICBMConstants.PREFIX + name)
        );

    }

    @SubscribeEvent
    public static void registerEntity(RegistryEvent.Register<EntityEntry> event)
    {
        event.getRegistry().register(buildEntityEntry(EntityFlyingBlock.class, ICBMEntities.BLOCK_GRAVITY, 128, 1));
        event.getRegistry().register(buildEntityEntry(EntityFragments.class, ICBMEntities.BLOCK_FRAGMENT, 40, 1));
        event.getRegistry().register(buildEntityEntry(EntityExplosive.class, ICBMEntities.BLOCK_EXPLOSIVE, 50, 5));

        event.getRegistry().register(buildEntityEntry(EntityMissileActionable.class, ICBMEntities.MISSILE_GENERIC, 500, 1));
        event.getRegistry().register(buildEntityEntry(EntitySurfaceToAirMissile.class, ICBMEntities.MISSILE_SAM, 500, 1));

        event.getRegistry().register(buildEntityEntry(EntityExplosion.class, ICBMEntities.EXPLOSION, 100, 5));
        event.getRegistry().register(buildEntityEntry(EntityLightBeam.class, ICBMEntities.BEAM, 80, 5));
        event.getRegistry().register(buildEntityEntry(EntityGrenade.class, ICBMEntities.GRENADE, 50, 5));
        event.getRegistry().register(buildEntityEntry(EntityBombCart.class, ICBMEntities.BOMB_CART, 50, 2));
        event.getRegistry().register(buildEntityEntry(EntityPlayerSeat.class, ICBMEntities.MISSILE_SEAT, 50, 2));
        event.getRegistry().register(buildEntityEntry(EntityRedmatter.class, ICBMEntities.REDMATTER, 500, 1));
        event.getRegistry().register(buildEntityEntry(EntitySmoke.class, ICBMEntities.SMOKE, 100, 15));
        event.getRegistry().register(buildEntityEntry(EntityBombDroplet.class, ICBMEntities.BOMB_DROPLET, 500, 1));
        event.getRegistry().register(buildEntityEntry(EntityParachute.class, ICBMEntities.PARACHUTE, 500, 1));
        event.getRegistry().register(buildEntityEntry(EntityBalloon.class, ICBMEntities.BALLOON, 500, 1));
    }
}

package icbm.classic.content.reg;

import icbm.classic.ICBMConstants;
import icbm.classic.api.actions.IActionData;
import icbm.classic.api.refs.ICBMExplosives;
import icbm.classic.config.missile.ConfigMissile;
import icbm.classic.content.blast.redmatter.EntityRedmatter;
import icbm.classic.content.cargo.parachute.EntityParachute;
import icbm.classic.content.cluster.bomblet.EntityBombDroplet;
import icbm.classic.content.entity.*;
import icbm.classic.content.entity.flyingblock.EntityFlyingBlock;
import icbm.classic.content.missile.entity.anti.EntitySurfaceToAirMissile;
import icbm.classic.content.missile.entity.explosive.EntityExplosiveMissile;
import icbm.classic.content.missile.entity.explosive.EntityMissileActionable;
import net.minecraft.block.BlockState;
import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.EntityType;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.util.NonNullSupplier;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

/**
 * Created by Dark(DarkGuardsman, Robin) on 1/7/19.
 */
public final class EntityReg {

    public static final DeferredRegister<EntityType<?>> ENTITIES = new DeferredRegister<>(ForgeRegistries.ENTITIES, ICBMConstants.DOMAIN);

    // <editor-fold desc="explosive missiles">
    // TODO make health per missile in the configs
    public static final RegistryObject<EntityType<EntityExplosiveMissile>> MISSILE_CONDENSED = explosiveMissile(
        "missile_explosive_condensed", ICBMExplosives.CONDENSED,
        () -> (float) ConfigMissile.TIER_1_HEALTH, () -> new ItemStack(ItemReg.MISSILE_CONDENSED.get()));

    public static final RegistryObject<EntityType<EntityExplosiveMissile>> MISSILE_SHRAPNEL = explosiveMissile(
        "missile_explosive_shrapnel", ICBMExplosives.SHRAPNEL,
        () -> (float) ConfigMissile.TIER_1_HEALTH, () -> new ItemStack(ItemReg.MISSILE_SHRAPNEL.get()));

    public static final RegistryObject<EntityType<EntityExplosiveMissile>> MISSILE_INCENDIARY = explosiveMissile(
        "missile_explosive_incendiary", ICBMExplosives.INCENDIARY,
        () -> (float) ConfigMissile.TIER_1_HEALTH, () -> new ItemStack(ItemReg.MISSILE_INCENDIARY.get()));

    public static final RegistryObject<EntityType<EntityExplosiveMissile>> MISSILE_DEBILITATION = explosiveMissile(
        "missile_explosive_debilitation", ICBMExplosives.DEBILITATION,
        () -> (float) ConfigMissile.TIER_1_HEALTH, () -> new ItemStack(ItemReg.MISSILE_DEBILITATION.get()));

    public static final RegistryObject<EntityType<EntityExplosiveMissile>> MISSILE_CHEMICAL = explosiveMissile(
        "missile_explosive_chemical", ICBMExplosives.CHEMICAL,
        () -> (float) ConfigMissile.TIER_1_HEALTH, () -> new ItemStack(ItemReg.MISSILE_CHEMICAL.get()));

    public static final RegistryObject<EntityType<EntityExplosiveMissile>> MISSILE_ANVIL = explosiveMissile(
        "missile_explosive_anvil", ICBMExplosives.ANVIL,
        () -> (float) ConfigMissile.TIER_1_HEALTH, () -> new ItemStack(ItemReg.MISSILE_ANVIL.get()));

    public static final RegistryObject<EntityType<EntityExplosiveMissile>> MISSILE_REPULSIVE = explosiveMissile(
        "missile_explosive_repulsive", ICBMExplosives.REPULSIVE,
        () -> (float) ConfigMissile.TIER_1_HEALTH, () -> new ItemStack(ItemReg.MISSILE_REPULSIVE.get()));

    public static final RegistryObject<EntityType<EntityExplosiveMissile>> MISSILE_ATTRACTIVE = explosiveMissile(
        "missile_explosive_attractive", ICBMExplosives.ATTRACTIVE,
        () -> (float) ConfigMissile.TIER_1_HEALTH, () -> new ItemStack(ItemReg.MISSILE_ATTRACTIVE.get()));

    public static final RegistryObject<EntityType<EntityExplosiveMissile>> MISSILE_COLOR = explosiveMissile(
        "missile_explosive_color", ICBMExplosives.COLOR,
        () -> (float) ConfigMissile.TIER_1_HEALTH, () -> new ItemStack(ItemReg.MISSILE_COLOR.get()));

    public static final RegistryObject<EntityType<EntityExplosiveMissile>> MISSILE_SMOKE = explosiveMissile(
        "missile_explosive_smoke", ICBMExplosives.SMOKE,
        () -> (float) ConfigMissile.TIER_1_HEALTH, () -> new ItemStack(ItemReg.MISSILE_SMOKE.get()));


    public static final RegistryObject<EntityType<EntityExplosiveMissile>> MISSILE_FRAGMENTATION = explosiveMissile(
        "missile_explosive_fragmentation", ICBMExplosives.FRAGMENTATION,
        () -> (float) ConfigMissile.TIER_2_HEALTH, () -> new ItemStack(ItemReg.MISSILE_FRAGMENTATION.get()));

    public static final RegistryObject<EntityType<EntityExplosiveMissile>> MISSILE_CONTAGIOUS = explosiveMissile(
        "missile_explosive_contagious", ICBMExplosives.CONTAGIOUS,
        () -> (float) ConfigMissile.TIER_2_HEALTH, () -> new ItemStack(ItemReg.MISSILE_CONTAGIOUS.get()));

    public static final RegistryObject<EntityType<EntityExplosiveMissile>> MISSILE_SONIC = explosiveMissile(
        "missile_explosive_sonic", ICBMExplosives.SONIC,
        () -> (float) ConfigMissile.TIER_2_HEALTH, () -> new ItemStack(ItemReg.MISSILE_SONIC.get()));

    public static final RegistryObject<EntityType<EntityExplosiveMissile>> MISSILE_BREACHING = explosiveMissile(
        "missile_explosive_breaching", ICBMExplosives.BREACHING,
        () -> (float) ConfigMissile.TIER_2_HEALTH, () -> new ItemStack(ItemReg.MISSILE_BREACHING.get()));

    public static final RegistryObject<EntityType<EntityExplosiveMissile>> MISSILE_THERMOBARIC = explosiveMissile(
        "missile_explosive_thermobaric", ICBMExplosives.THERMOBARIC,
        () -> (float) ConfigMissile.TIER_2_HEALTH, () -> new ItemStack(ItemReg.MISSILE_THERMOBARIC.get()));


    public static final RegistryObject<EntityType<EntityExplosiveMissile>> MISSILE_NUCLEAR = explosiveMissile(
        "missile_explosive_nuclear", ICBMExplosives.NUCLEAR,
        () -> (float) ConfigMissile.TIER_3_HEALTH, () -> new ItemStack(ItemReg.MISSILE_NUCLEAR.get()));

    public static final RegistryObject<EntityType<EntityExplosiveMissile>> MISSILE_EMP = explosiveMissile(
        "missile_explosive_emp", ICBMExplosives.EMP,
        () -> (float) ConfigMissile.TIER_3_HEALTH, () -> new ItemStack(ItemReg.MISSILE_EMP.get()));

    public static final RegistryObject<EntityType<EntityExplosiveMissile>> MISSILE_EXOTHERMIC = explosiveMissile(
        "missile_explosive_exothermic", ICBMExplosives.EXOTHERMIC,
        () -> (float) ConfigMissile.TIER_3_HEALTH, () -> new ItemStack(ItemReg.MISSILE_EXOTHERMIC.get()));

    public static final RegistryObject<EntityType<EntityExplosiveMissile>> MISSILE_ENDOTHERMIC = explosiveMissile(
        "missile_explosive_endothermic", ICBMExplosives.ENDOTHERMIC,
        () -> (float) ConfigMissile.TIER_3_HEALTH, () -> new ItemStack(ItemReg.MISSILE_ENDOTHERMIC.get()));

    public static final RegistryObject<EntityType<EntityExplosiveMissile>> MISSILE_GRAVITY = explosiveMissile(
        "missile_explosive_gravity", ICBMExplosives.GRAVITY,
        () -> (float) ConfigMissile.TIER_3_HEALTH, () -> new ItemStack(ItemReg.MISSILE_GRAVITY.get()));

    public static final RegistryObject<EntityType<EntityExplosiveMissile>> MISSILE_ENDER = explosiveMissile(
        "missile_explosive_ender", ICBMExplosives.ENDER,
        () -> (float) ConfigMissile.TIER_3_HEALTH, () -> new ItemStack(ItemReg.MISSILE_ENDER.get()));


    public static final RegistryObject<EntityType<EntityExplosiveMissile>> MISSILE_ANTIMATTER = explosiveMissile(
        "missile_explosive_antimatter", ICBMExplosives.ANTIMATTER,
        () -> (float) ConfigMissile.TIER_4_HEALTH, () -> new ItemStack(ItemReg.MISSILE_ANTIMATTER.get()));

    public static final RegistryObject<EntityType<EntityExplosiveMissile>> MISSILE_REDMATTER = explosiveMissile(
        "missile_explosive_redmatter", ICBMExplosives.REDMATTER,
        () -> (float) ConfigMissile.TIER_4_HEALTH, () -> new ItemStack(ItemReg.MISSILE_REDMATTER.get()));
    // </editor-fold>

    // <editor-fold desc="missiles">
    // TODO make health per missile in the configs
    public static final RegistryObject<EntityType<EntityMissileActionable>> MISSILE_CLUSTER = missile(
        "missile_cluster", null, // action is set in spawn item
        () -> ConfigMissile.CLUSTER_MISSILE.MAX_HEALTH, () -> new ItemStack(ItemReg.MISSILE_CLUSTER.get()));

    public static final RegistryObject<EntityType<EntitySurfaceToAirMissile>> MISSILE_SURFACE_TO_AIR = ENTITIES.register(
        "missile_surface_to_air", () -> EntityType.Builder.<EntitySurfaceToAirMissile>create(
                (t, w) -> new EntitySurfaceToAirMissile(t, w, () -> new ItemStack(ItemReg.MISSILE_SURFACE_TO_AIR::get)), EntityClassification.MISC)
        .setTrackingRange(500)
            .setUpdateInterval(1)
            .size(0.5f, 0.5f)
            .immuneToFire()
            .build(ICBMConstants.PREFIX + "missile_surface_to_air")
        );
    // </editor-fold>

    // <editor-fold desc="blocks">
    public static final RegistryObject<EntityType<EntityFlyingBlock>> FLYING_BLOCKS = ENTITIES.register("flying_block", () -> EntityType.Builder.create(
            EntityFlyingBlock::new, EntityClassification.MISC)
        .setTrackingRange(128)
        .setUpdateInterval(1)
        .size(0.98f, 0.98f)
        .immuneToFire()
        .build(ICBMConstants.PREFIX + "flying_block")
    );
    public static final RegistryObject<EntityType<AnvilEntity>> ANVIL = ENTITIES.register("anvil", () -> EntityType.Builder.create(
            AnvilEntity::new, EntityClassification.MISC)
        .setTrackingRange(128)
        .setUpdateInterval(1)
        .size(0.5f, 0.5f)
        .immuneToFire()
        .build(ICBMConstants.PREFIX + "anvil")
    );
    // </editor-fold>

    // <editor-fold desc="projectiles">
    public static final RegistryObject<EntityType<EntityShrapnel>> SHRAPNEL_GENERIC = ENTITIES.register("shrapnel_generic", () -> EntityType.Builder.create(
            EntityShrapnel::new, EntityClassification.MISC)
        .setTrackingRange(128)
        .setUpdateInterval(1)
        .size(0.5f, 0.5f)
        .immuneToFire()
        .build(ICBMConstants.PREFIX + "shrapnel")
    );


    public static final RegistryObject<EntityType<EntityExplosiveFragment>> EXPLOSIVE_FRAGMENT = ENTITIES.register("explosive_fragment", () -> EntityType.Builder.create(
            EntityExplosiveFragment::new, EntityClassification.MISC)
        .setTrackingRange(128)
        .setUpdateInterval(1)
        .size(0.5f, 0.5f)
        .immuneToFire()
        .build(ICBMConstants.PREFIX + "explosive_fragment")
    );
    // </editor-fold>

    // <editor-fold desc="explosive blocks">
    public static final RegistryObject<EntityType<EntityExplosive>> BLOCK_CONDENSED = explosiveBlock(
        "block_explosive_condensed", ICBMExplosives.CONDENSED,
        () -> new ItemStack(ItemReg.MISSILE_CONDENSED.get()), () -> BlockReg.EXPLOSIVE_CONDENSED.get().getDefaultState());

    public static final RegistryObject<EntityType<EntityExplosive>> BLOCK_SHRAPNEL = explosiveBlock(
        "block_explosive_shrapnel", ICBMExplosives.SHRAPNEL,
        () -> new ItemStack(ItemReg.MISSILE_SHRAPNEL.get()), () -> BlockReg.EXPLOSIVE_SHRAPNEL.get().getDefaultState());

    public static final RegistryObject<EntityType<EntityExplosive>> BLOCK_INCENDIARY = explosiveBlock(
        "block_explosive_incendiary", ICBMExplosives.INCENDIARY,
        () -> new ItemStack(ItemReg.MISSILE_INCENDIARY.get()), () -> BlockReg.EXPLOSIVE_INCENDIARY.get().getDefaultState());

    public static final RegistryObject<EntityType<EntityExplosive>> BLOCK_DEBILITATION = explosiveBlock(
        "block_explosive_debilitation", ICBMExplosives.DEBILITATION,
        () -> new ItemStack(ItemReg.MISSILE_DEBILITATION.get()), () -> BlockReg.EXPLOSIVE_DEBILITATION.get().getDefaultState());

    public static final RegistryObject<EntityType<EntityExplosive>> BLOCK_CHEMICAL = explosiveBlock(
        "block_explosive_chemical", ICBMExplosives.CHEMICAL,
        () -> new ItemStack(ItemReg.MISSILE_CHEMICAL.get()), () -> BlockReg.EXPLOSIVE_CHEMICAL.get().getDefaultState());

    public static final RegistryObject<EntityType<EntityExplosive>> BLOCK_ANVIL = explosiveBlock(
        "block_explosive_anvil", ICBMExplosives.ANVIL,
        () -> new ItemStack(ItemReg.MISSILE_ANVIL.get()), () -> BlockReg.EXPLOSIVE_ANVIL.get().getDefaultState());

    public static final RegistryObject<EntityType<EntityExplosive>> BLOCK_REPULSIVE = explosiveBlock(
        "block_explosive_repulsive", ICBMExplosives.REPULSIVE,
        () -> new ItemStack(ItemReg.MISSILE_REPULSIVE.get()), () -> BlockReg.EXPLOSIVE_REPULSIVE.get().getDefaultState());

    public static final RegistryObject<EntityType<EntityExplosive>> BLOCK_ATTRACTIVE = explosiveBlock(
        "block_explosive_attractive", ICBMExplosives.ATTRACTIVE,
        () -> new ItemStack(ItemReg.MISSILE_ATTRACTIVE.get()), () -> BlockReg.EXPLOSIVE_ATTRACTIVE.get().getDefaultState());

    public static final RegistryObject<EntityType<EntityExplosive>> BLOCK_COLOR = explosiveBlock(
        "block_explosive_color", ICBMExplosives.COLOR,
        () -> new ItemStack(ItemReg.MISSILE_COLOR.get()), () -> BlockReg.EXPLOSIVE_COLOR.get().getDefaultState());

    public static final RegistryObject<EntityType<EntityExplosive>> BLOCK_SMOKE = explosiveBlock(
        "block_explosive_smoke", ICBMExplosives.SMOKE,
        () -> new ItemStack(ItemReg.MISSILE_SMOKE.get()), () -> BlockReg.EXPLOSIVE_SMOKE.get().getDefaultState());


    public static final RegistryObject<EntityType<EntityExplosive>> BLOCK_FRAGMENTATION = explosiveBlock(
        "block_explosive_fragmentation", ICBMExplosives.FRAGMENTATION,
        () -> new ItemStack(ItemReg.MISSILE_FRAGMENTATION.get()), () -> BlockReg.EXPLOSIVE_FRAGMENTATION.get().getDefaultState());

    public static final RegistryObject<EntityType<EntityExplosive>> BLOCK_CONTAGIOUS = explosiveBlock(
        "block_explosive_contagious", ICBMExplosives.CONTAGIOUS,
        () -> new ItemStack(ItemReg.MISSILE_CONTAGIOUS.get()), () -> BlockReg.EXPLOSIVE_CONTAGIOUS.get().getDefaultState());

    public static final RegistryObject<EntityType<EntityExplosive>> BLOCK_SONIC = explosiveBlock(
        "block_explosive_sonic", ICBMExplosives.SONIC,
        () -> new ItemStack(ItemReg.MISSILE_SONIC.get()), () -> BlockReg.EXPLOSIVE_SONIC.get().getDefaultState());

    // breaching doesn't need an entity, its instant always

    public static final RegistryObject<EntityType<EntityExplosive>> BLOCK_THERMOBARIC = explosiveBlock(
        "block_explosive_thermobaric", ICBMExplosives.THERMOBARIC,
        () -> new ItemStack(ItemReg.MISSILE_THERMOBARIC.get()), () -> BlockReg.EXPLOSIVE_THERMOBARIC.get().getDefaultState());


    public static final RegistryObject<EntityType<EntityExplosive>> BLOCK_NUCLEAR = explosiveBlock(
        "block_explosive_nuclear", ICBMExplosives.NUCLEAR,
        () -> new ItemStack(ItemReg.MISSILE_NUCLEAR.get()), () -> BlockReg.EXPLOSIVE_NUCLEAR.get().getDefaultState());

    public static final RegistryObject<EntityType<EntityExplosive>> BLOCK_EMP = explosiveBlock(
        "block_explosive_emp", ICBMExplosives.EMP,
        () -> new ItemStack(ItemReg.MISSILE_EMP.get()), () -> BlockReg.EXPLOSIVE_EMP.get().getDefaultState());

    public static final RegistryObject<EntityType<EntityExplosive>> BLOCK_EXOTHERMIC = explosiveBlock(
        "block_explosive_exothermic", ICBMExplosives.EXOTHERMIC,
        () -> new ItemStack(ItemReg.MISSILE_EXOTHERMIC.get()), () -> BlockReg.EXPLOSIVE_EXOTHERMIC.get().getDefaultState());

    public static final RegistryObject<EntityType<EntityExplosive>> BLOCK_ENDOTHERMIC = explosiveBlock(
        "block_explosive_endothermic", ICBMExplosives.ENDOTHERMIC,
        () -> new ItemStack(ItemReg.MISSILE_ENDOTHERMIC.get()), () -> BlockReg.EXPLOSIVE_ENDOTHERMIC.get().getDefaultState());

    public static final RegistryObject<EntityType<EntityExplosive>> BLOCK_GRAVITY = explosiveBlock(
        "block_explosive_gravity", ICBMExplosives.GRAVITY,
        () -> new ItemStack(ItemReg.MISSILE_GRAVITY.get()), () -> BlockReg.EXPLOSIVE_GRAVITY.get().getDefaultState());

    public static final RegistryObject<EntityType<EntityExplosive>> BLOCK_ENDER = explosiveBlock(
        "block_explosive_ender", ICBMExplosives.ENDER,
        () -> new ItemStack(ItemReg.MISSILE_ENDER.get()), () -> BlockReg.EXPLOSIVE_ENDER.get().getDefaultState());


    public static final RegistryObject<EntityType<EntityExplosive>> BLOCK_ANTIMATTER = explosiveBlock(
        "block_explosive_antimatter", ICBMExplosives.ANTIMATTER,
        () -> new ItemStack(ItemReg.MISSILE_ANTIMATTER.get()), () -> BlockReg.EXPLOSIVE_ANTIMATTER.get().getDefaultState());

    public static final RegistryObject<EntityType<EntityExplosive>> BLOCK_REDMATTER = explosiveBlock(
        "block_explosive_redmatter", ICBMExplosives.REDMATTER,
        () -> new ItemStack(ItemReg.MISSILE_REDMATTER.get()), () -> BlockReg.EXPLOSIVE_REDMATTER.get().getDefaultState());
    // </editor-fold>

    // <editor-fold desc="grenades">
        //  event.getRegistry().register(buildEntityEntry(EntityGrenade.class, ICBMEntities.GRENADE, 50, 5));
    // </editor-fold>

    // <editor-fold desc="bomb carts">
        //  event.getRegistry().register(buildEntityEntry(EntityBombCart.class, ICBMEntities.BOMB_CART, 50, 2));
    // </editor-fold>

    // <editor-fold desc="droplet">
    //  event.getRegistry().register(buildEntityEntry(EntityBombDroplet.class, ICBMEntities.BOMB_DROPLET, 500, 1));
    public static final RegistryObject<EntityType<EntityBombDroplet>> BOMBLET_CONDENSED = bomblet(
        "bomblet_explosive_condensed", ICBMExplosives.CONDENSED,
        () -> new ItemStack(ItemReg.BOMBLET_CONDENSED.get()));
    // </editor-fold>

    // <editor-fold desc="entities">
    /** @deprecated being removed as each explosive should spawn its own entityType */
    public static final RegistryObject<EntityType<EntityExplosion>> TICKING_EXPLOSION = ENTITIES.register("ticking_explosion", () -> EntityType.Builder.create(
            EntityExplosion::new, EntityClassification.MISC)
        .setTrackingRange(128)
        .setUpdateInterval(1)
        .size(0.98f, 0.98f)
        .immuneToFire()
        .build(ICBMConstants.PREFIX + "ticking_explosion")
    );

    public static final RegistryObject<EntityType<EntityLightBeam>> LIGHT_BEAM = ENTITIES.register("light_beam", () -> EntityType.Builder.create(
            EntityLightBeam::new, EntityClassification.MISC)
        .setTrackingRange(128)
        .setUpdateInterval(1)
        .size(0.98f, 0.98f)
        .immuneToFire()
        .build(ICBMConstants.PREFIX + "ticking_explosion")
    );

    public static final RegistryObject<EntityType<EntityPlayerSeat>> HOLDER_SEAT = ENTITIES.register("holder_seat", () -> EntityType.Builder.create(
            EntityPlayerSeat::new, EntityClassification.MISC)
        .setTrackingRange(50)
        .setUpdateInterval(2)
        .size(0.98f, 0.98f)
        .immuneToFire()
        .build(ICBMConstants.PREFIX + "ticking_explosion")
    );

    public static final RegistryObject<EntityType<EntityRedmatter>> REDMATTER = ENTITIES.register("redmatter", () -> EntityType.Builder.create(
            EntityRedmatter::new, EntityClassification.MISC)
        .setTrackingRange(500)
        .setUpdateInterval(1)
        .size(0.98f, 0.98f)
        .immuneToFire()
        .build(ICBMConstants.PREFIX + "redmatter")
    );

    public static final RegistryObject<EntityType<EntitySmoke>> MARKING_SMOKE = ENTITIES.register("marking_smoke", () -> EntityType.Builder.create(
            EntitySmoke::new, EntityClassification.MISC)
        .setTrackingRange(100)
        .setUpdateInterval(1)
        .size(0.1f, 0.1f)
        .immuneToFire()
        .build(ICBMConstants.PREFIX + "marking_smoke")
    );

    public static final RegistryObject<EntityType<EntityParachute>> CARGO_PARACHUTE_SIZE_1 = ENTITIES.register("cargo_parachute_size_1", () -> EntityType.Builder.<EntityParachute>create(
            (t, w) -> new EntityParachute(t, w, () -> new ItemStack(ItemReg.PARACHUTE::get), () -> new ItemStack(ItemReg.PARACHUTE::get)), EntityClassification.MISC)
        .setTrackingRange(500)
        .setUpdateInterval(1)
        .size(0.5f, 0.5f)
        .immuneToFire()
        .build(ICBMConstants.PREFIX + "cargo_parachute_size_1")
    );

    public static final RegistryObject<EntityType<EntityParachute>> CARGO_PARACHUTE_SIZE_2 = ENTITIES.register("cargo_parachute_size_2", () -> EntityType.Builder.<EntityParachute>create(
            (t, w) -> new EntityParachute(t, w, () -> new ItemStack(ItemReg.PARACHUTE::get), () -> new ItemStack(ItemReg.PARACHUTE::get)), EntityClassification.MISC)
        .setTrackingRange(500)
        .setUpdateInterval(1)
        .size(0.98f, 0.98f)
        .immuneToFire()
        .build(ICBMConstants.PREFIX + "cargo_parachute_size_2")
    );

    // </editor-fold>

    /**
     * @deprecated replace with {@link #missile(String, IActionData, NonNullSupplier, NonNullSupplier)}
     */
    @Deprecated
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

    private static RegistryObject<EntityType<EntityMissileActionable>> missile(String name, IActionData action, NonNullSupplier<Float> maxHealth, NonNullSupplier<ItemStack> itemstack) {
        return ENTITIES.register(name, () -> EntityType.Builder.<EntityMissileActionable>create(
                (t, w) -> new EntityMissileActionable(t, w, action, maxHealth, itemstack), EntityClassification.MISC)
            .setTrackingRange(500)
            .setUpdateInterval(1)
            .size(0.5f, 0.5f)
            .immuneToFire()
            .build(ICBMConstants.PREFIX + name)
        );
    }

    private static RegistryObject<EntityType<EntityExplosive>> explosiveBlock(String name, IActionData action, NonNullSupplier<ItemStack> itemstack, NonNullSupplier<BlockState> blockstate) {
        return ENTITIES.register(name, () -> EntityType.Builder.<EntityExplosive>create(
                (t, w) -> new EntityExplosive(t, w, action, itemstack, blockstate), EntityClassification.MISC)
            .setTrackingRange(500)
            .setUpdateInterval(1)
            .size(0.5f, 0.5f)
            .immuneToFire()
            .build(ICBMConstants.PREFIX + name)
        );
    }

    private static RegistryObject<EntityType<EntityBombDroplet>> bomblet(String name, IActionData action, NonNullSupplier<ItemStack> itemstack) {
        return ENTITIES.register(name, () -> EntityType.Builder.<EntityBombDroplet>create(
                (t, w) -> new EntityBombDroplet(t, w, action, itemstack), EntityClassification.MISC)
            .setTrackingRange(500)
            .setUpdateInterval(1)
            .size(0.5f, 0.5f)
            .immuneToFire()
            .build(ICBMConstants.PREFIX + name)
        );
    }
}

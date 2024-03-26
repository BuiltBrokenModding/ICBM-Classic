package icbm.classic.world;

import icbm.classic.IcbmConstants;
import icbm.classic.world.blast.redmatter.RedmatterEntity;
import icbm.classic.world.entity.*;
import icbm.classic.world.entity.flyingblock.FlyingBlockEntity;
import icbm.classic.world.missile.entity.anti.SurfaceToAirMissileEntity;
import icbm.classic.world.missile.entity.explosive.ExplosiveMissileEntity;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.ArrayList;
import java.util.function.Supplier;

public final class IcbmEntityTypes {

    public static final DeferredRegister<EntityType<?>> REGISTER =
        DeferredRegister.create(Registries.ENTITY_TYPE, IcbmConstants.MOD_ID);

    public static final DeferredHolder<EntityType<?>, EntityType<FlyingBlockEntity>> FLYING_BLOCK = register(
        "flying_block",
        () -> EntityType.Builder.of(FlyingBlockEntity::new, MobCategory.MISC)
            .sized(0.98F, 0.98F)
            .clientTrackingRange(128)
            .updateInterval(1));

    public static final DeferredHolder<EntityType<?>, EntityType<FragmentsEntity>> FRAGMENTS = register(
        "fragments",
        () -> EntityType.Builder.<FragmentsEntity>of(FragmentsEntity::new, MobCategory.MISC)
            .sized(0.25F, 0.25F)
            .clientTrackingRange(40)
            .updateInterval(1));

    public static final DeferredHolder<EntityType<?>, EntityType<ExplosiveEntity>> EXPLOSIVE = register(
        "explosive",
        () -> EntityType.Builder.of(ExplosiveEntity::new, MobCategory.MISC)
            .sized(0.98F, 0.98F)
            .clientTrackingRange(50)
            .updateInterval(5));

    public static final DeferredHolder<EntityType<?>, EntityType<ExplosiveMissileEntity>> EXPLOSIVE_MISSILE = register(
        "explosive_missile",
        () -> EntityType.Builder.of(ExplosiveMissileEntity::new, MobCategory.MISC)
            .sized(0.98F, 0.98F)
            .clientTrackingRange(500)
            .updateInterval(1));

    public static final DeferredHolder<EntityType<?>, EntityType<ExplosionEntity>> EXPLOSION = register(
        "explosion",
        () -> EntityType.Builder.<ExplosionEntity>of(ExplosionEntity::new, MobCategory.MISC)
            .sized(0.98F, 0.98F)
            .clientTrackingRange(100)
            .updateInterval(5));

    public static final DeferredHolder<EntityType<?>, EntityType<LightBeamEntity>> LIGHT_BEAM = register(
        "light_beam",
        () -> EntityType.Builder.of(LightBeamEntity::new, MobCategory.MISC)
            .sized(0.98F, 0.98F)
            .clientTrackingRange(80)
            .updateInterval(5));

    public static final DeferredHolder<EntityType<?>, EntityType<GrenadeEntity>> GRENADE = register(
        "grenade",
        () -> EntityType.Builder.of(GrenadeEntity::new, MobCategory.MISC)
            .sized(0.98F, 0.98F)
            .clientTrackingRange(50)
            .updateInterval(5));

    public static final DeferredHolder<EntityType<?>, EntityType<BombCartEntity>> BOMB_CART = register(
        "bomb_cart",
        () -> EntityType.Builder.<BombCartEntity>of(BombCartEntity::new, MobCategory.MISC)
            .sized(0.98F, 0.98F)
            .clientTrackingRange(50)
            .updateInterval(2));

    public static final DeferredHolder<EntityType<?>, EntityType<PlayerSeatEntity>> MISSILE_SEAT = register(
        "missile_seat",
        () -> EntityType.Builder.of(PlayerSeatEntity::new, MobCategory.MISC)
            .sized(0.98F, 0.98F)
            .clientTrackingRange(50)
            .updateInterval(2));

    public static final DeferredHolder<EntityType<?>, EntityType<RedmatterEntity>> RED_MATTER = register(
        "red_matter",
        () -> EntityType.Builder.of(RedmatterEntity::new, MobCategory.MISC)
            .sized(0.98F, 0.98F)
            .clientTrackingRange(500)
            .updateInterval(1));

    public static final DeferredHolder<EntityType<?>, EntityType<SmokeEntity>> SMOKE = register(
        "smoke",
        () -> EntityType.Builder.of(SmokeEntity::new, MobCategory.MISC)
            .sized(0.98F, 0.98F)
            .clientTrackingRange(100)
            .updateInterval(15));

    public static final DeferredHolder<EntityType<?>, EntityType<SurfaceToAirMissileEntity>> SURFACE_TO_AIR_MISSILE = register(
        "surface_to_air_missile",
        () -> EntityType.Builder.of(SurfaceToAirMissileEntity::new, MobCategory.MISC)
            .sized(0.98F, 0.98F)
            .clientTrackingRange(500)
            .updateInterval(1));

    private static <T extends Entity> DeferredHolder<EntityType<?>, EntityType<T>> register(String name,
                                                                                            Supplier<EntityType.Builder<T>> supplier) {
        return REGISTER.register(name, () -> supplier.get().build(name));
    }

    private static final HolderSet<EntityType<?>> HOLDER_ALL = HolderSet.direct(new ArrayList<>(REGISTER.getEntries()));
    private static final HolderSet<EntityType<?>> HOLDER_MISSILES = HolderSet.direct(SURFACE_TO_AIR_MISSILE, EXPLOSIVE_MISSILE);
    private static final HolderSet<EntityType<?>> HOLDER_EXPLOSIONS = HolderSet.direct(EXPLOSIVE, EXPLOSION, EXPLOSIVE_MISSILE);

    public static HolderSet<EntityType<?>> all() {
        return HOLDER_ALL;
    }

    public static HolderSet<EntityType<?>> missiles() {
        return HOLDER_MISSILES;
    }

    public static HolderSet<EntityType<?>> explosions() {
        return HOLDER_EXPLOSIONS;
    }

}

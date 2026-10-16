package icbm.classic.content.reg;

import icbm.classic.ICBMConstants;
import icbm.classic.api.refs.ICBMEntities;
import icbm.classic.content.blast.redmatter.EntityRedmatter;
import icbm.classic.content.cargo.balloon.EntityBalloon;
import icbm.classic.content.cargo.parachute.EntityParachute;
import icbm.classic.content.cluster.bomblet.EntityBombDroplet;
import icbm.classic.content.entity.EntityBombCart;
import icbm.classic.content.entity.EntityExplosion;
import icbm.classic.content.entity.EntityExplosive;
import icbm.classic.content.entity.EntityFragments;
import icbm.classic.content.entity.EntityGrenade;
import icbm.classic.content.entity.EntityLightBeam;
import icbm.classic.content.entity.EntityPlayerSeat;
import icbm.classic.content.entity.EntitySmoke;
import icbm.classic.content.entity.flyingblock.EntityFlyingBlock;
import icbm.classic.content.missile.entity.anti.EntitySurfaceToAirMissile;
import icbm.classic.content.missile.entity.explosive.EntityExplosiveMissile;
import icbm.classic.content.missile.entity.explosive.EntityMissileActionable;
import icbm.classic.content.missile.entity.itemstack.EntityHeldItemMissile;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Entity type registry. Names match the 1.12 entity names from {@link ICBMEntities}.
 * <p>
 * Entity constructor convention: {@code EntityX(EntityType<? extends EntityX> type, Level level)} plus a
 * convenience {@code EntityX(Level level)} that passes the registered type.
 * <p>
 * 1.12 tracker settings (range in blocks, update frequency) were kept: clientTrackingRange is in chunks.
 * <p>
 * Created by Dark(DarkGuardsman, Robin) on 1/7/19.
 */
public final class EntityReg
{
    public static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(Registries.ENTITY_TYPE, ICBMConstants.DOMAIN);

    public static final DeferredHolder<EntityType<?>, EntityType<EntityFlyingBlock>> BLOCK_GRAVITY = register(ICBMEntities.BLOCK_GRAVITY,
        EntityType.Builder.<EntityFlyingBlock>of(EntityFlyingBlock::new, MobCategory.MISC).sized(0.98F, 0.98F).fireImmune(), 128, 1);
    public static final DeferredHolder<EntityType<?>, EntityType<EntityFragments>> BLOCK_FRAGMENT = register(ICBMEntities.BLOCK_FRAGMENT,
        EntityType.Builder.<EntityFragments>of(EntityFragments::new, MobCategory.MISC).sized(0.5F, 0.5F), 40, 1);
    public static final DeferredHolder<EntityType<?>, EntityType<EntityExplosive>> BLOCK_EXPLOSIVE = register(ICBMEntities.BLOCK_EXPLOSIVE,
        EntityType.Builder.<EntityExplosive>of(EntityExplosive::new, MobCategory.MISC).sized(0.98F, 0.98F).fireImmune(), 50, 5);

    public static final DeferredHolder<EntityType<?>, EntityType<EntityExplosiveMissile>> MISSILE_EXPLOSIVE = register(ICBMEntities.MISSILE_EXPLOSIVE,
        EntityType.Builder.<EntityExplosiveMissile>of(EntityExplosiveMissile::new, MobCategory.MISC).sized(0.5F, 0.5F), 500, 1);
    public static final DeferredHolder<EntityType<?>, EntityType<EntityMissileActionable>> MISSILE_GENERIC = register(ICBMEntities.MISSILE_GENERIC,
        EntityType.Builder.<EntityMissileActionable>of(EntityMissileActionable::new, MobCategory.MISC).sized(0.5F, 0.5F), 500, 1);
    public static final DeferredHolder<EntityType<?>, EntityType<EntitySurfaceToAirMissile>> MISSILE_SAM = register(ICBMEntities.MISSILE_SAM,
        EntityType.Builder.<EntitySurfaceToAirMissile>of(EntitySurfaceToAirMissile::new, MobCategory.MISC).sized(0.5F, 0.5F), 500, 1);
    public static final DeferredHolder<EntityType<?>, EntityType<EntityHeldItemMissile>> MISSILE_HELD_ITEM = register(ICBMEntities.MISSILE_HELD_ITEM,
        EntityType.Builder.<EntityHeldItemMissile>of(EntityHeldItemMissile::new, MobCategory.MISC).sized(0.5F, 0.5F), 500, 1);

    public static final DeferredHolder<EntityType<?>, EntityType<EntityExplosion>> EXPLOSION = register(ICBMEntities.EXPLOSION,
        EntityType.Builder.<EntityExplosion>of(EntityExplosion::new, MobCategory.MISC).sized(0.98F, 0.98F).fireImmune(), 100, 5);
    public static final DeferredHolder<EntityType<?>, EntityType<EntityLightBeam>> BEAM = register(ICBMEntities.BEAM,
        EntityType.Builder.<EntityLightBeam>of(EntityLightBeam::new, MobCategory.MISC).sized(1F, 1F), 80, 5);
    public static final DeferredHolder<EntityType<?>, EntityType<EntityGrenade>> GRENADE = register(ICBMEntities.GRENADE,
        EntityType.Builder.<EntityGrenade>of(EntityGrenade::new, MobCategory.MISC).sized(0.25F, 0.25F), 50, 5);
    public static final DeferredHolder<EntityType<?>, EntityType<EntityBombCart>> BOMB_CART = register(ICBMEntities.BOMB_CART,
        EntityType.Builder.<EntityBombCart>of(EntityBombCart::new, MobCategory.MISC).sized(0.98F, 0.7F), 50, 2);
    public static final DeferredHolder<EntityType<?>, EntityType<EntityPlayerSeat>> MISSILE_SEAT = register(ICBMEntities.MISSILE_SEAT,
        EntityType.Builder.<EntityPlayerSeat>of(EntityPlayerSeat::new, MobCategory.MISC).sized(0.5F, 2.5F), 50, 2);
    public static final DeferredHolder<EntityType<?>, EntityType<EntityRedmatter>> REDMATTER = register(ICBMEntities.REDMATTER,
        EntityType.Builder.<EntityRedmatter>of(EntityRedmatter::new, MobCategory.MISC).sized(0.98F, 0.98F).fireImmune(), 500, 1);
    public static final DeferredHolder<EntityType<?>, EntityType<EntitySmoke>> SMOKE = register(ICBMEntities.SMOKE,
        EntityType.Builder.<EntitySmoke>of(EntitySmoke::new, MobCategory.MISC).sized(0.1F, 0.1F), 100, 15);
    public static final DeferredHolder<EntityType<?>, EntityType<EntityBombDroplet>> BOMB_DROPLET = register(ICBMEntities.BOMB_DROPLET,
        EntityType.Builder.<EntityBombDroplet>of(EntityBombDroplet::new, MobCategory.MISC).sized(0.25F, 0.25F), 500, 1);
    public static final DeferredHolder<EntityType<?>, EntityType<EntityParachute>> PARACHUTE = register(ICBMEntities.PARACHUTE,
        EntityType.Builder.<EntityParachute>of(EntityParachute::new, MobCategory.MISC).sized(0.5F, 0.5F), 500, 1);
    public static final DeferredHolder<EntityType<?>, EntityType<EntityBalloon>> BALLOON = register(ICBMEntities.BALLOON,
        EntityType.Builder.<EntityBalloon>of(EntityBalloon::new, MobCategory.MISC).sized(0.5F, 0.5F), 500, 1);

    private EntityReg()
    {
    }

    private static <E extends Entity> DeferredHolder<EntityType<?>, EntityType<E>> register(ResourceLocation name, EntityType.Builder<E> builder, int trackingRange, int updateFrequency)
    {
        // 1.12 tracking range was in blocks, 1.21 uses chunks
        final int rangeChunks = Math.max(1, (int) Math.ceil(trackingRange / 16.0));
        return ENTITIES.register(name.getPath(), () -> builder
            .clientTrackingRange(rangeChunks)
            .updateInterval(updateFrequency)
            .build(name.toString()));
    }

    public static void register(IEventBus modBus)
    {
        ENTITIES.register(modBus);
    }
}

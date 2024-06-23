package icbm.classic.content.reg;

import icbm.classic.ICBMConstants;
import icbm.classic.api.refs.ICBMEntities;
import icbm.classic.content.blast.redmatter.EntityRedmatter;
import icbm.classic.content.cargo.balloon.EntityBalloon;
import icbm.classic.content.cargo.parachute.EntityParachute;
import icbm.classic.content.cluster.bomblet.EntityBombDroplet;
import icbm.classic.content.entity.*;
import icbm.classic.content.entity.flyingblock.EntityFlyingBlock;
import icbm.classic.content.missile.entity.anti.EntitySurfaceToAirMissile;
import icbm.classic.content.missile.entity.explosive.EntityExplosiveMissile;
import icbm.classic.content.missile.entity.explosive.EntityMissileActionable;
import icbm.classic.content.missile.entity.itemstack.EntityHeldItemMissile;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.EntityEntry;
import net.minecraftforge.fml.common.registry.EntityEntryBuilder;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

/**
 * Created by Dark(DarkGuardsman, Robin) on 1/7/19.
 */
@Mod.EventBusSubscriber(modid = ICBMConstants.DOMAIN)
public final class EntityReg
{
    private static int nextEntityID = 0;

    @SubscribeEvent
    public static void missingMapping(RegistryEvent.MissingMappings<EntityEntry> event) {
        // Name was changed in v4.2.0
        final ResourceLocation oldMissileName = new ResourceLocation(ICBMConstants.DOMAIN, "missile");
        for(RegistryEvent.MissingMappings.Mapping<EntityEntry> mapping : event.getMappings()) {
            if (oldMissileName.equals(mapping.key)) {
                mapping.remap(ForgeRegistries.ENTITIES.getValue(ICBMEntities.MISSILE_EXPLOSIVE));
            }
        }
    }

    @SubscribeEvent
    public static void registerEntity(RegistryEvent.Register<EntityEntry> event)
    {
        event.getRegistry().register(buildEntityEntry(EntityFlyingBlock.class, ICBMEntities.BLOCK_GRAVITY, 128, 1));
        event.getRegistry().register(buildEntityEntry(EntityFragments.class, ICBMEntities.BLOCK_FRAGMENT, 40, 1));
        event.getRegistry().register(buildEntityEntry(EntityExplosive.class, ICBMEntities.BLOCK_EXPLOSIVE, 50, 5));

        event.getRegistry().register(buildEntityEntry(EntityExplosiveMissile.class, ICBMEntities.MISSILE_EXPLOSIVE, 500, 1));
        event.getRegistry().register(buildEntityEntry(EntityMissileActionable.class, ICBMEntities.MISSILE_GENERIC, 500, 1));
        event.getRegistry().register(buildEntityEntry(EntitySurfaceToAirMissile.class, ICBMEntities.MISSILE_SAM, 500, 1));
        event.getRegistry().register(buildEntityEntry(EntityHeldItemMissile.class, ICBMEntities.MISSILE_HELD_ITEM, 500, 1));

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

        /*
        //Green team TODO move to addon
        event.getRegistry().register(buildMobEntry(EntityXmasSkeleton.class, "skeleton.xmas.elf", Color.GREEN, Color.CYAN));
        event.getRegistry().register(buildMobEntry(EntityXmasSkeletonBoss.class, "skeleton.xmas.boss", Color.GREEN, Color.CYAN));
        event.getRegistry().register(buildMobEntry(EntityXmasSnowman.class, "skeleton.xmas.snowman", Color.BLACK, Color.CYAN));

        //Red team
        event.getRegistry().register(buildMobEntry(EntityXmasZombie.class, "zombie.xmas.elf", Color.RED, Color.CYAN));
        event.getRegistry().register(buildMobEntry(EntityXmasZombieBoss.class, "zombie.xmas.boss", Color.RED, Color.CYAN));
        event.getRegistry().register(buildMobEntry(EntityXmasCreeper.class, "zombie.xmas.creeper", Color.RED, Color.CYAN));


        event.getRegistry().register(buildEntityEntry(EntityXmasRPG.class, "skeleton.snowman.rocket", 64, 1)); */
    }

    private static EntityEntry buildEntityEntry(Class<? extends Entity> entityClass, ResourceLocation name, int trackingRange, int updateFrequency)
    {
        EntityEntryBuilder builder = EntityEntryBuilder.create();
        builder.name(name.toString());
        builder.id(name, nextEntityID++);
        builder.tracker(trackingRange, updateFrequency, true);
        builder.entity(entityClass);
        return builder.build();
    }
}

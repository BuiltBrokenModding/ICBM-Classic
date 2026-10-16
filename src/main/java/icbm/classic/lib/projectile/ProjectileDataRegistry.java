package icbm.classic.lib.projectile;

import icbm.classic.api.ICBMClassicAPI;
import icbm.classic.api.missiles.projectile.IProjectileData;
import icbm.classic.api.missiles.projectile.IProjectileDataRegistry;
import icbm.classic.api.missiles.projectile.IProjectileStack;
import icbm.classic.lib.buildable.BuildableObjectRegistry;
import icbm.classic.lib.projectile.vanilla.*;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.InteractionHand;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

public class ProjectileDataRegistry extends BuildableObjectRegistry<IProjectileData> implements IProjectileDataRegistry {

    private final Map<Item, List<ProjectileItemConversion>> itemConversions = new HashMap();
    public ProjectileDataRegistry() {
        super("PROJECTILE", "projectile");
    }

    @Override
    public void lock() {
        super.lock();

        // TODO sort conversions from most complex to least complex
    }

    public void registerVanillaDefaults() {
        if(!this.isLocked()) {

            //General Purpose
            this.register(ItemProjectileData.NAME, ItemProjectileData::new);
            this.register(EntitySpawnProjectileData.NAME, EntitySpawnProjectileData::new);

            // Basic arrow
            final IProjectileData<AbstractArrow> arrowData = new ArrowProjectileData();
            this.register(ArrowProjectileData.NAME, () -> arrowData);
            registerItemStackConversation(new ItemStack(Items.ARROW), (itemStack) -> arrowData);

            // Spectral arrow
            final IProjectileData<AbstractArrow> spectralArrowData = new SpectralArrowProjectileData();
            this.register(SpectralArrowProjectileData.NAME, () -> spectralArrowData);
            registerItemStackConversation(new ItemStack(Items.SPECTRAL_ARROW), (itemStack) -> spectralArrowData);

            // Tipped arrow
            this.register(TippedArrowProjectileData.NAME, TippedArrowProjectileData::new);
            registerItemStackConversation(new ItemStack(Items.TIPPED_ARROW), (itemStack) -> new TippedArrowProjectileData().setArrowItem(itemStack)); //TODO cache ?

            //Items
            registerItemStackConversation(new ItemStack(Items.SNOWBALL), new CachedProjectileData(() -> new EntitySpawnProjectileData("minecraft:snowball")));
            registerItemStackConversation(new ItemStack(Items.EGG), new CachedProjectileData(() -> new EntitySpawnProjectileData("minecraft:egg")));

            // Spawn eggs, PORT: 1.12 had one spawn egg item, 1.21 has one item per entity type.
            // Handled generically in find() so modded eggs (NeoForge DeferredSpawnEggItem, not in SpawnEggItem.eggs()) work too
            //TODO tools as projectiles... because diggy diggy dwarf

            // TODO implement simple block renders as EntityFallingBlock
        }
    }

    public IProjectileData find(ItemStack itemStack) {
        if(itemConversions.containsKey(itemStack.getItem())) {
            final List<ProjectileItemConversion> possibleConversions = itemConversions.get(itemStack.getItem());
            for(ProjectileItemConversion conversion : possibleConversions) {
                final IProjectileData data = conversion.getBuilder().apply(itemStack);
                if(data != null) {
                    return data;
                }
            }
        }
        // Any spawn egg, vanilla or modded, checked after registered conversions so addons can still override a specific egg
        if(itemStack.getItem() instanceof SpawnEggItem egg) {
            final EntitySpawnProjectileData eggData = createSpawnEggData(egg, itemStack);
            if(eggData != null) {
                return eggData;
            }
        }
        final IProjectileStack data = itemStack.getCapability(ICBMClassicAPI.PROJECTILE_STACK_CAPABILITY);
        if(data != null) {
            return data.getProjectileData();
        }
        return new ItemProjectileData().setItemStack(itemStack.copy());
    }

    @Nullable
    private static EntitySpawnProjectileData createSpawnEggData(SpawnEggItem egg, ItemStack itemStack) {
        final EntityType<?> type = egg.getType(itemStack);
        if(type == null) {
            return null;
        }
        final EntitySpawnProjectileData projectileData = new EntitySpawnProjectileData(BuiltInRegistries.ENTITY_TYPE.getKey(type));
        if(itemStack.has(DataComponents.CUSTOM_NAME)) {
            projectileData.setEntityDisplayTag(itemStack.getHoverName().getString());
        }
        final CustomData entityTag = itemStack.get(DataComponents.ENTITY_DATA);
        if(entityTag != null && !entityTag.isEmpty()) {
            projectileData.setEntityData(entityTag.copyTag());
        }
        return projectileData;
    }

    @Override
    public void registerItemStackConversation(ItemStack itemStack, Function<ItemStack, IProjectileData> builder) {
        itemConversions.computeIfAbsent(itemStack.getItem(), k -> new ArrayList<>());
        itemConversions.get(itemStack.getItem()).add(new ProjectileItemConversion(itemStack, builder));
    }

    @Override
    public Entity spawnProjectile(ResourceLocation key, Level world, double x, double y, double z, @Nullable Entity source,
                                  boolean allowItemPickup, @Nullable Consumer<Entity> preSpawnCallback) {
        return spawnProjectile(this.getOrBuild(key), world, x, y, z, source, allowItemPickup, preSpawnCallback);
    }

    @Override
    public Entity spawnProjectile(ItemStack item, Level world, double x, double y, double z, @Nullable Entity source,
                                  boolean allowItemPickup, @Nullable Consumer<Entity> preSpawnCallback) {
        return spawnProjectile(find(item), world, x, y, z, source, allowItemPickup, preSpawnCallback);
    }

    @Override
    public <E extends Entity> E  spawnProjectile(IProjectileData<E> data, Level world, double x, double y, double z, Entity source,
                                  boolean allowItemPickup, Consumer<E> preSpawnCallback) {
        if (data == null) {
            return null;
        }
        final E entity = data.newEntity(world, allowItemPickup);
        if (entity == null) {
            return null;
        }

        entity.setPos(x, y, z);
        if (preSpawnCallback != null) {
            preSpawnCallback.accept(entity);
        }
        if (world.addFreshEntity(entity)) {
            data.onEntitySpawned(entity, source, InteractionHand.MAIN_HAND);
            return entity;
        }
        return null;
    }
}
// PORTED

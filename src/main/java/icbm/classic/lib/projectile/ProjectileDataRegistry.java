package icbm.classic.lib.projectile;

import icbm.classic.api.missiles.projectile.IProjectileData;
import icbm.classic.api.missiles.projectile.IProjectileDataRegistry;
import icbm.classic.lib.buildable.BuildableObjectRegistry;
import icbm.classic.lib.projectile.vanilla.*;
import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemMonsterPlacer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

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
            final IProjectileData<EntityArrow> arrowData = new ArrowProjectileData();
            this.register(ArrowProjectileData.NAME, () -> arrowData);
            registerItemStackConversation(new ItemStack(Items.ARROW), (itemStack) -> arrowData);

            // Spectral arrow
            final IProjectileData<EntityArrow> spectralArrowData = new SpectralArrowProjectileData();
            this.register(SpectralArrowProjectileData.NAME, () -> spectralArrowData);
            registerItemStackConversation(new ItemStack(Items.SPECTRAL_ARROW), (itemStack) -> spectralArrowData);

            // Tipped arrow
            this.register(TippedArrowProjectileData.NAME, TippedArrowProjectileData::new);
            registerItemStackConversation(new ItemStack(Items.TIPPED_ARROW), (itemStack) -> new TippedArrowProjectileData().setArrowItem(itemStack)); //TODO cache ?

            //Items
            registerItemStackConversation(new ItemStack(Items.SNOWBALL), new CachedProjectileData(() -> new EntitySpawnProjectileData("minecraft:snowball")));
            registerItemStackConversation(new ItemStack(Items.EGG), new CachedProjectileData(() -> new EntitySpawnProjectileData("minecraft:egg")));

            // Spawn eggs
            registerItemStackConversation(new ItemStack(Items.SPAWN_EGG), (itemStack) -> {
                final EntitySpawnProjectileData projectileData = new EntitySpawnProjectileData(ItemMonsterPlacer.getNamedIdFrom(itemStack));
                if(itemStack.hasDisplayName()) {
                    projectileData.setDisplayName(itemStack.getDisplayName());
                }
                if(itemStack.getTagCompound() != null && itemStack.getTagCompound().hasKey("EntityTag", 10)) {
                    projectileData.setEntityData(itemStack.getTagCompound().getCompoundTag("EntityTag"));
                }
                return projectileData;
            });
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
        return new ItemProjectileData().setItemStack(itemStack.copy());
    }

    @Override
    public void registerItemStackConversation(ItemStack itemStack, Function<ItemStack, IProjectileData> builder) {
        itemConversions.computeIfAbsent(itemStack.getItem(), k -> new ArrayList<>());
        itemConversions.get(itemStack.getItem()).add(new ProjectileItemConversion(itemStack, builder));
    }

    @Override
    public Entity spawnProjectile(ResourceLocation key, World world, double x, double y, double z, @Nullable Entity source,
                                  boolean allowItemPickup, @Nullable Consumer<Entity> preSpawnCallback) {
        return spawnProjectile(this.build(key), world, x, y, z, source, allowItemPickup, preSpawnCallback);
    }

    @Override
    public Entity spawnProjectile(ItemStack item, World world, double x, double y, double z, @Nullable Entity source,
                                  boolean allowItemPickup, @Nullable Consumer<Entity> preSpawnCallback) {
        return spawnProjectile(find(item), world, x, y, z, source, allowItemPickup, preSpawnCallback);
    }

    @Override
    public <E extends Entity> E  spawnProjectile(IProjectileData<E> data, World world, double x, double y, double z, Entity source,
                                  boolean allowItemPickup, Consumer<E> preSpawnCallback) {
        if(data != null) {
            final E entity = data.newEntity(world, allowItemPickup);
            if(entity != null) {
                entity.setPosition(x, y, z);
                if(preSpawnCallback != null) {
                    preSpawnCallback.accept(entity);
                }
                if(world.spawnEntity(entity)) {
                    data.onEntitySpawned(entity, source);
                    return entity;
                }
            }
        }
        return null;
    }
}

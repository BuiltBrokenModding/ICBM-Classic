package icbm.classic.lib.projectile.vanilla;

import icbm.classic.api.ICBMClassicAPI;
import icbm.classic.api.missiles.projectile.IProjectileData;
import icbm.classic.api.missiles.projectile.IProjectileDataRegistry;
import icbm.classic.lib.buildable.BuildableObject;
import icbm.classic.lib.saving.NbtSaveHandler;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.core.registries.BuiltInRegistries;

import java.util.Optional;
import java.util.UUID;

public class EntitySpawnProjectileData extends BuildableObject<EntitySpawnProjectileData, IProjectileDataRegistry> implements IProjectileData<Entity> {

    public static final ResourceLocation NAME = ResourceLocation.fromNamespaceAndPath("minecraft", "entity");

    private ResourceLocation entityKey;

    @Getter @Setter @Accessors(chain = true)
    private String entityDisplayTag;
    @Getter @Setter @Accessors(chain = true)
    private CompoundTag entityData;

    public EntitySpawnProjectileData() {
        super(NAME, ICBMClassicAPI.PROJECTILE_DATA_REGISTRY, SAVE_LOGIC);
    }

    public EntitySpawnProjectileData(ResourceLocation key) {
        this();
        this.entityKey = key;
    }

    public EntitySpawnProjectileData(String name) {
        this(ResourceLocation.parse(name));
    }

    @Override
    public Entity newEntity(Level world, boolean allowItemPickup) {
        if(entityKey != null) {
            final Optional<EntityType<?>> entry = BuiltInRegistries.ENTITY_TYPE.getOptional(entityKey);
            if(entry.isPresent()) {
                final Entity entity = entry.get().create(world);
                if(entity != null) {
                    if(entityDisplayTag != null) {
                        entity.setCustomName(Component.literal(entityDisplayTag));
                    }
                    if(entityData != null) {
                        final CompoundTag entityExistingSave = entity.saveWithoutId(new CompoundTag());
                        final UUID uuid = entity.getUUID();
                        entityExistingSave.merge(entityData);
                        entity.load(entityExistingSave);
                        entity.setUUID(uuid);
                    }
                }
                return entity;
            }
        }
        return null;
    }

    private static final NbtSaveHandler<EntitySpawnProjectileData> SAVE_LOGIC = new NbtSaveHandler<EntitySpawnProjectileData>()
        .mainRoot()
        /* */.nodeResourceLocation("entity_id", (e) -> e.entityKey, (e, r) -> e.entityKey = r)
        /* */.nodeString("custom_name", EntitySpawnProjectileData::getEntityDisplayTag, EntitySpawnProjectileData::setEntityDisplayTag)
        /* */.nodeCompoundTag("entity_data", EntitySpawnProjectileData::getEntityData, EntitySpawnProjectileData::setEntityData)
        .base();
}
// PORTED

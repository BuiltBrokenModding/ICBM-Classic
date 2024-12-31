package icbm.classic.lib.projectile.vanilla;

import icbm.classic.api.ICBMClassicAPI;
import icbm.classic.api.missiles.projectile.IProjectileData;
import icbm.classic.api.missiles.projectile.IProjectileDataRegistry;
import icbm.classic.lib.buildable.BuildableObject;
import icbm.classic.lib.saving.NbtSaveHandler;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.UUID;

public class EntitySpawnProjectileData extends BuildableObject<EntitySpawnProjectileData, IProjectileDataRegistry> implements IProjectileData<Entity> {

    public static final ResourceLocation NAME = new ResourceLocation("minecraft", "entity");

    private ResourceLocation entityKey;

    @Getter @Setter @Accessors(chain = true)
    private String entityDisplayTag;
    @Getter @Setter @Accessors(chain = true)
    private CompoundNBT entityData;

    public EntitySpawnProjectileData() {
        super(NAME, ICBMClassicAPI.PROJECTILE_DATA_REGISTRY, SAVE_LOGIC);
    }

    public EntitySpawnProjectileData(ResourceLocation key) {
        this();
        this.entityKey = key;
    }

    public EntitySpawnProjectileData(String name) {
        this(new ResourceLocation(name));
    }

    @Override
    public Entity newEntity(World world, boolean allowItemPickup) {
        if(entityKey != null) {
            final EntityType<?> entry = ForgeRegistries.ENTITIES.getValue(entityKey);
            if(entry != null) {
                final Entity entity = entry.create(world);
                if(entity != null) {
                    if(entityDisplayTag != null) {
                        entity.setCustomName(new StringTextComponent(entityDisplayTag));
                    }
                    if(entityData != null) {
                        final CompoundNBT entityExistingSave = entity.writeWithoutTypeId(new CompoundNBT());
                        final UUID uuid = entity.getUniqueID();
                        entityExistingSave.merge(entityData);
                        entity.setUniqueId(uuid);
                        entity.read(entityExistingSave);
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

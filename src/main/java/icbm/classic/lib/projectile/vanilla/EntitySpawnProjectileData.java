package icbm.classic.lib.projectile.vanilla;

import icbm.classic.api.missiles.projectile.IProjectileData;
import icbm.classic.content.missile.logic.source.MissileSource;
import icbm.classic.lib.saving.NbtSaveHandler;
import icbm.classic.lib.saving.NbtSaveRoot;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.EntityEntry;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

import java.util.UUID;

@NoArgsConstructor
public class EntitySpawnProjectileData implements IProjectileData<Entity> {

    public static final ResourceLocation NAME = new ResourceLocation("minecraft", "entity");

    private ResourceLocation entityKey;

    @Getter @Setter @Accessors(chain = true)
    private String displayName;
    @Getter @Setter @Accessors(chain = true)
    private NBTTagCompound entityData;

    public EntitySpawnProjectileData(ResourceLocation key) {
        this.entityKey = key;
    }

    public EntitySpawnProjectileData(String name) {
        this(new ResourceLocation(name));
    }

    @Override
    public ResourceLocation getRegistryName() {
        return NAME;
    }

    @Override
    public Entity newEntity(World world, boolean allowItemPickup) {
        if(entityKey != null) {
            final EntityEntry entry = ForgeRegistries.ENTITIES.getValue(entityKey);
            if(entry != null) {
                final Entity entity = entry.newInstance(world);
                if(entity != null) {
                    if(displayName != null) {
                        entity.setCustomNameTag(displayName);
                    }
                    if(entityData != null) {
                        final NBTTagCompound entityExistingSave = entity.writeToNBT(new NBTTagCompound());
                        final UUID uuid = entity.getUniqueID();
                        entityExistingSave.merge(entityData);
                        entity.setUniqueId(uuid);
                        entity.readFromNBT(entityExistingSave);
                    }
                }
                return entity;
            }
        }
        return null;
    }

    @Override
    public NBTTagCompound serializeNBT() {
        return SAVE_LOGIC.save(this);
    }

    @Override
    public void deserializeNBT(NBTTagCompound nbt) {
       SAVE_LOGIC.load(this, nbt);
    }

    private static final NbtSaveHandler<EntitySpawnProjectileData> SAVE_LOGIC = new NbtSaveHandler<EntitySpawnProjectileData>()
        .mainRoot()
        /* */.nodeResourceLocation("entity_id", (e) -> e.entityKey, (e, r) -> e.entityKey = r)
        /* */.nodeString("custom_name", EntitySpawnProjectileData::getDisplayName, EntitySpawnProjectileData::setDisplayName)
        /* */.nodeCompoundTag("entity_data", EntitySpawnProjectileData::getEntityData, EntitySpawnProjectileData::setEntityData)
        .base();
}

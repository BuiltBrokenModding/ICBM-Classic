package icbm.classic.content.cargo;

import icbm.classic.ICBMClassic;
import icbm.classic.api.ICBMClassicAPI;
import icbm.classic.api.missiles.parts.IBuildableObject;
import icbm.classic.api.missiles.projectile.IProjectileData;
import icbm.classic.api.missiles.projectile.IProjectileDataRegistry;
import icbm.classic.api.missiles.projectile.ProjectileType;
import icbm.classic.api.reg.obj.IBuilderRegistry;
import icbm.classic.lib.saving.NbtSaveHandler;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemMonsterPlacer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.common.util.INBTSerializable;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public abstract class CargoProjectileData<T extends IBuildableObject, ENTITY extends Entity> implements IBuildableObject, IProjectileData<ENTITY>, INBTSerializable<NBTTagCompound> {

    private final static ProjectileType[] TYPE = new ProjectileType[]{ProjectileType.TYPE_ENTITY, ProjectileType.TYPE_HOLDER, ProjectileType.TYPE_THROWABLE};

    /**
     * ItemStack to use to spawn as a passenger of this parachute
     */
    @Getter
    @Setter
    @Accessors(chain = true)
    private ItemStack heldItem = ItemStack.EMPTY; //TODO make builder


    /**
     * Handle {@link #heldItem} as an entity. Meaning it will attempt to generate
     * the entity version of that item. Which doesn't include spawn eggs or items containing entities.
     */
    @Getter
    @Setter
    @Accessors(chain = true)
    private ProjectileCargoMode parachuteMode = ProjectileCargoMode.ITEM;

    @Nonnull
    @Override
    public IProjectileDataRegistry getRegistry() {
        return ICBMClassicAPI.PROJECTILE_DATA_REGISTRY;
    }

    @Override
    public ProjectileType[] getTypes() {
        return TYPE;
    }

    @Override
    public ITextComponent getTooltip() {
        return new TextComponentTranslation(
            getTranslationKey() + ".info." + parachuteMode.name().toLowerCase(),
            heldItem.getItem().getItemStackDisplayName(heldItem)
        );
    }

    @Override
    public void onEntitySpawned(@Nonnull ENTITY entity, @Nullable Entity source) {
        if (!heldItem.isEmpty()) {
            switch (parachuteMode) {
                case PROJECTILE:
                    spawnProjectile(entity);
                    return;
                case ENTITY:
                    spawnEntity(entity);
                    return;
                case BLOCK:
                    spawnBlockEntity(entity);
                    return;
                default:
                    spawnItemEntity(entity);
                    return;
            }
        }
    }

    private void spawnProjectile(@Nonnull ENTITY entity) {
        final Entity projectile =
            ICBMClassicAPI.PROJECTILE_DATA_REGISTRY.spawnProjectile(heldItem,
                entity.world, entity.posX, entity.posY, entity.posZ,
                entity, true, (proj) -> {
                    // TODO add extra logic for parachute spawning
                    proj.rotationPitch = proj.prevRotationPitch = entity.rotationPitch;
                    proj.rotationYaw = proj.prevRotationYaw = entity.rotationYaw;
                }
            );

        if (projectile != null) {
            projectile.startRiding(entity);
        } else {
            spawnItemEntity(entity);
        }
    }

    private void spawnEntity(@Nonnull ENTITY entity) {
        //TODO for some entities attempt to render a parachute on their model instead of acting as a mount

        if (heldItem.getItem() instanceof ItemMonsterPlacer) {
            final Entity mob = ItemMonsterPlacer.spawnCreature(entity.world, ItemMonsterPlacer.getNamedIdFrom(heldItem), entity.posX, entity.posY, entity.posZ);
            if (mob != null) {
                mob.startRiding(entity);

                if (mob instanceof EntityLivingBase && heldItem.hasDisplayName()) {
                    entity.setCustomNameTag(heldItem.getDisplayName());
                }

                ItemMonsterPlacer.applyItemEntityDataToEntity(entity.world, null, heldItem, mob);
            } else {
                ICBMClassic.logger().warn("ParachuteProjectile: unknown item for entity spawning. Data: {}, Item: {}", this, heldItem);
                spawnItemEntity(entity);
            }
        } else {
            ICBMClassic.logger().warn("ParachuteProjectile: unknown item for entity spawning. Data: {}, Item: {}", this, heldItem);
            spawnItemEntity(entity);
        }
    }

    private void spawnItemEntity(@Nonnull ENTITY entity) {
       final EntityItem entityItem = createItemEntity(entity);

        // Spawn item
        if (!entity.world.spawnEntity(entityItem)) {
            ICBMClassic.logger().error("CargoProjectileData: Failed to spawn held item as {}, this likely resulted in loss of items", entityItem);
            //TODO see if we can undo cargo spawn if this fails
        }

        // Attach to host entity (parachute/balloon)
        if (!entityItem.startRiding(entity)) {
            ICBMClassic.logger().error("CargoProjectileData: Failed to set {} as rider of {}, this likely resulted in loss of items", entityItem, entity);
            //TODO see if we can undo cargo spawn if this fails
        }
    }

    private EntityItem createItemEntity(@Nonnull ENTITY entity) {
        final EntityItem entityItem = new EntityItem(entity.world);
        entityItem.setItem(heldItem.copy());
        entityItem.setPosition(entity.posX, entity.posY, entity.posZ);
        entityItem.setDefaultPickupDelay();
        return entityItem;
    }

    private void spawnBlockEntity(@Nonnull ENTITY entity) {

    }

    @Override
    public NBTTagCompound serializeNBT() {
        return SAVE_LOGIC.save(this);
    }

    @Override
    public void deserializeNBT(NBTTagCompound nbt) {
        SAVE_LOGIC.load(this, nbt);
    }

    private static final NbtSaveHandler<CargoProjectileData> SAVE_LOGIC = new NbtSaveHandler<CargoProjectileData>()
        .mainRoot()
        .nodeItemStack("stack", CargoProjectileData::getHeldItem, CargoProjectileData::setHeldItem)
        .nodeEnumString("mode", CargoProjectileData::getParachuteMode, CargoProjectileData::setParachuteMode, ProjectileCargoMode::valueOf)
        .base();
}
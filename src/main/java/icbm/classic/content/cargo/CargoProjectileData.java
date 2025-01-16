package icbm.classic.content.cargo;

import com.google.common.collect.ImmutableList;
import icbm.classic.ICBMClassic;
import icbm.classic.api.ICBMClassicAPI;
import icbm.classic.api.actions.data.EntityActionTypes;
import icbm.classic.api.data.meta.MetaTag;
import icbm.classic.api.missiles.projectile.IProjectileData;
import icbm.classic.api.missiles.projectile.IProjectileDataRegistry;
import icbm.classic.api.missiles.projectile.ProjectileTypes;
import icbm.classic.api.reg.obj.IBuildableObject;
import icbm.classic.content.entity.flyingblock.BlockCaptureData;
import icbm.classic.content.entity.flyingblock.FlyingBlock;
import icbm.classic.lib.saving.NbtSaveHandler;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SpawnEggItem;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Hand;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.common.util.INBTSerializable;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;

public abstract class CargoProjectileData<T extends IBuildableObject, ENTITY extends Entity> implements IBuildableObject, IProjectileData<ENTITY>, INBTSerializable<CompoundNBT> {

    private final static ImmutableList<MetaTag> TYPE = ImmutableList.of(EntityActionTypes.ENTITY_CREATION, ProjectileTypes.TYPE_HOLDER, ProjectileTypes.TYPE_THROWABLE);

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

    @Nonnull
    @Override
    public Collection<MetaTag> getTypeTags() {
        return TYPE;
    }

    @Override
    public ITextComponent getTooltip() {
        return new TranslationTextComponent(getTranslationKey() + ".info." + parachuteMode.name().toLowerCase());
    }

    @Override
    public void onEntitySpawned(@Nonnull ENTITY entity, @Nullable Entity source, @Nullable Hand hand) {
        if (!heldItem.isEmpty()) {
            switch (parachuteMode) {
                case PROJECTILE:
                    spawnProjectile(entity);
                    return;
                case ENTITY:
                    spawnEntity(entity);
                    return;
                case BLOCK:
                    spawnBlockEntity(entity, source, hand);
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

        if (entity.world.isRemote) {
            return;
        }

        if (heldItem.getItem() instanceof SpawnEggItem) {
            final EntityType entityType = ((SpawnEggItem)heldItem.getItem()).getType(heldItem.getTag());
            final Entity mob = entityType.spawn(entity.world, heldItem, null, entity.getPosition(), SpawnReason.SPAWN_EGG, false, false);
            if (mob != null) {
                mob.startRiding(entity);
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
        final ItemEntity entityItem = createItemEntity(entity);

        // Spawn item
        if (!entity.world.addEntity(entityItem)) {
            ICBMClassic.logger().error("CargoProjectileData: Failed to spawn held item as {}, this likely resulted in loss of items", entityItem);
            //TODO see if we can undo cargo spawn if this fails
        }

        // Attach to host entity (parachute/balloon)
        if (!entityItem.startRiding(entity)) {
            ICBMClassic.logger().error("CargoProjectileData: Failed to set {} as rider of {}, this likely resulted in loss of items", entityItem, entity);
            //TODO see if we can undo cargo spawn if this fails
        }
    }

    private ItemEntity createItemEntity(@Nonnull ENTITY entity) {
        final ItemEntity entityItem = new ItemEntity(EntityType.ITEM, entity.world);
        entityItem.setItem(heldItem.copy());
        entityItem.rotationYaw = entity.world.rand.nextFloat() * 360.0F;
        entityItem.setPosition(entity.posX, entity.posY, entity.posZ);
        entityItem.setDefaultPickupDelay();
        return entityItem;
    }

    private void spawnBlockEntity(@Nonnull ENTITY entity, Entity source, Hand hand) {
        if (!(heldItem.getItem() instanceof BlockItem)) { //TODO handle blocks that have non-itemBlock entities
            spawnItemEntity(entity);
            return;
        }
        final BlockState iblockstate = ((BlockItem) heldItem.getItem()).getBlock().getDefaultState();


        // TODO add itemstack to flying block for better placement and handling of TE data
        final BlockCaptureData blockCaptureData = new BlockCaptureData(iblockstate, heldItem.copy());
        if (!FlyingBlock.spawnFlyingBlock(entity.world, entity.posX, entity.posY, entity.posZ, blockCaptureData, (flyingBlock) -> flyingBlock.startRiding(entity), null, null)) {
            spawnItemEntity(entity);
        }
    }

    @Override
    public CompoundNBT serializeNBT() {
        return SAVE_LOGIC.save(this);
    }

    @Override
    public void deserializeNBT(CompoundNBT nbt) {
        SAVE_LOGIC.load(this, nbt);
    }

    private static final NbtSaveHandler<CargoProjectileData> SAVE_LOGIC = new NbtSaveHandler<CargoProjectileData>()
        .mainRoot()
        .nodeItemStack("stack", CargoProjectileData::getHeldItem, CargoProjectileData::setHeldItem)
        .nodeEnumString("mode", CargoProjectileData::getParachuteMode, CargoProjectileData::setParachuteMode, ProjectileCargoMode::valueOf)
        .base();
}
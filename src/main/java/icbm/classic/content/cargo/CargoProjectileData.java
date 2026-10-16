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
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import net.neoforged.neoforge.common.util.INBTSerializable;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;

public abstract class CargoProjectileData<T extends IBuildableObject, ENTITY extends Entity> implements IBuildableObject, IProjectileData<ENTITY>, INBTSerializable<CompoundTag> {

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

    @Nonnull
    @Override
    public Component getTooltip() {
        return Component.translatable(getTranslationKey() + ".info." + parachuteMode.name().toLowerCase());
    }

    @Override
    public void onEntitySpawned(@Nonnull ENTITY entity, @Nullable Entity source, @Nullable InteractionHand hand) {
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
                entity.level(), entity.getX(), entity.getY(), entity.getZ(),
                entity, true, (proj) -> {
                    // TODO add extra logic for parachute spawning
                    proj.setXRot(entity.getXRot());
                    proj.xRotO = entity.getXRot();
                    proj.setYRot(entity.getYRot());
                    proj.yRotO = entity.getYRot();
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

        if (entity.level().isClientSide()) {
            return;
        }

        if (heldItem.getItem() instanceof SpawnEggItem egg) {
            // PORT: 1.12 ItemMonsterPlacer.spawnCreature + applyItemEntityDataToEntity, EntityType#spawn applies stack name and entity data
            final EntityType<?> type = egg.getType(heldItem);
            final Entity mob = type.spawn((ServerLevel) entity.level(), heldItem, null, entity.blockPosition(), MobSpawnType.SPAWN_EGG, false, false);
            if (mob != null) {
                mob.moveTo(entity.getX(), entity.getY(), entity.getZ(), mob.getYRot(), mob.getXRot());
                mob.startRiding(entity);

                if (mob instanceof LivingEntity && heldItem.has(DataComponents.CUSTOM_NAME)) {
                    // PORT: kept 1.12 behaviour, names the holder entity
                    entity.setCustomName(heldItem.getHoverName());
                }
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

        // Attach to host entity (parachute/balloon)
        // PORT: 1.12 spawned then mounted, mounting first makes the item's spawn packet carry the passenger link
        // so clients never see the cargo without its holder
        if (!entityItem.startRiding(entity)) {
            ICBMClassic.logger().error("CargoProjectileData: Failed to set {} as rider of {}, this likely resulted in loss of items", entityItem, entity);
            //TODO see if we can undo cargo spawn if this fails
        }

        // Spawn item
        if (!entity.level().addFreshEntity(entityItem)) {
            // Don't leave a rider that isn't in the world attached to the holder
            entityItem.stopRiding();
            ICBMClassic.logger().error("CargoProjectileData: Failed to spawn held item as {}, this likely resulted in loss of items", entityItem);
            //TODO see if we can undo cargo spawn if this fails
        }
    }

    private ItemEntity createItemEntity(@Nonnull ENTITY entity) {
        final ItemEntity entityItem = new ItemEntity(EntityType.ITEM, entity.level());
        entityItem.setItem(heldItem.copy());
        entityItem.setPos(entity.getX(), entity.getY(), entity.getZ());
        entityItem.setDefaultPickUpDelay();
        return entityItem;
    }

    private void spawnBlockEntity(@Nonnull ENTITY entity, Entity source, InteractionHand hand) {
        if (!(heldItem.getItem() instanceof BlockItem blockItem)) { //TODO handle blocks that have non-itemBlock entities
            spawnItemEntity(entity);
            return;
        }
        BlockState iblockstate = null;

        try {
            // TODO if source is a missile try to get caused by player
            // PORT: placement context needs a Player, 1.12 accepted any living entity
            final Player placer = source instanceof Player ? (Player) source : FakePlayerFactory.getMinecraft((ServerLevel) entity.level());
            final BlockPos pos = entity.blockPosition();
            final BlockHitResult hit = new BlockHitResult(new Vec3(pos.getX() + 0.5, pos.getY() + 1, pos.getZ() + 0.5), Direction.NORTH, pos, false);
            iblockstate = blockItem.getBlock().getStateForPlacement(new BlockPlaceContext(entity.level(), placer, hand != null ? hand : InteractionHand.MAIN_HAND, heldItem, hit));
        } catch (Exception e) {
            ICBMClassic.logger().error("CargoProjectileData: Failed to use Block#getStateForPlacement to get block state. This may cause incorrect block placements", e);
        }
        if (iblockstate == null) {
            iblockstate = blockItem.getBlock().defaultBlockState();
        }

        // TODO add itemstack to flying block for better placement and handling of TE data
        final BlockCaptureData blockCaptureData = new BlockCaptureData(iblockstate, heldItem.copy());
        if (!FlyingBlock.spawnFlyingBlock(entity.level(), entity.getX(), entity.getY(), entity.getZ(), blockCaptureData, (flyingBlock) -> flyingBlock.startRiding(entity), null, null)) {
            spawnItemEntity(entity);
        }
    }

    @Override
    public CompoundTag serializeNBT(HolderLookup.Provider provider) {
        return SAVE_LOGIC.save(this);
    }

    @Override
    public void deserializeNBT(HolderLookup.Provider provider, CompoundTag nbt) {
        SAVE_LOGIC.load(this, nbt);
    }

    private static final NbtSaveHandler<CargoProjectileData> SAVE_LOGIC = new NbtSaveHandler<CargoProjectileData>()
        .mainRoot()
        .nodeItemStack("stack", CargoProjectileData::getHeldItem, CargoProjectileData::setHeldItem)
        .nodeEnumString("mode", CargoProjectileData::getParachuteMode, CargoProjectileData::setParachuteMode, ProjectileCargoMode::valueOf)
        .base();
}
// PORTED

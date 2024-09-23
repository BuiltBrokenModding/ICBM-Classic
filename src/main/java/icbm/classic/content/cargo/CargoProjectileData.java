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
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemMonsterPlacer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.common.util.INBTSerializable;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;

public abstract class CargoProjectileData<T extends IBuildableObject, ENTITY extends Entity> implements IBuildableObject, IProjectileData<ENTITY>, INBTSerializable<NBTTagCompound> {

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
        return new TextComponentTranslation(getTranslationKey() + ".info." + parachuteMode.name().toLowerCase());
    }

    @Override
    public void onEntitySpawned(@Nonnull ENTITY entity, @Nullable Entity source, @Nullable EnumHand hand) {
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

    private void spawnBlockEntity(@Nonnull ENTITY entity, Entity source, EnumHand hand) {
        if (!(heldItem.getItem() instanceof ItemBlock)) { //TODO handle blocks that have non-itemBlock entities
            spawnItemEntity(entity);
            return;
        }
        int i = heldItem.getItem().getMetadata(heldItem.getMetadata());
        IBlockState iblockstate = null;

        try {
            // TODO if source is a missile try to get caused by player
            final EntityLivingBase entityLivingBase = source instanceof EntityLivingBase ? (EntityLivingBase) source : FakePlayerFactory.getMinecraft((WorldServer) entity.world);
            iblockstate = ((ItemBlock) heldItem.getItem()).getBlock()
                .getStateForPlacement(entity.world, entity.getPosition(), EnumFacing.NORTH, 0.5f, 1f, 0.5f, i, entityLivingBase, hand);
        } catch (Exception e) {
            ICBMClassic.logger().error("CargoProjectileData: Failed to use Block#getStateForPlacement to get block state. This may cause incorrect block placements", e);
            iblockstate = ((ItemBlock) heldItem.getItem()).getBlock().getStateFromMeta(i);
        }

        // TODO add itemstack to flying block for better placement and handling of TE data
        final BlockCaptureData blockCaptureData = new BlockCaptureData(iblockstate, heldItem.copy());
        if (!FlyingBlock.spawnFlyingBlock(entity.world, entity.posX, entity.posY, entity.posZ, blockCaptureData, (flyingBlock) -> flyingBlock.startRiding(entity), null, null)) {
            spawnItemEntity(entity);
        }
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
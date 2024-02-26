package icbm.classic.content.parachute;

import icbm.classic.ICBMClassic;
import icbm.classic.ICBMConstants;
import icbm.classic.api.ICBMClassicAPI;
import icbm.classic.api.missiles.projectile.IProjectileData;
import icbm.classic.api.missiles.projectile.IProjectileDataRegistry;
import icbm.classic.api.missiles.projectile.ProjectileType;
import icbm.classic.api.reg.obj.IBuilderRegistry;
import icbm.classic.lib.buildable.BuildableObject;
import icbm.classic.lib.saving.NbtSaveHandler;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemMonsterPlacer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.common.util.INBTSerializable;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

@EqualsAndHashCode(callSuper = false)
public class ParachuteProjectileData extends BuildableObject<ParachuteProjectileData, IProjectileDataRegistry> implements IProjectileData<EntityParachute>, INBTSerializable<NBTTagCompound> {

    private final static ProjectileType[] TYPE = new ProjectileType[]{ProjectileType.TYPE_ENTITY, ProjectileType.TYPE_HOLDER};
    public final static ResourceLocation NAME = new ResourceLocation(ICBMConstants.DOMAIN, "holder.parachute");

    /**
     * ItemStack to use to spawn as a passenger of this parachute
     */
    @Getter
    @Setter
    @Accessors(chain = true)
    private ItemStack heldItem = ItemStack.EMPTY;


    /**
     * Handle {@link #heldItem} as an entity. Meaning it will attempt to generate
     * the entity version of that item. Which doesn't include spawn eggs or items containing entities.
     */
    @Getter
    @Setter
    @Accessors(chain = true)
    private ParachuteMode parachuteMode = ParachuteMode.ITEM;

    public ParachuteProjectileData() {
        super(NAME, ICBMClassicAPI.PROJECTILE_DATA_REGISTRY, SAVE_LOGIC);
    }

    @Override
    public ProjectileType[] getTypes() {
        return TYPE;
    }

    @Override
    public EntityParachute newEntity(World world, boolean allowItemPicku) {
        return new EntityParachute(world); //.setRenderItemStack(parachute);
    }

    @Override
    public ITextComponent getTooltip() {
        return new TextComponentTranslation(
            getTranslationKey() + ".info." + parachuteMode.name().toLowerCase(),
            heldItem.getItem().getItemStackDisplayName(heldItem)
        );
    }

    @Override
    public void onEntitySpawned(@Nonnull EntityParachute entity, @Nullable Entity source) {
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
                case ITEM:
                default:
                    spawnItemEntity(entity);
                    return;
            }
        }
    }

    private void spawnProjectile(@Nonnull EntityParachute entity) {

    }

    private void spawnEntity(@Nonnull EntityParachute entity) {
        if(heldItem.getItem() instanceof ItemMonsterPlacer) {
            final Entity mob = ItemMonsterPlacer.spawnCreature(entity.world, ItemMonsterPlacer.getNamedIdFrom(heldItem), entity.x(), entity.y(), entity.z());
            if(mob != null) {
                mob.startRiding(entity);

                if (mob instanceof EntityLivingBase && heldItem.hasDisplayName())
                {
                    entity.setCustomNameTag(heldItem.getDisplayName());
                }

                ItemMonsterPlacer.applyItemEntityDataToEntity(entity.world, null, heldItem, mob);
            }
            else {
                ICBMClassic.logger().warn("ParachuteProjectile: unknown item for entity spawning. Data: {}, Item: {}", this, heldItem);
                spawnItemEntity(entity);
            }
        }
        else {
            ICBMClassic.logger().warn("ParachuteProjectile: unknown item for entity spawning. Data: {}, Item: {}", this, heldItem);
            spawnItemEntity(entity);
        }
    }

    private void spawnItemEntity(@Nonnull EntityParachute entity) {
        final EntityItem entityItem = new EntityItem(entity.world);
        entityItem.setItem(heldItem.copy());
        entityItem.setPosition(entity.posX, entity.posY, entity.posZ);
        entityItem.setDefaultPickupDelay();

        if(entity.world.spawnEntity(entityItem)) {
            entityItem.startRiding(entityItem);
        }
    }

    private void spawnBlockEntity(@Nonnull EntityParachute entity) {

    }

    private static final NbtSaveHandler<ParachuteProjectileData> SAVE_LOGIC = new NbtSaveHandler<ParachuteProjectileData>()
        //Stuck in ground data
        .mainRoot()
        .nodeItemStack("stack", ParachuteProjectileData::getHeldItem, ParachuteProjectileData::setHeldItem)
        .nodeEnumString("mode", ParachuteProjectileData::getParachuteMode, ParachuteProjectileData::setParachuteMode, ParachuteMode::valueOf) //TODO maybe instead create different projectileData versions for each mode?
        .base();
}

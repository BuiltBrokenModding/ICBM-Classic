package icbm.classic.content.parachute;

import icbm.classic.ICBMConstants;
import icbm.classic.api.missiles.projectile.IProjectileData;
import icbm.classic.api.missiles.projectile.ProjectileType;
import icbm.classic.lib.saving.NbtSaveHandler;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ParachuteProjectileData implements IProjectileData<EntityParachute> {

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

    @Override
    public ProjectileType[] getTypes() {
        return TYPE;
    }

    @Override
    public ResourceLocation getRegistryName() {
        return NAME;
    }

    @Override
    public EntityParachute newEntity(World world, boolean allowItemPicku) {
        return new EntityParachute(world); //.setRenderItemStack(parachute);
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

    }

    private void spawnItemEntity(@Nonnull EntityParachute entity) {

    }

    private void spawnBlockEntity(@Nonnull EntityParachute entity) {

    }

    @Override
    public NBTTagCompound serializeNBT() {
        return null;
    }

    @Override
    public void deserializeNBT(NBTTagCompound nbt) {

    }

    private static final NbtSaveHandler<ParachuteProjectileData> SAVE_LOGIC = new NbtSaveHandler<ParachuteProjectileData>()
        //Stuck in ground data
        .mainRoot()
        .nodeItemStack("stack", ParachuteProjectileData::getHeldItem, ParachuteProjectileData::setHeldItem)
        .nodeEnumString("mode", ParachuteProjectileData::getParachuteMode, ParachuteProjectileData::setParachuteMode, ParachuteMode::valueOf) //TODO maybe instead create different projectileData versions for each mode?
        .base();
}

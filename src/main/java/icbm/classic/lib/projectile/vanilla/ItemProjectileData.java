package icbm.classic.lib.projectile.vanilla;

import icbm.classic.api.ICBMClassicAPI;
import icbm.classic.api.missiles.projectile.IProjectileData;
import icbm.classic.api.missiles.projectile.IProjectileDataRegistry;
import icbm.classic.lib.buildable.BuildableObject;
import icbm.classic.lib.saving.NbtSaveHandler;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

public class ItemProjectileData extends BuildableObject<ItemProjectileData, IProjectileDataRegistry> implements IProjectileData<ItemEntity> {

    public static final ResourceLocation NAME = ResourceLocation.fromNamespaceAndPath("minecraft", "item");

    @Getter @Setter @Accessors(chain = true)
    private ItemStack itemStack = ItemStack.EMPTY;

    public ItemProjectileData() {
        super(NAME, ICBMClassicAPI.PROJECTILE_DATA_REGISTRY, SAVE_LOGIC);
    }

    @Override
    public ItemEntity newEntity(Level world, boolean allowItemPickup) {
        final ItemEntity entityItem = new ItemEntity(EntityType.ITEM, world);
        entityItem.setItem(itemStack);
        if(!allowItemPickup) {
            entityItem.setNeverPickUp();
        }
        return entityItem;
    }

    private static final NbtSaveHandler<ItemProjectileData> SAVE_LOGIC = new NbtSaveHandler<ItemProjectileData>()
        .mainRoot()
        /* */.nodeItemStack("item", ItemProjectileData::getItemStack, ItemProjectileData::setItemStack)
        .base();
}
// PORTED

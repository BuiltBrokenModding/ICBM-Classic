package icbm.classic.lib.projectile.vanilla;

import icbm.classic.api.ICBMClassicAPI;
import icbm.classic.api.missiles.projectile.IProjectileData;
import icbm.classic.api.missiles.projectile.IProjectileDataRegistry;
import icbm.classic.lib.buildable.BuildableObject;
import icbm.classic.lib.saving.NbtSaveHandler;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.projectile.AbstractArrowEntity;
import net.minecraft.entity.projectile.ArrowEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

import javax.annotation.Nonnull;

public class TippedArrowProjectileData extends BuildableObject<TippedArrowProjectileData, IProjectileDataRegistry> implements IProjectileData<AbstractArrowEntity> {

    public static final ResourceLocation NAME = new ResourceLocation("minecraft", "arrow.tipped");

    @Getter @Setter @Accessors(chain = true)
    private ItemStack arrowItem = ItemStack.EMPTY;

    public TippedArrowProjectileData() {
        super(NAME, ICBMClassicAPI.PROJECTILE_DATA_REGISTRY, SAVE_LOGIC);
    }
    @Nonnull
    @Override
    public ResourceLocation getRegistryKey() {
        return NAME;
    }

    @Override
    public AbstractArrowEntity newEntity(World world, boolean allowItemPickup) {
        final ArrowEntity arrow = EntityType.ARROW.create(world);
        arrow.pickupStatus = allowItemPickup ? AbstractArrowEntity.PickupStatus.ALLOWED : AbstractArrowEntity.PickupStatus.DISALLOWED;
        if(!arrowItem.isEmpty()) {
            arrow.setPotionEffect(arrowItem);
        }
        return arrow;
    }

    private static final NbtSaveHandler<TippedArrowProjectileData> SAVE_LOGIC = new NbtSaveHandler<TippedArrowProjectileData>()
        .mainRoot()
        /* */.nodeItemStack("item", TippedArrowProjectileData::getArrowItem, TippedArrowProjectileData::setArrowItem)
        .base();
}

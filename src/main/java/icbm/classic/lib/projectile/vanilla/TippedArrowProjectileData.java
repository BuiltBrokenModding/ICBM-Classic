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
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

import javax.annotation.Nonnull;

public class TippedArrowProjectileData extends BuildableObject<TippedArrowProjectileData, IProjectileDataRegistry> implements IProjectileData<AbstractArrow> {

    public static final ResourceLocation NAME = ResourceLocation.fromNamespaceAndPath("minecraft", "arrow.tipped");

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
    public AbstractArrow newEntity(Level world, boolean allowItemPickup) {
        final Arrow arrow;
        if(!arrowItem.isEmpty()) {
            // PORT: 1.21 arrows read potion contents from their pickup stack
            arrow = new Arrow(world, 0, 0, 0, arrowItem.copyWithCount(1), null); // PORT-CHECK: Arrow(Level, double, double, double, ItemStack, @Nullable ItemStack) ctor
        }
        else {
            arrow = new Arrow(EntityType.ARROW, world);
        }
        arrow.pickup = allowItemPickup ? AbstractArrow.Pickup.ALLOWED : AbstractArrow.Pickup.DISALLOWED;
        return arrow;
    }

    private static final NbtSaveHandler<TippedArrowProjectileData> SAVE_LOGIC = new NbtSaveHandler<TippedArrowProjectileData>()
        .mainRoot()
        /* */.nodeItemStack("item", TippedArrowProjectileData::getArrowItem, TippedArrowProjectileData::setArrowItem)
        .base();
}
// PORTED

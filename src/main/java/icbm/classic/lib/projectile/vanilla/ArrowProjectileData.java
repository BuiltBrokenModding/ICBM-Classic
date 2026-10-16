package icbm.classic.lib.projectile.vanilla;

import icbm.classic.api.ICBMClassicAPI;
import icbm.classic.api.missiles.projectile.IProjectileData;
import icbm.classic.api.missiles.projectile.IProjectileDataRegistry;
import icbm.classic.lib.buildable.BuildableObject;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

public class ArrowProjectileData extends BuildableObject<ArrowProjectileData, IProjectileDataRegistry> implements IProjectileData<AbstractArrow> {

    public static final ResourceLocation NAME = ResourceLocation.fromNamespaceAndPath("minecraft", "arrow.normal");

    public ArrowProjectileData() {
        super(NAME, ICBMClassicAPI.PROJECTILE_DATA_REGISTRY, null);
    }

    @Override
    public AbstractArrow newEntity(Level world, boolean allowItemPickup) {
        final AbstractArrow arrow = new Arrow(EntityType.ARROW, world);
        arrow.pickup = allowItemPickup ? AbstractArrow.Pickup.ALLOWED : AbstractArrow.Pickup.DISALLOWED;
        return arrow;
    }
}
// PORTED

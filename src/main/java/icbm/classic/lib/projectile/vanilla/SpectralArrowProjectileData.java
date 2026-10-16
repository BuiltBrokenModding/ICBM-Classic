package icbm.classic.lib.projectile.vanilla;

import icbm.classic.api.ICBMClassicAPI;
import icbm.classic.api.missiles.projectile.IProjectileData;
import icbm.classic.api.missiles.projectile.IProjectileDataRegistry;
import icbm.classic.lib.buildable.BuildableObject;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.SpectralArrow;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

public class SpectralArrowProjectileData extends BuildableObject<SpectralArrowProjectileData, IProjectileDataRegistry> implements IProjectileData<AbstractArrow> {

    public static final ResourceLocation NAME = ResourceLocation.fromNamespaceAndPath("minecraft", "arrow.spectral");

    public SpectralArrowProjectileData() {
        super(NAME, ICBMClassicAPI.PROJECTILE_DATA_REGISTRY, null);
    }

    @Override
    public AbstractArrow newEntity(Level world, boolean allowItemPickup) {
        // PORT: 1.12 spawned a plain (tipped) arrow here, now a real spectral arrow (glowing, picks up as a spectral arrow)
        final SpectralArrow arrow = new SpectralArrow(EntityType.SPECTRAL_ARROW, world);
        arrow.pickup = allowItemPickup ? AbstractArrow.Pickup.ALLOWED : AbstractArrow.Pickup.DISALLOWED;
        return arrow;
    }
}
// PORTED

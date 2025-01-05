package icbm.classic.lib.projectile.vanilla;

import icbm.classic.api.ICBMClassicAPI;
import icbm.classic.api.missiles.projectile.IProjectileData;
import icbm.classic.api.missiles.projectile.IProjectileDataRegistry;
import icbm.classic.lib.buildable.BuildableObject;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.projectile.AbstractArrowEntity;
import net.minecraft.entity.projectile.ArrowEntity;
import net.minecraft.entity.projectile.SpectralArrowEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

public class SpectralArrowProjectileData extends BuildableObject<SpectralArrowProjectileData, IProjectileDataRegistry> implements IProjectileData<AbstractArrowEntity> {

    public static final ResourceLocation NAME = new ResourceLocation("minecraft", "arrow.spectral");

    public SpectralArrowProjectileData() {
        super(NAME, ICBMClassicAPI.PROJECTILE_DATA_REGISTRY, null);
    }

    @Override
    public AbstractArrowEntity newEntity(World world, boolean allowItemPickup) {
        final SpectralArrowEntity arrow = EntityType.SPECTRAL_ARROW.create(world);
        arrow.pickupStatus = allowItemPickup ? AbstractArrowEntity.PickupStatus.ALLOWED : AbstractArrowEntity.PickupStatus.DISALLOWED;
        return arrow;
    }
}

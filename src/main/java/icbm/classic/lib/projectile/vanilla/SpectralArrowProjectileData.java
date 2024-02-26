package icbm.classic.lib.projectile.vanilla;

import icbm.classic.api.ICBMClassicAPI;
import icbm.classic.api.missiles.projectile.IProjectileData;
import icbm.classic.api.missiles.projectile.IProjectileDataRegistry;
import icbm.classic.lib.buildable.BuildableObject;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.entity.projectile.EntityTippedArrow;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

import javax.annotation.Nonnull;

public class SpectralArrowProjectileData extends BuildableObject<SpectralArrowProjectileData, IProjectileDataRegistry> implements IProjectileData<EntityArrow> {

    public static final ResourceLocation NAME = new ResourceLocation("minecraft", "arrow.spectral");

    public SpectralArrowProjectileData() {
        super(NAME, ICBMClassicAPI.PROJECTILE_DATA_REGISTRY, null);
    }

    @Override
    public EntityArrow newEntity(World world, boolean allowItemPickup) {
        final EntityTippedArrow arrow = new EntityTippedArrow(world);
        arrow.pickupStatus = allowItemPickup ? EntityArrow.PickupStatus.ALLOWED : EntityArrow.PickupStatus.DISALLOWED;
        return arrow;
    }
}

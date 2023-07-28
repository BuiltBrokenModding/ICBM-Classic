package icbm.classic.lib.projectile.vanilla;

import icbm.classic.api.missiles.projectile.IProjectileData;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.entity.projectile.EntityTippedArrow;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

public class SpectralArrowProjectileData extends ArrowProjectileData {

    public static final ResourceLocation NAME = new ResourceLocation("minecraft", "arrow.spectral");
    @Override
    public ResourceLocation getRegistryName() {
        return NAME;
    }

    @Override
    public EntityArrow newEntity(World world, boolean allowItemPickup) {
        final EntityTippedArrow arrow = new EntityTippedArrow(world);
        arrow.pickupStatus = allowItemPickup ? EntityArrow.PickupStatus.ALLOWED : EntityArrow.PickupStatus.DISALLOWED;
        return arrow;
    }
}

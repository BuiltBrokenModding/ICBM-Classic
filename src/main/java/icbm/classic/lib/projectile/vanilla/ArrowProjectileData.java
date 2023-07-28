package icbm.classic.lib.projectile.vanilla;

import icbm.classic.api.missiles.projectile.IProjectileData;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.entity.projectile.EntityTippedArrow;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

public class ArrowProjectileData implements IProjectileData<EntityArrow> {

    public static final ResourceLocation NAME = new ResourceLocation("minecraft", "arrow.normal");

    @Override
    public ResourceLocation getRegistryName() {
        return NAME;
    }

    @Override
    public EntityArrow newEntity(World world, boolean allowItemPickup) {
        final EntityArrow arrow = new EntityTippedArrow(world);
        arrow.pickupStatus = allowItemPickup ? EntityArrow.PickupStatus.ALLOWED : EntityArrow.PickupStatus.DISALLOWED;
        return arrow;
    }

    @Override
    public NBTTagCompound serializeNBT() {
        return new NBTTagCompound();
    }

    @Override
    public void deserializeNBT(NBTTagCompound nbt) {
    }
}

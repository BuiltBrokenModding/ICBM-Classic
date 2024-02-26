package icbm.classic.lib.projectile.vanilla;

import icbm.classic.api.ICBMClassicAPI;
import icbm.classic.api.missiles.projectile.IProjectileData;
import icbm.classic.api.missiles.projectile.IProjectileDataRegistry;
import icbm.classic.lib.buildable.BuildableObject;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.entity.projectile.EntityTippedArrow;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

import javax.annotation.Nonnull;

public class ArrowProjectileData extends BuildableObject<ArrowProjectileData, IProjectileDataRegistry> implements IProjectileData<EntityArrow> {

    public static final ResourceLocation NAME = new ResourceLocation("minecraft", "arrow.normal");

    public ArrowProjectileData() {
        super(NAME, ICBMClassicAPI.PROJECTILE_DATA_REGISTRY, null);
    }

    @Override
    public EntityArrow newEntity(World world, boolean allowItemPickup) {
        final EntityArrow arrow = new EntityTippedArrow(world);
        arrow.pickupStatus = allowItemPickup ? EntityArrow.PickupStatus.ALLOWED : EntityArrow.PickupStatus.DISALLOWED;
        return arrow;
    }
}

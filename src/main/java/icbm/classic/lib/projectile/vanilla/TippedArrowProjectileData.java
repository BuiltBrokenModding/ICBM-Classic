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

public class TippedArrowProjectileData extends ArrowProjectileData {

    public static final ResourceLocation NAME = new ResourceLocation("minecraft", "arrow.tipped");

    @Getter @Setter @Accessors(chain = true)
    private ItemStack arrowItem = ItemStack.EMPTY;
    @Override
    public ResourceLocation getRegistryName() {
        return NAME;
    }

    @Override
    public EntityArrow newEntity(World world, boolean allowItemPickup) {
        final EntityTippedArrow arrow = new EntityTippedArrow(world);
        arrow.pickupStatus = allowItemPickup ? EntityArrow.PickupStatus.ALLOWED : EntityArrow.PickupStatus.DISALLOWED;
        if(!arrowItem.isEmpty()) {
            arrow.setPotionEffect(arrowItem);
        }
        return arrow;
    }

    @Override
    public NBTTagCompound serializeNBT() {
        final NBTTagCompound save = super.serializeNBT();
        save.setTag("item", arrowItem.serializeNBT());
        return save;
    }

    @Override
    public void deserializeNBT(NBTTagCompound nbt) {
        super.deserializeNBT(nbt);
        arrowItem = new ItemStack(nbt.getCompoundTag("item"));
    }
}

package icbm.classic.lib.projectile.vanilla;

import icbm.classic.api.missiles.projectile.IProjectileData;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.entity.projectile.EntityTippedArrow;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

public class ItemProjectileData implements IProjectileData<EntityItem> {

    public static final ResourceLocation NAME = new ResourceLocation("minecraft", "item");

    @Getter @Setter @Accessors(chain = true)
    private ItemStack itemStack = ItemStack.EMPTY;

    @Override
    public ResourceLocation getRegistryName() {
        return NAME;
    }

    @Override
    public EntityItem newEntity(World world, boolean allowItemPickup) {
        final EntityItem entityItem = new EntityItem(world);
        entityItem.setItem(itemStack);
        if(!allowItemPickup) {
            entityItem.setInfinitePickupDelay();
        }
        return entityItem;
    }

    @Override
    public NBTTagCompound serializeNBT() {
        final NBTTagCompound save = new NBTTagCompound();
        save.setTag("item", itemStack.serializeNBT());
        return save;
    }

    @Override
    public void deserializeNBT(NBTTagCompound nbt) {
        itemStack = new ItemStack(nbt.getCompoundTag("item"));
    }
}

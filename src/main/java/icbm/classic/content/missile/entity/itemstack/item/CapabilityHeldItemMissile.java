package icbm.classic.content.missile.entity.itemstack.item;

import icbm.classic.ICBMConstants;
import icbm.classic.api.missiles.ICapabilityMissileStack;
import icbm.classic.api.missiles.IMissile;
import icbm.classic.content.missile.entity.itemstack.EntityHeldItemMissile;
import icbm.classic.lib.saving.NbtSaveHandler;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.common.util.INBTSerializable;

public class CapabilityHeldItemMissile implements ICapabilityMissileStack, INBTSerializable<NBTTagCompound>
{
    @Getter @Setter
    private ItemStack heldItem = ItemStack.EMPTY;
    @Getter @Setter
    private boolean primaryAction = true;
    //TODO store homing on/off

    @Override
    public String getMissileId() {
        return ICBMConstants.PREFIX + "missile.item.held";
    }

    @Override
    public IMissile newMissile(World world)
    {
        final EntityHeldItemMissile missile = new EntityHeldItemMissile(world);
        missile.getItemStackHandler().setStackInSlot(0, heldItem.copy());
        return missile.getMissileCapability();
    }

    @Override
    public NBTTagCompound serializeNBT() {
        return SAVE_LOGIC.save(this);
    }

    @Override
    public void deserializeNBT(NBTTagCompound nbt) {
        SAVE_LOGIC.load(this, nbt);
    }

    private static final NbtSaveHandler<CapabilityHeldItemMissile> SAVE_LOGIC = new NbtSaveHandler<CapabilityHeldItemMissile>()
        .mainRoot()
        /* */.nodeItemStack("held", CapabilityHeldItemMissile::getHeldItem, CapabilityHeldItemMissile::setHeldItem)
        /* */.nodeBoolean("primary_action", CapabilityHeldItemMissile::isPrimaryAction, CapabilityHeldItemMissile::setPrimaryAction)
        .base();
}
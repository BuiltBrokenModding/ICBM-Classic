package icbm.classic.content.missile.entity.itemstack.item;

import icbm.classic.ICBMConstants;
import icbm.classic.api.missiles.ICapabilityMissileStack;
import icbm.classic.api.missiles.IMissile;
import icbm.classic.content.missile.entity.itemstack.EntityHeldItemMissile;
import icbm.classic.content.missile.entity.itemstack.HeldActionMode;
import icbm.classic.content.reg.EntityReg;
import icbm.classic.lib.saving.NbtSaveHandler;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.world.World;
import net.minecraftforge.common.util.INBTSerializable;

public class CapabilityHeldItemMissile implements ICapabilityMissileStack, INBTSerializable<CompoundNBT>
{
    @Getter @Setter
    private ItemStack heldItem = ItemStack.EMPTY;
    @Getter @Setter
    private HeldActionMode actionMode = HeldActionMode.PRIMARY_FIRST;
    //TODO store homing on/off

    @Override
    public String getMissileId() {
        return ICBMConstants.PREFIX + "missile.item.held";
    }

    @Override
    public IMissile newMissile(World world)
    {
        final EntityHeldItemMissile missile = EntityReg.MISSILE_HELD_ITEM.get().create(world);
        missile.getItemStackHandler().setStackInSlot(0, heldItem.copy());
        missile.setActionMode(actionMode);
        return missile.getMissileCapability();
    }

    @Override
    public CompoundNBT serializeNBT() {
        return SAVE_LOGIC.save(this);
    }

    @Override
    public void deserializeNBT(CompoundNBT nbt) {
        SAVE_LOGIC.load(this, nbt);
    }

    private static final NbtSaveHandler<CapabilityHeldItemMissile> SAVE_LOGIC = new NbtSaveHandler<CapabilityHeldItemMissile>()
        .mainRoot()
        /* */.nodeItemStack("held", CapabilityHeldItemMissile::getHeldItem, CapabilityHeldItemMissile::setHeldItem)
        /* *//* */.nodeEnumString("action_mode", CapabilityHeldItemMissile::getActionMode, CapabilityHeldItemMissile::setActionMode, HeldActionMode::valueOf)
        .base();
}
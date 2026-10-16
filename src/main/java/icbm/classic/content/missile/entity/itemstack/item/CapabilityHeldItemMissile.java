package icbm.classic.content.missile.entity.itemstack.item;

import icbm.classic.ICBMConstants;
import icbm.classic.api.missiles.ICapabilityMissileStack;
import icbm.classic.api.missiles.IMissile;
import icbm.classic.content.missile.entity.itemstack.EntityHeldItemMissile;
import icbm.classic.content.missile.entity.itemstack.HeldActionMode;
import icbm.classic.lib.StackNbt;
import icbm.classic.lib.saving.NbtSaveHandler;
import lombok.Getter;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.util.INBTSerializable;

import javax.annotation.Nullable;

public class CapabilityHeldItemMissile implements ICapabilityMissileStack, INBTSerializable<CompoundTag>
{
    /** Custom data key, matches the 1.12 capability provider key */
    public static final String STACK_KEY = "missile";

    @Getter
    private ItemStack heldItem = ItemStack.EMPTY;
    @Getter
    private HeldActionMode actionMode = HeldActionMode.PRIMARY_FIRST;
    //TODO store homing on/off

    /** PORT: item capabilities are rebuilt per lookup, state is read from and written to this stack */
    @Nullable
    private final ItemStack host;

    public CapabilityHeldItemMissile()
    {
        this.host = null;
    }

    public CapabilityHeldItemMissile(ItemStack host)
    {
        this.host = host;
        if (StackNbt.has(host, STACK_KEY))
        {
            SAVE_LOGIC.load(this, StackNbt.getCompound(host, STACK_KEY));
        }
    }

    public void setHeldItem(ItemStack heldItem)
    {
        this.heldItem = heldItem == null ? ItemStack.EMPTY : heldItem;
        saveToHost();
    }

    public void setActionMode(HeldActionMode actionMode)
    {
        this.actionMode = actionMode;
        saveToHost();
    }

    private void saveToHost()
    {
        if (host != null)
        {
            StackNbt.putCompound(host, STACK_KEY, SAVE_LOGIC.save(this));
        }
    }

    @Override
    public String getMissileId() {
        return ICBMConstants.PREFIX + "missile.item.held";
    }

    @Override
    public IMissile newMissile(Level world)
    {
        final EntityHeldItemMissile missile = new EntityHeldItemMissile(world);
        missile.getItemStackHandler().setStackInSlot(0, heldItem.copy());
        missile.setActionMode(actionMode);
        return missile.getMissileCapability();
    }

    @Override
    public CompoundTag serializeNBT(HolderLookup.Provider provider) {
        return SAVE_LOGIC.save(this);
    }

    @Override
    public void deserializeNBT(HolderLookup.Provider provider, CompoundTag nbt) {
        SAVE_LOGIC.load(this, nbt);
    }

    private static final NbtSaveHandler<CapabilityHeldItemMissile> SAVE_LOGIC = new NbtSaveHandler<CapabilityHeldItemMissile>()
        .mainRoot()
        /* */.nodeItemStack("held", CapabilityHeldItemMissile::getHeldItem, (cap, stack) -> cap.heldItem = stack)
        /* *//* */.nodeEnumString("action_mode", CapabilityHeldItemMissile::getActionMode, (cap, mode) -> cap.actionMode = mode, HeldActionMode::valueOf)
        .base();
}
// PORTED

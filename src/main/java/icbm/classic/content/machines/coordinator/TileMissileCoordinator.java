package icbm.classic.content.machines.coordinator;

import icbm.classic.api.items.tools.IWorldPosItem;
import icbm.classic.api.tile.provider.IInventoryProvider;
import icbm.classic.prefab.inventory.ExternalInventory;
import icbm.classic.prefab.tile.TileMachine;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;

/**
 * Missile Coordinator, used to calculate paths between two points to better plan missile actions
 *
 * @author Calclavia
 */
public class TileMissileCoordinator extends TileMachine implements IInventoryProvider<ExternalInventory>
{
    public static final String NBT_INVENTORY = "inventory";
    public ExternalInventory inventory;

    @Override
    public void readFromNBT(NBTTagCompound compound)
    {
        super.readFromNBT(compound);
        if (compound.hasKey(NBT_INVENTORY))
        {
            getInventory().load(compound.getCompoundTag(NBT_INVENTORY));
        }
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound)
    {
        if (inventory != null && !inventory.isEmpty())
        {
            compound.setTag(NBT_INVENTORY, inventory.save(new NBTTagCompound()));
        }
        return super.writeToNBT(compound);
    }

    @Override
    public Object getServerGuiElement(int ID, EntityPlayer player)
    {
        return new ContainerMissileCoordinator(player, this);
    }

    @Override
    public Object getClientGuiElement(int ID, EntityPlayer player)
    {
        return new GuiMissileCoordinator(player, this);
    }

    @Override
    public ExternalInventory getInventory()
    {
        return inventory;
    }

    @Override
    public boolean canStore(ItemStack stack, int slot, EnumFacing side)
    {
        return stack.getItem() instanceof IWorldPosItem;
    }
}

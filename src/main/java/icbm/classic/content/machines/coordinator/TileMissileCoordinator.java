package icbm.classic.content.machines.coordinator;

import com.builtbroken.mc.api.items.tools.IWorldPosItem;
import com.builtbroken.mc.api.tile.access.IGuiTile;
import com.builtbroken.mc.api.tile.provider.IInventoryProvider;
import com.builtbroken.mc.data.Direction;
import com.builtbroken.mc.prefab.inventory.ExternalInventory;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;

/**
 * Missile Coordinator, used to calculate paths between two points to better plan missile actions
 *
 * @author Calclavia
 */
public class TileMissileCoordinator extends TileEntity implements IGuiTile, IInventoryProvider<ExternalInventory>
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
    public boolean canStore(ItemStack stack, int slot, Direction side)
    {
        return stack.getItem() instanceof IWorldPosItem;
    }
}

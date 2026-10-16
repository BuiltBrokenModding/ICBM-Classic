package icbm.classic.lib.capability.emp;

import icbm.classic.ICBMClassic;
import icbm.classic.api.ICBMClassicAPI;
import icbm.classic.api.actions.IAction;
import icbm.classic.api.caps.IEMPReceiver;
import icbm.classic.config.blast.ConfigBlast;
import icbm.classic.content.actions.emp.ActionEmpArea;
import icbm.classic.lib.InventoryUtility;
import icbm.classic.lib.energy.system.EnergySystem;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.IItemHandlerModifiable;

/**
 * Used to wrapper an inventory capability to provide support to EMP the contents.
 * <p>
 * By default {@link ActionEmpArea} will generate this object
 * for the target of the EMP.
 *
 *
 * Created by Dark(DarkGuardsman, Robin) on 3/12/2018.
 */
public abstract class CapabilityEmpInventory<H extends Object> implements IEMPReceiver
{
    @Override
    public float applyEmpAction(Level world, double x, double y, double z, IAction emp_blast, float power, boolean doAction)
    {
        final IItemHandlerModifiable iItemHandler = getCapability();
        if (iItemHandler != null)
        {
            //Loop items
            for (int slotIndex = 0; slotIndex < iItemHandler.getSlots(); slotIndex++)
            {
                final ItemStack slotStack = iItemHandler.getStackInSlot(slotIndex);

                //Check to make sure its not a placeholder
                if (!slotStack.isEmpty())
                {
                    //Copy stack for editing
                    final ItemStack itemStack = slotStack.copy();

                    //Run call
                    power = empItemStack(itemStack, world, x, y, z, getHost(), emp_blast, power, doAction);

                    //Only apply changes if we are not simulating and there was a change in the stack
                    if (doAction && !InventoryUtility.stacksMatchExact(itemStack, slotStack))
                    {
                        try
                        {
                            iItemHandler.setStackInSlot(slotIndex, itemStack);
                        }
                        catch (RuntimeException e) //Safety check, just in case
                        {
                            ICBMClassic.logger().error("BlastEMP: Unexpected error while updating inventory item '" + itemStack
                                    + "' from slot '" + slotIndex
                                    + "' in '" + iItemHandler
                                    + "' contained in '" + getHost() + "' while running " + this, e);
                        }
                    }
                }
            }
        }
        return power;
    }

    public static float empItemStack(ItemStack itemStack, Level world, double x, double y, double z, Object container, IAction emp_blast, float power, boolean doAction)
    {
        boolean doInventory = ConfigBlast.emp.ALLOW_ITEM_INVENTORY;


        //Check for EMP support
        final IEMPReceiver cap = itemStack.getCapability(ICBMClassicAPI.EMP_ITEM_CAPABILITY);
        if (cap != null)
        {
            //Get EMP handler
            if (cap != null)
            {
                //Apply effect
                power = cap.applyEmpAction(world, x, y, z, emp_blast, power, true);
                doInventory = cap.shouldEmpSubObjects(world, x, y, z) && doInventory;
            }
        }
        else if (ConfigBlast.emp.DRAIN_ENERGY_ITEMS)
        {
            EnergySystem.getSystem(itemStack, null).setEnergy(itemStack, null, 0, false);
        }

        if (doInventory && getItemHandler(itemStack) != null)
        {
            if (container instanceof Entity)
            {
                power = new ItemInvInEntity((Entity) container, itemStack).applyEmpAction(world, x, y, z, emp_blast, power, doAction);
            }
            else if (container instanceof BlockEntity)
            {
                power = new ItemInvInTile((BlockEntity) container, itemStack).applyEmpAction(world, x, y, z, emp_blast, power, doAction);
            }
        }
        return power;
    }


    protected abstract IItemHandlerModifiable getCapability();

    /** PORT: replaces 1.12 hasCapability/getCapability(ITEM_HANDLER_CAPABILITY, null) */
    public static IItemHandler getItemHandler(ItemStack stack)
    {
        return stack.getCapability(Capabilities.ItemHandler.ITEM);
    }

    public static IItemHandler getItemHandler(Entity entity)
    {
        return entity.getCapability(Capabilities.ItemHandler.ENTITY);
    }

    public static IItemHandler getItemHandler(BlockEntity tile)
    {
        if (tile.getLevel() == null)
        {
            return null;
        }
        return tile.getLevel().getCapability(Capabilities.ItemHandler.BLOCK, tile.getBlockPos(), tile.getBlockState(), tile, null);
    }

    protected abstract H getHost();

    public static class EntityInv extends CapabilityEmpInventory<Entity>
    {
        public final Entity entity;

        public EntityInv(Entity entity)
        {
            this.entity = entity;
        }

        @Override
        protected IItemHandlerModifiable getCapability()
        {
            if (ConfigBlast.emp.ALLOW_ENTITY_INVENTORY && getItemHandler(entity) != null)
            {
                IItemHandler handler = getItemHandler(entity);

                //Currently only support IItemHandlerModifiable due to
                //  contract on IItemHandler preventing modification of returned getStack()
                if (handler instanceof IItemHandlerModifiable)
                {
                    return (IItemHandlerModifiable) handler;
                }
            }
            return null;
        }

        @Override
        protected Entity getHost()
        {
            return entity;
        }
    }

    public static class TileInv extends CapabilityEmpInventory<BlockEntity>
    {
        public final BlockEntity entity;

        public TileInv(BlockEntity entity)
        {
            this.entity = entity;
        }

        @Override
        protected IItemHandlerModifiable getCapability()
        {
            if (ConfigBlast.emp.ALLOW_TILE_INVENTORY && getItemHandler(entity) != null)
            {
                IItemHandler handler = getItemHandler(entity);

                //Currently only support IItemHandlerModifiable due to
                //  contract on IItemHandler preventing modification of returned getStack()
                if (handler instanceof IItemHandlerModifiable)
                {
                    return (IItemHandlerModifiable) handler;
                }
            }
            return null;
        }

        @Override
        protected BlockEntity getHost()
        {
            return entity;
        }
    }

    public static class ItemInvInEntity extends CapabilityEmpInventory<Entity>
    {
        public final ItemStack item;
        public final Entity host;

        public ItemInvInEntity(Entity host, ItemStack item)
        {
            this.host = host;
            this.item = item;
        }

        @Override
        protected IItemHandlerModifiable getCapability()
        {
            if (ConfigBlast.emp.ALLOW_ITEM_INVENTORY && getItemHandler(item) != null)
            {
                IItemHandler handler = getItemHandler(item);

                //Currently only support IItemHandlerModifiable due to
                //  contract on IItemHandler preventing modification of returned getStack()
                if (handler instanceof IItemHandlerModifiable)
                {
                    return (IItemHandlerModifiable) handler;
                }
            }
            return null;
        }

        @Override
        protected Entity getHost()
        {
            return host;
        }
    }

    public static class ItemInvInTile extends CapabilityEmpInventory<BlockEntity>
    {
        public final ItemStack item;
        public final BlockEntity host;

        public ItemInvInTile(BlockEntity host, ItemStack item)
        {
            this.host = host;
            this.item = item;
        }

        @Override
        protected IItemHandlerModifiable getCapability()
        {
            if (ConfigBlast.emp.ALLOW_ITEM_INVENTORY && getItemHandler(item) != null)
            {
                IItemHandler handler = getItemHandler(item);

                //Currently only support IItemHandlerModifiable due to
                //  contract on IItemHandler preventing modification of returned getStack()
                if (handler instanceof IItemHandlerModifiable)
                {
                    return (IItemHandlerModifiable) handler;
                }
            }
            return null;
        }

        @Override
        protected BlockEntity getHost()
        {
            return host;
        }
    }
}

// PORTED

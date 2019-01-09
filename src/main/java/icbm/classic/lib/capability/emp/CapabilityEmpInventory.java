package icbm.classic.lib.capability.emp;

import icbm.classic.ICBMClassic;
import icbm.classic.api.caps.IEMPReceiver;
import icbm.classic.api.explosion.IBlast;
import icbm.classic.config.ConfigEMP;
import icbm.classic.lib.energy.system.EnergySystem;
import icbm.classic.prefab.inventory.InventoryUtility;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;

/**
 * Used to wrapper an inventory capability to provide support to EMP the contents.
 * <p>
 * By default {@link icbm.classic.content.blast.BlastEMP} will generate this object
 * for the target of the EMP.
 *
 *
 * Created by Dark(DarkGuardsman, Robert) on 3/12/2018.
 */
public abstract class CapabilityEmpInventory<H extends Object> implements IEMPReceiver
{
    @Override
    public float applyEmpAction(World world, double x, double y, double z, IBlast emp_blast, float power, boolean doAction)
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

    public static float empItemStack(ItemStack itemStack, World world, double x, double y, double z, Object container, IBlast emp_blast, float power, boolean doAction)
    {
        boolean doInventory = ConfigEMP.ALLOW_ITEM_INVENTORY;


        //Check for EMP support
        if (itemStack.hasCapability(CapabilityEMP.EMP, null))
        {
            //Get EMP handler
            IEMPReceiver cap = itemStack.getCapability(CapabilityEMP.EMP, null);
            if (cap != null)
            {
                //Apply effect
                power = cap.applyEmpAction(world, x, y, z, emp_blast, power, true);
                doInventory = cap.shouldEmpSubObjects(world, x, y, z) && doInventory;
            }
        }
        else if (ConfigEMP.DRAIN_ENERGY_ITEMS)
        {
            EnergySystem.getSystem(itemStack, null).setEnergy(itemStack, null, 0, true);
        }

        if (doInventory && itemStack.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null))
        {
            if (container instanceof Entity)
            {
                power = new ItemInvInEntity((Entity) container, itemStack).applyEmpAction(world, x, y, z, emp_blast, power, doAction);
            }
            else if (container instanceof TileEntity)
            {
                power = new ItemInvInTile((TileEntity) container, itemStack).applyEmpAction(world, x, y, z, emp_blast, power, doAction);
            }
        }
        return power;
    }


    protected abstract IItemHandlerModifiable getCapability();

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
            if (ConfigEMP.ALLOW_ENTITY_INVENTORY && entity.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null))
            {
                IItemHandler handler = entity.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);

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

    public static class TileInv extends CapabilityEmpInventory<TileEntity>
    {
        public final TileEntity entity;

        public TileInv(TileEntity entity)
        {
            this.entity = entity;
        }

        @Override
        protected IItemHandlerModifiable getCapability()
        {
            if (ConfigEMP.ALLOW_TILE_INVENTORY && entity.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null))
            {
                IItemHandler handler = entity.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);

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
        protected TileEntity getHost()
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
            if (ConfigEMP.ALLOW_ITEM_INVENTORY && item.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null))
            {
                IItemHandler handler = item.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);

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

    public static class ItemInvInTile extends CapabilityEmpInventory<TileEntity>
    {
        public final ItemStack item;
        public final TileEntity host;

        public ItemInvInTile(TileEntity host, ItemStack item)
        {
            this.host = host;
            this.item = item;
        }

        @Override
        protected IItemHandlerModifiable getCapability()
        {
            if (ConfigEMP.ALLOW_ITEM_INVENTORY && item.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null))
            {
                IItemHandler handler = item.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);

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
        protected TileEntity getHost()
        {
            return host;
        }
    }
}


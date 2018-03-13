package icbm.classic.caps.emp;

import icbm.classic.ICBMClassic;
import icbm.classic.api.IWorldPosition;
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
 * @see <a href="https://github.com/BuiltBrokenModding/VoltzEngine/blob/development/license.md">License</a> for what you can and can't do with the code.
 * Created by Dark(DarkGuardsman, Robert) on 3/12/2018.
 */
public abstract class CapabilityEmpInventory<H extends Object> implements IEMPReceiver, IWorldPosition
{
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

                    //Check for EMP support
                    if (itemStack.hasCapability(CapabilityEMP.EMP, null))
                    {
                        //Get EMP handler
                        IEMPReceiver cap = itemStack.getCapability(CapabilityEMP.EMP, null);
                        if (cap != null)
                        {
                            //Apply effect
                            power = cap.applyEmpAction(world, x, y, z, emp_blast, power, true);
                        }
                    }
                    else if (ConfigEMP.DRAIN_ENERGY_ITEMS)
                    {
                        EnergySystem.getSystem(itemStack, null).setEnergy(itemStack, null, 0,  true);
                    }

                    if (!InventoryUtility.stacksMatchExact(itemStack, slotStack))
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

        @Override
        public World world()
        {
            return entity.world;
        }

        @Override
        public double z()
        {
            return entity.posZ;
        }

        @Override
        public double x()
        {
            return entity.posX;
        }

        @Override
        public double y()
        {
            return entity.posY;
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
        protected TileEntity getHost()
        {
            return entity;
        }

        @Override
        public World world()
        {
            return entity.getWorld();
        }

        @Override
        public double z()
        {
            return entity.getPos().getZ() + 0.5;
        }

        @Override
        public double x()
        {
            return entity.getPos().getX() + 0.5;
        }

        @Override
        public double y()
        {
            return entity.getPos().getY() + 0.5;
        }
    }
}


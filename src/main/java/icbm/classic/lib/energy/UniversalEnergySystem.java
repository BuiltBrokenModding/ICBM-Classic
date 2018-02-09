package icbm.classic.lib.energy;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * A module to extend for compatibility with other energy systems.
 *
 * @author Calclavia, Darkguardsman
 */
public class UniversalEnergySystem
{
    /** List of all registered energy handlers */
    public static final List<EnergyHandler> loadedModules = new ArrayList();
    public static EnergyHandler RF_HANDLER;

    /**
     * A cache to know which module to use with when facing objects with a specific class.
     */
    public static final HashMap<Class, EnergyHandler> energyHandlerCache = new HashMap<>();
    public static final HashMap<Class, EnergyHandler> energyStorageCache = new HashMap<>();

    /** Map of static objects(Block, Item) to thier handlers for quick reference */
    private static final HashMap<Object, EnergyHandler> objectHandlerMap = new HashMap();

    static
    {
        register(new UniversalEnergyHandler());
    }

    public static void register(EnergyHandler module)
    {
        loadedModules.add(module);
    }

    public static EnergyHandler getHandler(Object handler, EnumFacing dir)
    {
        if (isHandler(handler, dir))
        {
            Class clazz = handler instanceof ItemStack ? ((ItemStack) handler).getItem().getClass() : handler.getClass();
            return energyHandlerCache.get(clazz);
        }
        return null;
    }

    /**
     * Is this object a valid energy handler?
     *
     * @return True if the handler can store energy. This can be for items and blocks.
     */
    public static boolean isHandler(Object handler, EnumFacing dir)
    {
        if (handler != null)
        {
            if (objectHandlerMap.containsKey(handler))
            {
                return true;
            }
            //TODO move item stack handler to Item class map
            Class clazz = handler instanceof ItemStack ? ((ItemStack) handler).getItem().getClass() : handler.getClass();

            if (energyHandlerCache.containsKey(clazz))
            {
                return true;
            }

            for (EnergyHandler module : loadedModules)
            {
                if (dir != null ? module.doIsHandler(handler, dir) : module.doIsHandler(handler))
                {
                    if (handler instanceof Item || handler instanceof Block)
                    {
                        objectHandlerMap.put(handler, module);
                    }
                    energyHandlerCache.put(clazz, module);
                    return true;
                }
            }
        }

        return false;
    }

    public static EnergyHandler getContainer(Object handler)
    {
        if (isEnergyContainer(handler))
        {
            return energyStorageCache.get(handler.getClass());
        }
        return null;
    }

    /**
     * Is this object able to store energy?
     *
     * @param handler
     * @return True if the handler can store energy. The handler MUST be a block.
     */
    public static boolean isEnergyContainer(Object handler)
    {
        if (handler != null)
        {
            Class clazz = handler.getClass();

            if (energyStorageCache.containsKey(clazz))
            {
                return true;
            }

            for (EnergyHandler module : loadedModules)
            {
                if (module.doIsEnergyContainer(handler))
                {
                    energyStorageCache.put(clazz, module);
                    return true;
                }
            }
        }

        return false;
    }

    public static boolean canConnect(Object obj, EnumFacing direction, Object source)
    {
        //TODO prioritize UE code to prevent inf loops (RF#canConnect -> UESystem#canConnect -> RF#CanConnect....)
        EnergyHandler module = getHandler(obj, direction);
        return module != null && module.canConnect(obj, direction, source);
    }

    /**
     * Called to give an amount of energy to the handler in the direction given
     *
     * @param handler   - tile/multipart/etc getting the energy
     * @param direction - direction in relate to the handler
     * @param energy    - energy(should be possitive)
     * @param doReceive - true will actually give the energy, false will test
     * @return amount of energy received
     */
    public static double fill(Object handler, EnumFacing direction, double energy, boolean doReceive)
    {
        return receiveEnergy(handler, direction, energy, doReceive);
    }

    /**
     * Called to give an amount of energy to the handler in the direction given
     *
     * @param handler   - tile/multipart/etc getting the energy
     * @param direction - direction in relate to the handler
     * @param energy    - energy(should be possitive)
     * @param doReceive - true will actually give the energy, false will test
     * @return amount of energy received
     */
    public static double receiveEnergy(Object handler, EnumFacing direction, double energy, boolean doReceive)
    {
        EnergyHandler module = getHandler(handler, direction);
        if (module != null)
        {
            return module.receiveEnergy(handler, direction, energy, doReceive);
        }
        return 0;
    }

    public static double drain(Object handler, EnumFacing direction, double energy, boolean doExtract)
    {
        return extractEnergy(handler, direction, energy, doExtract);
    }

    public static double clearEnergy(Object handler, boolean doAction)
    {
        EnergyHandler module = getHandler(handler, null);
        if (module != null)
        {
            return module.clearEnergy(handler, doAction);
        }
        return 0;
    }

    /**
     * Called to take an amount of energy from the handler in the direction given
     *
     * @param handler   - tile/multipart/etc getting the energy
     * @param direction - direction in relate to the handler
     * @param energy    - energy(should be possitive)
     * @param doExtract - true will actually take the energy, false will test
     * @return amount of energy taken
     */
    public static double extractEnergy(Object handler, EnumFacing direction, double energy, boolean doExtract)
    {
        EnergyHandler module = getHandler(handler, direction);
        if (module != null)
        {
            return module.extractEnergy(handler, direction, energy, doExtract);
        }
        return 0;
    }

    /**
     * Called to take an amount of energy from any side of a tile,
     * <p>
     * use {@link #extractEnergy(Object, EnumFacing, double, boolean)} when
     * possible to ensure connection logic is maintained. Only use this
     * if there is no way to get the side being accessed.
     *
     * @param handler   - tile/multipart/etc getting the energy
     * @param energy    - energy(should be possitive)
     * @param doExtract - true will actually take the energy, false will test
     * @return amount of energy taken
     */
    public static double extractEnergy(Object handler, double energy, boolean doExtract)
    {
        double energyLeft = energy;
        for (EnumFacing dir : EnumFacing.values())
        {
            if (energyLeft > 0)
            {
                EnergyHandler module = getHandler(handler, dir);
                if (module != null)
                {
                    energyLeft -= module.extractEnergy(handler, dir, energyLeft, doExtract);
                }
            }
            else
            {
                break;
            }
        }
        return energy - energyLeft;
    }

    /**
     * Grabs the potential amount of energy the tile contains. In most
     * cases this is 6-7 times large due to sided behaviour of some objects
     * that do not contain APIs for getting total tile energy value.
     *
     * @param handler - tile
     * @return energy contained
     */
    public static double getPotentialEnergy(Object handler)
    {
        double energyContained = 0;
        for (EnumFacing dir : EnumFacing.values())
        {
            EnergyHandler module = getHandler(handler, dir);
            if (module != null)
            {
                energyContained += module.getEnergy(handler, dir);
            }
        }
        return energyContained;
    }

    /**
     * Charge's an ItemStack with energy
     *
     * @param itemStack - stack being charged
     * @param joules    - energy to charge
     * @param doCharge  - true will add the energy, false will test
     * @return energy actually charged
     */
    public static double fill(ItemStack itemStack, double joules, boolean doCharge)
    {
        return chargeItem(itemStack, joules, doCharge);
    }

    /**
     * Charge's an ItemStack with energy
     *
     * @param itemStack - stack being charged
     * @param joules    - energy to charge
     * @param doCharge  - true will add the energy, false will test
     * @return energy actually charged
     */
    public static double chargeItem(ItemStack itemStack, double joules, boolean doCharge)
    {
        EnergyHandler module = getHandler(itemStack, null);
        if (module != null)
        {
            return module.chargeItem(itemStack, joules, doCharge);
        }
        return 0;
    }

    /**
     * Discharge's an ItemStack's contained energy
     *
     * @param itemStack   - stack being charged
     * @param joules      - energy to charge
     * @param doDischarge - true will remove the energy, false will test
     * @return energy actually taken
     */
    public static double drain(ItemStack itemStack, double joules, boolean doDischarge)
    {
        return dischargeItem(itemStack, joules, doDischarge);
    }

    /**
     * Discharge's an ItemStack's contained energy
     *
     * @param itemStack   - stack being charged
     * @param joules      - energy to charge
     * @param doDischarge - true will remove the energy, false will test
     * @return energy actually taken
     */
    public static double dischargeItem(ItemStack itemStack, double joules, boolean doDischarge)
    {
        EnergyHandler module = getHandler(itemStack, null);
        if (module != null)
        {
            return module.dischargeItem(itemStack, joules, doDischarge);
        }
        return 0;
    }


    public static ItemStack getItemWithCharge(ItemStack itemStack, double energy)
    {
        EnergyHandler module = getHandler(itemStack, null);
        if (module != null)
        {
            return module.getItemWithCharge(itemStack, energy);
        }
        return itemStack;
    }

    public static double getEnergy(Object obj, EnumFacing direction)
    {
        EnergyHandler module = getHandler(obj, direction);
        if (module != null)
        {
            return module.getEnergy(obj, direction);
        }
        return 0;
    }

    public static double getMaxEnergy(Object handler, EnumFacing direction)
    {
        EnergyHandler module = getHandler(handler, direction);
        if (module != null)
        {
            return module.getMaxEnergy(handler, direction);
        }
        return 0;
    }

    public static double getEnergyItem(ItemStack is)
    {
        EnergyHandler module = getHandler(is, null);
        if (module != null)
        {
            return module.getEnergyItem(is);
        }
        return 0;
    }

    public static double getMaxEnergyItem(ItemStack is)
    {
        EnergyHandler module = getHandler(is, null);
        if (module != null)
        {
            return module.getMaxEnergyItem(is);
        }
        return 0;
    }

    public static double setFullCharge(ItemStack is)
    {
        EnergyHandler module = getHandler(is, null);
        if (module != null)
        {
            return module.setFullCharge(is);
        }
        return 0;
    }
}

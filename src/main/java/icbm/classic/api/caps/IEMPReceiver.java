package icbm.classic.api.caps;

import icbm.classic.api.explosion.IBlast;
import net.minecraft.world.World;

/**
 * Applied to an object as a capability (Item/Entity/TileEntity)
 * or registered to {@link icbm.classic.api.ICBMClassicAPI#registerBlockEmpHandler(net.minecraft.block.Block, IEMPReceiver)}
 * to receiver EMP effects from a EMP blast or similiar system.
 *
 *
 * Created by Dark(DarkGuardsman, Robert) on 3/12/2018.
 */
public interface IEMPReceiver
{
    /**
     * Called when an EMP goes off and triggers an action on the object at the given location.
     * <p>
     * Use this function to apply effects to this object or weaken the EMP
     *
     * @param world     - this object's world
     * @param x         - this object's x position
     * @param y         - this object's y position
     * @param z         - this object's z position
     * @param emp_blast - source of the EMP
     * @param power     - arbitrary value between 0 and 1 noting the power of the blast.
     *                  Can be greater than 1 to note overpowered blast. Though will rarely
     *                  have any effect on most actions other than to bypass shielding.
     * @param doAction  - set to try to apply effects to the world, false to simulate actions.
     * @return power to apply on the other side of the entity, any value under zero will be ignored
     */
    default float applyEmpAction(World world, double x, double y, double z, IBlast emp_blast, float power, boolean doAction)
    {
        //At the moment power is not used, check back later - 3/12/2018 Dark

        //Do something to your object
        //  Ex: Item -> Drain power
        //  Ex: EntityCreeper -> Super charge
        //  Ex: TileEntity -> Turn off machine or explode

        //Keep in mind ICBM-Classic's EMP is not realistic. It is very much game like in nature.
        //  This means the blast doesn't loss power over distance. Instead only shielding can
        //  reduce the effect of the EMP. Anything can act as shielding if desired. However, it
        //  is suggested to balance shielding to gameplay. This means no 100% blocking instead
        //  try to scale shielding to force players to think about designs.

        //Return power remaining, by default return value unless making shielding
        return power;
    }

    /**
     * Sub version of {@link #applyEmpAction(World, double, double, double, IBlast, float, boolean)} used in
     * connection with objects that are contained inside other objects. Allows gaining access to parent object
     * in case addition effects are required.
     * <p>
     * Common example is containers that are ItemHandlers or Inventories
     * Ex: {@link net.minecraft.entity.player.EntityPlayer},
     * {@link net.minecraft.tileentity.TileEntityChest},
     * {@link net.minecraft.entity.item.EntityMinecartChest}
     *
     * @param container - object containing this sub-object
     */
    default float applyEmpAction(World world, double x, double y, double z, IBlast emp_blast, Object container, float power, boolean doAction)
    {
        return applyEmpAction(world, x, y, z, emp_blast, power, doAction);
    }

    /**
     * Called to get a theoretical resistance value for the object. Mainly will
     * be used for simulation code and display of data to the user.
     *
     * @param world - this object's world
     * @param x     - this object's x position
     * @param y     - this object's y position
     * @param z     - this object's z position
     * @param power - power input into the object
     * @return value between 0 to 1 to note resistance level
     */
    default float getEmpResistance(World world, double x, double y, double z, float power)
    {
        return 0;
    }

    /**
     * Called to check if the theoretical resistance value for the object is flat or ratio based.
     * Mainly will be used for simulation code and display of data to the user.
     *
     * @param world - this object's world
     * @param x     - this object's x position
     * @param y     - this object's y position
     * @param z     - this object's z position
     * @param power - power input into the object
     * @return true if the reduction is flat (power - constant) or ratio (power * reduction or power / ratio)
     */
    default boolean isEmpResistanceFlat(World world, double x, double y, double z, float power)
    {
        return true;
    }

    /**
     * Allow the EMP to automatically handle sub objects contained in inventories inside this object
     *
     * @param world - this object's world
     * @param x     - this object's x position
     * @param y     - this object's y position
     * @param z     - this object's z position
     * @return true to allow code to run on sub-objects
     */
    default boolean shouldEmpSubObjects(World world, double x, double y, double z)
    {
        return true;
    }
}

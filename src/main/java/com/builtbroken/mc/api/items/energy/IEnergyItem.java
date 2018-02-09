package com.builtbroken.mc.api.items.energy;

import net.minecraft.item.ItemStack;

/** Applied to items that accept energy
 * @see <a href="https://github.com/BuiltBrokenModding/VoltzEngine/blob/development/license.md">License</a> for what you can and can't do with the code.
 * Created by Dark(DarkGuardsman, Robert) on ????.
 */
public interface IEnergyItem //TODO move node system
{
	/**
	 * Adds energy to an item. Returns the quantity of energy that was accepted. This should always
	 * return 0 if the item cannot be externally charged.
	 *
	 * @param itemStack  ItemStack to be charged.
	 * @param energy     Maximum amount of energy to be sent into the item.
	 * @param doRecharge If false, the charge will only be simulated.
	 * @return Amount of energy that was accepted by the item.
	 */
	int recharge(ItemStack itemStack, int energy, boolean doRecharge);

	/**
	 * Removes energy from an item. Returns the quantity of energy that was removed. This should
	 * always return 0 if the item cannot be externally discharged.
	 *
	 * @param itemStack   ItemStack to be discharged.
	 * @param energy      Maximum amount of energy to be removed from the item.
	 * @param doDischarge If false, the discharge will only be simulated.
	 * @return Amount of energy that was removed from the item.
	 */
	int discharge(ItemStack itemStack, int energy, boolean doDischarge);
}

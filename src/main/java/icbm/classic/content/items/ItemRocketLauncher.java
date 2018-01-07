package icbm.classic.content.items;

import com.builtbroken.mc.core.Engine;
import com.builtbroken.mc.lib.helper.LanguageUtility;
import icbm.classic.Settings;
import icbm.classic.content.entity.EntityMissile;
import icbm.classic.content.explosive.Explosives;
import icbm.classic.content.explosive.ex.Explosion;
import icbm.classic.prefab.item.ItemICBMElectrical;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumAction;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;

import java.util.HashMap;
import java.util.List;

/**
 * Rocket Launcher
 *
 * @author Calclavia
 */

public class ItemRocketLauncher extends ItemICBMElectrical
{
    private static final int ENERGY = 1000000;
    private static final int firingDelay = 1000;
    private HashMap<String, Long> clickTimePlayer = new HashMap<String, Long>();

    public ItemRocketLauncher()
    {
        super("rocketLauncher");
    }

    @Override
    public EnumAction getItemUseAction(ItemStack par1ItemStack)
    {
        return EnumAction.BOW;
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand handIn)
    {
        ItemStack stack = player.getHeldItem(handIn);
        if (!world.isRemote)
        {
            long clickMs = System.currentTimeMillis();
            if (clickTimePlayer.containsKey(player.getName()))
            {
                if (clickMs - clickTimePlayer.get(player.getName()) < firingDelay)
                {
                    //TODO play weapon empty click audio to note the gun is reloading
                    return new ActionResult<ItemStack>(EnumActionResult.SUCCESS, stack);
                }
            }
            if (this.getEnergy(stack) >= ENERGY || player.capabilities.isCreativeMode)
            {
                // Check the player's inventory and look for missiles.
                for (int slot = 0; slot < player.inventory.getSizeInventory(); slot++)
                {
                    ItemStack inventoryStack = player.inventory.getStackInSlot(slot);

                    if (inventoryStack != null)
                    {
                        if (inventoryStack.getItem() instanceof ItemMissile)
                        {
                            int meta = inventoryStack.getItemDamage();
                            Explosives ex = Explosives.get(meta);

                            if (ex.handler != null)
                            {
                                // Limit the missile to tier two.
                                if ((ex.handler.getTier() <= Settings.ROCKET_LAUNCHER_TIER_FIRE_LIMIT || Engine.runningAsDev) && ((Explosion) ex.handler).isCruise())
                                {
                                    EntityMissile entityMissile = new EntityMissile(player);
                                    entityMissile.missileType = EntityMissile.MissileType.LAUNCHER;
                                    entityMissile.explosiveID = ex;
                                    entityMissile.acceleration = 1;
                                    entityMissile.launch(null);
                                    world.spawnEntity(entityMissile);

                                    if (player.isSneaking())
                                    {
                                        player.startRiding(entityMissile);
                                        player.setSneaking(false);
                                    }

                                    if (!player.capabilities.isCreativeMode)
                                    {
                                        player.inventory.setInventorySlotContents(slot, null);
                                        player.inventoryContainer.detectAndSendChanges();
                                        this.discharge(stack, ENERGY, true);
                                    }

                                    //Store last time player launched a rocket
                                    clickTimePlayer.put(player.getName(), clickMs);

                                    return new ActionResult<ItemStack>(EnumActionResult.SUCCESS, stack);
                                }
                            }
                        }
                    }
                }
            }
        }

        return new ActionResult<ItemStack>(EnumActionResult.SUCCESS, stack);
    }

    @Override
    protected void getDetailedInfo(ItemStack stack, EntityPlayer player, List lines)
    {
        String str = LanguageUtility.getLocal("item.icbmclassic:rocketLauncher.info").replaceAll("%s", String.valueOf(Settings.ROCKET_LAUNCHER_TIER_FIRE_LIMIT));
        lines.add(str);
    }
}

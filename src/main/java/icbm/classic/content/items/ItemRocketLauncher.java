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
import net.minecraft.util.ChatComponentText;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import resonant.api.explosion.ExplosionEvent.ExplosivePreDetonationEvent;
import resonant.api.explosion.ExplosiveType;

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
        return EnumAction.bow;
    }

    @Override
    public ItemStack onItemRightClick(ItemStack itemStack, World world, EntityPlayer player)
    {
        if (!world.isRemote)
        {
            long clickMs = System.currentTimeMillis();
            if (clickTimePlayer.containsKey(player.getCommandSenderName()))
            {
                if (clickMs - clickTimePlayer.get(player.getCommandSenderName()) < firingDelay)
                {
                    //TODO play weapon empty click audio to note the gun is reloading
                    return itemStack;
                }
            }
            if (this.getEnergy(itemStack) >= ENERGY || player.capabilities.isCreativeMode)
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

                            ExplosivePreDetonationEvent evt = new ExplosivePreDetonationEvent(world, player.posX, player.posY, player.posZ, ExplosiveType.AIR, Explosives.get(meta).handler);
                            MinecraftForge.EVENT_BUS.post(evt);

                            if (((Explosion) ex.handler) != null && !evt.isCanceled())
                            {
                                // Limit the missile to tier two.
                                if ((((Explosion) ex.handler).getTier() <= Settings.ROCKET_LAUNCHER_TIER_FIRE_LIMIT || Engine.runningAsDev) && ((Explosion) ex.handler).isCruise())
                                {
                                    EntityMissile entityMissile = new EntityMissile(player);
                                    entityMissile.missileType = EntityMissile.MissileType.LAUNCHER;
                                    entityMissile.explosiveID = ex;
                                    entityMissile.acceleration = 1;
                                    entityMissile.launch(null);
                                    world.spawnEntityInWorld(entityMissile);

                                    if (player.isSneaking())
                                    {
                                        player.mountEntity(entityMissile);
                                        player.setSneaking(false);
                                    }

                                    if (!player.capabilities.isCreativeMode)
                                    {
                                        player.inventory.setInventorySlotContents(slot, null);
                                        player.inventoryContainer.detectAndSendChanges();
                                        this.discharge(itemStack, ENERGY, true);
                                    }

                                    //Store last time player launched a rocket
                                    clickTimePlayer.put(player.getCommandSenderName(), clickMs);

                                    return itemStack;
                                }
                            }
                            else
                            {
                                player.addChatComponentMessage(new ChatComponentText(LanguageUtility.getLocal("message.launcher.protected")));
                            }
                        }

                    }
                }
            }
        }

        return itemStack;
    }

    @Override
    protected void getDetailedInfo(ItemStack stack, EntityPlayer player, List lines)
    {
        String str = LanguageUtility.getLocal("item.icbmclassic:rocketLauncher.info").replaceAll("%s", String.valueOf(Settings.ROCKET_LAUNCHER_TIER_FIRE_LIMIT));
        lines.add(str);
    }
}

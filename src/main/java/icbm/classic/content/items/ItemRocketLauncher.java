package icbm.classic.content.items;

import com.builtbroken.mc.lib.helper.LanguageUtility;
import com.builtbroken.mc.lib.transform.vector.Pos;
import icbm.classic.Settings;
import icbm.classic.content.entity.EntityMissile;
import icbm.classic.content.explosive.ExplosiveRegistry;
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

                            ExplosivePreDetonationEvent evt = new ExplosivePreDetonationEvent(world, player.posX, player.posY, player.posZ, ExplosiveType.AIR, ExplosiveRegistry.get(meta));
                            MinecraftForge.EVENT_BUS.post(evt);

                            if (((Explosion) ex.handler) != null && !evt.isCanceled())
                            {
                                // Limit the missile to tier two.
                                if (((Explosion) ex.handler).getTier() <= Settings.MAX_ROCKET_LAUCNHER_TIER && ((Explosion) ex.handler).isCruise())
                                {
                                    Pos launcher = new Pos(player).add(new Pos(0, 0.5, 0));
                                    Pos playerAim = new Pos(player.getLook(1));
                                    Pos start = launcher.add(playerAim.multiply(1.1));
                                    Pos target = launcher.add(playerAim.multiply(100));

                                    //TOD: Fix this rotation when we use the proper model loader.
                                    EntityMissile entityMissile = new EntityMissile(world, start, ex, -player.rotationYaw, -player.rotationPitch);
                                    world.spawnEntityInWorld(entityMissile);

                                    if (player.isSneaking())
                                    {
                                        player.mountEntity(entityMissile);
                                        player.setSneaking(false);
                                    }

                                    entityMissile.ignore(player);
                                    entityMissile.launch(target);

                                    if (!player.capabilities.isCreativeMode)
                                    {
                                        player.inventory.setInventorySlotContents(slot, null);
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
    public void addInformation(ItemStack itemStack, EntityPlayer entityPlayer, List list, boolean par4)
    {
        String str = LanguageUtility.getLocal("info.rocketlauncher.tooltip").replaceAll("%s", String.valueOf(Settings.MAX_ROCKET_LAUCNHER_TIER));
        list.add(str);

        super.addInformation(itemStack, entityPlayer, list, par4);
    }
}

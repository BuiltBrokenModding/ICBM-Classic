package icbm.classic.content.items;

import icbm.classic.ICBMClassic;
import icbm.classic.config.ConfigMain;
import icbm.classic.lib.LanguageUtility;
import icbm.classic.content.entity.EntityMissile;
import icbm.classic.content.explosive.Explosives;
import icbm.classic.content.explosive.handlers.Explosion;
import icbm.classic.prefab.item.ItemICBMElectrical;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumAction;
import net.minecraft.item.IItemPropertyGetter;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
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
        this.addPropertyOverride(new ResourceLocation("pulling"), new IItemPropertyGetter()
        {
            @SideOnly(Side.CLIENT)
            public float apply(ItemStack stack, @Nullable World worldIn, @Nullable EntityLivingBase entityIn)
            {
                return entityIn != null && entityIn.isHandActive() && entityIn.getActiveItemStack() == stack ? 1.0F : 0.0F;
            }
        });
    }

    @Override
    public void onUpdate(ItemStack stack, World world, Entity entity, int itemSlot, boolean isSelected)
    {

    }

    @Override
    public int getMaxItemUseDuration(ItemStack stack)
    {
        return Integer.MAX_VALUE;
    }

    @Override
    public EnumAction getItemUseAction(ItemStack par1ItemStack)
    {
        return EnumAction.BOW;
    }

    @Override
    public void onPlayerStoppedUsing(ItemStack stack, World world, EntityLivingBase entityLiving, int timeLeft)
    {
        if (entityLiving instanceof EntityPlayer)
        {
            EntityPlayer player = (EntityPlayer) entityLiving;
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
                                if ((ex.handler.getTier().ordinal() <= ConfigMain.ROCKET_LAUNCHER_TIER_FIRE_LIMIT || ICBMClassic.runningAsDev) && ((Explosion) ex.handler).isCruise())
                                {
                                    if(!world.isRemote)
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
                                    }

                                    //Store last time player launched a rocket
                                    clickTimePlayer.put(player.getName(), System.currentTimeMillis());
                                    return;
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World worldIn, EntityPlayer player, EnumHand handIn)
    {
        ItemStack itemstack = player.getHeldItem(handIn);

        long clickMs = System.currentTimeMillis();
        if (clickTimePlayer.containsKey(player.getName()))
        {
            if (clickMs - clickTimePlayer.get(player.getName()) < firingDelay)
            {
                //TODO play weapon empty click audio to note the gun is reloading
                return new ActionResult<ItemStack>(EnumActionResult.FAIL, itemstack);
            }
        }

        player.setActiveHand(handIn);
        return new ActionResult<ItemStack>(EnumActionResult.SUCCESS, itemstack);
    }

    @Override
    protected void getDetailedInfo(ItemStack stack, EntityPlayer player, List lines)
    {
        String str = LanguageUtility.getLocal("item.icbmclassic:rocketLauncher.info").replaceAll("%s", String.valueOf(ConfigMain.ROCKET_LAUNCHER_TIER_FIRE_LIMIT));
        lines.add(str);
    }
}

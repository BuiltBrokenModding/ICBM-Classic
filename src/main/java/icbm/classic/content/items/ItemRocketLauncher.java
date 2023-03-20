package icbm.classic.content.items;

import icbm.classic.api.ICBMClassicAPI;
import icbm.classic.api.missiles.ICapabilityMissileStack;
import icbm.classic.api.missiles.IMissile;
import icbm.classic.api.missiles.IMissileAiming;
import icbm.classic.config.ConfigMain;
import icbm.classic.config.missile.ConfigMissile;
import icbm.classic.content.missile.logic.flight.DeadFlightLogic;
import icbm.classic.content.missile.source.EntitySourceData;
import icbm.classic.content.missile.source.MissileSourceEntity;
import icbm.classic.lib.LanguageUtility;
import icbm.classic.prefab.item.ItemICBMElectrical;
import net.minecraft.client.Minecraft;
import net.minecraft.client.util.ITooltipFlag;
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
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
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
    private final HashMap<String, Long> clickTimePlayer = new HashMap<String, Long>();

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
                for (int slot = 0; slot < player.inventory.getSizeInventory(); slot++) //TODO add ammo wheel to select missile to use
                {
                    final ItemStack inventoryStack = player.inventory.getStackInSlot(slot);

                    if (inventoryStack.hasCapability(ICBMClassicAPI.MISSILE_STACK_CAPABILITY, null))
                    {
                        final ICapabilityMissileStack capabilityMissileStack = inventoryStack.getCapability(ICBMClassicAPI.MISSILE_STACK_CAPABILITY, null);
                        if (capabilityMissileStack != null)
                        {
                            if (!world.isRemote)
                            {
                                final IMissile missile = capabilityMissileStack.newMissile(world);
                                final Entity entity = missile.getMissileEntity();

                                if (entity instanceof IMissileAiming)
                                {
                                    //Setup aiming and offset from player
                                    ((IMissileAiming) entity).initAimingPosition(player, 1, ConfigMissile.DIRECT_FLIGHT_SPEED);

                                    //Init missile
                                    missile.setFlightLogic(new DeadFlightLogic(ConfigMissile.HANDHELD_FUEL));
                                    missile.setMissileSource(new MissileSourceEntity(world, entity.getPositionVector(), new EntitySourceData(player)));
                                    missile.launch();

                                    //Spawn entity into world
                                    if(world.spawnEntity(entity))
                                    {
                                        if (player.isSneaking()) //TODO allow missile to have control of when riding is allowed
                                        {
                                            player.startRiding(entity);
                                            player.setSneaking(false);
                                        }

                                        if (!player.capabilities.isCreativeMode)
                                        {
                                            player.inventory.setInventorySlotContents(slot, capabilityMissileStack.consumeMissile());
                                            player.inventoryContainer.detectAndSendChanges();
                                            this.discharge(stack, ENERGY, true);
                                        }
                                    }
                                    else
                                    {
                                        player.sendStatusMessage(new TextComponentTranslation("item.icbmclassic:rocketLauncher.error.spawning"), true);
                                    }
                                }
                                else
                                {
                                    player.sendStatusMessage(new TextComponentTranslation("item.icbmclassic:rocketLauncher.error.IMissileAiming", inventoryStack), true);
                                }

                                //Exit loop to prevent firing all missiles in inventory
                                return;
                            }

                            //Store last time player launched a rocket
                            clickTimePlayer.put(player.getName(), System.currentTimeMillis());
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
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, World world, List<String> list, ITooltipFlag flag)
    {
        final String key = "item.icbmclassic:rocketLauncher.info";
        String translation = LanguageUtility.getLocal(key);

        if (translation.contains("%s"))
        {
            String str = String.format(translation, String.valueOf(ConfigMain.ROCKET_LAUNCHER_TIER_FIRE_LIMIT));
            splitAdd(str, list, false, false);
        }

        if (Minecraft.getMinecraft().player != null && Minecraft.getMinecraft().player.isCreative())
            list.add(new TextComponentTranslation("item.icbmclassic:rocketLauncher.info.creative").setStyle(new Style().setColor(TextFormatting.LIGHT_PURPLE)).getFormattedText());
    }
}

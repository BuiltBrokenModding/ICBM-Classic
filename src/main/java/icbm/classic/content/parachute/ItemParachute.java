package icbm.classic.content.parachute;

import icbm.classic.api.ICBMClassicAPI;
import icbm.classic.lib.LanguageUtility;
import icbm.classic.prefab.item.ItemBase;
import icbm.classic.prefab.item.ItemStackCapProvider;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.EnumAction;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.*;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public class ItemParachute extends ItemBase
{
    public static final int MAX_USE_DURATION = 3 * 20; //TODO config

    public ItemParachute()
    {
        this.setName("parachute");
        this.setMaxStackSize(16);
        this.setMaxDamage(0);
        this.setHasSubtypes(true);
    }

    @Override
    @Nullable
    public net.minecraftforge.common.capabilities.ICapabilityProvider initCapabilities(@Nonnull ItemStack stack, @Nullable NBTTagCompound nbt)
    {
        final ItemStackCapProvider provider = new ItemStackCapProvider(stack);
        provider.add("projectile", ICBMClassicAPI.PROJECTILE_STACK_CAPABILITY, new ParachuteProjectileStack());
        return provider;
    }

    @Override
    public EnumAction getItemUseAction(@Nonnull ItemStack stack)
    {
        return EnumAction.BOW;
    }

    @Override
    public int getMaxItemUseDuration(@Nonnull ItemStack stack)
    {
        return MAX_USE_DURATION;
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(@Nonnull World world, EntityPlayer playerIn, @Nonnull EnumHand handIn)
    {
        final ItemStack itemstack = playerIn.getHeldItem(handIn);
        playerIn.setActiveHand(handIn);
        return new ActionResult<ItemStack>(EnumActionResult.SUCCESS, itemstack);
    }

    @Override
    public void onPlayerStoppedUsing(@Nonnull ItemStack stack, @Nonnull World world, @Nonnull EntityLivingBase entityLiving, int timeLeft)
    {
        if (!world.isRemote) {
            final EntityParachute parachute = new EntityParachute(world);
            parachute.initAimingPosition(entityLiving, 1, 0.1f);

            final EntityItem entityItem = new EntityItem(world);
            entityItem.setPickupDelay(100);
            entityItem.setItem(new ItemStack(Items.EGG));
            entityItem.setPosition(parachute.x(), parachute.y(), parachute.z());
            if(world.spawnEntity(entityItem)) {

                entityItem.startRiding(parachute);

                // Spawn and then consume item if successful
                if ((world.spawnEntity(parachute) && !(entityLiving instanceof EntityPlayer) || !((EntityPlayer) entityLiving).isCreative())) {
                    stack.shrink(1);
                }
            }
        }
    }

    @Override
    public int getMetadata(int damage)
    {
        return damage;
    }

    @Override
    public void addInformation(ItemStack stack, World world, List<String> list, ITooltipFlag flag)
    {
        final String key = getUnlocalizedName(stack) + ".info";
        final float gravity = -EntityParachute.GRAVITY * 20;
        final float air = (1 - EntityParachute.AIR_RESISTANCE) * 100;
        LanguageUtility.outputLines(new TextComponentTranslation(key, String.format("%.2f", air) + " %", String.format("%.2f", gravity)), list::add);
    }
}

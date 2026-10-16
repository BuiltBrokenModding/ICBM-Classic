package icbm.classic.content.items;

import icbm.classic.api.ICBMClassicAPI;
import icbm.classic.content.entity.EntityGrenade;
import icbm.classic.content.reg.ItemReg;
import icbm.classic.lib.capability.ex.CapabilityExplosiveStack;
import icbm.classic.lib.capability.holder.ICapabilityHolderItem;
import icbm.classic.prefab.item.ISubItemProvider;
import icbm.classic.prefab.item.ItemBase;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.capabilities.ItemCapability;

import javax.annotation.Nullable;
import java.util.List;

public class ItemGrenade extends ItemBase implements ICapabilityHolderItem, ISubItemProvider
{
    public static final int MAX_USE_DURATION = 3 * 20; //TODO config

    public ItemGrenade()
    {
        super(new Item.Properties().stacksTo(16));
    }

    @Nullable
    @Override
    @SuppressWarnings("unchecked")
    public <T> T getCapability(ItemCapability<T, Void> capability, ItemStack stack)
    {
        if (capability == ICBMClassicAPI.EXPLOSIVE_ITEM_CAPABILITY)
        {
            return (T) new CapabilityExplosiveStack(stack);
        }
        return null;
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack)
    {
        return UseAnim.BOW;
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity)
    {
        return MAX_USE_DURATION;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level worldIn, Player playerIn, InteractionHand handIn)
    {
        ItemStack itemstack = playerIn.getItemInHand(handIn);
        playerIn.startUsingItem(handIn);
        // PORT: consume instead of success, in 1.21 success swings the arm before the throw pose
        return InteractionResultHolder.consume(itemstack);
    }

    @Override
    public void releaseUsing(ItemStack itemStack, Level world, LivingEntity entityLiving, int timeLeft)
    {
        if (!world.isClientSide())
        {
            //Calculate energy based on player hold time
            final float throwEnergy = (float) (MAX_USE_DURATION - timeLeft) / (float) MAX_USE_DURATION;
            throwGrenade(itemStack, world, entityLiving, throwEnergy);
        }
    }

    /**
     * PORT: 1.12 silently dropped the throw once the full use duration ran out, as the use finished
     * without a release. Throw at full power instead.
     */
    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level world, LivingEntity entityLiving)
    {
        if (!world.isClientSide())
        {
            throwGrenade(stack, world, entityLiving, 1.0F);
        }
        return stack;
    }

    private void throwGrenade(ItemStack itemStack, Level world, LivingEntity entityLiving, float throwEnergy)
    {
        //Play throw sound
        world.playSound(null, entityLiving.getX(), entityLiving.getY(), entityLiving.getZ(), SoundEvents.TNT_PRIMED, SoundSource.BLOCKS, 0.5F, 0.4F / (world.random.nextFloat() * 0.4F + 0.8F));

        //Create generate entity
        new EntityGrenade(world)
            .setItemStack(itemStack)
            .setThrower(entityLiving)
            .aimFromThrower()
            .setThrowMotion(throwEnergy).spawn();

        //Consume item
        if (!(entityLiving instanceof Player) || !((Player) entityLiving).isCreative())
        {
            itemStack.shrink(1);
        }
    }

    @Override
    public String getDescriptionId(ItemStack itemstack)
    {
        return ExplosiveItemHelper.getVariantKey("grenade", itemstack);
    }

    @Override
    public String getDescriptionId()
    {
        return "grenade";
    }

    @Override
    protected boolean hasDetailedInfo(ItemStack stack, @Nullable Player player)
    {
        return true;
    }

    @Override
    protected void getDetailedInfo(ItemStack stack, @Nullable Player player, List<Component> list)
    {
        ItemReg.itemBlockExplosive.get().getDetailedInfo(stack, player, list);
    }

    @Override
    public void getSubItems(NonNullList<ItemStack> list)
    {
        ExplosiveItemHelper.addSubItems(this, ICBMClassicAPI.EX_GRENADE_REGISTRY, list);
    }
}
// PORTED

package icbm.classic.content.items;

import icbm.classic.prefab.item.ItemBase;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;

public class ItemAntidote extends ItemBase
{

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level world, LivingEntity entityLiving)
    {
        if (!world.isClientSide())
        {
            entityLiving.removeAllEffects();
        }
        if (!(entityLiving instanceof Player) || !((Player) entityLiving).isCreative())
        {
            stack.shrink(1);
        }
        return stack;
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity)
    {
        return 32;
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack)
    {
        return UseAnim.EAT;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level worldIn, Player playerIn, InteractionHand handIn)
    {
        final ItemStack itemstack = playerIn.getItemInHand(handIn);
        playerIn.startUsingItem(handIn);
        // PORT: consume instead of success, in 1.21 success swings the arm before the drink pose
        return InteractionResultHolder.consume(itemstack);
    }
}
// PORTED

package icbm.classic.content.items;

import icbm.classic.content.entity.EntityGrenade;
import icbm.classic.prefab.item.ItemBase;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.UseAction;
import net.minecraft.util.*;
import net.minecraft.world.World;
import net.minecraftforge.common.util.NonNullSupplier;

public class ItemGrenade extends ItemBase
{
    public static final int MAX_USE_DURATION = 3 * 20; //TODO config

    private final NonNullSupplier<EntityType<EntityGrenade>> entityType;
    public ItemGrenade(NonNullSupplier<EntityType<EntityGrenade>> entityType, Item.Properties properties)
    {
        super(properties);
        this.entityType = entityType;
    }

    @Override
    public UseAction getUseAction(ItemStack par1ItemStack)
    {
        return UseAction.BOW;
    }

    @Override
    public int getUseDuration(ItemStack par1ItemStack)
    {
        return MAX_USE_DURATION;
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World worldIn, PlayerEntity playerIn, Hand handIn)
    {
        ItemStack itemstack = playerIn.getHeldItem(handIn);
        playerIn.setActiveHand(handIn);
        return new ActionResult<ItemStack>(ActionResultType.SUCCESS, itemstack);
    }

    @Override
    public void onPlayerStoppedUsing(ItemStack itemStack, World world, LivingEntity entityLiving, int timeLeft)
    {
        if (!world.isRemote)
        {
            //Play throw sound
            world.playSound(null,
                entityLiving.posX, entityLiving.posY, entityLiving.posZ,
                SoundEvents.ENTITY_TNT_PRIMED, SoundCategory.BLOCKS, 0.5F, 0.4F / world.rand.nextFloat() * 0.4F + 0.8F); //TODO find a different audio?

            //Calculate energy based on player hold time
            final float throwEnergy = (float) (this.getUseDuration(itemStack) - timeLeft) / (float) this.getUseDuration(itemStack);

            //Create generate entity
            final EntityGrenade grenade = entityType.get().create(world);
            grenade.initAimingPosition(entityLiving, 1, 1.8F * throwEnergy);

            //Consume item
            if (world.addEntity(grenade) && (!(entityLiving instanceof PlayerEntity) || !((PlayerEntity) entityLiving).isCreative()))
            {
                itemStack.shrink(1);
            }
        }
    }

    /*@Override
    protected boolean hasDetailedInfo(ItemStack stack, PlayerEntity player)
    {
        return true;
    }

    @Override
    protected void getDetailedInfo(ItemStack stack, PlayerEntity player, List list)
    {
        ((ItemBlockExplosive) Item.getItemFromBlock(BlockReg.blockExplosive)).getDetailedInfo(stack, player, list);
    }*/
}

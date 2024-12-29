package icbm.classic.content.items;

import icbm.classic.api.ICBMClassicAPI;
import icbm.classic.api.reg.IExplosiveData;
import icbm.classic.content.blocks.explosive.ItemBlockExplosive;
import icbm.classic.content.entity.EntityGrenade;
import icbm.classic.content.reg.BlockReg;
import icbm.classic.lib.capability.ex.CapabilityExplosiveStack;
import icbm.classic.prefab.item.ItemBase;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.SoundEvents;
import net.minecraft.item.UseAction;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.util.*;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.List;

public class ItemGrenade extends ItemBase
{
    public static final int MAX_USE_DURATION = 3 * 20; //TODO config

    private final IExplosiveData data;

    public ItemGrenade(IExplosiveData data)
    {
        super(new Properties().maxStackSize(16));
        this.data = data;
    }

    @Override
    @Nullable
    public net.minecraftforge.common.capabilities.ICapabilityProvider initCapabilities(ItemStack stack, @Nullable CompoundNBT nbt)
    {
        final CapabilityExplosiveStack capabilityExplosive = new CapabilityExplosiveStack(stack);
        if(nbt != null)
        {
            capabilityExplosive.deserializeNBT(nbt);
        }
        return capabilityExplosive;
    }

    @Override
    public UseAction getItemUseAction(ItemStack par1ItemStack)
    {
        return UseAction.BOW;
    }

    @Override
    public int getMaxItemUseDuration(ItemStack par1ItemStack)
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
            world.playSound(null, entityLiving.posX, entityLiving.posY, entityLiving.posZ, SoundEvents.ENTITY_TNT_PRIMED, SoundCategory.BLOCKS, 0.5F, 0.4F / (itemRand.nextFloat() * 0.4F + 0.8F));

            //Calculate energy based on player hold time
            final float throwEnergy = (float) (this.getMaxItemUseDuration(itemStack) - timeLeft) / (float) this.getMaxItemUseDuration(itemStack);

            //Create generate entity
            new EntityGrenade(world)
            .setItemStack(itemStack)
            .setThrower(entityLiving)
            .aimFromThrower()
            .setThrowMotion(throwEnergy).spawn();

            //Consume item
            if (!(entityLiving instanceof PlayerEntity) || !((PlayerEntity) entityLiving).capabilities.isCreativeMode)
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

    @Override
    public void getSubItems(ItemGroup tab, NonNullList<ItemStack> list)
    {
        if (tab == getCreativeTab() || tab == ItemGroup.SEARCH)
        {
            for (int id : ICBMClassicAPI.EX_GRENADE_REGISTRY.getExplosivesIDs())
            {
                list.add(new ItemStack(this, 1, id));
            }
        }
    }
}

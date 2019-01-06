package icbm.classic.content.items;

import icbm.classic.api.ICBMClassicAPI;
import icbm.classic.api.reg.IExplosiveData;
import icbm.classic.content.entity.EntityGrenade;
import icbm.classic.content.explosive.Explosives;
import icbm.classic.prefab.item.ItemICBMBase;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.EnumAction;
import net.minecraft.item.ItemStack;
import net.minecraft.util.*;
import net.minecraft.world.World;

import java.util.List;

public class ItemGrenade extends ItemICBMBase
{
    public ItemGrenade()
    {
        super("grenade");
        this.setMaxStackSize(16);
        this.setMaxDamage(0);
        this.setHasSubtypes(true);
    }

    @Override
    public EnumAction getItemUseAction(ItemStack par1ItemStack)
    {
        return EnumAction.BOW;
    }

    @Override
    public int getMaxItemUseDuration(ItemStack par1ItemStack)
    {
        return 3 * 20;
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World worldIn, EntityPlayer playerIn, EnumHand handIn)
    {
        ItemStack itemstack = playerIn.getHeldItem(handIn);
        playerIn.setActiveHand(handIn);
        return new ActionResult<ItemStack>(EnumActionResult.SUCCESS, itemstack);
    }

    @Override
    public void onPlayerStoppedUsing(ItemStack itemStack, World world, EntityLivingBase entityLiving, int timeLeft)
    {
        if (!world.isRemote)
        {
            final Explosives explosive = Explosives.get(itemStack.getItemDamage());

            world.playSound(null, entityLiving.posX, entityLiving.posY, entityLiving.posZ, SoundEvents.ENTITY_TNT_PRIMED, SoundCategory.BLOCKS, 0.5F, 0.4F / (itemRand.nextFloat() * 0.4F + 0.8F));
            world.spawnEntity(new EntityGrenade(world, entityLiving, explosive, (float) (this.getMaxItemUseDuration(itemStack) - timeLeft) / (float) this.getMaxItemUseDuration(itemStack)));

            if (!(entityLiving instanceof EntityPlayer) || !((EntityPlayer) entityLiving).capabilities.isCreativeMode)
            {
                itemStack.shrink(1);
            }
        }
    }

    @Override
    public int getMetadata(int damage)
    {
        return damage;
    }

    @Override
    public String getTranslationKey(ItemStack itemstack)
    {
        final IExplosiveData data = ICBMClassicAPI.EXPLOSIVE_REGISTRY.getExplosiveData(itemstack.getItemDamage());
        if (data != null)
        {
            return super.getTranslationKey() + data.getRegistryName();
        }
        return super.getTranslationKey(itemstack);
    }

    @Override
    public String getTranslationKey()
    {
        return "icbm.grenade";
    }

    @Override
    protected boolean hasDetailedInfo(ItemStack stack, EntityPlayer player)
    {
        return true;
    }

    @Override
    protected void getDetailedInfo(ItemStack stack, EntityPlayer player, List list)
    {
        //TODO ((ItemBlockExplosive) Item.getItemFromBlock(ICBMClassic.blockExplosive)).getDetailedInfo(stack, player, list);
    }

    @Override
    public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> list)
    {
        if (tab == getCreativeTab())
        {
            for (Explosives ex : Explosives.values())
            {
                if (ex.handler.hasGrenadeForm())
                {
                    list.add(new ItemStack(this, 1, ex.ordinal()));
                }
            }
        }
    }
}

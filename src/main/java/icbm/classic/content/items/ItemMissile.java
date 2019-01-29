package icbm.classic.content.items;

import icbm.classic.api.ICBMClassicAPI;
import icbm.classic.api.ICBMClassicHelpers;
import icbm.classic.api.reg.IExplosiveData;
import icbm.classic.content.entity.missile.CapabilityMissile;
import icbm.classic.content.reg.BlockReg;
import icbm.classic.lib.LanguageUtility;
import icbm.classic.content.blocks.explosive.ItemBlockExplosive;
import icbm.classic.api.EnumTier;
import icbm.classic.lib.capability.ex.CapabilityExplosive;
import icbm.classic.lib.capability.ex.CapabilityExplosiveStack;
import icbm.classic.prefab.item.ItemICBMBase;
import icbm.classic.prefab.item.ItemStackCapProvider;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.NonNullList;

import javax.annotation.Nullable;
import java.util.List;

public class ItemMissile extends ItemICBMBase
{

    public ItemMissile()
    {
        super("missile");
        this.setMaxDamage(0);
        this.setHasSubtypes(true);
        this.setMaxStackSize(1);
    }

    @Override
    @Nullable
    public net.minecraftforge.common.capabilities.ICapabilityProvider initCapabilities(ItemStack stack, @Nullable NBTTagCompound nbt)
    {
        ItemStackCapProvider provider = new ItemStackCapProvider(stack);
        //provider.add("missile", ICBMClassicAPI.MISSILE_CAPABILITY, new CapabilityMissile()); //TODO create an itemstack version
        provider.add(CapabilityExplosive.NBT_EXPLOIVE, ICBMClassicAPI.EXPLOSIVE_CAPABILITY, new CapabilityExplosiveStack(stack));
        return provider;
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
            return "missile." + data.getRegistryName();
        }
        return "missile";
    }

    @Override
    public String getTranslationKey()
    {
        return "missile";
    }

    @Override
    public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> items)
    {
        if (tab == getCreativeTab())
        {
            for (int id : ICBMClassicAPI.EX_MISSILE_REGISTRY.getExplosivesIDs())
            {
                items.add(new ItemStack(this, 1, id));
            }
        }
    }

    @Override
    protected boolean hasDetailedInfo(ItemStack stack, EntityPlayer player)
    {
        return true;
    }

    @Override
    protected void getDetailedInfo(ItemStack stack, EntityPlayer player, List list)
    {
        IExplosiveData data = ICBMClassicHelpers.getExplosive(stack.getItemDamage(), true);
        if (data != null)
        {
            final EnumTier tierdata = data.getTier();
            list.add(LanguageUtility.getLocal("info.misc.tier") + ": " + tierdata.getName());

            //TODO add hook
            ((ItemBlockExplosive) Item.getItemFromBlock(BlockReg.blockExplosive)).getDetailedInfo(stack, player, list);
        }
    }
}

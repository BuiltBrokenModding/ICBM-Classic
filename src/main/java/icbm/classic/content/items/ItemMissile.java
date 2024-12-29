package icbm.classic.content.items;

import icbm.classic.api.ICBMClassicAPI;
import icbm.classic.api.ICBMClassicHelpers;
import icbm.classic.api.caps.IExplosive;
import icbm.classic.api.reg.IExplosiveData;
import icbm.classic.content.blocks.explosive.ItemBlockExplosive;
import icbm.classic.content.reg.BlockReg;
import icbm.classic.lib.capability.ex.CapabilityExplosiveStack;
import icbm.classic.lib.capability.missile.CapabilityMissileStack;
import icbm.classic.prefab.item.ItemBase;
import icbm.classic.prefab.item.ItemStackCapProvider;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.NonNullList;

import javax.annotation.Nullable;
import java.util.List;

public class ItemMissile extends ItemBase
{
    private final IExplosiveData data;

    public ItemMissile(IExplosiveData data, Properties p_i48487_1_) {
        super(p_i48487_1_);
        this.data = data;
    }

    @Override
    @Nullable
    public net.minecraftforge.common.capabilities.ICapabilityProvider initCapabilities(ItemStack stack, @Nullable CompoundNBT nbt)
    {
        final ItemStackCapProvider provider = new ItemStackCapProvider(stack);
        provider.add("explosive", ICBMClassicAPI.EXPLOSIVE_CAPABILITY, new CapabilityExplosiveStack(stack));
        provider.add("missile", ICBMClassicAPI.MISSILE_STACK_CAPABILITY, new CapabilityMissileStack(stack));
        return provider;
    }

    @Override
    protected boolean hasDetailedInfo(ItemStack stack, PlayerEntity player)
    {
        return true;
    }

    @Override
    protected void getDetailedInfo(ItemStack stack, PlayerEntity player, List list)
    {
        //TODO add hook
        //((ItemBlockExplosive) Item.getItemFromBlock(BlockReg.blockExplosive)).getDetailedInfo(stack, player, list);
        final IExplosive explosive = ICBMClassicHelpers.getExplosive(stack);
        if(explosive != null) { //TODO make shift-key display?
            explosive.collectInformation(list::add);
        }
    }
}

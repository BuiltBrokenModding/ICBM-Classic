package icbm.classic.content.blocks.explosive;

import icbm.classic.api.refs.ICBMExplosives;
import icbm.classic.api.reg.IExplosiveData;
import icbm.classic.lib.capability.ex.CapabilityExplosiveStatic;
import icbm.classic.prefab.item.ItemBlockAbstract;
import net.minecraft.block.Block;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraftforge.common.capabilities.ICapabilityProvider;

import javax.annotation.Nullable;

public class ItemBlockExplosive extends BlockItem
{
    public ItemBlockExplosive(Block block)
    {
        super(block, new Properties().maxStackSize(64));
    }

    @Override
    @Nullable
    public ICapabilityProvider initCapabilities(ItemStack stack, @Nullable CompoundNBT nbt)
    {
        IExplosiveData explosiveData = ICBMExplosives.CONDENSED;
        if(getBlock() instanceof BlockExplosive) {
            explosiveData = ((BlockExplosive) getBlock()).explosiveData;
        }
        return new CapabilityExplosiveStatic(explosiveData, () -> stack);
    }
}

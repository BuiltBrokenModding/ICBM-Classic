package icbm.classic.content.blocks.explosive;

import net.minecraft.block.Block;
import net.minecraft.item.BlockItem;

public class ItemBlockExplosive extends BlockItem
{
    public ItemBlockExplosive(Block block, Properties properties)
    {
        super(block, properties);
    }

   /* @Override TODO implement capability for providing action data
    @Nullable
    public ICapabilityProvider initCapabilities(ItemStack stack, @Nullable CompoundNBT nbt)
    {
        IExplosiveData explosiveData = ICBMExplosives.CONDENSED;
        if(getBlock() instanceof BlockExplosive) {
            explosiveData = ((BlockExplosive) getBlock()).explosiveData;
        }
        return new CapabilityExplosiveStatic(explosiveData, () -> stack);
    }*/
}

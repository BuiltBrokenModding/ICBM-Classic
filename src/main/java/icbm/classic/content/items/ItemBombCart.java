package icbm.classic.content.items;

import icbm.classic.api.reg.IExplosiveData;
import icbm.classic.content.entity.EntityBombCart;
import icbm.classic.prefab.item.ItemBase;
import net.minecraft.block.AbstractRailBlock;
import net.minecraft.block.BlockState;
import net.minecraft.entity.EntityType;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.state.properties.RailShape;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.NonNullSupplier;

import javax.annotation.Nullable;

public class ItemBombCart extends ItemBase
{
    private final NonNullSupplier<EntityType<EntityBombCart>> entityType;
    public ItemBombCart(NonNullSupplier<EntityType<EntityBombCart>> entityType, Properties properties)
    {
        super(properties);
        this.entityType = entityType;
    }

    @Override
    public ActionResultType onItemUse(ItemUseContext context) {
        World world = context.getWorld();
        BlockPos blockpos = context.getPos();
        BlockState blockstate = world.getBlockState(blockpos);
        if (!blockstate.isIn(BlockTags.RAILS)) {
            return ActionResultType.FAIL;
        } else {
            ItemStack itemstack = context.getItem();
            if (!world.isRemote) {
                RailShape railshape = blockstate.getBlock() instanceof AbstractRailBlock ? ((AbstractRailBlock)blockstate.getBlock()).getRailDirection(blockstate, world, blockpos, null) : RailShape.NORTH_SOUTH;
                double d0 = 0.0D;
                if (railshape.isAscending()) {
                    d0 = 0.5D;
                }

                final EntityBombCart cart = this.entityType.get().create(world);
                cart.setPosition((double)blockpos.getX() + 0.5D, (double)blockpos.getY() + 0.0625D + d0, (double)blockpos.getZ() + 0.5D);

                if (itemstack.hasDisplayName()) {
                    cart.setCustomName(itemstack.getDisplayName());
                }

                world.addEntity(cart);
            }

            itemstack.shrink(1);
            return ActionResultType.SUCCESS;
        }
    }

   /* @Override
    protected boolean hasDetailedInfo(ItemStack stack, PlayerEntity player)
    {
        return true;
    }

    @Override
    protected void getDetailedInfo(ItemStack stack, PlayerEntity player, List list)
    {
        //TODO change over to a hook
        //((ItemBlockExplosive) Item.getItemFromBlock(BlockReg.blockExplosive)).getDetailedInfo(stack, player, list);
    }*/
}

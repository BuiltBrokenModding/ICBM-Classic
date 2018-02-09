package icbm.classic.content.items;

import icbm.classic.lib.LanguageUtility;
import icbm.classic.ICBMClassic;
import icbm.classic.content.entity.EntityBombCart;
import icbm.classic.content.explosive.Explosives;
import icbm.classic.content.explosive.tile.BlockExplosive;
import icbm.classic.content.explosive.tile.ItemBlockExplosive;
import icbm.classic.prefab.tile.EnumTier;
import icbm.classic.prefab.item.ItemICBMBase;
import net.minecraft.block.BlockRailBase;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.item.EntityMinecart;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.List;

public class ItemBombCart extends ItemICBMBase
{
    public ItemBombCart()
    {
        super("bombcart");
        this.setMaxStackSize(3);
        this.setHasSubtypes(true);
    }

    /**
     * Callback for item usage. If the item does something special on right clicking, he will have
     * one of those. Return True if something happen and false if it don't. This is for ITEMS, not
     * BLOCKS
     */
    @Override
    public EnumActionResult onItemUse(EntityPlayer player, World worldIn, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ)
    {
        IBlockState iblockstate = worldIn.getBlockState(pos);

        if (!BlockRailBase.isRailBlock(iblockstate))
        {
            return EnumActionResult.FAIL;
        }
        else
        {
            ItemStack itemstack = player.getHeldItem(hand);

            if (!worldIn.isRemote)
            {
                BlockRailBase.EnumRailDirection blockrailbase$enumraildirection = iblockstate.getBlock() instanceof BlockRailBase ? ((BlockRailBase)iblockstate.getBlock()).getRailDirection(worldIn, pos, iblockstate, null) : BlockRailBase.EnumRailDirection.NORTH_SOUTH;
                double d0 = 0.0D;

                if (blockrailbase$enumraildirection.isAscending())
                {
                    d0 = 0.5D;
                }

                EntityMinecart entityminecart = new EntityBombCart(worldIn, (double)pos.getX() + 0.5D, (double)pos.getY() + 0.0625D + d0, (double)pos.getZ() + 0.5D, Explosives.get(itemstack.getItemDamage()));

                if (itemstack.hasDisplayName())
                {
                    entityminecart.setCustomNameTag(itemstack.getDisplayName());
                }

                worldIn.spawnEntity(entityminecart);
            }

            itemstack.shrink(1);
            return EnumActionResult.SUCCESS;
        }
    }

    @Override
    public int getMetadata(int damage)
    {
        return damage;
    }

    @Override
    public String getUnlocalizedName(ItemStack itemstack)
    {
        return "icbm.minecart." + Explosives.get(itemstack.getItemDamage()).handler.getUnlocalizedName();
    }

    @Override
    public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> items)
    {
        if (tab == getCreativeTab())
        {
            for (Explosives zhaPin : Explosives.values())
            {
                if (zhaPin.handler.hasMinecartForm())
                {
                    items.add(new ItemStack(this, 1, zhaPin.ordinal()));
                }
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
        EnumTier tierdata = Explosives.get(stack.getItemDamage()).handler.getTier();
        list.add(LanguageUtility.getLocal("info.misc.tier") + ": " + tierdata.ordinal());
        if (ICBMClassic.blockExplosive instanceof BlockExplosive)
        {
            ((ItemBlockExplosive) Item.getItemFromBlock(ICBMClassic.blockExplosive)).getDetailedInfo(stack, player, list);
        }
    }
}

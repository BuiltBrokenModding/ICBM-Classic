package icbm.classic.world.item;

import icbm.classic.api.ICBMClassicAPI;
import icbm.classic.api.reg.ExplosiveType;
import icbm.classic.lib.capability.ex.CapabilityExplosiveStack;
import icbm.classic.prefab.item.ItemBase;
import icbm.classic.world.block.explosive.ItemBlockExplosive;
import icbm.classic.world.entity.BombCartEntity;
import net.minecraft.block.BlockRailBase;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.NonNullList;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.item.EntityMinecart;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.common.capabilities.ICapabilityProvider;

import javax.annotation.Nullable;
import java.util.List;

public class BombCartItem extends ItemBase {

    public BombCartItem(Properties properties, ExplosiveType type) {
        super(properties);
        this.setMaxStackSize(3);
        this.setHasSubtypes(true);
    }

    @Override
    @Nullable
    public ICapabilityProvider initCapabilities(ItemStack stack, @Nullable CompoundTag nbt) {
        CapabilityExplosiveStack capabilityExplosive = new CapabilityExplosiveStack(stack);
        if (nbt != null) {
            capabilityExplosive.deserializeNBT(nbt);
        }
        return capabilityExplosive;
    }

    /**
     * Callback for item usage. If the item does something special on right clicking, he will have
     * one of those. Return True if something happen and false if it don't. This is for ITEMS, not
     * BLOCKS
     */
    @Override
    public EnumActionResult onItemUse(Player player, Level levelIn, BlockPos pos, InteractionHand hand, Direction facing, float hitX, float hitY, float hitZ) {
        BlockState iblockstate = worldIn.getBlockState(pos);

        if (!BlockRailBase.isRailBlock(iblockstate)) {
            return EnumActionResult.FAIL;
        } else {
            ItemStack itemstack = player.getHeldItem(hand);

            if (!worldIn.isClientSide()) {
                BlockRailBase.EnumRailDirection railBlock = iblockstate.getBlock() instanceof BlockRailBase ? ((BlockRailBase) iblockstate.getBlock()).getRailDirection(worldIn, pos, iblockstate, null) : BlockRailBase.EnumRailDirection.NORTH_SOUTH;
                double d0 = 0.0D;

                if (railBlock.isAscending()) {
                    d0 = 0.5D;
                }

                EntityMinecart entityminecart = new BombCartEntity(worldIn, (double) pos.getX() + 0.5D, (double) pos.getY() + 0.0625D + d0, (double) pos.getZ() + 0.5D, itemstack.getItemDamage());

                if (itemstack.hasDisplayName()) {
                    entityminecart.setCustomNameTag(itemstack.getDisplayName());
                }

                worldIn.spawnEntity(entityminecart);
            }

            itemstack.shrink(1);
            return EnumActionResult.SUCCESS;
        }
    }

    @Override
    public int getMetadata(int damage) {
        return damage;
    }

    @Override
    public String getUnlocalizedName(ItemStack itemstack) {
        final ExplosiveType data = ICBMClassicAPI.EXPLOSIVE_REGISTRY.getExplosiveData(itemstack.getItemDamage());
        if (data != null) {
            return "bombcart." + data.getRegistryName();
        }
        return "bombcart";
    }

    @Override
    public String getUnlocalizedName() {
        return "bombcart";
    }

    @Override
    public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> items) {
        if (tab == getCreativeTab() || tab == CreativeTabs.SEARCH) {
            for (int id : ICBMClassicAPI.EX_MINECART_REGISTRY.getExplosivesIDs()) {
                items.add(new ItemStack(this, 1, id));
            }
        }
    }

    @Override
    protected boolean hasDetailedInfo(ItemStack stack, Player player) {
        return true;
    }

    @Override
    protected void getDetailedInfo(ItemStack stack, Player player, List list) {
        //TODO change over to a hook
        ((ItemBlockExplosive) Item.getItemFromBlock(BlockReg.blockExplosive)).getDetailedInfo(stack, player, list);
    }
}

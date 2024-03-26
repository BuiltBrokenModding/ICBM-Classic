package icbm.classic.prefab.item;

import icbm.classic.lib.LanguageUtility;
import net.minecraft.block.SoundType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.SoundCategory;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.lwjgl.input.Keyboard;

import javax.annotation.Nullable;
import java.util.List;

/**
 * Generic prefab to use in all items providing common implementation
 * <p>
 * <p>
 * Created by Dark(DarkGuardsman, Robert) on 12/20/2016.
 */
public class IcbmBlockItem extends BlockItem {
    //Make sure to mirror all changes to other abstract class
    public IcbmBlockItem(Block block, Item.Properties properties) {
        super(block, properties);
    }

    @Override
    public EnumActionResult onItemUse(Player player, Level levelIn, BlockPos pos, InteractionHand hand, Direction facing, float hitX, float hitY, float hitZ) {
        BlockState iblockstate = worldIn.getBlockState(pos);
        Block block = iblockstate.getBlock();

        if (!block.isReplaceable(worldIn, pos)) {
            pos = pos.offset(facing);
        }

        ItemStack itemstack = player.getHeldItem(hand);

        if (!itemstack.isEmpty() && canPlace(player, worldIn, pos, itemstack, facing, hitX, hitY, hitZ)) {
            int i = this.getMetadata(itemstack.getMetadata());
            BlockState iblockstate1 = this.block.getStateForPlacement(worldIn, pos, facing, hitX, hitY, hitZ, i, player, hand);

            if (placeBlockAt(itemstack, player, worldIn, pos, facing, hitX, hitY, hitZ, iblockstate1)) {
                iblockstate1 = worldIn.getBlockState(pos);
                SoundType soundtype = iblockstate1.getBlock().getSoundType(iblockstate1, worldIn, pos, player);
                worldIn.playSound(player, pos, soundtype.getPlaceSound(), SoundCategory.BLOCKS, (soundtype.getVolume() + 1.0F) / 2.0F, soundtype.getPitch() * 0.8F);
                itemstack.shrink(1);
            }

            return EnumActionResult.SUCCESS;
        } else {
            return EnumActionResult.FAIL;
        }
    }

    /**
     * Called to check if the player can place the block
     * <p>
     * Allows for easy override of placement checks
     *
     * @param player
     * @param worldIn
     * @param pos
     * @param itemstack
     * @param facing
     * @param hitX
     * @param hitY
     * @param hitZ
     * @return
     */
    protected boolean canPlace(Player player, Level levelIn, BlockPos pos, ItemStack itemstack, Direction facing, float hitX, float hitY, float hitZ) {
        return player.canPlayerEdit(pos, facing, itemstack) && worldIn.mayPlace(this.block, pos, false, facing, (Entity) null);
    }

    @Override
    public Component getDescription() {
        return super.getName();
    }

    @Override
    public Component addInformation(ItemStack stack, @Nullable Level level, List list, ITooltipFlag flag) {
        //Get player, don't run tool tips without
        final Player player = Minecraft.getMinecraft().player;
        try {
            //Generic info
            String translationKey = getUnlocalizedName(stack) + ".info";
            String translation = LanguageUtility.getLocal(translationKey);
            if (!translation.isEmpty() && !translation.equals(translationKey)) {
                list.add(translation);
            }

            getDetailedInfo(stack, player, list);

            if (hasShiftInfo(stack, player)) {
                if (!Keyboard.isKeyDown(Keyboard.KEY_LSHIFT)) {
                    list.add(LanguageUtility.getLocal("info.voltzengine:tooltip.noShift").replace("#0", "\u00a7b").replace("#1", "\u00a77"));
                } else {
                    getShiftDetailedInfo(stack, player, list);
                }
            }
        } catch (Exception e) {
            //TODO display tooltip if error happens to often
            e.printStackTrace();
        }
    }

    /**
     * Gets the detailed information for the item shown after the
     * global generic item details.
     *
     * @param stack
     * @param player
     * @param list
     */
    protected void getDetailedInfo(ItemStack stack, @Nullable Player player, List list) {
        //Per item detailed info
        String translationKey = getUnlocalizedName(stack) + ".info.detailed";
        String translation = LanguageUtility.getLocal(translationKey);
        if (!translation.isEmpty() && !translation.equals(translationKey)) {
            list.addAll(LanguageUtility.splitByLine(translation));
        }
    }

    /**
     * Gets the detailed when shift is held information for the item shown after the
     * global generic item details.
     * <p>
     * This is in addition to normal details
     *
     * @param stack
     * @param player
     * @param list
     */
    protected void getShiftDetailedInfo(ItemStack stack, @Nullable Player player, List list) {
        //Per item detailed info
        String translationKey = getUnlocalizedName(stack) + ".info.shifted";
        String translation = LanguageUtility.getLocal(translationKey);
        if (!translation.isEmpty() && !translation.equals(translationKey)) {
            list.addAll(LanguageUtility.splitByLine(translation));
        }
    }

    /**
     * Does the item have detailed information to be shown when
     * shift is held
     *
     * @param stack
     * @param player
     * @return
     */
    protected boolean hasShiftInfo(ItemStack stack, @Nullable Player player) {
        return false;
    }
}

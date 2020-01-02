package icbm.classic.prefab.item;

import icbm.classic.lib.LanguageUtility;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Keyboard;

import javax.annotation.Nullable;
import java.util.List;

/**
 * Generic prefab to use in all items providing common implementation
 *
 *
 * Created by Dark(DarkGuardsman, Robert) on 12/20/2016.
 */
public class ItemBlockAbstract extends ItemBlock
{
    //Make sure to mirror all changes to other abstract class
    public ItemBlockAbstract(Block p_i45328_1_)
    {
        super(p_i45328_1_);
    }

    @Override
    public EnumActionResult onItemUse(EntityPlayer player, World worldIn, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ)
    {
        IBlockState iblockstate = worldIn.getBlockState(pos);
        Block block = iblockstate.getBlock();

        if (!block.isReplaceable(worldIn, pos))
        {
            pos = pos.offset(facing);
        }

        ItemStack itemstack = player.getHeldItem(hand);

        if (!itemstack.isEmpty() && canPlace(player, worldIn, pos, itemstack, facing, hitX, hitY, hitZ))
        {
            int i = this.getMetadata(itemstack.getMetadata());
            IBlockState iblockstate1 = this.block.getStateForPlacement(worldIn, pos, facing, hitX, hitY, hitZ, i, player, hand);

            if (placeBlockAt(itemstack, player, worldIn, pos, facing, hitX, hitY, hitZ, iblockstate1))
            {
                iblockstate1 = worldIn.getBlockState(pos);
                SoundType soundtype = iblockstate1.getBlock().getSoundType(iblockstate1, worldIn, pos, player);
                worldIn.playSound(player, pos, soundtype.getPlaceSound(), SoundCategory.BLOCKS, (soundtype.getVolume() + 1.0F) / 2.0F, soundtype.getPitch() * 0.8F);
                itemstack.shrink(1);
            }

            return EnumActionResult.SUCCESS;
        }
        else
        {
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
    protected boolean canPlace(EntityPlayer player, World worldIn, BlockPos pos, ItemStack itemstack, EnumFacing facing, float hitX, float hitY, float hitZ)
    {
        return player.canPlayerEdit(pos, facing, itemstack) && worldIn.mayPlace(this.block, pos, false, facing, (Entity) null);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World world, List list, ITooltipFlag flag)
    {
        //Get player, don't run tool tips without
        EntityPlayer player = Minecraft.getMinecraft().player;
        try
        {
            //Generic info
            String translationKey = getTranslationKey() + ".info";
            String translation = LanguageUtility.getLocal(translationKey);
            if (!translation.isEmpty() && !translation.equals(translationKey))
            {
                list.add(translation);
            }

            if (hasDetailedInfo(stack, player))
            {
                getDetailedInfo(stack, player, list);
            }

            if (hasShiftInfo(stack, player))
            {
                if (!Keyboard.isKeyDown(Keyboard.KEY_LSHIFT))
                {
                    list.add(LanguageUtility.getLocal("info.voltzengine:tooltip.noShift").replace("#0", "\u00a7b").replace("#1", "\u00a77"));
                }
                else
                {
                    getShiftDetailedInfo(stack, player, list);
                }
            }
        }
        catch (Exception e)
        {
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
    protected void getDetailedInfo(ItemStack stack, @Nullable EntityPlayer player, List list)
    {
        //Per item detailed info
        String translationKey = getTranslationKey(stack) + ".info";
        String translation = LanguageUtility.getLocal(translationKey);
        if (!translation.isEmpty() && !translation.equals(translationKey))
        {
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
    protected void getShiftDetailedInfo(ItemStack stack, @Nullable EntityPlayer player, List list)
    {
        //Per item detailed info
        String translationKey = getTranslationKey(stack) + ".info.detailed";
        String translation = LanguageUtility.getLocal(translationKey);
        if (!translation.isEmpty() && !translation.equals(translationKey))
        {
            list.addAll(LanguageUtility.splitByLine(translation));
        }
    }

    /**
     * Does the item have detailed information to be shown
     *
     * @param stack
     * @param player
     * @return
     */
    protected boolean hasDetailedInfo(ItemStack stack, @Nullable EntityPlayer player)
    {
        String translationKey = getTranslationKey() + ".info";
        String translationKey2 = getTranslationKey(stack) + ".info";
        return !translationKey.equals(translationKey2);
    }

    /**
     * Does the item have detailed information to be shown when
     * shift is held
     *
     * @param stack
     * @param player
     * @return
     */
    protected boolean hasShiftInfo(ItemStack stack, @Nullable EntityPlayer player)
    {
        return false;
    }
}

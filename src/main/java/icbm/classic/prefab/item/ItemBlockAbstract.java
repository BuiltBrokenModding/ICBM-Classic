package icbm.classic.prefab.item;

import icbm.classic.ICBMClassic;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;
import java.util.List;

/**
 * Generic prefab to use in all items providing common implementation
 * <p>
 * PORT: the 1.12 onItemUse copy of ItemBlock placement is gone, vanilla {@link BlockItem#place} does the same
 * (replaceable check, offset, sound, shrink). Placement checks are overridden with {@link #canPlace(BlockPlaceContext, BlockState)}.
 * <p>
 * Created by Dark(DarkGuardsman, Robin) on 12/20/2016.
 */
public class ItemBlockAbstract extends BlockItem
{
    //Make sure to mirror all changes to other abstract class
    public ItemBlockAbstract(Block block)
    {
        this(block, new Item.Properties());
    }

    public ItemBlockAbstract(Block block, Item.Properties properties)
    {
        super(block, properties);
    }

    /**
     * Called to check if the player can place the block
     * <p>
     * Allows for easy override of placement checks
     */
    @Override
    protected boolean canPlace(BlockPlaceContext context, BlockState state)
    {
        return super.canPlace(context, state);
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> list, TooltipFlag flag)
    {
        //Get player, don't run tool tips without
        final Player player = ItemBase.getClientPlayer();
        try
        {
            //Generic info
            final String translationKey = getDescriptionId(stack) + ".info";
            final String translation = ItemBase.translate(translationKey);
            if (!translation.isEmpty() && !translation.equals(translationKey))
            {
                list.add(Component.literal(translation));
            }

            getDetailedInfo(stack, player, list);

            if (hasShiftInfo(stack, player))
            {
                if (!ICBMClassic.proxy.isShiftHeld())
                {
                    list.add(Component.literal(ItemBase.translate("info.voltzengine:tooltip.noShift").replace("#0", "§b").replace("#1", "§7")));
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
            ICBMClassic.logger().error("Failed to build tooltip for " + stack, e);
        }
    }

    /**
     * Gets the detailed information for the item shown after the
     * global generic item details.
     *
     * @param stack
     * @param player - client player, may be null
     * @param list
     */
    public void getDetailedInfo(ItemStack stack, @Nullable Player player, List<Component> list)
    {
        //Per item detailed info
        final String translationKey = getDescriptionId(stack) + ".info.detailed";
        final String translation = ItemBase.translate(translationKey);
        if (!translation.isEmpty() && !translation.equals(translationKey))
        {
            ItemBase.splitLines(translation, list);
        }
    }

    /**
     * Gets the detailed when shift is held information for the item shown after the
     * global generic item details.
     * <p>
     * This is in addition to normal details
     *
     * @param stack
     * @param player - client player, may be null
     * @param list
     */
    protected void getShiftDetailedInfo(ItemStack stack, @Nullable Player player, List<Component> list)
    {
        //Per item detailed info
        final String translationKey = getDescriptionId(stack) + ".info.shifted";
        final String translation = ItemBase.translate(translationKey);
        if (!translation.isEmpty() && !translation.equals(translationKey))
        {
            ItemBase.splitLines(translation, list);
        }
    }

    /**
     * Does the item have detailed information to be shown when
     * shift is held
     *
     * @param stack
     * @param player - client player, may be null
     * @return
     */
    protected boolean hasShiftInfo(ItemStack stack, @Nullable Player player)
    {
        return false;
    }
}
// PORTED

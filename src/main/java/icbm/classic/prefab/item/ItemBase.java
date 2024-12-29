package icbm.classic.prefab.item;

import icbm.classic.lib.LanguageUtility;
import net.minecraft.client.Minecraft;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nullable;
import java.util.List;

/**
 * Generic prefab to use in all items providing common implementation
 * <p>
 * <p>
 * Created by Dark(DarkGuardsman, Robin) on 12/20/2016.
 */
public class ItemBase extends Item
{
    public ItemBase(Properties p_i48487_1_) {
        super(p_i48487_1_);
    }

    //Make sure to mirror all changes to other abstract class
    @Override
    @OnlyIn(Dist.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<ITextComponent> list, ITooltipFlag flagIn)
    {
        PlayerEntity player = Minecraft.getInstance().player;

        //Generic info, shared by item group
        /*splitAdd(this.getTranslationKey(stack) + ".info", list, false, true);

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
        }*/
    }

    /**
     * Gets the detailed information for the item shown after the
     * global generic item details.
     *
     * @param stack
     * @param player
     * @param list
     */
    protected void getDetailedInfo(ItemStack stack, PlayerEntity player, List<String> list)
    {
        //Per item detailed info
        splitAdd(getTranslationKey(stack) + ".info", list, true, true);
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
    protected void getShiftDetailedInfo(ItemStack stack, PlayerEntity player, List<String> list)
    {
        //Per item detailed info
        splitAdd(getTranslationKey(stack) + ".info.detailed", list, true, true);
    }

    protected void splitAdd(String translationKey, List<String> list, boolean addKeyIfEmpty, boolean translate)
    {
        String translation = translate ? LanguageUtility.getLocal(translationKey) : translationKey;
        if (!translate || !translation.isEmpty() && !translation.equals(translationKey))
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
    protected boolean hasDetailedInfo(ItemStack stack, PlayerEntity player)
    {
        return false;
    }

    /**
     * Does the item have detailed information to be shown when
     * shift is held
     *
     * @param stack
     * @param player
     * @return
     */
    protected boolean hasShiftInfo(ItemStack stack, PlayerEntity player)
    {
        return false;
    }
}

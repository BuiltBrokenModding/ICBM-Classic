package icbm.classic.prefab.item;

import icbm.classic.IcbmConstants;
import icbm.classic.lib.LanguageUtility;
import net.minecraft.client.Minecraft;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.lwjgl.input.Keyboard;

import java.util.List;

/**
 * Generic prefab to use in all items providing common implementation
 * <p>
 * <p>
 * Created by Dark(DarkGuardsman, Robert) on 12/20/2016.
 */
public class ItemBase extends Item {

    public ItemBase(Properties properties) {
        super(properties);
    }

    public ItemBase setName(String name) {
        this.setUnlocalizedName(IcbmConstants.PREFIX + name);
        this.setRegistryName(IcbmConstants.PREFIX + name);
        return this;
    }

    //Make sure to mirror all changes to other abstract class
    @Override
    @OnlyIn(Dist.CLIENT)
    public void addInformation(ItemStack stack, Level level, List<String> list, ITooltipFlag flag) {
        Player player = Minecraft.getMinecraft().player;

        //Generic info, shared by item group
        splitAdd(getUnlocalizedName(stack) + ".info", list, false, true);

        if (hasDetailedInfo(stack, player)) {
            getDetailedInfo(stack, player, list);
        }

        if (hasShiftInfo(stack, player)) {
            if (!Keyboard.isKeyDown(Keyboard.KEY_LSHIFT)) {
                list.add(LanguageUtility.getLocal("info.voltzengine:tooltip.noShift").replace("#0", "\u00a7b").replace("#1", "\u00a77"));
            } else {
                getShiftDetailedInfo(stack, player, list);
            }
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
    protected Component getDetailedInfo(ItemStack stack, Player player, List<String> list) {
        return Component.translatable(getDescriptionId(stack) + ".info");
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
    protected Component getShiftDetailedInfo(ItemStack stack, Player player, List<String> list) {
        return Component.translatable(getDescriptionId(stack) + ".info.detailed");
    }

    protected void splitAdd(String translationKey, List<String> list, boolean addKeyIfEmpty, boolean translate) {
        String translation = translate ? LanguageUtility.getLocal(translationKey) : translationKey;
        if (!translate || !translation.isEmpty() && !translation.equals(translationKey)) {
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
    protected boolean hasDetailedInfo(ItemStack stack, Player player) {
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
    protected boolean hasShiftInfo(ItemStack stack, Player player) {
        return false;
    }
}

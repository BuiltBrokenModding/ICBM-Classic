package icbm.classic.prefab.item;

import icbm.classic.ICBMClassic;
import icbm.classic.ICBMConstants;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.neoforged.fml.loading.FMLEnvironment;

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
    /** 1.12 unlocalized name, e.g. item.icbmclassic:radarGun. Null to use the vanilla registry based key. */
    private String unlocalizedName;

    public ItemBase()
    {
        this(new Item.Properties());
    }

    public ItemBase(Item.Properties properties)
    {
        super(properties);
    }

    /**
     * Sets the translation key to the 1.12 format. Registry name is handled by the DeferredRegister.
     *
     * @param name - 1.12 name, e.g. radarGun
     * @return this
     */
    public ItemBase setName(String name)
    {
        this.unlocalizedName = "item." + ICBMConstants.PREFIX + name;
        return this;
    }

    @Override
    public String getDescriptionId()
    {
        if (unlocalizedName != null)
        {
            return unlocalizedName;
        }
        return super.getDescriptionId();
    }

    //Make sure to mirror all changes to other abstract class
    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> list, TooltipFlag flag)
    {
        final Player player = getClientPlayer();

        //Generic info, shared by item group
        splitAdd(getDescriptionId(stack) + ".info", list, false, true);

        if (hasDetailedInfo(stack, player))
        {
            getDetailedInfo(stack, player, list);
        }

        if (hasShiftInfo(stack, player))
        {
            if (!ICBMClassic.proxy.isShiftHeld())
            {
                list.add(Component.literal(translate("info.voltzengine:tooltip.noShift").replace("#0", "§b").replace("#1", "§7")));
            }
            else
            {
                getShiftDetailedInfo(stack, player, list);
            }
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
    protected void getDetailedInfo(ItemStack stack, @Nullable Player player, List<Component> list)
    {
        //Per item detailed info
        splitAdd(getDescriptionId(stack) + ".info", list, true, true);
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
        splitAdd(getDescriptionId(stack) + ".info.detailed", list, true, true);
    }

    protected void splitAdd(String translationKey, List<Component> list, boolean addKeyIfEmpty, boolean translate)
    {
        final String translation = translate ? translate(translationKey) : translationKey;
        if (!translate || !translation.isEmpty() && !translation.equals(translationKey))
        {
            splitLines(translation, list);
        }
    }

    /**
     * Does the item have detailed information to be shown
     *
     * @param stack
     * @param player - client player, may be null
     * @return
     */
    protected boolean hasDetailedInfo(ItemStack stack, @Nullable Player player)
    {
        return false;
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

    // ---- Shared helpers, also used by ItemBlockAbstract ----

    /**
     * Translates a key using the loaded language. Safe on both sides, the server only knows en_us.
     *
     * @return translation, or the key if missing
     */
    public static String translate(String key)
    {
        return Language.getInstance().getOrDefault(key);
    }

    /** @return true if the key has a translation */
    public static boolean hasTranslation(String key)
    {
        return Language.getInstance().has(key);
    }

    /** Splits a translated string into tooltip lines the same way 1.12 LanguageUtility#splitByLine did */
    public static void splitLines(String translation, List<Component> list)
    {
        icbm.classic.lib.LanguageUtility.splitByLine(translation, icbm.classic.lib.LanguageUtility.toolTipLineLength, line -> list.add(Component.literal(line)));
    }

    /**
     * Gets the client player for tooltips. 1.12 used Minecraft#player directly.
     *
     * @return player, or null on a dedicated server or before the player exists
     */
    @Nullable
    public static Player getClientPlayer()
    {
        if (FMLEnvironment.dist.isClient())
        {
            return icbm.classic.client.ClientHelpers.getPlayer();
        }
        return null;
    }
}
// PORTED

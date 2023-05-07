package icbm.classic.lib;

import icbm.classic.ICBMClassic;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.translation.I18n;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Utility class to handle working with Strings
 *
 * @author Darkguardsman
 *         based on Calclavia version from Resonant Engine but has been mostly rewritten so no longer marked as author
 */
public class LanguageUtility
{
    public static int toolTipLineLength = 30;

    /**
     * Grabs the localization for the string provided. Make sure the string
     * matches the exact key in a translation file.
     *
     * @param key - translation key, Example 'tile.sometile.name' or 'tile.modname:sometile.name'
     * @return translated key, or the same string provided if the key didn't match anything
     */
    public static String getLocal(String key)
    {
        //Check for empty or null keys
        if (key == null || key.isEmpty())
        {
            if (ICBMClassic.runningAsDev)
            {
                ICBMClassic.logger().error("LanguageUtility.getLocal(" + key + ") - invalid key", new RuntimeException());
            }
            return "error.key.empty";
        }

        //Get translation
        String translation = I18n.translateToLocal(key);
        if (translation == null || translation.isEmpty())
        {
            if (ICBMClassic.runningAsDev)
            {
                ICBMClassic.logger().error("LanguageUtility.getLocal(" + key + ") - no translation", new RuntimeException());
            }
            return key;
        }
        return translation;
    }

    /**
     * Same as getLocal(String) but appends '.name' if it is missing
     *
     * @param key - translation key, Example 'tile.sometile.name' or 'tile.modname:sometile.name'
     * @return translated key, or the same string provided if the key didn't match anything
     */
    public static String getLocalName(String key)
    {
        //Check for empty or null keys
        if (key == null || key.isEmpty())
        {
            if (ICBMClassic.runningAsDev)
            {
                ICBMClassic.logger().error("LanguageUtility.getLocalName(" + key + ")", new RuntimeException());
            }
            return "error.key.empty";
        }
        if (!key.endsWith(".name"))
        {
            key = key + ".name";
        }

        //Get translation
        String translation = I18n.translateToLocal(key);
        if (translation == null || translation.isEmpty())
        {
            if (ICBMClassic.runningAsDev)
            {
                ICBMClassic.logger().error("LanguageUtility.getLocal(" + key + ") - no translation", new RuntimeException());
            }
            return key;
        }
        return translation;
    }


    /**
     * Uses the language file as a place to store settings
     * for GUI components that use translations. In which
     * the same component may need to change sizes with
     * changes in words.
     *
     * @param key    - string to use to look up the result
     * @param backup - returned if key fails to be found or parsed
     * @return integer parsed from a lang file
     */
    public static Integer getLangSetting(String key, int backup)
    {
        String result = getLocal(key);
        if (result != null && !result.isEmpty())
        {
            try
            {
                return Integer.parseInt(key);
            }
            catch (NumberFormatException e)
            {
                if (ICBMClassic.runningAsDev)
                {
                    ICBMClassic.logger().error("LanguageUtility.getLangSetting(" + key + ")", e);
                }
            }
        }
        return backup;
    }

    /**
     * Helper version of getLocalName that places the translated string inside
     * minecraft's chat component system.
     *
     * @param key
     * @return
     */
    public static ITextComponent getLocalChat(String key)
    {
        return new TextComponentTranslation(key);
    }

    /**
     * Helper method to translate, wrap, then send the msg to the player.
     * Designed to save line length when creating a lot of feed back for the player
     *
     * @param player - player who will receive the message
     * @param key    - - translation key, Example 'tile.sometile.name' or 'tile.modname:sometile.name'
     */
    public static void addChatToPlayer(EntityPlayer player, String key)
    {
        if (player != null)
        {
            player.sendMessage(getLocalChat(key));
        }
        else if (ICBMClassic.runningAsDev)
        {
            ICBMClassic.logger().error("LanguageUtility.addChatToPlayer(Null Player, " + key + ")", new RuntimeException());
        }
    }

    public static List<String> splitStringPerWord(String string)
    {
        return Arrays.asList(toWordArray(string));
    }

    public static String[] toWordArray(String string)
    {
        return string.trim().split("\\W+");
    }

    public static List<String> splitByLine(String string)
    {
        return splitByLine(string, toolTipLineLength);
    }

    public static List<String> splitByLine(String string, int charsPerLine)
    {
        String[] words = string.split(" ");
        List<String> lines = new ArrayList(); //TODO predict size for faster runtime
        String line = "";
        int indent = 0;
        for (String word : words)
        {
            // Indent logic
            if(word.trim().startsWith("\\t") || word.trim().startsWith("\t")) {
                indent += 2;
            }
            else if(word.trim().startsWith("-\\t") || word.trim().startsWith("-\t")) {
                indent -= 2;
            }
            // Line break logic
            else if(word.contains("\\n") || word.contains("\n")) {
                if(word.equals("\\n") || word.equals("\n")) {
                    lines.add(addSpacesLeft(line, indent));
                    line = "";
                }
                // invalid format but insert anyways TODO process by splitting first
                else {
                    line += word.replace("\\n", "").replace("\n", "").trim();
                }
            }
            // Continue building line
            else if (word.length() + line.length() <= charsPerLine)
            {
                line += word + " ";
            }
            else
            {
                // Add existing line
                lines.add(addSpacesLeft(line, indent));

                // Start next line
                line = word + " ";
            }
        }

        // Add remaining
        lines.add(addSpacesLeft(line.trim(), indent));

        return lines;
    }

    private static String addSpacesLeft(String line, int pad) {
        if(pad <= 0) {
            return line;
        }
        return String.format("%1$" + pad + "s", "") + line;
    }

    public static String capitalizeFirst(String str)
    {
        return str.substring(0, 1).toUpperCase() + str.substring(1, str.length());
    }

    public static String decapitalizeFirst(String str)
    {
        return str.substring(0, 1).toLowerCase() + str.substring(1, str.length());
    }

    public static String posFormatted(Vec3d pos) {
        return String.format("%.2f, %.2f, %.2f", pos.x, pos.y, pos.z);
    }

    public static String posFormatted(BlockPos pos) {
        return String.format("%d, %d, %d", pos.getX(), pos.getX(), pos.getZ());
    }
}

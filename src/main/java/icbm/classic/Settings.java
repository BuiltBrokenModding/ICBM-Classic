package icbm.classic;

import cpw.mods.fml.common.ModMetadata;
import net.minecraftforge.common.config.Configuration;

import java.util.Arrays;

/** Settings class for various configuration settings.
 *
 * @author Calclavia */
public class Settings
{
    /** Should ICBM use external fuel? **/
    //@Config(key = "Use Fuel", category = Configuration.CATEGORY_GENERAL)
    public static boolean USE_FUEL = true;
    //@Config(key = "Allow Chunk Loading", category = Configuration.CATEGORY_GENERAL)
    public static boolean LOAD_CHUNKS = true;
    //@Config(key = "Max Missile Distance", category = Configuration.CATEGORY_GENERAL)
    public static int DAO_DAN_ZUI_YUAN = 10000;
    //@Config(key = "Antimatter Explosion Size", category = Configuration.CATEGORY_GENERAL)
    public static int ANTIMATTER_SIZE = 55;
    //@Config(key = "Antimatter Destroy Bedrock", category = Configuration.CATEGORY_GENERAL)
    public static boolean DESTROY_BEDROCK = true;
    //@Config(key = "Max Tier the Rocket Launcher can Fire", category = Configuration.CATEGORY_GENERAL)
    public static int MAX_ROCKET_LAUCNHER_TIER = 2;

    public static boolean POLLUTIVE_NUCLEAR = true;


    public static void initiate(Configuration configuration)
    {
        USE_FUEL = configuration.get(Configuration.CATEGORY_GENERAL, "Use Fuel", Settings.USE_FUEL).getBoolean(Settings.USE_FUEL);
        LOAD_CHUNKS = configuration.get(Configuration.CATEGORY_GENERAL, "Allow Chunk Loading", LOAD_CHUNKS).getBoolean(LOAD_CHUNKS);
        DAO_DAN_ZUI_YUAN = configuration.get(Configuration.CATEGORY_GENERAL, "Max Missile Distance", Settings.DAO_DAN_ZUI_YUAN).getInt(Settings.DAO_DAN_ZUI_YUAN);
        ANTIMATTER_SIZE = configuration.get(Configuration.CATEGORY_GENERAL, "Antimatter Explosion Size", ANTIMATTER_SIZE).getInt(ANTIMATTER_SIZE);
        DESTROY_BEDROCK = configuration.get(Configuration.CATEGORY_GENERAL, "Antimatter Destroy Bedrock", DESTROY_BEDROCK).getBoolean(DESTROY_BEDROCK);
        MAX_ROCKET_LAUCNHER_TIER = configuration.get(Configuration.CATEGORY_GENERAL, "Limits the max missile tier for rocket launcher item", MAX_ROCKET_LAUCNHER_TIER).getInt(MAX_ROCKET_LAUCNHER_TIER);

        POLLUTIVE_NUCLEAR = configuration.get(Configuration.CATEGORY_GENERAL, "Pollutive Nuclear", POLLUTIVE_NUCLEAR).getBoolean(POLLUTIVE_NUCLEAR);
    }

    public static void setModMetadata(String id, String name, ModMetadata metadata)
    {
        setModMetadata(id, name, metadata, "");
    }

    public static void setModMetadata(String id, String name, ModMetadata metadata, String parent)
    {
        metadata.modId = id;
        metadata.name = name;
        metadata.description = "ICBM is a Minecraft Mod that introduces intercontinental ballistic missiles to Minecraft. But the fun doesn't end there! This mod also features many different explosives, missiles and machines classified in three different tiers. If strategic warfare, carefully coordinated airstrikes, messing with matter and general destruction are up your alley, then this mod is for you!";
        metadata.url = "http://www.builtbroken.com/";
        metadata.logoFile = "/icbm_logo.png";
        metadata.version = ICBMClassic.VERSION;
        metadata.authorList = Arrays.asList(new String[] { "Calclavia", "DarkGuardsman aka Darkcow" });
        metadata.parent = parent;
        metadata.credits = "Please visit the website.";
        metadata.autogenerated = false;
    }

}

package com.builtbroken.mc.core;

import java.io.File;

/**
 * References to common static objects used by Voltz Engine and its sub mods
 */
public final class References
{
    //=============================================
    //============== IDS ==========================
    //=============================================

    public static final String ID = "voltzengine";
    public static final String NAME = "Voltz Engine";
    public static final String DOMAIN = ID;
    public static final String PREFIX = DOMAIN + ":";

    //=============================================
    //============== Version ====================
    //=============================================

    public static final String MAJOR_VERSION = "@MAJOR@";
    public static final String MINOR_VERSION = "@MINOR@";
    public static final String REVISION_VERSION = "@REVIS@";
    public static final String BUILD_VERSION = "@BUILD@";
    public static final String MC_VERSION = "@MC@";
    public static final String VERSION = MC_VERSION + "-" + MAJOR_VERSION + "." + MINOR_VERSION + "." + REVISION_VERSION + "." + BUILD_VERSION;

    //=============================================
    //============== Resources ====================
    //=============================================

    public static final String DIRECTORY = "/assets/" + DOMAIN + "/";
    public static final String CHANNEL = "voltzengine";
    public static final String TEXTURE_DIRECTORY = "textures/";
    public static final String GUI_DIRECTORY = TEXTURE_DIRECTORY + "gui/";

    public static final String BLOCK_TEXTURE_DIRECTORY = TEXTURE_DIRECTORY + "blocks/";
    public static final String ITEM_TEXTURE_DIRECTORY = TEXTURE_DIRECTORY + "items/";
    public static final String MODEL_PATH = "models/";
    public static final String MODEL_DIRECTORY = DIRECTORY + MODEL_PATH;


    //=============================================
    //============== VEP keys ====================
    //=============================================

    public static final String LASER_EFFECT = "VEP_laser";

    //=============================================
    //============== File System ==================
    //=============================================

    /** Main run folder for everything MC */
    public static File ROOT_FOLDER;
    /** Config folder shared by all mods */
    public static File GLOBAL_CONFIG_FOLDER;
    /** Config folder shared by all mods */
    public static File BBM_CONFIG_FOLDER;

    //=============================================
    //============== JSON keys ====================
    //=============================================

    public static final String JSON_BLOCK_KEY = "block";
    public static final String JSON_ITEM_KEY = "item";
    public static final String JSON_ORENAME_KEY = "oreName";
    public static final String JSON_FURNACE_RECIPE_KEY = "furnaceRecipe";
    public static final String JSON_CRAFTING_GRID_KEY = "craftingGridRecipe";
    public static final String JSON_LISTENER_KEY = "listeners";

    //=============================================
    //============== Settings =====================
    //=============================================
    /**
     * Conversion ratio of ingot to fluid volume, based on Tinkers *in theory*
     */
    public static int INGOT_VOLUME = 144;

    public static double TO_RF_RATIO = 500;
    public static double TO_BC_RATIO = 50;
}

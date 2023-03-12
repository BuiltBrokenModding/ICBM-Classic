package icbm.classic.lib;

import icbm.classic.ICBMClassic;
import org.apache.logging.log4j.Level;
import java.lang.reflect.Field;
import java.util.LinkedList;

@Deprecated //We don't want to have a master file for data that is not exposed as an API
public class NBTConstants
{
    /* Verifies that the nbt tag constants are distinct (only exist once).
     * This ensures that save files don't get corrupted. (Imagine writing a byte-array and an integer with the
     * same name and then trying to load that again)
     *
     * FAILING THIS CHECK WILL RESULT IN A CRASH!
     *
     */
    public static void ensureThatAllTagNamesAreDistinct()
    {
        Field[] fields = NBTConstants.class.getDeclaredFields(); // grab all fields
        LinkedList<String> alreadySeen = new LinkedList<>(); // keep track of all already seen fields
        for (Field field : fields) { // iterate the fields
            try {
                String value = (String)field.get(null); // get the field's value
                if (alreadySeen.contains(value)) { // check if an equal value was seen before
                    // crash the game to prevent save corruptions
                    ICBMClassic.logger().log(Level.FATAL, "FAILED AND NBT INIT CHECK! This is a severe problem as it can cause save data to get messed up. Because of this the game is going to crash now. Please report this! Conflicting value: " + value);
                    throw new RuntimeException( "ICBM Classic failed an nbt init check! Fatal conflict: " + value);
                }
                else
                {
                    alreadySeen.add(value); // add value to check against it later
                }
            } catch (IllegalAccessException ex) {
                ICBMClassic.logger().log(Level.ERROR, "Illegal access exception thrown while checking nbt tags! Please report this!" + ex.toString());
            }
        }
    }

    public static final String ACCELERATION = "acceleration";
    public static final String ADDITIONAL_MISSILE_DATA = "additionalMissileData";

    public static final String BLAST = "blast";
    public static final String BLAST_DATA = "blastData";
    public static final String BLAST_EXPLODER_ENT_ID = "blastExpEntId";
    public static final String BLAST_POS_Y = "blastPosY";
    public static final String BLOCK_STATE = "blockState";
    public static final String BLUE = "blue";
    public static final String CALL_COUNT = "callCount";
    public static final String CENTER = "center";
    public static final String CLASS = "class";
    public static final String CURRENT_AIM = "currentAim";
    public static final String CUSTOM_EX_DATA = "custom_ex_data";
    public static final String DATA = "data";
    public static final String DESTROY_ITEM = "destroyItem";
    public static final String DIMENSION = "dimension";
    public static final String DURATION = "duration";
    public static final String ENERGY = "energy";
    public static final String EXPLOSION_SIZE = "explosionSize";
    public static final String EXPLOSIVE = "explosive";
    public static final String EXPLOSIVE_ID = "explosiveID";
    public static final String EXPLOSIVE_STACK = "explosive_stack";
    public static final String EX_ID = "ex_id";
    public static final String FREQUENCY = "frequency";
    public static final String FUSE = "Fuse";
    public static final String GRAVITY = "gravity";
    public static final String GREEN = "green";
    public static final String HEALTH = "health";
    public static final String HOST_POS = "hostPos";
    public static final String HZ = "hz";
    public static final String ID = "id";
    public static final String INVENTORY = "inventory";
    public static final String IS_CONFUSE = "isConfuse";
    public static final String IS_CONTAGIOUS = "isContagious";
    public static final String IS_EXPLOSIVE = "isExplosive";
    public static final String IS_MUTATE = "isMutate";
    public static final String IS_POISONOUS = "isPoisonous";
    public static final String IS_VISIBLE = "isVisible";
    public static final String ITEMS = "Items";
    public static final String LAUNCHER_POS = "launcherPos";
    public static final String LOCK_HEIGHT = "lockHeight";
    public static final String MISSILES = "missiles";
    public static final String MISSILE_TYPE = "missileType";
    public static final String PITCH = "pitch";
    public static final String PLAY_SHORT_SOUND_FX = "playShortSoundFX";
    public static final String POINT_ONE = "pointOne";
    public static final String POINT_TWO = "pointTwo";
    public static final String PRE_LAUNCH_SMOKE_TIMER = "preLaunchSmokeTimer";
    public static final String PUSH_TYPE = "pushType";
    public static final String RED = "red";
    public static final String ROLL = "roll";

    public static final String SHAKE = "shake";
    public static final String SLOT = "Slot";
    public static final String SPAWNS = "spawns";
    public static final String SPAWN_MORE_PARTICLES = "spawnMoreParticles";
    public static final String TARGET = "target";
    public static final String TARGET_HEIGHT = "targetHeight";
    public static final String TARGET_X = "target_x";
    public static final String TARGET_Y = "target_y";
    public static final String TARGET_Z = "target_z";
    public static final String TICKS = "ticks";
    public static final String TICKS_IN_AIR = "ticksInAir";
    public static final String TIER = "tier";
    public static final String TRACKING_ENTITY = "trackingEntity";
    public static final String X = "x";
    public static final String Y = "y";
    public static final String YAW  = "yaw";
    public static final String Z = "z";
}

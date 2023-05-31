package icbm.classic.lib.tracker;

import com.builtbroken.jlib.lang.StringHelpers;
import icbm.classic.lib.LanguageUtility;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTBase;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

@NoArgsConstructor(access = AccessLevel.NONE)
final class EventTrackerData {
    final Set<Class> VALID_OBJECTS = new HashSet<>();
   final Set<Class> INVALID_OBJECTS = new HashSet<>();
    final Set<Class> CURRENTLY_SCANNING = new HashSet<>();

    public boolean DEBUG = false;

    /**
     * Loads default valid/invalid map
     *
     * @return self
     */
    public EventTrackerData initDefaults() {
        // Left as a different method for unit testing

        // Java
        VALID_OBJECTS.add(Integer.class);
        VALID_OBJECTS.add(Double.class);
        VALID_OBJECTS.add(Float.class);
        VALID_OBJECTS.add(Long.class);
        VALID_OBJECTS.add(String.class);
        VALID_OBJECTS.add(Byte.class);
        VALID_OBJECTS.add(Short.class);
        VALID_OBJECTS.add(Date.class);
        VALID_OBJECTS.add(Object.class);

        // Minecraft
        VALID_OBJECTS.add(BlockPos.class);
        VALID_OBJECTS.add(Vec3d.class);
        VALID_OBJECTS.add(ResourceLocation.class);

        INVALID_OBJECTS.add(Entity.class);
        INVALID_OBJECTS.add(World.class);
        INVALID_OBJECTS.add(NBTBase.class);

        return this;
    }

    public boolean isValidType(Class objClass) {
        return isValidType(objClass, 0);
    }

    private boolean isValidType(Class objClass, int depth) {

        debug("checking " + objClass, depth);

        // Always use errors, as they are effectively immutable in usage
        if(Throwable.class.isAssignableFrom(objClass)) {
            debug("isThrowable " + objClass, depth);
            return true;
        }

        if(VALID_OBJECTS.contains(objClass)) {
            debug("isValid " + objClass, depth);
            return true;
        }
        if(INVALID_OBJECTS.contains(objClass)) {
            debug("isInvalid " + objClass, depth);
            return false;
        }

        if(!scanClass(objClass, depth)) {
            debug("adding to invalid " + objClass, depth);
            INVALID_OBJECTS.add(objClass);
            return false;
        }

        debug("adding to valid " + objClass, depth);
        VALID_OBJECTS.add(objClass);
        return true;
    }

    boolean scanClass(Class objClass, int depth) {
        // Track that we are scanning this class to prevent loops
        CURRENTLY_SCANNING.add(objClass);

        debug("scanning " + objClass, depth);

        // Check parent first
        if(objClass.getSuperclass() != null && !isValidType(objClass.getSuperclass(), depth + 1)) {
            return false;
        }

        // Check each field, we should be final and of a type that is immutable
        final Field[] objFields = objClass.getDeclaredFields();
        boolean isValid = true;
        for (Field objField : objFields) {

            debug("field " + objField, depth);

            if(!Modifier.isFinal(objField.getModifiers())) {
                debug("notFinal " + objField, depth);
                isValid = false;
            }
            // Prevent re-scanning the same object causing a loop
            else if (!CURRENTLY_SCANNING.contains(objField.getType())) {
                isValid = isValid && isValidType(objField.getType(), depth + 1);
            }
            else {
                debug("already scanning " + objField, depth);
            }
        }

        debug("done scanning " + objClass + " -> isValid=" + isValid, depth);
        CURRENTLY_SCANNING.remove(objClass);
        return isValid;
    }

    private void debug(String msg, int depth) {
        if(DEBUG) {
            System.out.println("EvenTrackerData: " + StringUtils.repeat("\t", depth) + msg);
        }
    }
}

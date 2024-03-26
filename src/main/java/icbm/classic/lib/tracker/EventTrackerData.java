package icbm.classic.lib.tracker;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.NBTBase;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

@NoArgsConstructor(access = AccessLevel.NONE)
final class EventTrackerData {
    private static final Set<Class> VALID_OBJECTS = new HashSet<>();
    private static final Set<Class> INVALID_OBJECTS = new HashSet<>();
    private static final Set<Class> CURRENTLY_SCANNING = new HashSet<>();

    static {
        // Java
        VALID_OBJECTS.add(Integer.class);
        VALID_OBJECTS.add(Double.class);
        VALID_OBJECTS.add(Float.class);
        VALID_OBJECTS.add(Long.class);
        VALID_OBJECTS.add(String.class);
        VALID_OBJECTS.add(Byte.class);
        VALID_OBJECTS.add(Short.class);
        VALID_OBJECTS.add(Date.class);

        // Minecraft
        VALID_OBJECTS.add(BlockPos.class);
        VALID_OBJECTS.add(Vec3.class);

        INVALID_OBJECTS.add(Entity.class);
        INVALID_OBJECTS.add(World.class);
        INVALID_OBJECTS.add(NBTBase.class);
    }

    public static boolean isValidType(Class objClass) {

        // Always use errors, as they are effectively immutable in usage
        if (Throwable.class.isAssignableFrom(objClass)) {
            return true;
        }

        if (VALID_OBJECTS.contains(objClass)) {
            return true;
        }
        if (INVALID_OBJECTS.contains(objClass)) {
            return false;
        }

        if (!scanClass(objClass)) {
            INVALID_OBJECTS.add(objClass);
            return false;
        }
        VALID_OBJECTS.add(objClass);
        return true;
    }

    private static boolean scanClass(Class objClass) {
        // Track that we are scanning this class to prevent loops
        CURRENTLY_SCANNING.add(objClass);

        // Check parent first
        if (objClass.getSuperclass() != null && !isValidType(objClass.getSuperclass())) {
            return false;
        }

        // Check each field, we should be final and of a type that is immutable
        final Field[] objFields = objClass.getDeclaredFields();
        boolean isValid = true;
        for (Field objField : objFields) {
            if (!Modifier.isFinal(objField.getModifiers())) {
                isValid = false;
            }

            // Prevent re-scanning the same object causing a loop
            if (!CURRENTLY_SCANNING.contains(objField.getType())) {
                isValid = isValid && !isValidType(objField.getType());
            }
        }

        CURRENTLY_SCANNING.remove(objClass);
        return isValid;
    }
}

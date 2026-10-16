package icbm.classic;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

/**
 * Created by Dark(DarkGuardsman, Robert) on 12/26/2019.
 */
public final class ReflectionHelpers
{
    public static void removeFinal(Field field) throws Exception
    {
        field.setAccessible(true);

        Field modifiersField = Field.class.getDeclaredField("modifiers");
        modifiersField.setAccessible(true);
        modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);
    }
}

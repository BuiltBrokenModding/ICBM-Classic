package icbm.classic.config;

import com.ibm.icu.impl.Assert;
import net.minecraftforge.common.config.Config;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.*;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class ConfigFieldTests
{
    public static final Class[] classes = new Class[]
            {
                    ConfigBattery.class, ConfigDebug.class, ConfigEMP.class, ConfigIC2.class,
                    ConfigItems.class, ConfigLauncher.class, ConfigMain.class, ConfigMissile.class,
                    ConfigThread.class
            };


    ArrayList<Field> getFieldsThatLackAnnotation(Class<? extends Annotation> annotationType, Class<?> classContainingField)
    {
        Field[] fields = annotationType.getDeclaredFields();
        ArrayList<Field> nonAnnotatedFields = new ArrayList<>();

        for (Field field: fields)
        {
            if (!field.isAnnotationPresent(annotationType))
            {
                nonAnnotatedFields.add(field);
            }
        }

        return nonAnnotatedFields;
    }

    @ParameterizedTest
    @ArgumentsSource(ConfigClassesArgProvider.class)
    void ensureFieldHasNameAttribute(Field f)
    {
        Assertions.assertTrue(
                f.isAnnotationPresent(Config.Name.class),
                String.format("Config field %s.%s is lacking the @Config.Name annotation!", f.getDeclaringClass().getName(), f.getName())
        );
    }

    @ParameterizedTest
    @ArgumentsSource(ConfigClassesArgProvider.class)
    void ensureFieldHasCommentAttribute(Field f)
    {
        Assertions.assertTrue(
                f.isAnnotationPresent(Config.Comment.class),
                String.format("Config field %s.%s is lacking the @Config.Comment annotation!", f.getDeclaringClass().getName(), f.getName())
        );
    }

    public static class ConfigClassesArgProvider implements ArgumentsProvider
    {
        @Override
        public Stream<? extends Arguments> provideArguments(ExtensionContext extensionContext)
        {
            final List<Arguments> list = new ArrayList<>();

            for (Class classItem : classes)
            {
                Field[] fields = classItem.getDeclaredFields();

                for (Field field : fields)
                {
                    list.add(Arguments.of(field));
                }
            }

            return list.stream();
        }
    }
}
package icbm.classic.config;

import net.minecraftforge.common.config.Config;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

class ConfigFieldTests
{
    private static final Class[] classes = new Class[]
            {
                    ConfigBattery.class, ConfigDebug.class, ConfigEMP.class, ConfigIC2.class,
                    ConfigItems.class, ConfigLauncher.class, ConfigMain.class, ConfigMissile.class,
                    ConfigThread.class
            };

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
                    if(field.getModifiers() == (Modifier.PUBLIC | Modifier.STATIC))
                    {
                        list.add(Arguments.of(field));
                    }
                }
            }

            return list.stream();
        }
    }
}
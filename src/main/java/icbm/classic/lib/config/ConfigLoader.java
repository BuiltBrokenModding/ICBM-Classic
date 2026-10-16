package icbm.classic.lib.config;

import icbm.classic.ICBMClassic;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Builds {@link ModConfigSpec}s from classes annotated with the 1.12 style {@link Config} annotations
 * and keeps the annotated fields in sync with the loaded values.
 */
public final class ConfigLoader
{
    private static final Map<ModConfigSpec, List<Binding>> BINDINGS = new HashMap<>();
    private static final List<Runnable> RELOAD_LISTENERS = new ArrayList<>();

    private ConfigLoader()
    {
    }

    /** Registers a callback fired after any ICBM config file is loaded or reloaded */
    public static void addReloadListener(Runnable runnable)
    {
        RELOAD_LISTENERS.add(runnable);
    }

    /**
     * Builds and registers a config spec for the given annotated class
     *
     * @param container mod container to register with
     * @param configClass class annotated with {@link Config}
     */
    public static void register(ModContainer container, Class<?> configClass)
    {
        final Config config = configClass.getAnnotation(Config.class);
        if (config == null)
        {
            throw new IllegalArgumentException("Class " + configClass + " is missing @Config");
        }

        final ModConfigSpec.Builder builder = new ModConfigSpec.Builder();
        final List<Binding> bindings = new ArrayList<>();
        buildFields(builder, configClass, null, bindings, toType(config.type()));
        final ModConfigSpec spec = builder.build();
        BINDINGS.put(spec, bindings);

        final String fileName = (config.name().isEmpty() ? config.modid() : config.name()).replace('/', '-') + ".toml";
        container.registerConfig(toType(config.type()), spec, fileName);

        // Startup configs are loaded during registration, sync now so registry code sees values
        if (config.type() == Config.Type.STARTUP)
        {
            sync(spec);
        }
    }

    private static ModConfig.Type toType(Config.Type type)
    {
        switch (type)
        {
            case STARTUP:
                return ModConfig.Type.STARTUP;
            case CLIENT:
                return ModConfig.Type.CLIENT;
            case SERVER:
                return ModConfig.Type.SERVER;
            default:
                return ModConfig.Type.COMMON;
        }
    }

    private static boolean isValueType(Class<?> type)
    {
        return type == boolean.class || type == Boolean.class
            || type == int.class || type == Integer.class
            || type == long.class || type == Long.class
            || type == double.class || type == Double.class
            || type == float.class || type == Float.class
            || type == String.class || type == String[].class
            || type.isEnum();
    }

    /**
     * Marks the next value with the restart type matching the 1.12 annotations. NeoForge ties restart types to
     * config types (WORLD only makes sense for COMMON/SERVER, GAME not for SERVER) and rejects a mismatch when the
     * config is registered, so only combinations that fit the config type are used. STARTUP values (ConfigItems) are
     * only read while loading, always need a game restart and get no marker.
     * Must only be called right before a value is defined, a dangling restart type fails on the next push().
     */
    private static void applyRestart(ModConfigSpec.Builder builder, Field field, ModConfig.Type cfgType)
    {
        final boolean mcRestart = field.isAnnotationPresent(Config.RequiresMcRestart.class);
        final boolean worldRestart = field.isAnnotationPresent(Config.RequiresWorldRestart.class);
        if (mcRestart && (cfgType == ModConfig.Type.CLIENT || cfgType == ModConfig.Type.COMMON))
        {
            builder.gameRestart();
        }
        else if ((mcRestart || worldRestart) && (cfgType == ModConfig.Type.COMMON || cfgType == ModConfig.Type.SERVER))
        {
            // A server config can not ask for a game restart, reloading the world is the closest match
            builder.worldRestart();
        }
    }

    private static void buildFields(ModConfigSpec.Builder builder, Class<?> clazz, Object instance, List<Binding> bindings, ModConfig.Type cfgType)
    {
        for (Field field : clazz.getDeclaredFields())
        {
            final int mod = field.getModifiers();
            final boolean isStatic = Modifier.isStatic(mod);

            // Root classes only use static fields, nested config objects only use instance fields
            if ((instance == null) != isStatic || Modifier.isFinal(mod) && isStatic || Modifier.isTransient(mod)
                || field.isSynthetic() || field.isAnnotationPresent(Config.Ignore.class) || !Modifier.isPublic(mod))
            {
                continue;
            }

            final Config.Name nameAnnotation = field.getAnnotation(Config.Name.class);
            final String name = nameAnnotation != null ? nameAnnotation.value() : field.getName();
            final List<String> path = Collections.singletonList(name);

            final Config.Comment comment = field.getAnnotation(Config.Comment.class);
            if (comment != null)
            {
                builder.comment(comment.value());
            }
            final Config.LangKey langKey = field.getAnnotation(Config.LangKey.class);
            if (langKey != null)
            {
                builder.translation(langKey.value());
            }
            // Restart types are only valid on values, never on a nested category
            if (isValueType(field.getType()))
            {
                applyRestart(builder, field, cfgType);
            }

            try
            {
                field.setAccessible(true);
                final Class<?> type = field.getType();
                final Object defaultValue = field.get(instance);

                if (type == boolean.class || type == Boolean.class)
                {
                    bindings.add(new Binding(field, instance, builder.define(path, (boolean) (Boolean) defaultValue)));
                }
                else if (type == int.class || type == Integer.class)
                {
                    final Config.RangeInt range = field.getAnnotation(Config.RangeInt.class);
                    final int min = range != null ? range.min() : Integer.MIN_VALUE;
                    final int max = range != null ? range.max() : Integer.MAX_VALUE;
                    bindings.add(new Binding(field, instance, builder.defineInRange(path, (int) (Integer) defaultValue, min, max, Integer.class)));
                }
                else if (type == long.class || type == Long.class)
                {
                    bindings.add(new Binding(field, instance, builder.defineInRange(path, (long) (Long) defaultValue, Long.MIN_VALUE, Long.MAX_VALUE, Long.class)));
                }
                else if (type == double.class || type == Double.class || type == float.class || type == Float.class)
                {
                    final Config.RangeDouble range = field.getAnnotation(Config.RangeDouble.class);
                    final double min = range != null ? range.min() : -Double.MAX_VALUE;
                    final double max = range != null ? range.max() : Double.MAX_VALUE;
                    final double def = ((Number) defaultValue).doubleValue();
                    bindings.add(new Binding(field, instance, builder.defineInRange(path, def, Math.min(min, def), Math.max(max, def), Double.class)));
                }
                else if (type == String.class)
                {
                    bindings.add(new Binding(field, instance, builder.define(path, (String) defaultValue)));
                }
                else if (type == String[].class)
                {
                    final List<String> def = defaultValue == null ? new ArrayList<>() : new ArrayList<>(Arrays.asList((String[]) defaultValue));
                    bindings.add(new Binding(field, instance, builder.defineListAllowEmpty(path, def, () -> "", o -> o instanceof String)));
                }
                else if (type.isEnum())
                {
                    bindings.add(new Binding(field, instance, builder.defineEnum(path, (Enum) defaultValue)));
                }
                else if (defaultValue != null)
                {
                    // Nested category
                    builder.push(name);
                    buildFields(builder, type, defaultValue, bindings, cfgType);
                    builder.pop();
                }
            }
            catch (IllegalAccessException e)
            {
                throw new RuntimeException("Failed to build config entry for " + field, e);
            }
        }
    }

    /** Called from the mod event bus for both load and reload */
    public static void onConfigEvent(ModConfigEvent event)
    {
        if (event instanceof ModConfigEvent.Unloading)
        {
            return;
        }
        if (event.getConfig().getSpec() instanceof ModConfigSpec spec && BINDINGS.containsKey(spec))
        {
            sync(spec);
            RELOAD_LISTENERS.forEach(Runnable::run);
        }
    }

    private static void sync(ModConfigSpec spec)
    {
        for (Binding binding : BINDINGS.get(spec))
        {
            binding.apply();
        }
    }

    private record Binding(Field field, Object instance, ModConfigSpec.ConfigValue<?> value)
    {
        void apply()
        {
            try
            {
                final Object raw = value.get();
                final Class<?> type = field.getType();
                if (type == float.class || type == Float.class)
                {
                    field.set(instance, ((Number) raw).floatValue());
                }
                else if (type == double.class || type == Double.class)
                {
                    field.set(instance, ((Number) raw).doubleValue());
                }
                else if (type == int.class || type == Integer.class)
                {
                    field.set(instance, ((Number) raw).intValue());
                }
                else if (type == String[].class)
                {
                    field.set(instance, ((List<?>) raw).stream().map(String::valueOf).toArray(String[]::new));
                }
                else
                {
                    field.set(instance, raw);
                }
            }
            catch (IllegalStateException e)
            {
                // Config not loaded yet, keep defaults
            }
            catch (Exception e)
            {
                ICBMClassic.logger().error("Failed to sync config value {}", field, e);
            }
        }
    }
}

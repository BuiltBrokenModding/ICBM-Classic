package icbm.classic.lib.config;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Port of the Forge 1.12 annotation config system. Classes annotated with {@link Config} are turned
 * into NeoForge {@link net.neoforged.neoforge.common.ModConfigSpec} files by {@link ConfigLoader}, which
 * copies loaded values back into the static (and nested instance) fields.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Config
{
    /** Mod id that owns the config */
    String modid();

    /** File name relative to the config folder, '/' is replaced with '-' */
    String name() default "";

    /** Which config type this file is loaded as */
    Type type() default Type.COMMON;

    enum Type
    {
        /** Loaded immediately on registration, use for values needed during registry events */
        STARTUP,
        COMMON,
        CLIENT,
        SERVER
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.FIELD, ElementType.TYPE})
    @interface LangKey
    {
        String value();
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.FIELD, ElementType.TYPE})
    @interface Comment
    {
        String[] value();
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    @interface Name
    {
        String value();
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    @interface RangeInt
    {
        int min() default Integer.MIN_VALUE;

        int max() default Integer.MAX_VALUE;
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    @interface RangeDouble
    {
        double min() default Double.MIN_VALUE;

        double max() default Double.MAX_VALUE;
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.FIELD, ElementType.TYPE})
    @interface RequiresMcRestart
    {
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.FIELD, ElementType.TYPE})
    @interface RequiresWorldRestart
    {
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    @interface Ignore
    {
    }
}

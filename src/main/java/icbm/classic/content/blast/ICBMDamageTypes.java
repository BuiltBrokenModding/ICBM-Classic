package icbm.classic.content.blast;

import icbm.classic.ICBMConstants;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;

/**
 * Data driven damage types replacing the 1.12 {@code new DamageSource("...")} constants.
 * <p>
 * Each key needs a JSON file at {@code data/icbmclassic/damage_type/<path>.json}. The msgId in the
 * JSON is the 1.12 damage type name so the existing death message lang keys
 * ({@code death.attack.<msgId>}) keep working.
 * <pre>
 * key                path               msgId                          tags / 1.12 flags
 * CHEMICAL           chemical           icbm.chemical                  no_knockback
 * CONTAGIOUS         contagious         icbm.contagious                no_knockback
 * REDMATTER          redmatter          icbm.redmatter                 bypasses_armor, bypasses_effects, bypasses_enchantments, bypasses_shield (1.12: bypass armor + absolute)
 * RADIOACTIVE_BLOCK  radioactive_block  icbmclassic:radioactive_block  no_knockback
 * SPIKES             spikes             icbmclassic:spikes             no_knockback
 * FRAGMENTS          fragments          arrow                          is_projectile, always_kills_armor_stands (1.12 used indirect "arrow" damage)
 * PROJECTILE         projectile         icbmclassic:projectile         is_projectile, no_knockback
 * BOMBLET            bomblet            icbmclassic:bomblet            no_knockback
 * MISSILE            missile            missile                        - (SAM hitting its target)
 * </pre>
 */
public final class ICBMDamageTypes
{
    public static final ResourceKey<DamageType> CHEMICAL = key("chemical");
    public static final ResourceKey<DamageType> CONTAGIOUS = key("contagious");
    public static final ResourceKey<DamageType> REDMATTER = key("redmatter");
    public static final ResourceKey<DamageType> RADIOACTIVE_BLOCK = key("radioactive_block");
    public static final ResourceKey<DamageType> SPIKES = key("spikes");
    public static final ResourceKey<DamageType> FRAGMENTS = key("fragments");
    public static final ResourceKey<DamageType> PROJECTILE = key("projectile");
    public static final ResourceKey<DamageType> BOMBLET = key("bomblet");
    public static final ResourceKey<DamageType> MISSILE = key("missile");

    private ICBMDamageTypes()
    {
    }

    private static ResourceKey<DamageType> key(String path)
    {
        return ResourceKey.create(Registries.DAMAGE_TYPE, ResourceLocation.fromNamespaceAndPath(ICBMConstants.DOMAIN, path));
    }

    /**
     * Resolves the damage type holder from the level's registries
     */
    public static Holder<DamageType> holder(Level level, ResourceKey<DamageType> key)
    {
        return level.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(key);
    }

    /**
     * Builds a damage source without an entity, same as 1.12 {@code new DamageSource(name)}
     */
    public static DamageSource source(Level level, ResourceKey<DamageType> key)
    {
        return new DamageSource(holder(level, key));
    }

    /**
     * Builds a damage source with entities
     *
     * @param level  to pull the registry from
     * @param key    damage type
     * @param direct entity that hit (projectile, fragment), may be null
     * @param cause  entity responsible (shooter), may be null
     */
    public static DamageSource source(Level level, ResourceKey<DamageType> key, @Nullable Entity direct, @Nullable Entity cause)
    {
        return new DamageSource(holder(level, key), direct, cause);
    }

    // PORT: a (direct, cause, position) overload was removed, the 4-arg DamageSource constructor is private in 1.21.1.
    // DamageSource#getSourcePosition falls back to the direct entity's position.
}
// PORTED

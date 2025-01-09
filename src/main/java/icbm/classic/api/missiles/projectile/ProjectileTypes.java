package icbm.classic.api.missiles.projectile;

import icbm.classic.api.data.meta.MetaTag;
import lombok.Data;

@Data
public final class ProjectileTypes {

    /** All projectiles */
    public static final MetaTag TYPE_PROJECTILE = MetaTag.getOrCreateRoot("projectile", "icbmclassic");

    /** Applied to types that act as holders for other objects or entities... think landing rockets or parachutes */
    public static final MetaTag TYPE_HOLDER = MetaTag.getOrCreateSubTag(TYPE_PROJECTILE, "holder");

    /** Applied to projectiles that can be thrown or projected by a source */
    public static final MetaTag TYPE_THROWABLE = MetaTag.getOrCreateSubTag(TYPE_PROJECTILE, "throwable");

    /** Missiles that use the capability {@link icbm.classic.api.missiles.IMissile} */
    public static final MetaTag TYPE_MISSILE = MetaTag.getOrCreateSubTag(TYPE_PROJECTILE, "missile");

    /** Projectiles that are explosive */
    public static final MetaTag TYPE_EXPLOSIVE = MetaTag.getOrCreateSubTag(TYPE_PROJECTILE, "explosive");
}

package icbm.classic.api.actions.data;

import icbm.classic.api.data.meta.MetaTag;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.NONE)
public final class EntityActionTypes {
    /** Action type involving a single {@link net.minecraft.world.entity.Entity} instance */
    public static final MetaTag ENTITY = MetaTag.getOrCreateSubTag(ActionTypes.ROOT, "entity");
    /** Action type involving one or more entities being spawned into the world */
    public static final MetaTag ENTITY_CREATION = MetaTag.getOrCreateSubTag(ENTITY, "creation");
    /** Entity spawn action that creates projectile entities from {@link icbm.classic.api.missiles.projectile.IProjectileData} */
    public static final MetaTag PROJECTILE = MetaTag.getOrCreateSubTag(ENTITY_CREATION, "projectile");
    /** Action type involving an {@link net.minecraft.world.entity.Entity} being modified/mutated. This includes healing, doing damage, changing properties, and replacement. Anything that would change entities stored in world/chunk */
    public static final MetaTag ENTITY_EDIT = MetaTag.getOrCreateSubTag(ENTITY, "edit");
    /** Action type involving several {@link net.minecraft.world.entity.Entity} instances in an area */
    public static final MetaTag ENTITY_AREA = MetaTag.getOrCreateSubTag(ENTITY, "area");
}
// PORTED

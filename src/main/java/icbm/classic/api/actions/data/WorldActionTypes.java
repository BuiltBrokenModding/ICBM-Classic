package icbm.classic.api.actions.data;

import icbm.classic.api.data.meta.MetaTag;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.NONE)
public final class WorldActionTypes {

    /** Action type involving a {@link net.minecraft.world.level.Level} in any capacity */
    public static final MetaTag WORLD = MetaTag.getOrCreateSubTag(ActionTypes.ROOT, "world");

    /** Action type involving a {@link net.minecraft.world.level.Level} being modified/mutated.
     * This can be placing blocks, removing blocks, changing biomes, properties on blocks, etc.
     * Anything that updated what is stored in a world/chunk, excluding entities. */
    public static final MetaTag WORLD_EDIT = MetaTag.getOrCreateSubTag(WORLD, "edit");

    /** Action type covering an area of effect */
    public static final MetaTag WORLD_AREA = MetaTag.getOrCreateSubTag(WORLD, "area");
}
// PORTED

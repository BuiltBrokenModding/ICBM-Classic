package icbm.classic.api.actions.data;

import icbm.classic.api.data.meta.MetaTag;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.NONE)
public final class BlockActionTypes {
    /** Action type involving a {@link net.minecraft.world.level.Level} being modified/mutated.
     * This can be placing blocks, removing blocks, changing biomes, properties on blocks, etc.
     * Anything that updated what is stored in a world/chunk, excluding entities.
     */
    public static final MetaTag BLOCK_EDIT = MetaTag.getOrCreateSubTag(WorldActionTypes.WORLD_EDIT, "block");

    /** Block being mutated into another block, this is sorta a remove and add but perceived logically as block transmuted or replaced.
     * Think of water to stream (constructive), or machine into scrap metal (destructive) */
    public static final MetaTag BLOCK_REPLACE = MetaTag.getOrCreateSubTag(BLOCK_EDIT, "replace");

    /** Block being modified but staying consistent as the same {@link net.minecraft.world.level.block.Block} instance.
     *  Maybe {@link net.minecraft.world.level.block.state.BlockState} property or data on the {@link net.minecraft.world.level.block.entity.BlockEntity} */
    public static final MetaTag BLOCK_CHANGE = MetaTag.getOrCreateSubTag(BLOCK_EDIT, "change");

    /** Block being removed from the world, setAir is counted as removed */
    public static final MetaTag BLOCK_REMOVE = MetaTag.getOrCreateSubTag(BLOCK_EDIT, "remove");

    /** Block being added to the world, air is not counted as addition */
    public static final MetaTag BLOCK_ADD = MetaTag.getOrCreateSubTag(BLOCK_EDIT, "add");
}
// PORTED

package icbm.classic.datafix;

import net.minecraft.nbt.CompoundTag;

public class DataFixerHelpers { //TODO consider moving to a nbt helper class?

    /**
     * Removes all tags matching the tag names
     *
     * @param compound to edit
     * @param tags     to remove
     */
    public static void removeTags(CompoundTag compound, String... tags) {
        for (String str : tags) {
            compound.remove(str);
        }
    }

    /**
     * Removes a tag at a nested level without having to `compound.getCompound("name)` several times
     *
     * @param compound to edit
     * @param tags     set to remove, EX: ["firstLayer", "secondLayer", "target"]
     */
    public static void removeNestedTag(final CompoundTag compound, final String... tags) {
        CompoundTag current = compound;

        // Loop through nested levels, ignore last tag as that is our remove target
        for (int i = 0; i < (tags.length - 1); i++) {
            final String tag = tags[i];
            if (current.contains(tag, 10)) {
                current = current.getCompound(tag);
            } else {
                return;
            }
        }

        // Remove tag using last entry
        current.remove(tags[tags.length - 1]);
    }
}

package icbm.classic.datafix;

import net.minecraft.nbt.NBTTagCompound;

public class DataFixerHelpers { //TODO consider moving to a nbt helper class?

    /**
     * Removes all tags matching the tag names
     *
     * @param compound to edit
     * @param tags to remove
     */
    public static void removeTags(NBTTagCompound compound, String... tags) {
        for(String str : tags) {
            compound.removeTag(str);
        }
    }

    /**
     * Removes a tag at a nested level without having to `compound.getCompound("name)` several times
     *
     * @param compound to edit
     * @param tags set to remove, EX: ["firstLayer", "secondLayer", "target"]
     */
    public static void removeNestedTag(final NBTTagCompound compound, final String... tags) {
        NBTTagCompound current = compound;

        // Loop through nested levels, ignore last tag as that is our remove target
        for(int i = 0; i < (tags.length - 1); i++) {
            final String tag = tags[i];
            if(current.hasKey(tag, 10)) {
                current = current.getCompoundTag(tag);
            }
            else {
                return;
            }
        }

        // Remove tag using last entry
        current.removeTag(tags[tags.length - 1]);
    }
}

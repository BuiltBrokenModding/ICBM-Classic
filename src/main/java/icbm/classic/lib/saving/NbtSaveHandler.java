package icbm.classic.lib.saving;

import net.minecraft.nbt.CompoundTag;

import java.util.LinkedList;
import java.util.List;

/**
 * Handles logic for saving/loading without needing to duplicate logic or checks
 * <p>
 * Each content entry should have a single save handler. However, some cases might want a different save
 * handler for game save, packets, spawn data, or other useful cases.
 * <p>
 * To use the handler create a new instance. Then call {@link #addRoot(String)} to create new save groups.
 * Groups are a great way to organize the save for players/admins editing saves. Such as grouping `flags` under
 * a `flag` root or grouping all player settings under `settings`
 * <p>
 * To add things to save group chain nodes and end with a call to {@link NbtSaveRoot#base()} to return to this level.
 * <p>
 * Using {@link #mainRoot()} can allow saving directly to the base save. This is useful for basic content or saving
 * data that doesn't need to be grouped. Works the same as a save group but will direct all nodes to the base save.
 * <p>
 * Example:
 *
 * <pre>
 *     {@code
 *
 *      public static final NbtSaveHandler<Furnace> SAVE_LOGIC = new NbtSaveHandler()
 *      .mainRoot()
 *          .nodeInteger("fuel", (furnace) -> furnace.fuel, (furnace, integer) -> furnace.fuel = integer)
 *     .base()
 *     .addRoot("inventory")
 *          .nodeInventory("items", (furnace) -> furnace.inventory)
 *          .node(new NbtSlotSettings("settings", (furnace) -> furnace.settings)
 *      .base();
 *
 *
 *     public void readEntityFromNBT(CompoundTag nbt)
 *     {
 *         super.readEntityFromNBT(nbt);
 *         SAVE_LOGIC.load(this, nbt); *
 *     }
 *
 *     public void writeEntityToNBT(CompoundTag nbt)
 *     {
 *         SAVE_LOGIC.save(this, nbt);
 *         super.writeEntityToNBT(nbt);
 *     }
 *
 *     }
 * </pre>
 *
 * @param <E> type of object to save
 */
public class NbtSaveHandler<E> {
    private static final String ROOT_KEY = "root";

    private final List<NbtSaveRoot<E>> roots = new LinkedList<NbtSaveRoot<E>>();
    private final NbtSaveRoot<E> mainRoot = new NbtSaveRoot<E>(ROOT_KEY, this, null);

    /**
     * Called to save data
     *
     * @param objectToSave to pull data from
     * @return save object
     */
    public CompoundTag save(E objectToSave) {
        return save(objectToSave, new CompoundTag());
    }

    /**
     * Called to save data
     *
     * @param objectToSave to pull data from
     * @param save         to push data into
     * @return save object
     */
    public CompoundTag save(E objectToSave, CompoundTag save) {
        roots.forEach(root -> {
            final CompoundTag saveData = root.save(objectToSave);
            if (saveData != null && !saveData.isEmpty()) {
                save.put(root.getSaveKey(), saveData);
            }
        });
        mainRoot.save(objectToSave, save);
        return save;
    }

    /**
     * Called to load save data
     *
     * @param objectToLoad to push data back into
     * @param save         from minecraft
     */
    public void load(E objectToLoad, CompoundTag save) {
        if (save != null && !save.isEmpty()) {
            roots.forEach(root -> {
                if (save.contains(root.getSaveKey())) {
                    root.load(objectToLoad, save.getCompound(root.getSaveKey()));
                }
            });
            mainRoot.load(objectToLoad, save);
        }
    }

    /**
     * Adds a new parent root for saving against
     *
     * @param name of the root, can't be "root"
     * @return root created, used .base() to exit back up a layer
     */
    public NbtSaveRoot<E> addRoot(final String name) {
        final NbtSaveRoot<E> root = new NbtSaveRoot<>(name, this, null);
        roots.add(root);
        return root;
    }

    /**
     * Gets the main root for saving directly to the base compound tag
     *
     * @return main root
     */
    public NbtSaveRoot<E> mainRoot() {
        return mainRoot;
    }
}

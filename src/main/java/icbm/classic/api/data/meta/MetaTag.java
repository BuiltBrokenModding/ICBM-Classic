package icbm.classic.api.data.meta;

import icbm.classic.ICBMClassic;
import lombok.Data;
import net.minecraft.util.NonNullList;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

/**
 * General purpose tags for use in specifying metadata about an object. Each tag needs to be globally
 * unique regardless of system used inside. This is to ensure the tag can be saved then loaded again. Otherwise,
 * we would need a registry per system to handle tags. When tags already should have a root per system to
 * act as a parent.
 *
 * Each system will use meta-tags differently. As well some tags may exist as a way to refine searching
 * and provide additional information to players. Meaning not all tags are meant to be functional and
 * will be documented on expected usage.
 *
 * Tags are also expected to work together. With parent acting as a grouping of related tags, or folder. Then
 * each tag sub-tags as a way to refine representation. To avoid making 100s of sub-tags combinations can be created.
 * Such that tagA + tagB being used as a fuzzy check to limit system interaction.
 *
 * Example of tags working together: an action creates an entity projectile that damages the player. The tags
 * might be as followed (entity, creation, destructive, projectile). With the first tag describing the action
 * as related to entities. Second tag saying it creates something, likely the entity. Third will say the
 * action is destructive/harmful in some capacity. Final tag notes the action is related to projectiles.
 *
 * With the above example more detailed tags may be used. Such as entity_creation tag combing the creation
 * and entity tag into one value. With another option being entity_projectile which could combine creation, entity,
 * and projectile all into one. Reducing how many checks other logic blocks need to do to understand the results.
 *
 * <p>
 * Used with {@link icbm.classic.api.actions.IActionData} to describe type of action and
 * {@link icbm.classic.api.missiles.projectile.IProjectileData} to describe type of projectile
 */
@Data
public final class MetaTag {
    private static final Map<String, MetaTag> TAG_MAP = new HashMap<>();

    /** Unique id of the tag */
    @Nonnull
    private final String key;
    /** Mod who owns this meta-tag */
    @Nonnull
    private final String namespace;

    @Nonnull
    private final String path;
    /**
     * Parent of this tag
     */
    private final MetaTag parent;

    private NonNullList<MetaTag> children;

    private MetaTag(@Nonnull String namespace, @Nonnull String path, @Nullable MetaTag parent) {
        this.key = namespace + ":" + path;
        this.path = path;
        this.namespace = namespace;
        this.parent = parent;
    }

    @Deprecated
    public String getDomain() {
        return getNamespace();
    }

    public static MetaTag find(String key) {
        return TAG_MAP.get(key);
    }

    /**
     * Creates a new root, for externals mods you rarely need this as roots are provided
     * for major systems to leverage. If root already exists it will be returned instead.
     *
     * @param namespace of the owning mod
     * @param path of the resource
     * @return meta tag
     */
    public static MetaTag getOrCreateRoot(@Nonnull String namespace, @Nonnull String path) {
        return getOrCreate(null, namespace, path);
    }

    /**
     * Creates a new tag under an existing parent of the same namespace. If the tag
     * already exists it will be returned instead
     *
     * Use {@link #getOrCreate(MetaTag, String, String)} if parent is not the
     * same namespace. Otherwise other mods and users may become confused on the
     * ownership of the tags.
     *
     * @param parent to use for creating the tag
     * @param subtype to create
     * @return tag created
     */
    public static MetaTag getOrCreateSubTag(@Nonnull MetaTag parent, @Nonnull String subtype) {
        return getOrCreate(parent, parent.namespace, parent.path + "." + subtype);
    }

    /**
     * Creates a new tag or pulls an existing tag if already created
     *
     * @param parent to attach to the newly created tag
     * @param namespace to register tag with
     * @param path of the resource representing the tag
     * @return tag created
     */
    public static MetaTag getOrCreate(@Nullable MetaTag parent, @Nonnull String namespace, @Nonnull String path) {
        final MetaTag exist = find(namespace + ":" + path);
        if(exist != null) {
            return exist;
        }

        final MetaTag metaTag = new MetaTag(namespace, path, parent);
        TAG_MAP.put(path, metaTag);

        ICBMClassic.logger().debug("MetaTag[{}] parent={}", metaTag.getKey(), parent != null ? parent.getKey() : "nil");

        if (parent != null) {
            parent.add(metaTag);
        }
        return metaTag;
    }

    private void add(MetaTag tag) {
        if (this.children == null) {
            this.children = NonNullList.create();
        }
        this.children.add(tag);
    }

    /**
     * Checks if the tag is a direct child of this tag. Doesn't check children's children
     *
     * @param tag to check
     * @return true if is a direct child
     */
    public boolean isChild(MetaTag tag) {
        return children != null && children.contains(tag);
    }

    /**
     * Checks if the type is a subtype of this tag. Will check children using the same method. Expect O(N) performance
     * where N is the SUM of all subtypes of this tag.
     *
     * @param type to check
     * @return true if is subtype, excluding self
     */
    public boolean isSubType(MetaTag type) {
        // TODO if this is being called often and performance isn't great, cache result
        return isChild(type) || children != null && children.stream().anyMatch((c) -> c.isSubType(type));
    }

    /**
     * <pre>
     * Checks if this tag or it's parent can be applied to the type
     *
     * Works similar to field assignment.
     *
     * Example:
     *  TagA exists with no parent
     *  TagB exists with TagA parent
     *
     *  TagA is assignable to TagA
     *  TagB is assignable to TagA
     *  TagB is not assignable to TagA
     * </pre>
     *
     * @param type to validate
     * @return true if this tag or parent would validate as matching
     */
    public boolean isAssignable(MetaTag type) {
        return parent != null && parent.isAssignable(type) || type == this;
    }

    public MetaTag getTopParent() {
        return this.getTopParent(null);
    }

    public MetaTag getTopParent(MetaTag stop) {
        if (this.parent == null || stop != null && this.parent == stop) {
            return this;
        }
        return this.parent.getTopParent(stop);
    }
}

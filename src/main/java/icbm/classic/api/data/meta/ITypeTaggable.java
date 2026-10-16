package icbm.classic.api.data.meta;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.Collections;

/**
 * Applied to data objects that can be filtered by meta tags
 */
public interface ITypeTaggable {

    /**
     * Type(s) to apply to this data object
     *
     * @return type(s) as immutable collection
     */
    @Nonnull
    default Collection<MetaTag> getTypeTags() {
        return Collections.EMPTY_LIST;
    }

    /**
     * Checks if the data object applies for the given type
     *
     * @param type to check
     * @return true if is valid
     */
    default boolean isType(MetaTag type) {
        for(MetaTag projectileType : getTypeTags()) {
            if(projectileType.isAssignable(type)) {
                return true;
            }
        }
        return false;
    }
}
// PORTED

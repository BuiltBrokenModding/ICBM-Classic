package icbm.classic.api.actions.data;

import icbm.classic.api.data.meta.MetaTag;
import lombok.Data;

@Data
public final class ActionTypes {
    /** Root for all actions */
    public static final MetaTag ROOT = MetaTag.getOrCreateRoot("icbmclassic", "action");

    /** Action type that are considered destructive, harmful, or negative in outcome */
    public static final MetaTag DESTRUCTIVE = MetaTag.getOrCreateSubTag(ROOT, "destructive");

    /** Action type that are considered constructive, additive, or positive in outcome */
    public static final MetaTag CONSTRUCTIVE = MetaTag.getOrCreateSubTag(ROOT, "constructive");

    /** Decorative tag, Actions flavored to with effects to look like explosive blasts. */
    public static final MetaTag BLAST = MetaTag.getOrCreateSubTag(ROOT, "blast");
}

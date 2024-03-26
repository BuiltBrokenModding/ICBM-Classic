package icbm.classic.api.explosion;

import javax.annotation.Nullable;

/**
 * Simple interface for use in creating blasts
 * <p>
 * <p>
 * Created by Dark(DarkGuardsman, Robert) on 1/3/19.
 */
@FunctionalInterface
public interface IBlastFactory {
    /**
     * Creates a new blast
     *
     * @return new blast
     */
    @Nullable
    IBlastInit create();
}

package icbm.classic.api.explosion;

/**
 * Simple interface for use in creating blasts
 *
 *
 * Created by Dark(DarkGuardsman, Robert) on 1/3/19.
 */
@FunctionalInterface
public interface IBlastFactory
{
    IBlastInit createNewBlast();
}

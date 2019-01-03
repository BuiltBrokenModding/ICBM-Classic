package icbm.classic.api.explosion;

/**
 * Simple interface for use in creating blasts
 *
 * @see <a href="https://github.com/BuiltBrokenModding/VoltzEngine/blob/development/license.md">License</a> for what you can and can't do with the code.
 * Created by Dark(DarkGuardsman, Robert) on 1/3/19.
 */
@FunctionalInterface
public interface IBlastFactory
{
    IBlastInit createNewBlast();
}

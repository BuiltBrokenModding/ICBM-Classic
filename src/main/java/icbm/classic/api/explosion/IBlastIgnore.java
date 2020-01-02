package icbm.classic.api.explosion;

/**
 * Applied to entities that ignore the affects of a specific explosion.
 *
 * @author Calclavia
 */
public interface IBlastIgnore
{
    /**
     * Can 'this' entity ignore the explosive
     *
     * @param explosion
     * @return
     */
    boolean canIgnore(IBlast explosion); //TODO rethink this design - 3/12/2018 Dark
}

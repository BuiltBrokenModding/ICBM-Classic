package icbm.classic.api.reg;

import icbm.classic.api.explosion.IBlast;
import icbm.classic.api.explosion.IBlastInit;
import icbm.classic.api.missiles.parts.IBuildableObject;

/**
 * Way to customize how a blast is built before it is spawned into the world
 */
public interface IExplosiveCustomization extends IBuildableObject {

    /**
     * Called to apply the settings
     *
     * Use instanceof checks to match on setters in the blast object.
     *
     * @param explosiveData used to create the blast instance
     * @param blast instance created
     */
    void apply(IExplosiveData explosiveData, IBlast blast);
}

package icbm.classic.content.blast.caps;

import icbm.classic.api.actions.cause.IActionSource;
import icbm.classic.api.actions.status.IActionStatus;
import icbm.classic.api.explosion.IBlast;
import icbm.classic.api.reg.IExplosiveData;
import icbm.classic.content.blast.BlastStatus;
import net.minecraft.world.level.Level;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Created by Robin Seifert on 5/22/2021.
 * @deprecated
 */
public class CapabilityBlast implements IBlast
{
    @Override
    public double x()
    {
        return 0;
    }

    @Override
    public double y()
    {
        return 0;
    }

    @Override
    public double z()
    {
        return 0;
    }

    @Override
    public Level world()
    {
        return null;
    }

    @Nonnull
    @Override
    public IActionStatus doAction()
    {
        return BlastStatus.SETUP_ERROR;
    }

    @Nonnull
    @Override
    public IActionSource getSource() {
        return null;
    }

    @Nonnull
    @Override
    public IExplosiveData getActionData() {
        return null;
    }

    @Override
    public void clearBlast()
    {

    }

    // PORT: 1.12 registered this as the capability default through CapabilityManager. NeoForge capabilities
    // have no default instance or storage, ICBMClassicAPI declares the capability and providers return instances.
}
// PORTED

package icbm.classic.content.blast.caps;

import icbm.classic.api.explosion.IBlast;
import icbm.classic.api.explosion.redmatter.IBlastVelocity;
import net.minecraft.world.entity.Entity;

import javax.annotation.Nullable;

/**
 * Created by Robin Seifert on 5/22/2021.
 * @deprecated
 */
public class CapabilityBlastVelocity implements IBlastVelocity
{
    @Override
    public boolean onBlastApplyMotion(@Nullable Entity source, IBlast blast, double xDifference, double yDifference, double zDifference, double distance)
    {
        return false;
    }

    // PORT: 1.12 registered this as the capability default through CapabilityManager. NeoForge capabilities
    // have no default instance or storage, ICBMClassicAPI declares the capability and providers return instances.
}
// PORTED

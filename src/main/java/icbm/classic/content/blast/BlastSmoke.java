package icbm.classic.content.blast;

import icbm.classic.api.explosion.BlastState;
import icbm.classic.api.explosion.responses.BlastForgeResponses;
import icbm.classic.api.explosion.responses.BlastResponse;
import icbm.classic.content.blast.imp.BlastBase;
import icbm.classic.content.entity.EntitySmoke;

import javax.annotation.Nonnull;

public class BlastSmoke extends BlastBase
{
    @Nonnull
    @Override
    public BlastResponse triggerBlast()
    {
        final EntitySmoke smoke = new EntitySmoke(world());
        smoke.setPosition(x(), y(), z());
        if(world().spawnEntity(smoke))
        {
            return BlastState.TRIGGERED.genericResponse;
        }
        return BlastForgeResponses.ENTITY_SPAWNING.get();
    }
}

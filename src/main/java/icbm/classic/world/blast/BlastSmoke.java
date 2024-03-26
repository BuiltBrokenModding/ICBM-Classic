package icbm.classic.world.blast;

import icbm.classic.api.explosion.BlastState;
import icbm.classic.api.explosion.responses.BlastForgeResponses;
import icbm.classic.api.explosion.responses.BlastResponse;
import icbm.classic.world.blast.imp.BlastBase;
import icbm.classic.world.entity.SmokeEntity;

import javax.annotation.Nonnull;

public class BlastSmoke extends BlastBase {
    @Nonnull
    @Override
    public BlastResponse triggerBlast() {
        final SmokeEntity smoke = new SmokeEntity(level());
        smoke.setPosition(x(), y(), z());
        if (level().spawnEntity(smoke)) {
            return BlastState.TRIGGERED.genericResponse;
        }
        return BlastForgeResponses.ENTITY_SPAWNING.get();
    }
}

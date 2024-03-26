package icbm.classic.world.blast.redmatter;

import icbm.classic.api.explosion.BlastState;
import icbm.classic.api.explosion.responses.BlastForgeResponses;
import icbm.classic.api.explosion.responses.BlastResponse;
import icbm.classic.config.blast.ConfigBlast;
import icbm.classic.world.blast.imp.BlastBase;

import javax.annotation.Nonnull;

/**
 * Blast that exists purely to spawn the redmatter entity into the world
 * <p>
 * Created by Dark(DarkGuardsman, Robert) on 4/19/2020.
 */
public class BlastRedmatterSpawner extends BlastBase {

    @Nonnull
    @Override
    public BlastResponse triggerBlast() {
        //Build entity
        final RedmatterEntity entityRedmatter = new RedmatterEntity(level());
        entityRedmatter.setPosition(x(), y(), z());
        entityRedmatter.setBlastSize(getBlastRadius());
        entityRedmatter.setBlastMaxSize(ConfigBlast.redmatter.MAX_SIZE);

        //Attempt to spawn
        if (level().spawnEntity(entityRedmatter)) {
            return BlastState.TRIGGERED.genericResponse;
        }
        return BlastForgeResponses.ENTITY_SPAWNING.get();
    }
}

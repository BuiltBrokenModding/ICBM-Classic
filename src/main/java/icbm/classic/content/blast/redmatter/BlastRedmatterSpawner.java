package icbm.classic.content.blast.redmatter;

import icbm.classic.api.explosion.BlastState;
import icbm.classic.api.explosion.responses.BlastForgeResponses;
import icbm.classic.api.explosion.responses.BlastNullResponses;
import icbm.classic.api.explosion.responses.BlastResponse;
import icbm.classic.config.blast.ConfigBlast;
import icbm.classic.content.blast.imp.BlastBase;
import net.minecraft.world.World;

import javax.annotation.Nonnull;

/**
 * Blast that exists purely to spawn the redmatter entity into the world
 * <p>
 * Created by Dark(DarkGuardsman, Robert) on 4/19/2020.
 */
public class BlastRedmatterSpawner extends BlastBase
{
    private float startingSize = 1f;
    private float maxSize = ConfigBlast.REDMATTER.MAX_SIZE;

    @Nonnull
    @Override
    public BlastResponse runBlast()
    {
        final World world = world();
        if (world != null)
        {
            if(!world.isRemote)
            {
                //Build entity
                final EntityRedmatter entityRedmatter = new EntityRedmatter(world);
                entityRedmatter.posX = x();
                entityRedmatter.posY = y();
                entityRedmatter.posZ = z();
                entityRedmatter.setBlastSize(startingSize);
                entityRedmatter.setBlastMaxSize(maxSize);

                //Attempt to spawn
                if (world.spawnEntity(entityRedmatter))
                {
                    return BlastState.TRIGGERED.genericResponse;
                }
                return BlastForgeResponses.ENTITY_SPAWNING.get();
            }
            return BlastState.TRIGGERED.genericResponse;
        }
        return BlastNullResponses.WORLD.get();
    }
}

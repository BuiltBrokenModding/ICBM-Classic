package icbm.classic.content.blast.redmatter;

import icbm.classic.api.explosion.responses.BlastForgeResponses;
import icbm.classic.api.explosion.responses.BlastNullResponses;
import icbm.classic.api.explosion.responses.BlastResponse;
import icbm.classic.api.explosion.BlastState;
import icbm.classic.api.explosion.IBlastInit;
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
    private double size = ConfigBlast.REDMATTER.NORMAL_RADIUS;

    @Nonnull
    @Override
    public BlastResponse runBlast()
    {
        final World world = world();
        if (world != null)
        {
            //Build entity
            final EntityRedmatter entityRedmatter = new EntityRedmatter(world);
            entityRedmatter.posX = x();
            entityRedmatter.posY = y();
            entityRedmatter.posZ = z();
            entityRedmatter.setBlastSize((float) size);

            //Attempt to spawn
            if (world.spawnEntity(entityRedmatter))
            {
                return BlastState.TRIGGERED.genericResponse;
            }
            return BlastForgeResponses.ENTITY_SPAWNING.get();
        }
        return BlastNullResponses.WORLD.get();
    }

    public IBlastInit setBlastSize(double size)
    {
        this.size = size;
        return this;
    }
}

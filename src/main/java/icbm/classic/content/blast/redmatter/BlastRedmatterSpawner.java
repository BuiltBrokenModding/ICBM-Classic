package icbm.classic.content.blast.redmatter;

import icbm.classic.api.explosion.BlastState;
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
    @Nonnull
    @Override
    public BlastState runBlast()
    {
        final World world = world();
        if (world != null)
        {
            final EntityRedmatter entityRedmatter = new EntityRedmatter(world);
            entityRedmatter.posX = x();
            entityRedmatter.posY = y();
            entityRedmatter.posZ = z();
            if (world.spawnEntity(entityRedmatter))
            {
                return BlastState.TRIGGERED;
            }
            return BlastState.FORGE_EVENT_CANCEL;
        }
        return BlastState.ERROR;
    }
}

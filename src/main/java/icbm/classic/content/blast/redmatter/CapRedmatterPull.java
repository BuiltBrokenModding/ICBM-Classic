package icbm.classic.content.blast.redmatter;

import icbm.classic.api.explosion.IBlast;
import icbm.classic.api.explosion.redmatter.IBlastVelocity;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;

import javax.annotation.Nullable;

/**
 * Created by Dark(DarkGuardsman, Robert) on 4/19/2020.
 */
public class CapRedmatterPull implements IBlastVelocity
{
    private final BlastRedmatter redmatter;

    public CapRedmatterPull(BlastRedmatter redmatter) {
        this.redmatter = redmatter;
    }

    @Override
    public boolean onBlastApplyMotion(@Nullable Entity source, IBlast blast, double xDifference, double yDifference, double zDifference, double distance)
    {
        if (blast instanceof BlastRedmatter)
        {
            final BlastRedmatter rmBlast = (BlastRedmatter) blast;

            final int otherSize = (int) Math.pow(redmatter.getBlastRadius(), 3); //TODO this might be reversed
            final int thisSize = (int) Math.pow(blast.getBlastRadius(), 3);
            final double totalSize = otherSize + thisSize;

            final double thisSizePct = thisSize / totalSize;

            final Vec3d totalDelta = rmBlast.getPosition().subtract(redmatter.getPosition());
            final Vec3d thisDelta = totalDelta.scale(thisSizePct);

            if (redmatter.exploder != null)
            {
                redmatter.exploder.addVelocity(thisDelta.x, thisDelta.y, thisDelta.z);
                return true;
            }
        }
        return false;
    }
}

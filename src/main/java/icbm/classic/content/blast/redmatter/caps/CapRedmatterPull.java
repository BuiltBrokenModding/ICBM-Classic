package icbm.classic.content.blast.redmatter.caps;

import icbm.classic.api.explosion.IBlast;
import icbm.classic.api.explosion.redmatter.IBlastVelocity;
import icbm.classic.content.blast.redmatter.EntityRedmatter;
import net.minecraft.entity.Entity;

import javax.annotation.Nullable;

/**
 * Handles redmatter reaction to force being applied to it from other blasts.
 * <p>
 * By default this handles redmatters pulling each other. The logic calculates
 * the pull towards another redmatter. The idea is to simulate gravity attraction
 * between two objects. As well to introduce the merge mechanic of redmatters.
 * <p>
 * Created by Dark(DarkGuardsman, Robert) on 4/19/2020. Original logic by GHXX
 */
public class CapRedmatterPull implements IBlastVelocity
{
    private final EntityRedmatter redmatter;

    public CapRedmatterPull(EntityRedmatter redmatter)
    {
        this.redmatter = redmatter;
    }

    @Override
    public boolean onBlastApplyMotion(@Nullable Entity source, IBlast blast, double xDifference, double yDifference, double zDifference, double distance)
    {
        if (source instanceof EntityRedmatter)
        {
            handlePull((EntityRedmatter) source, xDifference, yDifference, zDifference, distance);
            return true;
        }
        return false;
    }

    private void handlePull(EntityRedmatter otherRedmatter, double xDifference, double yDifference, double zDifference, double distance)
    {
        final double sizeScale = calculatePullPower(otherRedmatter);

        //Normalize vector (creates direction of pull)
        final double vectorX = xDifference / distance;
        final double vectorY = yDifference / distance;
        final double vectorZ = zDifference / distance;

        //Calculate a pull force towards the other redmatter
        final double motionX = vectorX * sizeScale;
        final double motionY = vectorY * sizeScale;
        final double motionZ = vectorZ * sizeScale;

        //Apply the pull towards the other redmatter
        redmatter.addVelocity(motionX, motionY, motionZ);
    }

    private double calculatePullPower(EntityRedmatter otherRedmatter)
    {
        //Get the cubic size of each redmatter
        final double otherSize = Math.max(1, Math.pow(otherRedmatter.getBlastSize(), 3));
        final double thisSize = Math.max(1, Math.pow(redmatter.getBlastSize(), 3));

        //Figure out the power difference between the two
        final double combinedSize = otherSize + thisSize;
        return thisSize / combinedSize;
    }
}

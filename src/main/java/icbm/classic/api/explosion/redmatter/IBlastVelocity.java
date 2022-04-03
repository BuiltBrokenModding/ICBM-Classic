package icbm.classic.api.explosion.redmatter;

import icbm.classic.api.explosion.IBlast;
import net.minecraft.entity.Entity;

import javax.annotation.Nullable;

/**
 * Applied to entities that handle their own logic when being moved by a blast.
 * <p>
 * This is used by any blast that applies motion to an entity. This includes the
 * redmatter gravity effect and push effect of blasts going off.
 * <p>
 * Created by Dark(DarkGuardsman, Robert) on 1/26/2020.
 */
public interface IBlastVelocity
{
    /**
     * Called to handle motion being applied
     *
     * @param source      - entity representing the blast, not always passed
     * @param blast       - the blast instance
     * @param xDifference - difference between the source and this entity in the X
     * @param yDifference - difference between the source and this entity in the Y
     * @param zDifference - difference between the source and this entity in the Z
     * @param distance    - distance from entity source
     * @return true if handled, false to apply default handling
     */
    boolean onBlastApplyMotion(@Nullable Entity source, IBlast blast, double xDifference, double yDifference, double zDifference, double distance);
}

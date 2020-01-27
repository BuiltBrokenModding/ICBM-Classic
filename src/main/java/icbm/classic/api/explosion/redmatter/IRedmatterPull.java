package icbm.classic.api.explosion.redmatter;

import icbm.classic.api.explosion.IBlast;
import net.minecraft.entity.Entity;

/**
 * Applied to entities that change handle their own logic when pulled by a redmatter
 * Created by Dark(DarkGuardsman, Robert) on 1/26/2020.
 */
public interface IRedmatterPull
{
    void onPulledByRedmatter(Entity source, IBlast redmatter);
}

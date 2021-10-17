package icbm.classic.content.blast.redmatter;

import icbm.classic.content.blast.redmatter.logic.RedmatterLogic;
import net.minecraft.entity.Entity;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.Vec3d;

/**
 * Created by Dark(DarkGuardsman, Robert) on 5/23/2020.
 */
public class DamageSourceRedmatter extends DamageSource
{
    public final RedmatterLogic blastRedmatter;

    public DamageSourceRedmatter(RedmatterLogic blastRedmatter)
    {
        super("icbm.redmatter");
        this.blastRedmatter = blastRedmatter;
        setDamageBypassesArmor();
        setDamageIsAbsolute();

    }

    @Override
    public Entity getTrueSource()
    {
        return blastRedmatter != null ? blastRedmatter.host : null; //TODO see if we can get trigger source
    }

    @Override
    public Vec3d getDamageLocation()
    {
        return blastRedmatter != null && blastRedmatter.host != null
                ? blastRedmatter.host.getPositionVector()
                : null;
    }

    //TODO add handling for death messages
}

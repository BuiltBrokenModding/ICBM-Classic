package icbm.classic.world.blast.redmatter;

import icbm.classic.world.blast.redmatter.logic.RedmatterLogic;
import net.minecraft.util.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

/**
 * Created by Dark(DarkGuardsman, Robert) on 5/23/2020.
 */
public class DamageSourceRedmatter extends DamageSource {
    public final RedmatterLogic blastRedmatter;

    public DamageSourceRedmatter(RedmatterLogic blastRedmatter) {
        super("icbm.redmatter");
        this.blastRedmatter = blastRedmatter;
        setDamageBypassesArmor();
        setDamageIsAbsolute();

    }

    @Override
    public Entity getTrueSource() {
        return blastRedmatter != null ? blastRedmatter.host : null; //TODO see if we can get trigger source
    }

    @Override
    public Vec3 getDamageLocation() {
        return blastRedmatter != null && blastRedmatter.host != null
            ? blastRedmatter.host.getPositionVector()
            : null;
    }

    //TODO add handling for death messages
}

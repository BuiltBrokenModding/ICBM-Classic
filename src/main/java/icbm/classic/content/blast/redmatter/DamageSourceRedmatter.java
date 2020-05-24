package icbm.classic.content.blast.redmatter;

import net.minecraft.util.DamageSource;

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
    }

    //TODO add handling for source

    //TODO add handling for death messages
}

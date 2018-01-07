package resonant.api.explosion;

import com.builtbroken.mc.imp.transform.vector.Pos;
import net.minecraft.item.ItemStack;

/** Applied to all launcher TileEntitiies that operates the launching of missiles.
 *
 * @author Calclavia */
public interface ILauncherController
{
    /** What type of launcher is this? */
    public LauncherType getLauncherType();

    /** Launches the missile into the specified target. */
    public void launch();

    /** Can the launcher launch the missile? */
    public boolean canLaunch();

    /** @return The status of the launcher. */
    public String getStatus();

    /** @return The target of the launcher. */
    public Pos getTarget();

    /** @param target Sets the target of the launcher */
    public void setTarget(Pos target);

    /** Places a missile into the launcher. */
    public void placeMissile(ItemStack itemStack);
}
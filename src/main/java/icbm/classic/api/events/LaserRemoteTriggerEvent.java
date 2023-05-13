package icbm.classic.api.events;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.eventhandler.Cancelable;
import net.minecraftforge.fml.common.eventhandler.Event;

import javax.annotation.Nonnull;

/**
 * Called on the server side when the player rightclicks
 * the laser detonator. Use this to change the target
 * block position, or cancel the event to not activate
 * the affected launcher.
 */
@Cancelable
public class LaserRemoteTriggerEvent extends Event
{
    public final World world;
    public final EntityPlayer player;
    private Vec3d pos;

    /** Optional translation key to show user for why it was canceled */
    public String cancelReason;

    public LaserRemoteTriggerEvent(World world, Vec3d pos, EntityPlayer player)
    {
        this.world = world;
        this.pos = pos;
        this.player = player;
    }

    /**
     * Updates the target position of the event
     *
     * @param pos to set, can't be null or will throw exception
     */
    public void setPos(@Nonnull Vec3d pos) {
        if(pos == null) {
            throw new IllegalArgumentException("LaserRemoteTriggerEvent: target pos can not be set to null");
        }
        this.pos = pos;
    }

    public Vec3d getPos() {
        return pos;
    }
}

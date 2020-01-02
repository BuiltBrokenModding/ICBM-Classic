package icbm.classic.api.events;

import icbm.classic.content.entity.missile.EntityMissile;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.common.eventhandler.Cancelable;
import net.minecraftforge.fml.common.eventhandler.Event;

public class MissileRideEvent extends Event
{
    public final EntityMissile missile;
    public final EntityPlayer player;

    public MissileRideEvent(EntityMissile missile, EntityPlayer player)
    {
        this.missile = missile;
        this.player = player;
    }

    /**
     * Called right before a player starts to ride a missile.
     * Cancel this event to disallow the player to ride the missile.
     */
    @Cancelable
    public static class Start extends MissileRideEvent
    {
        public Start(EntityMissile missile, EntityPlayer player)
        {
            super(missile, player);
        }
    }

    /**
     * Called right before a player stops to ride a missile.
     * Cancel this event to disallow the player to dismount the missile.
     */
    @Cancelable
    public static class Stop extends MissileRideEvent
    {
        public Stop(EntityMissile missile, EntityPlayer player)
        {
            super(missile, player);
        }
    }
}

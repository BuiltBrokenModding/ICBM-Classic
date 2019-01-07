package icbm.classic.content.entity.missile;

/**
 *
 * Created by Dark(DarkGuardsman, Robert) on 3/14/2018.
 */
public enum MissileFlightType
{
    /** Missile fired from a tile launcher, missile will ark up then back down */
    PAD_LAUNCHER(false),
    /** Missile fired from a cruise launcher, missile will fire in a strait line */
    CRUISE_LAUNCHER(true),
    /** Missile fired from a hand launcher (RPG), missile will fire in a strait line */
    HAND_LAUNCHER(true),
    /** Special type, will track towards a position, will use a strait line unless position is moving */
    HOMING(true),
    /** Missile that will move with no logic */
    DEAD_AIM(true);

    /**
     * Temp var to tell the missile code to not use the ark path
     * Will be replaced by a flight object later
     */
    public final boolean movesDirectly;

    MissileFlightType(boolean movesDirectly)
    {
        this.movesDirectly = movesDirectly;
    }


    public static MissileFlightType get(int index)
    {
        if (index >= 0 && index < values().length)
        {
            return MissileFlightType.values()[index];
        }
        return DEAD_AIM;
    }
}

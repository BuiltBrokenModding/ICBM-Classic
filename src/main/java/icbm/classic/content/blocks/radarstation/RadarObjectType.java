package icbm.classic.content.blocks.radarstation;

/**
 *
 * Created by Dark(DarkGuardsman, Robert) on 9/9/2018.
 */
public enum RadarObjectType
{
    MISSILE,
    MISSILE_IMPACT,
    OTHER;

    public static RadarObjectType get(int index)
    {
        if(index == 0)
        {
            return MISSILE;
        }
        else if(index == 1)
        {
            return MISSILE_IMPACT;
        }
        return OTHER;
    }
}

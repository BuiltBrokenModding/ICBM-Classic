package icbm.classic.content.machines.radarstation;

/**
 * @see <a href="https://github.com/BuiltBrokenModding/VoltzEngine/blob/development/license.md">License</a> for what you can and can't do with the code.
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

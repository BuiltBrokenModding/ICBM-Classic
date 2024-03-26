package icbm.classic.world.block.radarstation;

/**
 * Created by Dark(DarkGuardsman, Robert) on 9/9/2018.
 */
public enum RadarObjectType {
    THREAT,
    THREAT_IMPACT,
    OTHER;

    public static RadarObjectType get(int index) {
        if (index == 0) {
            return THREAT;
        } else if (index == 1) {
            return THREAT_IMPACT;
        }
        return OTHER;
    }
}

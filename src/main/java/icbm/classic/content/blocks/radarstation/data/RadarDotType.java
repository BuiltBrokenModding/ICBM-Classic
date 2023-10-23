package icbm.classic.content.blocks.radarstation.data;

public enum RadarDotType {
    MARKER,
    DETECTED,
    HOSTILE,
    INCOMING;

    public static RadarDotType get(int index) {
        if(index >= 0 && index < values().length) {
            return values()[index];
        }
        return DETECTED;
    }
}

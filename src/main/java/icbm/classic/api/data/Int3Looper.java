package icbm.classic.api.data;

/**
 * Created by Dark(DarkGuardsman, Robert) on 2020-02-16.
 */
public interface Int3Looper {
    /**
     * Loops each xyz until told to stop
     *
     * @param x - x axis position
     * @param y - y axis position
     * @param z - z axis position
     * @return true to keep looping, false to exit
     */
    boolean apply(int x, int y, int z);
}

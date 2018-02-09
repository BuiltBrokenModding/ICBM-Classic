package icbm.classic.lib.radar.data;

import icbm.classic.api.IWorldPosition;
import net.minecraft.util.math.ChunkPos;

/**
 * Special type of weak reference used to track radar objects. This prevents the radar system from holding on to
 * references that should be unloaded from the map.
 *
 * @see <a href="https://github.com/BuiltBrokenModding/VoltzEngine/blob/development/license.md">License</a> for what you can and can't do with the code.
 * Created by Dark(DarkGuardsman, Robert) on 3/5/2016.
 */
public abstract class RadarObject<J> implements IWorldPosition
{
    /**
     * Is the radar object valid?
     *
     * @return true if the object is valid, normally a null check
     */
    public abstract boolean isValid();

    public ChunkPos getChunkPos()
    {
        return  new ChunkPos((int)x() >> 4, (int)z() >> 4);
    }
}

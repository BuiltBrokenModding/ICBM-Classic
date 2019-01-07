package icbm.classic.api.data;

import net.minecraft.world.World;

/**
 * @see <a href="https://github.com/BuiltBrokenModding/VoltzEngine/blob/development/license.md">License</a> for what you can and can't do with the code.
 * Created by Dark(DarkGuardsman, Robert) on 1/7/19.
 */
@FunctionalInterface
public interface WorldPosIntSupplier
{
    int get(World world, double x, double y, double z);
}

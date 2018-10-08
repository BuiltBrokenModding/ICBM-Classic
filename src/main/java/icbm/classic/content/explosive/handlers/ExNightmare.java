package icbm.classic.content.explosive.handlers;

import icbm.classic.content.explosive.blast.BlastNightmare;
import icbm.classic.prefab.tile.EnumTier;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * Holiday event missile
 *
 * @see <a href="https://github.com/BuiltBrokenModding/VoltzEngine/blob/development/license.md">License</a> for what you can and can't do with the code.
 * Created by Dark(DarkGuardsman, Robert) on 10/2/2017.
 */
public class ExNightmare extends Explosion
{
    public ExNightmare()
    {
        super("nightmare", EnumTier.ONE);
        hasGrenade = false;
        missileRenderScale = 0.07f;
    }

    @Override
    public void doCreateExplosion(World world, BlockPos pos, Entity entity, float scale)
    {
        new BlastNightmare(world, entity, pos.getX() + 0.5f, pos.getY() + 0.5f, pos.getZ() + 0.5f, 50 * scale).runBlast();
    }
}

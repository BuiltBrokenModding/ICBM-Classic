package icbm.classic.api.missiles;

import net.minecraft.world.World;

import javax.swing.text.html.parser.Entity;

/**
 * Applied to itemstacks that have the ability to be stored and used as missiles
 */
public interface IMissileStack
{
    /**
     * Called to generate a new missile
     * @param world to spawn inside
     * @return missile capability with contained entity
     */
    IMissile newMissile(World world);
}

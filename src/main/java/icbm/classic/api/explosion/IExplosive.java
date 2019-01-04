package icbm.classic.api.explosion;

import icbm.classic.api.EnumTier;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * An interface used to find various types of explosive's information.
 *
 * @author Calclavia
 */
@Deprecated //Very likely will be recoded or replace, so do not implement
public interface IExplosive
{
    /** @return The unique name key in the ICBM language file. */
    public String getTranslationKey();

    /** @return Gets the specific translated name of the block versions of the explosive. */
    public String getExplosiveName();

    /** @return Gets the specific translated name of the grenade versions of the explosive. */
    public String getGrenadeName();

    /** @return Gets the specific translated name of the missile versions of the explosive. */
    public String getMissileName();

    /** @return Gets the specific translated name of the minecart versions of the explosive. */
    public String getMinecartName();

    /** @return The tier of the explosive. */
    public EnumTier getTier();

    /**
     * Creates a new explosion at a given location.
     *
     * @param world  The world in which the explosion takes place.
     * @param pos
     * @param entity Entity that caused the explosion.
     */
    public void createExplosion(World world, BlockPos pos, Entity entity, float scale);

}

package icbm.classic.api.radio;

import icbm.classic.api.data.IBoundBox;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * Applied to tiles to note they are capable of doing radio
 * communication over data channels.
 *
 * Try to avoid exposing positional data about radio sources to players. This information
 * should be hidden and only used as needed. As the player should have to work for finding
 * the source. This way players can use radios without instantly being found.
 */
public interface IRadio {

    /**
     * Position of the radio.
     *
     * @return position
     */
    BlockPos getBlockPos(); //TODO move to IRadioTile so we can have itemstack version

    /**
     * World of the radio
     *
     * @return
     */
    World getWorld();

    /**
     * Range of the radio
     *
     * @return
     */
    IBoundBox<BlockPos> getRange();

    /**
     * Is this radio disabled
     *
     * @return true if disabled
     */
    default boolean isDisabled() {
        return false;
    }
}

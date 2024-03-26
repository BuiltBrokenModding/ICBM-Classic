package icbm.classic.api.energy;


import net.minecraft.core.Direction;

/**
 * Applied to tiles that provide access to an energy buffer for a side. This should
 * be the only interface needed to manage power for a tile.
 * <p>
 * If the machine implements {@link IEnergyBuffer} or another power interface try
 * to use that interface first before access this interface. This way the machine
 * can directly effect how power is managed. However, usage of those interfaces
 * should be rare as there is little to no need for them.
 * <p>
 * Created by Dark on 8/15/2015.
 */
@Deprecated //Will be replaced by Capability system
public interface IEnergyBufferProvider {
    /**
     * Gets access to power buffer for the given side
     *
     * @param side - side, null should be treated as unknown
     * @return energy buffer or null if none provider
     */
    IEnergyBuffer getEnergyBuffer(Direction side);
}

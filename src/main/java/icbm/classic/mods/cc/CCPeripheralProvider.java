package icbm.classic.mods.cc;

import dan200.computercraft.api.peripheral.IPeripheral;
import dan200.computercraft.api.peripheral.IPeripheralProvider;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Peripheral provider to allow Dan200's ComputerCraft to interface with some machines
 */
public class CCPeripheralProvider implements IPeripheralProvider {
	@Nullable
	@Override
	public IPeripheral getPeripheral( @Nonnull World world, @Nonnull BlockPos pos, @Nonnull EnumFacing side ) {
		TileEntity tileEntity = world.getTileEntity( pos );
		if (tileEntity instanceof IPeripheral) {
			return (IPeripheral) tileEntity;
		}
		return null;
	}
}

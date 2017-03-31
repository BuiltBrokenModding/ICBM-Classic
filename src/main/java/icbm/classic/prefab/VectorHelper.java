package icbm.classic.prefab;

import com.builtbroken.mc.imp.transform.vector.Pos;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

public class VectorHelper
{
	public static final int[][] RELATIVE_MATRIX = { { 3, 2, 1, 0, 5, 4 }, { 4, 5, 0, 1, 2, 3 }, { 0, 1, 3, 2, 4, 5 }, { 0, 1, 2, 3, 5, 4 }, { 0, 1, 5, 4, 3, 2 }, { 0, 1, 4, 5, 2, 3 } };

	/**
	 * Finds the direction relative to a base direction.
	 *
	 * @param front - The direction in which this block is facing/front. Use a number between 0 and
	 * 5. Default is 3.
	 * @param side - The side you are trying to find. A number between 0 and 5.
	 * @return The side relative to the facing direction.
	 */
	public static ForgeDirection getOrientationFromSide(ForgeDirection front, ForgeDirection side)
	{
		if (front != ForgeDirection.UNKNOWN && side != ForgeDirection.UNKNOWN)
		{
			return ForgeDirection.getOrientation(RELATIVE_MATRIX[front.ordinal()][side.ordinal()]);
		}
		return ForgeDirection.UNKNOWN;
	}

	public static TileEntity getTileEntityFromSide(World world, Pos position, ForgeDirection side)
	{
		return position.add(side).getTileEntity(world);
	}

}

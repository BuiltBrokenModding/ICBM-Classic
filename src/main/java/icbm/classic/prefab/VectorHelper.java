package icbm.classic.prefab;

import com.builtbroken.mc.data.Direction;
import com.builtbroken.mc.imp.transform.vector.Pos;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

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
	public static Direction getOrientationFromSide(Direction front, Direction side)
	{
		if (front != Direction.UNKNOWN && side != Direction.UNKNOWN)
		{
			return Direction.getOrientation(RELATIVE_MATRIX[front.ordinal()][side.ordinal()]);
		}
		return Direction.UNKNOWN;
	}

	public static TileEntity getTileEntityFromSide(World world, Pos position, Direction side)
	{
		return position.add(side).getTileEntity(world);
	}

}

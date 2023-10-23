package icbm.classic.content.blocks.multiblock;

import icbm.classic.lib.NBTConstants;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;

/**
 * Basic implementation of a multi block
 * Created by Dark on 8/9/2015.
 */
@Deprecated
public class TileMulti extends TileEntity
{
    private BlockPos hostPosition = null;

    @Override
    public void onLoad()
    {
        // TODO place blocks
    }

    @Override
    public void readFromNBT(NBTTagCompound compound)
    {
        super.readFromNBT(compound);
        if (compound.hasKey(NBTConstants.HOST_POS))
        {
            int[] data = compound.getIntArray(NBTConstants.HOST_POS);
            hostPosition = new BlockPos(data[0], data[1], data[2]);
        }
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound)
    {
        if (hostPosition != null)
        {
            compound.setIntArray(NBTConstants.HOST_POS, new int[]{hostPosition.getX(), hostPosition.getY(), hostPosition.getZ()});
        }
        return super.writeToNBT(compound);
    }
}

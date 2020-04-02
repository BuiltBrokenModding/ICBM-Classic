package icbm.classic.lib.transform.vector;

import com.builtbroken.jlib.data.network.IByteBufWriter;
import com.builtbroken.jlib.data.vector.IPos2D;
import com.builtbroken.jlib.data.vector.IPos3D;
import com.builtbroken.jlib.data.vector.Pos2D;

import icbm.classic.lib.NBTConstants;
import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.NBTTagCompound;

/**
 * 2D position/point on a plane. Used to traction the location data of something.
 * <p>
 * Created by robert on 1/11/2015.
 */
public class Point extends Pos2D<Point> implements IByteBufWriter, IPos2D
{
    public static final Point ZERO = new Point(0, 0);
    public static final Point UP = new Point(0, 1);
    public static final Point DOWN = new Point(0, -1);
    public static final Point LEFT = new Point(-1, 0);
    public static final Point RIGHT = new Point(0, 1);

    public Point(double x, double y)
    {
        super(x, y);
    }

    public Point()
    {
        this(0, 0);
    }

    public Point(IPos2D pos)
    {
        this(pos.x(), pos.y());
    }

    public Point(IPos3D pos)
    {
        this(pos.x(), pos.y());
    }

    public Point(ByteBuf data)
    {
        this(data.readDouble(), data.readDouble());
    }

    public Point(NBTTagCompound nbt)
    {
        this(nbt.getDouble(NBTConstants.X), nbt.getDouble(NBTConstants.Y));
    }

    public NBTTagCompound toNBT()
    {
        return save(new NBTTagCompound());
    }

    public NBTTagCompound save(NBTTagCompound nbt)
    {
        nbt.setDouble(NBTConstants.X, x());
        nbt.setDouble(NBTConstants.Y, y());
        return nbt;
    }

    @Override
    public ByteBuf writeBytes(ByteBuf data)
    {
        data.writeDouble(x());
        data.writeDouble(y());
        return data;
    }


    @Override
    public Point newPos(double x, double y)
    {
        return new Point(x, y);
    }
}

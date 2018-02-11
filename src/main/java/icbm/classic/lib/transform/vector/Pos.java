package icbm.classic.lib.transform.vector;

import com.builtbroken.jlib.data.vector.IPos3D;
import icbm.classic.api.IWorldPosition;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;

/**
 * Basic implementation of Pos3D that contains helper methods for interacting with MC worlds
 * Created by robert on 1/13/2015.
 */
public class Pos extends AbstractPos<Pos> implements IPos3D
{
    public static final Pos zero = new Pos();
    public static final Pos up = new Pos(EnumFacing.UP);
    public static final Pos down = new Pos(EnumFacing.DOWN);
    public static final Pos north = new Pos(EnumFacing.NORTH);
    public static final Pos south = new Pos(EnumFacing.SOUTH);
    public static final Pos east = new Pos(EnumFacing.EAST);
    public static final Pos west = new Pos(EnumFacing.WEST);


    public Pos()
    {
        this(0, 0, 0);
    }

    public Pos(double a)
    {
        this(a, a, a);
    }

    public Pos(double x, double y, double z)
    {
        super(x, y, z);
    }

    public Pos(double yaw, double pitch)
    {
        this(-Math.sin(Math.toRadians(yaw)), Math.sin(Math.toRadians(pitch)), -Math.cos(Math.toRadians(yaw)) * Math.cos(Math.toRadians(pitch)));
    }

    public Pos(TileEntity tile)
    {
        this(tile.getPos());
    }

    public Pos(Entity entity)
    {
        this(entity.posX, entity.posY, entity.posZ);
    }

    public Pos(IPos3D vec)
    {
        this(vec.x(), vec.y(), vec.z());
    }

    public Pos(IWorldPosition vec)
    {
        this(vec.x(), vec.y(), vec.z());
    }

    public Pos(NBTTagCompound nbt)
    {
        this(nbt.getDouble("x"), nbt.getDouble("y"), nbt.getDouble("z"));
    }

    public Pos(ByteBuf data)
    {
        this(data.readDouble(), data.readDouble(), data.readDouble());
    }

    public Pos(BlockPos par1)
    {
        this(par1.getX(), par1.getY(), par1.getZ());
    }

    public Pos(EnumFacing dir)
    {
        this(dir.getDirectionVec());
    }

    public Pos(Vec3d vec)
    {
        this(vec.x, vec.y, vec.z);
    }

    public Pos(Vec3i vec)
    {
        this(vec.getX(), vec.getY(), vec.getZ());
    }

    @Override
    public Pos newPos(double x, double y, double z)
    {
        return new Pos(x, y, z);
    }

    public static Pos getLook(Entity entity, double distance)
    {
        double f1 = 0D;
        double f2 = 0D;
        double f3 = 0D;
        double f4 = 0D;

        if (distance == 1.0F)
        {
            f1 = Math.cos(-entity.rotationYaw * 0.017453292F - Math.PI);
            f2 = Math.sin(-entity.rotationYaw * 0.017453292F - Math.PI);
            f3 = -Math.cos(-entity.rotationPitch * 0.017453292F);
            f4 = Math.sin(-entity.rotationPitch * 0.017453292F);
            return new Pos((f2 * f3), f4, (f1 * f3));
        }
        else
        {
            f1 = entity.prevRotationPitch + (entity.rotationPitch - entity.prevRotationPitch) * distance;
            f2 = entity.prevRotationYaw + (entity.rotationYaw - entity.prevRotationYaw) * distance;
            f3 = Math.cos(-f2 * 0.017453292F - Math.PI);
            f4 = Math.sin(-f2 * 0.017453292F - Math.PI);
            double f5 = -Math.cos(-f1 * 0.017453292F);
            double f6 = Math.sin(-f1 * 0.017453292F);
            return new Pos((f4 * f5), f6, (f3 * f5));
        }
    }

    public static Pos getLook(double yaw, double pitch, double distance)
    {
        double f1 = 0D;
        double f2 = 0D;
        double f3 = 0D;
        double f4 = 0D;

        if (distance == 1.0F)
        {
            f1 = Math.cos(-yaw * 0.017453292F - Math.PI);
            f2 = Math.sin(-yaw * 0.017453292F - Math.PI);
            f3 = -Math.cos(-pitch * 0.017453292F);
            f4 = Math.sin(-pitch * 0.017453292F);
            return new Pos((f2 * f3), f4, (f1 * f3));
        }
        else
        {
            f1 = pitch * distance;
            f2 = yaw * distance;
            f3 = Math.cos(-f2 * 0.017453292F - Math.PI);
            f4 = Math.sin(-f2 * 0.017453292F - Math.PI);
            double f5 = -Math.cos(-f1 * 0.017453292F);
            double f6 = Math.sin(-f1 * 0.017453292F);
            return new Pos((f4 * f5), f6, (f3 * f5));
        }
    }

    @Override
    public boolean equals(Object object)
    {
        return object instanceof IPos3D && Math.abs(((IPos3D) object).x() - x()) <= 0.001 && Math.abs(((IPos3D) object).y() - y()) <= 0.001 && Math.abs(((IPos3D) object).z() - z()) <= 0.001;
    }

    @Override
    public int hashCode()
    {
        long x = Double.doubleToLongBits(this.x() * 100 / 100);
        long y = Double.doubleToLongBits(this.y() * 100 / 100);
        long z = Double.doubleToLongBits(this.z() * 100 / 100);
        long hash = (x ^ (x >>> 32));
        hash = 31 * hash + y ^ (y >>> 32);
        hash = 31 * hash + z ^ (z >>> 32);
        return (int) hash;
    }
}

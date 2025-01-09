package icbm.classic.lib.transform.vector;

import com.builtbroken.jlib.data.vector.IPos3D;
import com.builtbroken.jlib.data.vector.ITransform;
import com.builtbroken.jlib.data.vector.Pos3D;
import icbm.classic.lib.NBTConstants;
import icbm.classic.lib.transform.rotation.EulerAngle;
import io.netty.buffer.ByteBuf;
import net.minecraft.dispenser.IPosition;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;

/**
 * Abstract version of Pos3D for interaction with the minecraft world
 * Created by Robin on 1/13/2015.
 */
public abstract class AbstractPos<R extends AbstractPos> extends Pos3D<R> implements IPosition
{
    public AbstractPos(double x, double y, double z)
    {
        super(x, y, z);
    }

    public double angle(IPos3D other)
    {
        return Math.acos((this.cross(other)).magnitude() / (new Pos(other).magnitude() * magnitude()));
    }

    //=========================
    //========Converters=======
    //=========================

    @Deprecated
    public EulerAngle toEulerAngle(IPos3D target)
    {
        return sub(target).toEulerAngle();
    }

    public EulerAngle toEulerAngle(Vec3d target)
    {
        return sub(target).toEulerAngle();
    }

    public EulerAngle toEulerAngle()
    {
        return new EulerAngle(Math.toDegrees(Math.atan2(x(), z())), Math.toDegrees(-Math.atan2(y(), Math.hypot(z(), x()))));
    }

    public IPos3D transform(ITransform transformer)
    {
        if (this instanceof IPos3D)
        {
            return transformer.transform((IPos3D) this);
        }
        return null;
    }

    //=========================
    //======Math Operators=====
    //=========================

    public R add(BlockPos other)
    {
        return add(other.getX(), other.getY(), other.getZ());
    }

    public R add(Direction face)
    {
        return add(face.getXOffset(), face.getYOffset(), face.getZOffset());
    }

    public R add(Vec3d vec)
    {
        return add(vec.x, vec.y, vec.z);
    }

    public R sub(Direction face)
    {
        return sub(face.getXOffset(), face.getYOffset(), face.getZOffset());
    }

    public R sub(Vec3d vec)
    {
        return sub(vec.x, vec.y, vec.z);
    }

    public double distance(Vec3i vec)
    {
        return distance(vec.getX() + 0.5, vec.getY() + 0.5, vec.getZ() + 0.5);
    }

    public double distance(Vec3d vec)
    {
        return distance(vec.x, vec.y, vec.z);
    }

    public double distance(Entity entity)
    {
        return distance(entity.posX, entity.posY, entity.posZ);
    }

    @Override
    public R floor()
    {
        return newPos(Math.floor(x()), Math.floor(y()), Math.floor(z()));
    }

    //=========================
    //========NBT==============
    //=========================

    public CompoundNBT writeNBT(CompoundNBT nbt)
    {
        nbt.putDouble(NBTConstants.X, x());
        nbt.putDouble(NBTConstants.Y, y());
        nbt.putDouble(NBTConstants.Z, z());
        return nbt;
    }

    public ByteBuf writeByteBuf(ByteBuf data)
    {
        data.writeDouble(x());
        data.writeDouble(y());
        data.writeDouble(z());
        return data;
    }

    //===================
    //==ILocation Accessors==
    //===================
    @Override
    public double getX()
    {
        return x();
    }

    @Override
    public double getY()
    {
        return y();
    }

    @Override
    public double getZ()
    {
        return z();
    }
}

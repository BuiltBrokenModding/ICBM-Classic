package icbm.classic.lib.network.packet;

import com.builtbroken.jlib.data.network.IByteBufWriter;
import icbm.classic.lib.network.IPacket;
import icbm.classic.lib.transform.vector.Pos;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.minecraftforge.fml.common.network.ByteBufUtils;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;

/**
 *
 * Created by Dark(DarkGuardsman, Robert) on 1/27/2018.
 */
public class PacketBase<P extends PacketBase> implements IPacket<P>
{
    protected List<Consumer<ByteBuf>> writers = new ArrayList();
    protected ByteBuf dataToRead;

    @Override
    public void encodeInto(ChannelHandlerContext ctx, ByteBuf buffer)
    {
        writers.forEach((func) -> func.accept(buffer));
    }

    @Override
    public void decodeInto(ChannelHandlerContext ctx, ByteBuf buffer)
    {
        dataToRead = buffer.slice().copy();
    }


    /**
     * Called to write data without manually defining the write
     *
     * @param object - object to write
     * @param buffer - location to write to
     */
    private void writeData(Object object, ByteBuf buffer)
    {
        if (object.getClass().isArray())
        {
            for (int i = 0; i < Array.getLength(object); i++)
            {
                writeData(Array.get(object, i), buffer);
            }
        }
        else if (object instanceof Collection)
        {
            for (Object o : (Collection) object)
            {
                writeData(o, buffer);
            }
        }
        else if (object instanceof Byte)
        {
            buffer.writeByte((Byte) object);
        }
        else if (object instanceof Integer)
        {
            buffer.writeInt((Integer) object);
        }
        else if (object instanceof Short)
        {
            buffer.writeShort((Short) object);
        }
        else if (object instanceof Long)
        {
            buffer.writeLong((Long) object);
        }
        else if (object instanceof Float)
        {
            buffer.writeFloat((Float) object);
        }
        else if (object instanceof Double)
        {
            buffer.writeDouble((Double) object);
        }
        else if (object instanceof Boolean)
        {
            buffer.writeBoolean((Boolean) object);
        }
        else if (object instanceof String)
        {
            ByteBufUtils.writeUTF8String(buffer, (String) object);
        }
        else if (object instanceof NBTTagCompound)
        {
            ByteBufUtils.writeTag(buffer, (NBTTagCompound) object);
        }
        else if (object instanceof ItemStack)
        {
            ByteBufUtils.writeItemStack(buffer, (ItemStack) object);
        }
        else if (object instanceof Pos)
        {
            ((Pos) object).writeByteBuf(buffer);
        }
        else if (object instanceof IByteBufWriter)
        {
            ((IByteBufWriter) object).writeBytes(buffer);
        }
        else if (object instanceof Enum)
        {
            buffer.writeInt(((Enum) object).ordinal());
        }
        else if (object instanceof Vec3i)
        {
            buffer.writeInt(((Vec3i) object).getX());
            buffer.writeInt(((Vec3i) object).getY());
            buffer.writeInt(((Vec3i) object).getZ());
        }

        else if (object instanceof Vec3d)
        {
            buffer.writeDouble(((Vec3d) object).x);
            buffer.writeDouble(((Vec3d) object).y);
            buffer.writeDouble(((Vec3d) object).z);
        }
        else
        {
            throw new IllegalArgumentException("PacketBase: Unsupported write data type " + object);
        }
    }

    public ByteBuf data()
    {
        return dataToRead;
    }


    @Override
    public P addData(Object... objects)
    {
        for (Object object : objects)
        {
            addData((byteBuf) -> writeData(object, byteBuf));
        }
        return (P) this;
    }

    @Override
    public P addData(Consumer<ByteBuf> writer) {
        writers.add(writer);
        return (P) this;
    }

    @Deprecated
    public P write(Object object)
    {
        return addData(object);
    }
}

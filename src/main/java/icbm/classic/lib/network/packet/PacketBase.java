package icbm.classic.lib.network.packet;

import com.builtbroken.jlib.data.network.IByteBufWriter;
import icbm.classic.lib.network.IPacket;
import icbm.classic.lib.transform.vector.Pos;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.DecoderException;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;

/**
 *
 * Created by Dark(DarkGuardsman, Robin) on 1/27/2018.
 */
@Deprecated
public class PacketBase<P extends PacketBase> implements IPacket<P>
{
    protected List<Consumer<RegistryFriendlyByteBuf>> writers = new ArrayList();
    protected ByteBuf dataToRead;

    @Override
    public void encodeInto(RegistryFriendlyByteBuf buffer)
    {
        // Prefix with length so the remaining data can be copied out on decode
        final RegistryFriendlyByteBuf data = new RegistryFriendlyByteBuf(Unpooled.buffer(), buffer.registryAccess());
        writers.forEach((func) -> func.accept(data));
        buffer.writeVarInt(data.readableBytes());
        buffer.writeBytes(data);
    }

    @Override
    public void decodeInto(RegistryFriendlyByteBuf buffer)
    {
        final int length = buffer.readVarInt();
        // Validate before allocating, the length comes from the other side
        if (length < 0 || length > buffer.readableBytes())
        {
            throw new DecoderException("PacketBase: data length " + length + " is invalid, readable bytes " + buffer.readableBytes());
        }
        // Copy into an unpooled heap buffer, ByteBuf#readBytes(int) would allocate from the (pooled) network
        // allocator and nothing releases this buffer
        final byte[] bytes = new byte[length];
        buffer.readBytes(bytes);
        dataToRead = new RegistryFriendlyByteBuf(Unpooled.wrappedBuffer(bytes), buffer.registryAccess());
    }


    /**
     * Called to write data without manually defining the write
     *
     * @param object - object to write
     * @param buffer - location to write to
     */
    private void writeData(Object object, RegistryFriendlyByteBuf buffer)
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
            buffer.writeUtf((String) object);
        }
        else if (object instanceof CompoundTag)
        {
            buffer.writeNbt((CompoundTag) object);
        }
        else if (object instanceof ItemStack)
        {
            ItemStack.OPTIONAL_STREAM_CODEC.encode(buffer, (ItemStack) object);
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

        else if (object instanceof Vec3)
        {
            buffer.writeDouble(((Vec3) object).x);
            buffer.writeDouble(((Vec3) object).y);
            buffer.writeDouble(((Vec3) object).z);
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
    public P addData(Consumer<RegistryFriendlyByteBuf> writer) {
        writers.add(writer);
        return (P) this;
    }

    @Deprecated
    public P write(Object object)
    {
        return addData(object);
    }
}

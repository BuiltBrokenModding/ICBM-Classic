package icbm.classic.lib.network;

import icbm.classic.lib.network.packet.PacketRedmatterSizeSync;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Created by Dark(DarkGuardsman, Robert) on 1/29/2020.
 */
public class PacketRedmatterSizeSyncTest
{
    @Test
    public void encodePacket() {

        //Build packet
        final PacketRedmatterSizeSync packet = new PacketRedmatterSizeSync(123f, 4567);

        //Encode data
        final ByteBuf buf = Unpooled.buffer();
        packet.encodeInto(null, buf);

        //Should read a float and int back out
        Assertions.assertEquals(123f, buf.readFloat());
        Assertions.assertEquals(4567, ByteBufUtils.readVarInt(buf, 5));

        //Should be no more data
        Assertions.assertEquals(buf.writerIndex(), buf.readerIndex());
    }
}

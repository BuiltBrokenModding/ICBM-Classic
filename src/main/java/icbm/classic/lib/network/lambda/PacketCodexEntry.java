package icbm.classic.lib.network.lambda;

import io.netty.buffer.ByteBuf;
import lombok.Data;

import java.lang.reflect.Type;
import java.util.function.BiConsumer;
import java.util.function.Function;

@Data
public class PacketCodexEntry<OBJECT, DATA> {
    private final Type type;
    private final Function<OBJECT, DATA> getter;
    private final BiConsumer<OBJECT, DATA> setter;
    private final BiConsumer<ByteBuf, DATA> encoder;
    private final Function<ByteBuf, DATA> decoder;
}

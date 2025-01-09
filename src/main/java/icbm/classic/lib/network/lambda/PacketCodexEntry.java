package icbm.classic.lib.network.lambda;

import lombok.Data;
import net.minecraft.network.PacketBuffer;

import java.lang.reflect.Type;
import java.util.function.BiConsumer;
import java.util.function.Function;

@Data
public class PacketCodexEntry<OBJECT, DATA> {
    private final Type type;
    private final boolean isArray;
    private final Function<OBJECT, DATA> getter;
    private final BiConsumer<OBJECT, DATA> setter;
    private final BiConsumer<PacketBuffer, DATA> encoder;
    private final Function<PacketBuffer, DATA> decoder;
}

package icbm.classic.lib.network.lambda;

import net.minecraft.network.RegistryFriendlyByteBuf;
import lombok.Data;

import java.lang.reflect.Type;
import java.util.function.BiConsumer;
import java.util.function.Function;

@Data
public class PacketCodexEntry<OBJECT, DATA> {
    private final Type type;
    private final boolean isArray;
    private final Function<OBJECT, DATA> getter;
    private final BiConsumer<OBJECT, DATA> setter;
    private final BiConsumer<RegistryFriendlyByteBuf, DATA> encoder;
    private final Function<RegistryFriendlyByteBuf, DATA> decoder;
}

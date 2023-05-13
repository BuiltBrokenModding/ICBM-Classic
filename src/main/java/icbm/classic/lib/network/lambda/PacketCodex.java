package icbm.classic.lib.network.lambda;

import icbm.classic.ICBMClassic;
import icbm.classic.lib.network.IPacket;
import io.netty.buffer.ByteBuf;
import lombok.AccessLevel;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.network.ByteBufUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;

/**
 * Codex for creating packets to read/write data using lambda accessors
 *
 * @param <RAW> object to access
 * @param <TARGET> object converted from raw and used for Read/Write, often is the same as {@link RAW}
 */
@Data
@RequiredArgsConstructor
public abstract class PacketCodex<RAW, TARGET> {
    @Setter(value = AccessLevel.PACKAGE)
    private int id = -1;

    @Accessors(chain = true, fluent = true)
    private boolean allowServer = true;
    @Accessors(chain = true, fluent = true)
    private boolean allowClient = true;

    /** Content representing this packet set, if this is a TileEntity then it is the block */
    private final ResourceLocation parent;
    /** Name of the packet, useful for debugging and error handling */
    private final ResourceLocation name;
    /** Convert to go from RAW to TARGET, often RAW == TARGET but in cases it can be useful to go from TilEntity to Capability */
    private final Function<RAW, TARGET> converter;

    /** Entries of readers and writers */
    private final List<PacketCodexEntry<TARGET, ?>> entries = new ArrayList<>(); // TODO make optional so we can do single field encodes

    /** Called when decoding is finished and all data is written to the target */
    @Accessors(chain = true, fluent = true)
    private BiConsumer<RAW, TARGET> onFinished;

    public PacketCodex<RAW, TARGET> asClientOnly() {
        this.allowClient = true;
        this.allowServer = false;
        return this;
    }

    public PacketCodex<RAW, TARGET> asServerOnly() {
        this.allowClient = false;
        this.allowServer = true;
        return this;
    }

    public PacketCodex<RAW, TARGET> fromClient() {
        return asServerOnly();
    }

    public PacketCodex<RAW, TARGET> fromServer() {
        return asClientOnly();
    }

    public PacketCodex<RAW, TARGET> nodeInt(Function<TARGET, Integer> getter, BiConsumer<TARGET, Integer> setter) {
        entries.add(new PacketCodexEntry<TARGET, Integer>(Integer.class, getter, setter, ByteBuf::writeInt, ByteBuf::readInt));
        return this;
    }

    public PacketCodex<RAW, TARGET> nodeDouble(Function<TARGET, Double> getter, BiConsumer<TARGET, Double> setter) {
        entries.add(new PacketCodexEntry<TARGET, Double>(Double.class, getter, setter, ByteBuf::writeDouble, ByteBuf::readDouble));
        return this;
    }

    public PacketCodex<RAW, TARGET> nodeString(Function<TARGET, String> getter, BiConsumer<TARGET, String> setter) {
        entries.add(new PacketCodexEntry<TARGET, String>(String.class, getter, setter, ByteBufUtils::writeUTF8String, ByteBufUtils::readUTF8String));
        return this;
    }

    public PacketCodex<RAW, TARGET> nodeBoolean(Function<TARGET, Boolean> getter, BiConsumer<TARGET, Boolean> setter) {
        entries.add(new PacketCodexEntry<TARGET, Boolean>(Boolean.class, getter, setter, ByteBuf::writeBoolean, ByteBuf::readBoolean));
        return this;
    }

    public PacketCodex<RAW, TARGET> toggleBoolean(Function<TARGET, Boolean> getter, BiConsumer<TARGET, Boolean> setter) {
        return nodeBoolean(getter, (t, b) -> setter.accept(t, !b));
    }

    public abstract boolean isValid(RAW tile);

    public abstract IPacket build(RAW tile);

    public void sendToServer(RAW raw) {
        try {
            ICBMClassic.packetHandler.sendToServer(build(raw));
        }
        catch (Exception e) {
            ICBMClassic.logger().error("Failed to send packet(" + parent + ", " + name + ") to server for " + raw, e);
        }
    }

    @Override
    public String toString() {
        return String.format("%s(%s,%s,%s)", getClass().getName(), id, parent, name);
    }
}

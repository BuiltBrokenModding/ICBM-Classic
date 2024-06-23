package icbm.classic.lib.network.lambda;

import icbm.classic.ICBMClassic;
import icbm.classic.lib.network.IPacket;
import io.netty.buffer.ByteBuf;
import lombok.AccessLevel;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.storage.WorldInfo;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import org.apache.logging.log4j.util.TriConsumer;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Codex for creating packets to read/write data using lambda accessors
 *
 * @param <RAW> object to access
 * @param <TARGET> object converted from raw and used for Read/Write, often is the same as {@link RAW}
 */
@Data
@RequiredArgsConstructor
public abstract class PacketCodex<RAW, TARGET> {

    public static final String LOGGER_TEMPLATE = "Packet(%s): %s\n\tWorld:%s\n\tPos: %sx %sy %sz\n\n\tCodex: %s\n\tParent: %s\n\tName: %s";

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
    private TriConsumer<RAW, TARGET, EntityPlayer> onFinished;

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
        return node(Integer.class,false,  getter, setter, ByteBuf::writeInt, ByteBuf::readInt);
    }

    public PacketCodex<RAW, TARGET> nodeByte(Function<TARGET, Byte> getter, BiConsumer<TARGET, Byte> setter) {
        entries.add(new PacketCodexEntry<TARGET, Byte>(Byte.class,false,  getter, setter, ByteBuf::writeByte, ByteBuf::readByte));
        return this;
    }

    public <E extends Enum<E>> PacketCodex<RAW, TARGET> nodeEnum(Class<E> e, Function<TARGET, E> getter, BiConsumer<TARGET, E> setter) {
        if(e.getEnumConstants().length > 255) {
            return nodeInt((t) -> getter.apply(t).ordinal(), (t, v) -> setter.accept(t, e.getEnumConstants()[v]));
        }
        return nodeByte((t) -> (byte) getter.apply(t).ordinal(), (t, v) -> setter.accept(t, e.getEnumConstants()[v]));
    }

    public PacketCodex<RAW, TARGET> nodeFacing(Function<TARGET, EnumFacing> getter, BiConsumer<TARGET, EnumFacing> setter) {
        return node(EnumFacing.class, false, getter, setter, (byteBuf, face) -> byteBuf.writeByte((byte)face.ordinal()), (byteBuf) -> EnumFacing.getFront(byteBuf.readByte()));
    }

    public PacketCodex<RAW, TARGET> nodeDouble(Function<TARGET, Double> getter, BiConsumer<TARGET, Double> setter) {
        return node(Double.class,false,  getter, setter, ByteBuf::writeDouble, ByteBuf::readDouble);
    }

    public PacketCodex<RAW, TARGET> nodeFloat(Function<TARGET, Float> getter, BiConsumer<TARGET, Float> setter) {
        return node(Float.class,false,  getter, setter, ByteBuf::writeFloat, ByteBuf::readFloat);
    }

    public PacketCodex<RAW, TARGET> nodeString(Function<TARGET, String> getter, BiConsumer<TARGET, String> setter) {
        return node(String.class,false,  getter, setter, ByteBufUtils::writeUTF8String, ByteBufUtils::readUTF8String);
    }

    public PacketCodex<RAW, TARGET> nodeItemStack(Function<TARGET, ItemStack> getter, BiConsumer<TARGET, ItemStack> setter) {
        return node(ItemStack.class,false,  getter, setter,
            (byteBuf, stack) -> { ByteBufUtils.writeTag(byteBuf, stack.serializeNBT());},
            (byteBuf) -> new ItemStack(ByteBufUtils.readTag(byteBuf))
        );
    }

    public PacketCodex<RAW, TARGET> nodeVec3d(Function<TARGET, Vec3d> getter, BiConsumer<TARGET, Vec3d> setter) {
        entries.add(new PacketCodexEntry<TARGET, Vec3d>(Vec3d.class,false,  getter, setter,
            (byteBuf, vec3d) -> {
                byteBuf.writeDouble(vec3d.x);
                byteBuf.writeDouble(vec3d.y);
                byteBuf.writeDouble(vec3d.z);
            },
            (byteBuf) -> new Vec3d(byteBuf.readDouble(), byteBuf.readDouble(), byteBuf.readDouble())
        ));
        return this;
    }

    public PacketCodex<RAW, TARGET> nodeNbtCompound(Function<TARGET, NBTTagCompound> getter, BiConsumer<TARGET, NBTTagCompound> setter) {
        return node(NBTTagCompound.class,false,  getter, setter,ByteBufUtils::writeTag, ByteBufUtils::readTag);
    }

    public PacketCodex<RAW, TARGET> nodeBoolean(Function<TARGET, Boolean> getter, BiConsumer<TARGET, Boolean> setter) {
        return node(Boolean.class, false, getter, setter, ByteBuf::writeBoolean, ByteBuf::readBoolean);
    }

    public PacketCodex<RAW, TARGET> toggleBoolean(Function<TARGET, Boolean> getter, BiConsumer<TARGET, Boolean> setter) {
        return nodeBoolean(getter, (t, b) -> setter.accept(t, !b));
    }
    public <DATA> PacketCodex<RAW, TARGET> node(Class<DATA> clazz, boolean isArray, Function<TARGET, DATA> getter, BiConsumer<TARGET, DATA> setter, BiConsumer<ByteBuf, DATA> encoder, Function<ByteBuf, DATA> decoder) {
        return node(new PacketCodexEntry<>(clazz, isArray, getter, setter, encoder, decoder));
    }

    public <DATA> PacketCodex<RAW, TARGET> node(Function<TARGET, DATA> getter, BiConsumer<TARGET, DATA> setter, BiConsumer<ByteBuf, DATA> encoder, Function<ByteBuf, DATA> decoder) {
        return node(new PacketCodexEntry<>(null, false, getter, setter, encoder, decoder));
    }

    public <DATA> PacketCodex<RAW, TARGET> node(PacketCodexEntry<TARGET, DATA> entry) {
        entries.add(entry);
        return this;
    }

    /**
     * Pulls the data from the targets and prepares writers for each.
     *
     * Purpose of this is to exact field data at time of packet creation. This
     * way any changes on main thread do not impact the packet writing process.
     *
     * @param target to encode
     * @return list of consumers to run for each field
     */
    public List<Consumer<ByteBuf>> encodeAsWriters(TARGET target) {
        return getEntries().stream()
            .map(entry -> {
                final Object o = entry.getGetter().apply(target);
                final BiConsumer<ByteBuf, Object> encoder = (BiConsumer<ByteBuf, Object>) entry.getEncoder();
                return (Consumer<ByteBuf>) (byteBuf) -> encoder.accept(byteBuf, o);
            })
            .collect(Collectors.toList());
    }

    /**
     * Decodes packet data into fields
     *
     * @param byteBuf
     * @return
     */
    public List<Consumer<TARGET>> decodeAsSetters(ByteBuf byteBuf) {
        return getEntries().stream()
            .map(entry -> {
                final Function<ByteBuf, Object> decoder = (Function<ByteBuf, Object>) entry.getDecoder();
                final Object data = decoder.apply(byteBuf);
                final BiConsumer<TARGET, Object> setter = (BiConsumer<TARGET, Object>) entry.getSetter();
                return (Consumer<TARGET>) (target) -> setter.accept(target, data);
            })
            .collect(Collectors.toList());
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

    public void sendPacketToGuiUsers(RAW raw, Collection<EntityPlayer> players)
    {
        try {
            final IPacket packet = build(raw);
            players.stream().filter(player -> player instanceof EntityPlayerMP).forEach((player) -> {
                ICBMClassic.packetHandler.sendToPlayer(packet, (EntityPlayerMP) player);
            });
        }
        catch (Exception e) {
            ICBMClassic.logger().error("Failed to send packet(" + parent + ", " + name + ") to gui users for " + raw, e);
        }
    }

    public void sendToAllAround(RAW raw, NetworkRegistry.TargetPoint point){
        try {
            ICBMClassic.packetHandler.sendToAllAround(build(raw), point);
        }
        catch (Exception e) {
            ICBMClassic.logger().error("Failed to send packet(" + parent + ", " + name + ") to server for " + raw, e);
        }
    }

    @Override
    public String toString() {
        return String.format("%s(%s,%s,%s)", getClass().getName(), id, parent, name);
    }

    public String generateLogMessage(@Nullable World world, @Nullable BlockPos pos, @Nonnull String message) {
        return String.format(
            LOGGER_TEMPLATE,
            getId(),
            message,
            Optional.ofNullable(world).map(World::getWorldInfo).map(WorldInfo::getWorldName).orElse("--"),
            Optional.ofNullable(pos).map(BlockPos::getX).map(Object::toString).orElse("-"),
            Optional.ofNullable(pos).map(BlockPos::getY).map(Object::toString).orElse("-"),
            Optional.ofNullable(pos).map(BlockPos::getZ).map(Object::toString).orElse("-"),
            this.getClass(),
            this.getParent(),
            this.getName()
        );
    }

    public void logDebug(@Nullable World world, @Nullable BlockPos pos, @Nonnull String message) {
        if(ICBMClassic.logger().isDebugEnabled()) {
            ICBMClassic.logger().debug(generateLogMessage(world, pos, message));
        }
    }

    public void logError(@Nullable World world, @Nullable BlockPos pos, @Nonnull String message) { //TODO consider custom exceptions with doTrace() logic
        ICBMClassic.logger().error(generateLogMessage(world, pos, message));
    }

    public void logError(@Nullable World world, @Nullable BlockPos pos, @Nonnull String message, Exception e) { //TODO consider custom exceptions with doTrace() logic
        ICBMClassic.logger().error(generateLogMessage(world, pos, message), e);
    }
}

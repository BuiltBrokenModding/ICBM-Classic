package icbm.classic.lib.network.lambda.tile;

import com.builtbroken.mc.testing.junit.TestManager;
import com.builtbroken.mc.testing.junit.world.FakeWorldServer;
import com.google.common.collect.Lists;
import icbm.classic.BlockEntityFakeData;
import icbm.classic.lib.network.lambda.PacketCodexReg;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraft.network.PacketBuffer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.BlockPos;
import org.junit.jupiter.api.*;
import org.mockito.InOrder;
import org.mockito.Mockito;

class PacketLambdaTileTest {

    static ResourceLocation PARENT = new ResourceLocation("test", "packet");

    static TestManager testManager = new TestManager("packet.tile", Assertions::fail).withWorldWrapper(Mockito::spy);

    final FakeWorldServer world = testManager.getLevel(0);
    final BlockEntityFakeData tile = Mockito.spy(BlockEntityFakeData.class);

    public static final PacketCodexTile<BlockEntityFakeData, BlockEntityFakeData> CODEX =
        (PacketCodexTile<BlockEntityFakeData, BlockEntityFakeData>) new PacketCodexTile<BlockEntityFakeData, BlockEntityFakeData>(PARENT, "target")
            .fromClient()
            .nodeInt(BlockEntityFakeData::getField1, BlockEntityFakeData::setField1)
            .nodeFloat(BlockEntityFakeData::getField2, BlockEntityFakeData::setField2)
            .nodeString(BlockEntityFakeData::getField3, BlockEntityFakeData::setField3)
            .onFinished((r, t, p) -> t.wasRead(true));
    ;

    @BeforeAll
    public static void beforeAllTests() {
        PacketCodexReg.register(new PacketCodexTile<>(PARENT, "fake1"));
        PacketCodexReg.register(new PacketCodexTile<>(PARENT, "fake2"));
        PacketCodexReg.register(CODEX);
    }


    @AfterAll
    public static void afterAllTests() {
        testManager.tearDownTest();
    }

    @BeforeEach
    public void beforeEachTest() {
        tile.setPos(new BlockPos(100, 105, 6789));
        tile.setLevel(world);
    }

    @AfterEach
    public void afterEachTest() {
        testManager.cleanupBetweenTests();
    }

    @Test
    @DisplayName("Verify we can build a packet and copy data from the target")
    void buildPacket() {
        final PacketLambdaTile packet = new PacketLambdaTile(CODEX, tile, tile);

        Assertions.assertEquals(0, packet.getDimensionId());
        Assertions.assertEquals(100, packet.getPos().getX());
        Assertions.assertEquals(105, packet.getPos().getY());
        Assertions.assertEquals(6789, packet.getPos().getZ());
        Assertions.assertEquals(3, packet.getWriters().size());
    }

    @Test
    @DisplayName("Verify we encode data in the correct order and with values")
    void encode() {

        // Setup tile data
        tile.setField1(567);
        tile.setField2(5.61f);
        tile.setField3("test5");

        //Setup packet
        final PacketLambdaTile packet = new PacketLambdaTile(CODEX, tile, tile);
        final ChannelHandlerContext ctx = Mockito.mock(ChannelHandlerContext.class);
        final PacketBuffer buffer = Mockito.spy(new PacketBuffer(Unpooled.buffer()));

        // run encoding
        packet.encodeInto(ctx, buffer);

        // Validate order of encoding
        final InOrder inOrder = Mockito.inOrder(buffer);
        inOrder.verify(buffer).writeInt(2);
        inOrder.verify(buffer).writeInt(0);
        inOrder.verify(buffer).writeInt(100);
        inOrder.verify(buffer).writeInt(105);
        inOrder.verify(buffer).writeInt(6789);
        inOrder.verify(buffer).writeInt(567);
        inOrder.verify(buffer).writeFloat(5.61f);
        inOrder.verify(buffer).writeBytes((byte[]) Mockito.any());
    }

    @Test
    @DisplayName("Verify we decode data in the correct order and with values")
    void decode() {

        //Setup packet
        final PacketLambdaTile packet = new PacketLambdaTile();
        final ChannelHandlerContext ctx = Mockito.mock(ChannelHandlerContext.class);

        // encode to buffer
        final PacketBuffer buffer = Mockito.spy(new PacketBuffer(Unpooled.buffer()));
        buffer.writeInt(2);
        buffer.writeInt(0);
        buffer.writeInt(100);
        buffer.writeInt(105);
        buffer.writeInt(6789);
        buffer.writeInt(567);
        buffer.writeFloat(5.61f);
        ByteBufUtil.writeUtf8(buffer, "tree");

        // run encoding
        packet.decodeInto(ctx, Unpooled.wrappedBuffer(buffer.array()));

        // Validate
        Assertions.assertEquals(2, packet.getCodex().getId());
        Assertions.assertEquals(0, packet.getDimensionId());
        Assertions.assertEquals(100, packet.getPos().getX());
        Assertions.assertEquals(105, packet.getPos().getY());
        Assertions.assertEquals(6789, packet.getPos().getZ());
        Assertions.assertEquals(3, packet.getSetters().size());
    }

    @Test
    @DisplayName("Verify we handle client side")
    void handleClient() {
        //Setup packet
        final PacketLambdaTile<BlockEntityFakeData> packet = new PacketLambdaTile<BlockEntityFakeData>();
        final BlockPos pos = new BlockPos(567, 345, 123);
        packet.setCodex(CODEX);
        packet.setDimensionId(0);
        packet.setPos(pos);
        packet.setSetters(Lists.newArrayList(
            (tile) -> tile.setField1(999),
            (tile) -> tile.setField2(3.57f),
            (tile) -> tile.setField3("cat")
        ));

        final Player player = testManager.getPlayer();

        final Minecraft minecraft = Mockito.mock(Minecraft.class);
        Mockito.when(minecraft.addScheduledTask((Runnable) Mockito.any())).then((answer) -> {
            ((Runnable) answer.getArgument(0)).run();
            return null;
        });

        Mockito.when(world.getBlockEntity(pos)).thenReturn(tile);
        Mockito.doReturn(true).when(world).isBlockLoaded(pos);

        // Invoke
        packet.handleClientSide(minecraft, player);

        // Verification
        Assertions.assertEquals(999, tile.getField1());
        Assertions.assertEquals(3.57f, tile.getField2());
        Assertions.assertEquals("cat", tile.getField3());
        Assertions.assertTrue(tile.wasRead());
    }

    @Test
    @DisplayName("Verify we handle server side")
    void handleServer() {
        //Setup packet
        final PacketLambdaTile<BlockEntityFakeData> packet = new PacketLambdaTile<BlockEntityFakeData>();
        final BlockPos pos = new BlockPos(567, 345, 123);
        packet.setCodex(CODEX);
        packet.setDimensionId(0);
        packet.setPos(pos);
        packet.setSetters(Lists.newArrayList(
            (tile) -> tile.setField1(999),
            (tile) -> tile.setField2(3.57f),
            (tile) -> tile.setField3("cat")
        ));

        final Player player = testManager.getPlayer();

        Mockito.doAnswer((answer) -> {
            ((Runnable) answer.getArgument(0)).run();
            return null;
        }).when(world).addScheduledTask((Runnable) Mockito.any());

        Mockito.when(world.getBlockEntity(pos)).thenReturn(tile);
        Mockito.doReturn(true).when(world).isBlockLoaded(pos);

        // Invoke
        packet.handleServerSide(player);

        // Verification
        Assertions.assertEquals(999, tile.getField1());
        Assertions.assertEquals(3.57f, tile.getField2());
        Assertions.assertEquals("cat", tile.getField3());
        Assertions.assertTrue(tile.wasRead());
    }
}
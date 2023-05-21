package icbm.classic.lib.network.lambda.tile;

import com.builtbroken.mc.testing.junit.TestManager;
import icbm.classic.TileEntityFakeData;
import icbm.classic.content.blocks.launcher.screen.TileLauncherScreen;
import icbm.classic.lib.network.lambda.PacketCodex;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldProvider;
import org.junit.jupiter.api.*;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.*;

class PacketLambdaTileTest {

    static TestManager testManager = new TestManager("packet.tile", Assertions::fail);

    final World world = testManager.getWorld();
    final TileEntity tile = Mockito.spy(TileEntityFakeData.class);

    public static final PacketCodexTile<TileEntityFakeData, TileEntityFakeData> CODEX =
        (PacketCodexTile<TileEntityFakeData, TileEntityFakeData>) new PacketCodexTile<TileEntityFakeData, TileEntityFakeData>(new ResourceLocation("test", "packet"), "target")
            .fromClient()
            .nodeInt(TileEntityFakeData::getField1, TileEntityFakeData::setField1)
            .nodeFloat(TileEntityFakeData::getField2, TileEntityFakeData::setField2)
            .nodeString(TileEntityFakeData::getField3, TileEntityFakeData::setField3)
            .onFinished((r, t, p) -> r.markDirty());
    ;

    @AfterAll
    public static void afterAllTests() {
        testManager.tearDownTest();
    }

    @BeforeEach
    public void beforeEachTest() {
        tile.setPos(new BlockPos(100, 105, 6789));
        tile.setWorld(world);
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
        Assertions.assertEquals(100, packet.getX());
        Assertions.assertEquals(105, packet.getY());
        Assertions.assertEquals(6789, packet.getZ());
        Assertions.assertEquals(3, packet.getWriters().size());
    }

    @Test
    void encode() {
        final PacketLambdaTile packet = new PacketLambdaTile(CODEX, tile, tile);

        final ChannelHandlerContext ctx = Mockito.mock(ChannelHandlerContext.class);
        final PacketBuffer buffer = new PacketBuffer(Unpooled.buffer());

        packet.encodeInto(ctx, buffer);
    }
}
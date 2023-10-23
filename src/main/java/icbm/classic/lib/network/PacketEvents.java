package icbm.classic.lib.network;

import icbm.classic.ICBMClassic;
import icbm.classic.ICBMConstants;
import icbm.classic.lib.network.lambda.PacketCodex;
import icbm.classic.lib.network.lambda.PacketEventUtils;
import icbm.classic.lib.tracker.EventTrackerHelpers;
import icbm.classic.lib.tracker.EventTrackerType;
import net.minecraft.util.ResourceLocation;

public class PacketEvents {

    public static final ResourceLocation EXPECTED_WORLD = new ResourceLocation(ICBMConstants.DOMAIN, "level.id.expected");
    public static final EventTrackerType WRONG_WORLD =
        new EventTrackerType.Builder(new ResourceLocation(ICBMConstants.DOMAIN, "packet.tile.error.handling"))
            .asWarn()
            .with(PacketEventUtils.fieldCodexId(0))
            .with(PacketEventUtils.fieldCodexKey(1))
            .withString(EventTrackerHelpers.WORLD_SIDE, EventTrackerHelpers.stringAt(2))
            .withInt(EventTrackerHelpers.WORLD_ID, EventTrackerHelpers.intAt(3))
            .withInt(EXPECTED_WORLD, EventTrackerHelpers.intAt(4))
            // Console logging
            .listen(new ResourceLocation(ICBMConstants.DOMAIN, "logger"),
                () -> ICBMClassic.logger().isDebugEnabled(),
                (entry) -> {
                    final String ERROR_MSG = "Packet(%s, %s): Received packet on side %s for world(%s) but got world(%s)... ignoring.";
                    final String message = String.format(ERROR_MSG,
                        entry.getString(PacketEventUtils.CODEX_ID, "?"),
                        entry.getString(PacketEventUtils.CODEX_KEY, "?"),
                        entry.getString(EventTrackerHelpers.WORLD_SIDE, "?"),
                        entry.getString(EventTrackerHelpers.WORLD_ID, "?"),
                        entry.getString(EXPECTED_WORLD, "?")
                    );
                    ICBMClassic.logger().debug(message);
                })

            .build();

    public static final EventTrackerType NOT_SERVER_WORLD =
        new EventTrackerType.Builder(new ResourceLocation(ICBMConstants.DOMAIN, "packet.tile.error.handling"))
            .asWarn()
            .with(PacketEventUtils.fieldCodexId(0))
            .with(PacketEventUtils.fieldCodexKey(1))
            .withInt(EventTrackerHelpers.WORLD_ID, EventTrackerHelpers.intAt(2))
            // Console logging
            .listen(new ResourceLocation(ICBMConstants.DOMAIN, "logger"),
                () -> ICBMClassic.logger().isDebugEnabled(),
                (entry) -> {
                    final String ERROR_MSG = "Packet(%s, %s): Received packet server side but world(%s) is not WorldServer";
                    final String message = String.format(ERROR_MSG,
                        entry.getString(PacketEventUtils.CODEX_ID, "?"),
                        entry.getString(PacketEventUtils.CODEX_KEY, "?"),
                        entry.getString(EventTrackerHelpers.WORLD_ID, "?")
                    );
                    ICBMClassic.logger().debug(message);
                })

            .build();

    public static void onWrongWorld(PacketCodex builder, String side, int expected, int received) {
        ICBMClassic.MAIN_TRACKER.post(WRONG_WORLD, () -> new Object[]{
            PacketEventUtils.getPacketId(builder),
            PacketEventUtils.getPacketName(builder),
            side,
            received,
            expected
        });
    }

    public static void onNotServerWorld(PacketCodex builder, int dim) {
        ICBMClassic.MAIN_TRACKER.post(NOT_SERVER_WORLD, () -> new Object[]{
            PacketEventUtils.getPacketId(builder),
            PacketEventUtils.getPacketName(builder),
            dim
        });
    }
}

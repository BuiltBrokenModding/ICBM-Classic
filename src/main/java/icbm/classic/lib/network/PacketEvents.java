package icbm.classic.lib.network;

import icbm.classic.ICBMClassic;
import icbm.classic.ICBMConstants;
import icbm.classic.lib.network.lambda.PacketCodex;
import icbm.classic.lib.network.lambda.PacketEventUtils;
import icbm.classic.lib.tracker.EventTrackerHelpers;
import icbm.classic.lib.tracker.EventTrackerType;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

public class PacketEvents {

    public static final ResourceLocation EXPECTED_WORLD = ResourceLocation.fromNamespaceAndPath(ICBMConstants.DOMAIN, "level.id.expected");
    public static final EventTrackerType WRONG_WORLD =
        new EventTrackerType.Builder(ResourceLocation.fromNamespaceAndPath(ICBMConstants.DOMAIN, "packet.tile.error.handling"))
            .asWarn()
            .with(PacketEventUtils.fieldCodexId(0))
            .with(PacketEventUtils.fieldCodexKey(1))
            .withString(EventTrackerHelpers.WORLD_SIDE, EventTrackerHelpers.stringAt(2))
            .withString(EventTrackerHelpers.WORLD_ID, EventTrackerHelpers.stringAt(3))
            .withString(EXPECTED_WORLD, EventTrackerHelpers.stringAt(4))
            // Console logging
            .listen(ResourceLocation.fromNamespaceAndPath(ICBMConstants.DOMAIN, "logger"),
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
        new EventTrackerType.Builder(ResourceLocation.fromNamespaceAndPath(ICBMConstants.DOMAIN, "packet.tile.error.handling"))
            .asWarn()
            .with(PacketEventUtils.fieldCodexId(0))
            .with(PacketEventUtils.fieldCodexKey(1))
            .withString(EventTrackerHelpers.WORLD_ID, EventTrackerHelpers.stringAt(2))
            // Console logging
            .listen(ResourceLocation.fromNamespaceAndPath(ICBMConstants.DOMAIN, "logger"),
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

    public static void onWrongWorld(PacketCodex builder, String side, ResourceKey<Level> expected, ResourceKey<Level> received) {
        ICBMClassic.MAIN_TRACKER.post(WRONG_WORLD, () -> new Object[]{
            PacketEventUtils.getPacketId(builder),
            PacketEventUtils.getPacketName(builder),
            side,
            String.valueOf(received.location()),
            String.valueOf(expected.location())
        });
    }

    public static void onNotServerWorld(PacketCodex builder, ResourceKey<Level> dim) {
        ICBMClassic.MAIN_TRACKER.post(NOT_SERVER_WORLD, () -> new Object[]{
            PacketEventUtils.getPacketId(builder),
            PacketEventUtils.getPacketName(builder),
            String.valueOf(dim.location())
        });
    }
}

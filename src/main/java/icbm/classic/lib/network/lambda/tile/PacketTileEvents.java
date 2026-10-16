package icbm.classic.lib.network.lambda.tile;

import icbm.classic.ICBMClassic;
import icbm.classic.ICBMConstants;
import icbm.classic.lib.network.lambda.PacketCodex;
import icbm.classic.lib.network.lambda.PacketEventUtils;
import icbm.classic.lib.tracker.EventTrackerHelpers;
import icbm.classic.lib.tracker.EventTrackerType;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

public class PacketTileEvents {

    public static final EventTrackerType ERROR_HANDLING =
        new EventTrackerType.Builder(ResourceLocation.fromNamespaceAndPath(ICBMConstants.DOMAIN, "packet.tile.error.handling"))
            .asError()
            .with(PacketEventUtils.fieldCodexId(0))
            .with(PacketEventUtils.fieldCodexKey(1))
            .withString(EventTrackerHelpers.WORLD_ID, EventTrackerHelpers.stringAt(2))
            .withString(EventTrackerHelpers.WORLD_NAME, EventTrackerHelpers.stringAt(3))
            .withBlockPos(EventTrackerHelpers.TILE_POS, (entry) -> (BlockPos) entry.getData()[4])
            .with(PacketEventUtils.fieldError(5))

            // Console logging
            .listen(ResourceLocation.fromNamespaceAndPath(ICBMConstants.DOMAIN, "logger"), (entry) -> {
                final String ERROR_HANDLING = "Packet(%s, %s): unexpected error writing to tile.\n\tWorld(%s): '%s'\n\tPos: %sx %sy %sz";
                final String message = String.format(ERROR_HANDLING,
                    entry.getString(PacketEventUtils.CODEX_ID, "?"),
                    entry.getString(PacketEventUtils.CODEX_KEY, "?"),
                    entry.getString(EventTrackerHelpers.WORLD_ID, "?"),
                    entry.getString(EventTrackerHelpers.WORLD_NAME, "?"),
                    entry.getString(EventTrackerHelpers.TILE_POS_X, "?"),
                    entry.getString(EventTrackerHelpers.TILE_POS_Y, "?"),
                    entry.getString(EventTrackerHelpers.TILE_POS_Z, "?")
                );
                final Exception error = entry.get(PacketEventUtils.ERROR);
                ICBMClassic.logger().error(message, error);
            })

            .build();

    public static final EventTrackerType INVALID_TILE =
        new EventTrackerType.Builder(ResourceLocation.fromNamespaceAndPath(ICBMConstants.DOMAIN, "packet.tile.error.handling"))
            .asWarn()
            .with(PacketEventUtils.fieldCodexId(0))
            .with(PacketEventUtils.fieldCodexKey(1))
            .withString(EventTrackerHelpers.WORLD_ID, EventTrackerHelpers.stringAt(2))
            .withString(EventTrackerHelpers.WORLD_NAME, EventTrackerHelpers.stringAt(3))
            .withBlockPos(EventTrackerHelpers.TILE_POS, (entry) -> (BlockPos) entry.getData()[4])

            // Console logging
            .listen(ResourceLocation.fromNamespaceAndPath(ICBMConstants.DOMAIN, "logger"),
                () -> ICBMClassic.logger().isDebugEnabled(),
                (entry) -> {
                    final String DEBUG_INVALID_TILE = "Packet(%s, %s): invalid tile for packet.\n\tWorld(%s): '%s'\n\tPos: %sx %sy %sz";
                    final String message = String.format(DEBUG_INVALID_TILE,
                        entry.getString(PacketEventUtils.CODEX_ID, "?"),
                        entry.getString(PacketEventUtils.CODEX_KEY, "?"),
                        entry.getString(EventTrackerHelpers.WORLD_ID, "?"),
                        entry.getString(EventTrackerHelpers.WORLD_NAME, "?"),
                        entry.getString(EventTrackerHelpers.TILE_POS_X, "?"),
                        entry.getString(EventTrackerHelpers.TILE_POS_Y, "?"),
                        entry.getString(EventTrackerHelpers.TILE_POS_Z, "?")
                    );
                    ICBMClassic.logger().debug(message);
                })

            .build();

    public static void onHandlingError(PacketCodex builder, Level world, BlockPos pos, Exception e) {
        ICBMClassic.MAIN_TRACKER.post(ERROR_HANDLING, () -> new Object[]{
            PacketEventUtils.getPacketId(builder),
            PacketEventUtils.getPacketName(builder),
            world != null ? world.dimension().location().toString() : "null",
            world != null ? world.dimension().location().toString() : "null",
            pos,
            e
        });
    }

    public static void onInvalidTile(PacketCodex builder, Level world, BlockPos pos) {
        ICBMClassic.MAIN_TRACKER.post(INVALID_TILE, () -> new Object[]{
            PacketEventUtils.getPacketId(builder),
            PacketEventUtils.getPacketName(builder),
            world != null ? world.dimension().location().toString() : "null",
            world != null ? world.dimension().location().toString() : "null",
            pos
        });
    }
}

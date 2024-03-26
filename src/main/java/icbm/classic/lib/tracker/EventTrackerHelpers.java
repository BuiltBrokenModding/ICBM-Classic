package icbm.classic.lib.tracker;

import icbm.classic.IcbmConstants;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.level.Level;
import net.minecraft.world.storage.WorldInfo;

import java.util.Optional;
import java.util.function.Function;

public class EventTrackerHelpers {
    public static final ResourceLocation TILE_POS = new ResourceLocation(IcbmConstants.MOD_ID, "tile.pos");
    public static final ResourceLocation TILE_POS_X = new ResourceLocation(IcbmConstants.MOD_ID, "tile.pos.x");
    public static final ResourceLocation TILE_POS_Y = new ResourceLocation(IcbmConstants.MOD_ID, "tile.pos.y");
    public static final ResourceLocation TILE_POS_Z = new ResourceLocation(IcbmConstants.MOD_ID, "tile.pos.z");

    public static final ResourceLocation WORLD_ID = new ResourceLocation(IcbmConstants.MOD_ID, "level.id");
    public static final ResourceLocation WORLD_NAME = new ResourceLocation(IcbmConstants.MOD_ID, "level.name");
    public static final ResourceLocation WORLD_SIDE = new ResourceLocation(IcbmConstants.MOD_ID, "level.side");

    public static String SIDE_CLIENT = "client";
    public static String SIDE_SERVER = "server";

    public static Function<EventTrackerEntry, String> stringAt(int index) {
        return (entry) -> (String) entry.getData()[index];
    }

    public static Function<EventTrackerEntry, Integer> intAt(int index) {
        return (entry) -> (Integer) entry.getData()[index];
    }

    public static Integer getLevelId(Level level) {
        return Optional.ofNullable(world).map(w -> w.provider).map(WorldProvider::getDimension).orElse(null);
    }

    public static String getLevelName(Level level) {
        return Optional.ofNullable(world).map(World::getLevelInfo).map(WorldInfo::getLevelName).orElse(null);
    }

    public static String getSide(Level level) {
        return Optional.ofNullable(world).map((w) -> w.isClientSide() ? SIDE_CLIENT : SIDE_SERVER).orElse(null);
    }
}

package icbm.classic.lib.tracker;

import icbm.classic.ICBMConstants;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.storage.WorldInfo;

import java.util.Optional;
import java.util.function.Function;

public class EventTrackerHelpers {
    public static final ResourceLocation TILE_POS = new ResourceLocation(ICBMConstants.DOMAIN, "tile.pos");
    public static final ResourceLocation TILE_POS_X = new ResourceLocation(ICBMConstants.DOMAIN, "tile.pos.x");
    public static final ResourceLocation TILE_POS_Y = new ResourceLocation(ICBMConstants.DOMAIN, "tile.pos.y");
    public static final ResourceLocation TILE_POS_Z = new ResourceLocation(ICBMConstants.DOMAIN, "tile.pos.z");

    public static final ResourceLocation WORLD_ID = new ResourceLocation(ICBMConstants.DOMAIN, "level.id");
    public static final ResourceLocation WORLD_NAME = new ResourceLocation(ICBMConstants.DOMAIN, "level.name");
    public static final ResourceLocation WORLD_SIDE = new ResourceLocation(ICBMConstants.DOMAIN, "level.side");

    public static String SIDE_CLIENT = "client";
    public static String SIDE_SERVER = "server";

    public static Function<EventTrackerEntry, String> stringAt(int index) {
        return (entry) -> (String) entry.getData()[index];
    }

    public static Function<EventTrackerEntry, Integer> intAt(int index) {
        return (entry) -> (Integer) entry.getData()[index];
    }

    public static Integer getWorldId(World world) {
        return Optional.ofNullable(world).map(w -> w.provider).map(WorldProvider::getDimension).orElse(null);
    }

    public static String getWorldName(World world) {
        return Optional.ofNullable(world).map(World::getWorldInfo).map(WorldInfo::getWorldName).orElse(null);
    }

    public static String getSide(World world) {
        return Optional.ofNullable(world).map((w) -> w.isRemote ? SIDE_CLIENT : SIDE_SERVER).orElse(null);
    }
}

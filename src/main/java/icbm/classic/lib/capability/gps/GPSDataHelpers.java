package icbm.classic.lib.capability.gps;

import icbm.classic.api.caps.IGPSData;
import icbm.classic.lib.LanguageUtility;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.storage.WorldInfo;

import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class GPSDataHelpers {

    public static boolean handlePlayerInteraction(IGPSData gpsData, Player player, BiConsumer<World, Vec3> setter) {
        return handlePlayerInteraction(gpsData, player, true, setter);
    }

    public static boolean handlePlayerInteraction(IGPSData gpsData, Player player, Consumer<Vec3> setter) {
        return handlePlayerInteraction(gpsData, player, false, (w, v) -> setter.accept(v));
    }

    public static boolean handlePlayerInteraction(IGPSData gpsData, Player player, boolean setWorld, BiConsumer<World, Vec3> setter) {
        if (gpsData == null) {
            return false;
        }

        final Vec3 position = gpsData.getPosition();
        final Level level = gpsData.getLevel();
        if (position == null) {
            player.sendMessage(new TextComponentString(LanguageUtility.getLocal("chat.launcher.noTargetInTool")));
        } else if (setLevel && world != null) {
            setter.accept(world, position);

            final String x = String.format("%.1f", position.x);
            final String y = String.format("%.1f", position.y);
            final String z = String.format("%.1f", position.z);

            final String name = Optional.of(world.getLevelInfo()).map(WorldInfo::getLevelName).orElse("--");
            final String worldName = String.format("(%s)%s", world.provider.getDimension(), name);

            player.sendMessage(new TextComponentTranslation("info.icbmclassic:gps.set.all", x, y, z, worldName));
        } else {
            setter.accept(null, gpsData.getPosition());

            final String x = String.format("%.1f", position.x);
            final String y = String.format("%.1f", position.y);
            final String z = String.format("%.1f", position.z);
            player.sendMessage(new TextComponentTranslation("info.icbmclassic:gps.set.pos", x, y, z));
        }
        return true;
    }
}

package icbm.classic.lib.capability.gps;

import icbm.classic.api.caps.IGPSData;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ServerLevelData;
import net.minecraft.world.phys.Vec3;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class GPSDataHelpers {

    public static boolean handlePlayerInteraction(IGPSData gpsData, Player player, BiConsumer<Level, Vec3> setter) {
        return handlePlayerInteraction(gpsData, player, true, setter);
    }

    public static boolean handlePlayerInteraction(IGPSData gpsData, Player player, Consumer<Vec3> setter) {
        return handlePlayerInteraction(gpsData, player, false, (w, v) -> setter.accept(v));
    }

    public static boolean handlePlayerInteraction(IGPSData gpsData, Player player, boolean setWorld, BiConsumer<Level, Vec3> setter) {
        if (gpsData == null)
        {
            return false;
        }

        final Vec3 position = gpsData.getPosition();
        final Level world = gpsData.getWorld();
        if(position == null) {
            player.sendSystemMessage(Component.translatable("chat.launcher.noTargetInTool"));
        }
        else if(setWorld && world != null) {
            setter.accept(world, position);

            final String x = String.format("%.1f", position.x);
            final String y = String.format("%.1f", position.y);
            final String z = String.format("%.1f", position.z);

            // PORT: 1.12 showed dimension int and WorldInfo#getWorldName
            final String name = world.getLevelData() instanceof ServerLevelData data ? data.getLevelName() : "--";
            final String worldName = String.format("(%s)%s", world.dimension().location(), name);

            player.sendSystemMessage(Component.translatable("info.icbmclassic:gps.set.all", x, y, z, worldName));
        }
        else {
            setter.accept(null, gpsData.getPosition());

            final String x = String.format("%.1f", position.x);
            final String y = String.format("%.1f", position.y);
            final String z = String.format("%.1f", position.z);
            player.sendSystemMessage(Component.translatable("info.icbmclassic:gps.set.pos", x, y, z));
        }
        return true;
    }
}
// PORTED

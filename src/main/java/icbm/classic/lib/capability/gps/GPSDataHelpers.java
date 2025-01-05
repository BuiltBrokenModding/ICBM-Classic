package icbm.classic.lib.capability.gps;

import icbm.classic.api.caps.IGPSData;
import icbm.classic.lib.LanguageUtility;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraft.world.storage.WorldInfo;

import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class GPSDataHelpers {


    public static boolean handlePlayerInteraction(IGPSData gpsData, PlayerEntity player, Consumer<Vec3d> setter) {
        return handlePlayerInteraction(gpsData, player, false, (w, v) -> setter.accept(v));
    }

    public static boolean handlePlayerInteraction(IGPSData gpsData, PlayerEntity player, boolean setWorld, BiConsumer<ResourceLocation, Vec3d> setter) {
        if (gpsData == null)
        {
            return false;
        }

        final Vec3d position = gpsData.getPosition();
        final ResourceLocation dimensionKey = gpsData.getDimensionKey();
        if(position == null) {
            player.sendMessage(new StringTextComponent(LanguageUtility.getLocal("chat.launcher.noTargetInTool")));
        }
        else if(setWorld && dimensionKey != null) {
            setter.accept(dimensionKey, position );

            final String x = String.format("%.1f", position .x);
            final String y = String.format("%.1f", position .y);
            final String z = String.format("%.1f", position .z);

            player.sendMessage(new TranslationTextComponent("info.icbmclassic:gps.set.all", x, y, z, dimensionKey)); //TODO get world name
        }
        else {
            setter.accept(null, gpsData.getPosition());

            final String x = String.format("%.1f", position .x);
            final String y = String.format("%.1f", position .y);
            final String z = String.format("%.1f", position .z);
            player.sendMessage(new TranslationTextComponent("info.icbmclassic:gps.set.pos", x, y, z));
        }
        return true;
    }
}

package icbm.classic.lib.network.lambda;

import icbm.classic.ICBMConstants;
import icbm.classic.lib.tracker.EventTrackerField;
import net.minecraft.resources.ResourceLocation;

public class PacketEventUtils {

    public static final ResourceLocation CODEX_KEY = ResourceLocation.fromNamespaceAndPath(ICBMConstants.DOMAIN, "packet.codex.key");
    public static final ResourceLocation CODEX_ID = ResourceLocation.fromNamespaceAndPath(ICBMConstants.DOMAIN, "packet.codex.id");
    public static final ResourceLocation ERROR = ResourceLocation.fromNamespaceAndPath(ICBMConstants.DOMAIN, "packet.error");

    public static EventTrackerField<String> fieldCodexKey(int index) {
        return new EventTrackerField<>(CODEX_KEY, String.class, (entry) -> (String)entry.getData()[index]);
    }

    public static EventTrackerField<Integer> fieldCodexId(int index) {
        return new EventTrackerField<>(CODEX_ID, Integer.class, (entry) -> (Integer)entry.getData()[index]);
    }

    public static EventTrackerField<Exception> fieldError(int index) {
        return new EventTrackerField<>(ERROR, Exception.class, (entry) -> (Exception) entry.getData()[index]);
    }

    public static ResourceLocation getPacketName(PacketCodex builder) {
        if(builder != null) {
            return builder.getName();
        }
        return null;
    }

    public static Integer getPacketId(PacketCodex builder) {
        if(builder != null) {
            return builder.getId();
        }
        return null;
    }
}

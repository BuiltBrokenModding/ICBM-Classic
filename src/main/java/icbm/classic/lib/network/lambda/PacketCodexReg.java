package icbm.classic.lib.network.lambda;

import java.util.HashMap;
import java.util.Map;

public class PacketCodexReg {
    private static Map<Integer, PacketCodex> builders = new HashMap();

    private static int nextId = 0;


    public static void register(PacketCodex... builder) {
        for(PacketCodex packetCodex : builder) {
            register(packetCodex);
        }
    }
    public static void register(PacketCodex builder) {
        final int id = nextId++;
        builder.setId(id);
        builders.put(id, builder);
    }

    public static PacketCodex get(int id) {
        return builders.get(id);
    }
}

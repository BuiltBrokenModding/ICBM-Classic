package icbm.classic.lib.data;

import java.util.Map;
import java.util.function.BiConsumer;

public interface IMachineInfo {

    /**
     * Provide information about the machine, settings related to the machine,
     * and useful facts. This will be used in mods like ComputerCraft to
     * query to get dynamic information. Allowing scripts to easily reflect
     * differences between servers, mod versions, and customizations.
     *
     * @param consumer to invoke to add key:value pairs, values supported are Double, Integer, String, and Map. Anything
     * not supported may show as NULL in other systems to users.
     */
    void provideInformation(BiConsumer<String, Object> consumer);
}

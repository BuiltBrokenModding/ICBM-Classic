package icbm.classic.lib;

import net.minecraft.client.Minecraft;
import net.minecraft.core.RegistryAccess;

/** Client side half of {@link RegistryHelper}, only class loaded on the physical client */
final class ClientRegistryHelper
{
    private ClientRegistryHelper()
    {
    }

    static RegistryAccess access()
    {
        final Minecraft mc = Minecraft.getInstance();
        if (mc.getConnection() != null)
        {
            return mc.getConnection().registryAccess();
        }
        return mc.level != null ? mc.level.registryAccess() : null;
    }

    /** @return client registries if called from the client (render) thread, null on any other thread */
    static RegistryAccess accessIfClientThread()
    {
        final Minecraft mc = Minecraft.getInstance();
        return mc != null && mc.isSameThread() ? access() : null;
    }
}
// PORTED

package icbm.classic.content.blast;

import icbm.classic.ICBMConstants;
import icbm.classic.lib.actions.status.ImmutableStatus;
import net.minecraft.resources.ResourceLocation;

@Deprecated
public class BlastStatus {
    public static ImmutableStatus SETUP_ERROR = ImmutableStatus.error(ResourceLocation.fromNamespaceAndPath(ICBMConstants.DOMAIN, "blast.setup.error"));
    public static ImmutableStatus TRIGGERED_THREADING = ImmutableStatus.blocking(ResourceLocation.fromNamespaceAndPath(ICBMConstants.DOMAIN, "blast.threading"));
}
// PORTED

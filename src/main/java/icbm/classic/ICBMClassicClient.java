package icbm.classic;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

/**
 * Client only mod entry point, FML never constructs it on a dedicated server.
 * <p>
 * PORT: Forge 1.12 gave every mod using the {@code @Config} annotations a "Config" button and GUI in the mods list.
 * NeoForge 21.1 only does this when the mod registers {@link IConfigScreenFactory}, so the generic
 * {@link ConfigurationScreen} is registered here for all ICBM config files.
 */
@Mod(value = ICBMConstants.DOMAIN, dist = Dist.CLIENT)
public class ICBMClassicClient
{
    public ICBMClassicClient(ModContainer container)
    {
        container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
    }
}

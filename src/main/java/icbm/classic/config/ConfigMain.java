package icbm.classic.config;

import icbm.classic.ICBMConstants;
import icbm.classic.config.machines.ConfigSpikes;
import icbm.classic.content.cargo.CargoHolderHandler;
import icbm.classic.content.cluster.missile.ClusterMissileHandler;
import icbm.classic.content.entity.flyingblock.FlyingBlock;
import icbm.classic.content.gas.ProtectiveArmorHandler;
import icbm.classic.content.radioactive.RadioactiveHandler;
import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.ConfigManager;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/**
 * Settings class for various configuration settings.
 *
 * @author Calclavia, DarkCow
 */
@Config(modid = ICBMConstants.DOMAIN, name = "icbmclassic/main")
@Config.LangKey("config.icbmclassic:main.title")
@Mod.EventBusSubscriber(modid = ICBMConstants.DOMAIN)
public class ConfigMain
{
    @Config.Name("use_energy")
    @Config.Comment("Range of tier 1 launcher")
    public static boolean REQUIRES_POWER = true;

    @Config.Name("spike_blocks")
    @Config.Comment("Config for spike blocks")
    public static ConfigSpikes spikes = new ConfigSpikes();

    @Config.Name("protective_armor")
    @Config.Comment("Settings for setting protection support on armor items")
    public static ProtectiveArmorConfig protectiveArmor = new ProtectiveArmorConfig();

    public static class ProtectiveArmorConfig {

        @Config.Name("min_chemical_gas_protection")
        @Config.Comment("Minimal percentage of protection required to start guarding from chemical attacks")
        @Config.RangeDouble(min = 0, max = 1)
        public float minProtectionChemicalGas = 0.5f;

        @Config.Name("min_viral_gas_protection")
        @Config.Comment("Minimal percentage of protection required to start guarding from viral attacks")
        @Config.RangeDouble(min = 0, max = 1)
        public float minProtectionViralGas = 0.8f;

        @Config.Name("min_radiation_protection")
        @Config.Comment("Minimal percentage of protection required to start guarding from radiation attacks")
        @Config.RangeDouble(min = 0, max = 1)
        public float minProtectionRadiation = 1f;

        @Config.Name("min_debilitation_gas_protection")
        @Config.Comment("Minimal percentage of protection required to start guarding from debilitation attacks")
        @Config.RangeDouble(min = 0, max = 1)
        public float minProtectionDebilitationGas = 0.1f;

        @Config.Name("require_helmet")
        @Config.Comment("Is head slot required to count as protection")
        public boolean requireHelmet = true;

        //TODO once on latest MC rethinking gas protection to be per weapon system.
        @Config.Name("item_ratings")
        @Config.Comment("Item/ItemStack to percentage protection between 0.0 to 1.0 'domain:resource=floating_point', ex: 'minecraft:iron_helmet=0.02'")
        public String[] ITEMS = new String[]{"example:mask=0.95"};
    }

    @SubscribeEvent
    public static void onConfigChangedEvent(final ConfigChangedEvent.OnConfigChangedEvent event)
    {
        if (event.getModID().equals(ICBMConstants.DOMAIN))
        {
            ConfigManager.sync(ICBMConstants.DOMAIN, Config.Type.INSTANCE);

            // Reload config so we can convert to easier to hash lists
            FlyingBlock.loadFromConfig();
            ClusterMissileHandler.loadFromConfig();
            CargoHolderHandler.loadFromConfig();
            RadioactiveHandler.loadFromConfig();
            ProtectiveArmorHandler.loadFromConfig();
        }
    }
}

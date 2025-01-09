package icbm.classic.config.machines;

//@Config(modid = ICBMConstants.DOMAIN, name = "icbmclassic/radar")
//@Config.LangKey("config.icbmclassic:radar.title")
public class ConfigRadar
{
    //@Config.Name("max_range")
    //@Config.Comment("Max range of radar in blocks (meters) to detect entities")
    public static int MAX_RANGE = 500;

    //@Config.Name("power_capacity")
    //@Config.Comment("Size of the energy buffer")
    public static int POWER_CAPACITY = 20_000;

    //@Config.Name("power_cost")
    //@Config.Comment("Energy consumed per tick")
    public static int POWER_COST = 1_000;

    //@Config.Name("scan_rate")
    //@Config.Comment("Delay in ticks before scanning for entities")
    public static int SCAN_TICKS = 1;

    //@Config.Name("sam_delay")
    //@Config.Comment("Delay in ticks before triggering more SAM launches")
    public static int SAM_TICKS = 40;
}

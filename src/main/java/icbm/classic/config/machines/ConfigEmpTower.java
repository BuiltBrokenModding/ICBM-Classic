package icbm.classic.config.machines;

import icbm.classic.ICBMConstants;
import net.minecraftforge.common.config.Config;

/**
 * Settings class for various configuration settings.
 *
 * @author Calclavia, DarkCow
 */
@Config(modid = ICBMConstants.DOMAIN, name = "icbmclassic/emp_tower")
@Config.LangKey("config.icbmclassic:emp_tower.title")
public class ConfigEmpTower {
    @Config.Name("extender_range_bonus")
    @Config.Comment("Bonus max range (meters) given for each extender added above the base")
    public static int BONUS_RADIUS = 20;

    @Config.Name("firing_cost_area")
    @Config.Comment("Scaling energy cost (FE, ForgeEnergy) per meter^2 covered. Math: (range * range) * cost")
    public static int ENERGY_COST_AREA = 100;

    @Config.Name("energy_upkeep")
    @Config.Comment("Scaling energy cost (FE, ForgeEnergy) to keep the emp tower charged. Math: range * cost")
    public static int ENERGY_COST_TICKING = 10;

    @Config.Name("energy_upkeep_capacity")
    @Config.Comment("Ticks (20 ticks a second) of energy to store to cover ticking cost")
    public static int ENERGY_COST_TICKING_CAP = 100;

    @Config.Name("firing_cooling_ticks")
    @Config.Comment("Time in ticks (20 ticks a second) to wait before recharging, after firing.")
    public static int COOLDOWN = 10 * 20;

    @Config.Name("energy_receive_limit")
    @Config.Comment("Input energy limit (FE, ForgeEnergy) per energy received action (can receive several actions per tick).")
    public static int ENERGY_INPUT = 1000;

    @Config.Name("base_max_range")
    @Config.Comment("Max user configurable range of base emp tower (meters)")
    public static int MAX_BASE_RANGE = 10000;
}

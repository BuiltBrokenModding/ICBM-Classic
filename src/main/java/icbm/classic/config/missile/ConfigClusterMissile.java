package icbm.classic.config.missile;

import net.minecraftforge.common.config.Config;

/**
 * Config for Surface to Air missiles
 */
public class ConfigClusterMissile
{
    @Config.Name("health")
    @Config.Comment("Amount of damage a missile can take from any source before death")
    @Config.RangeDouble(min = 0.0001, max = 10)
    public float MAX_HEALTH = 100;

    @Config.Name("item_ban_allow")
    @Config.Comment("Items allowed for insertion into cluster missile as a projectile payload")
    public BanList BAN_ALLOW = new BanList();

    @Config.Name("item_sizes")
    @Config.Comment("Items volume size for insertion into cluster missiles")
    public ItemSizes ITEM_SIZES = new ItemSizes();

    public static class ItemSizes {

        @Config.Name("max_size")
        @Config.Comment("Volume of the cluster missile, most items default to size of 1. Keep it small to avoid lag")
        @Config.RangeInt(min = 1)
        public int MAX_SIZE = 200;

        @Config.Name("default_size")
        @Config.Comment("Default volume to use for items if no configuration exists")
        @Config.RangeInt(min = 1)
        public int DEFAULT_SIZE = 1;

        @Config.Name("list")
        @Config.Comment("Item/ItemStack sizes 'domain:resource=whole_number', ex: 'minecraft:stone=4'")
        public String[] ITEMS = new String[]{};
    }

    public static class BanList {

        @Config.Name("ban")
        @Config.Comment("Set to true to ban all blocks contained. False to use as allow list")
        public boolean BAN = true;

        @Config.Name("list")
        @Config.Comment("Item/ItemStack names 'domain:resource=ignore' set 'ignore' to true to skip the entry during loading; ex: 'minecraft:stone' will be used while 'minecraft:fire=true' will be ignored")
        public String[] ITEMS = new String[]{};
    }

}

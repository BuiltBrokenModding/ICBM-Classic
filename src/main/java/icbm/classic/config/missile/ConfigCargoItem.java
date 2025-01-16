package icbm.classic.config.missile;



/**
 * Config for any item that deploys an entity that acts as a holder of cargo (balloon, parachute)
 */
public class ConfigCargoItem
{
    //@Config.Name("item_ban_allow")
    //@Config.Comment("Items allowed for insertion into cluster missile as a projectile payload")
    public BanList BAN_ALLOW = new BanList();

    public static class BanList {

        //@Config.Name("ban")
        //@Config.Comment("Set to true to ban all blocks contained. False to use as allow list")
        public boolean BAN = true;

        //@Config.Name("list")
        //@Config.Comment("Item/ItemStack names 'domain:resource=ignore' set 'ignore' to true to skip the entry during loading; ex: 'minecraft:stone' will be used while 'minecraft:fire=true' will be ignored")
        public String[] ITEMS = new String[]{};
    }
}

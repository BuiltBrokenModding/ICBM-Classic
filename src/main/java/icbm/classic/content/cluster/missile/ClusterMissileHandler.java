package icbm.classic.content.cluster.missile;

import icbm.classic.api.ICBMClassicAPI;
import icbm.classic.api.reg.IExplosiveData;
import icbm.classic.config.missile.ConfigMissile;
import icbm.classic.config.util.ItemStackConfigList;
import icbm.classic.content.reg.ItemReg;
import icbm.classic.lib.explosive.ExplosiveStacks;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import net.minecraft.world.item.ItemStack;

@NoArgsConstructor(access = AccessLevel.NONE)
public final class ClusterMissileHandler {

    public static final ItemStackConfigList.ContainsCheck banAllowItems = new ItemStackConfigList.ContainsCheck("[Cluster Contents][Ban/Allow Config]", (configList) -> {
        configList.load("icbmclassic/missile/cluster/item_ban_allow/list", ConfigMissile.CLUSTER_MISSILE.BAN_ALLOW.ITEMS);
    });

    public static final ItemStackConfigList.IntOut itemSizes = new ItemStackConfigList.IntOut("[Cluster Contents][Item Sizes]", (configList) -> {

        configList.setDefault(ItemReg.itemBalloon.getId(), 2, 0);
        configList.setDefault(ItemReg.itemParachute.getId(), 2, 0);

        configList.setDefault(ItemReg.itemClusterMissile.getId(), 20, 0);
        configList.setDefault(ItemReg.itemExplosiveMissile.getId(), 20, 0);
        configList.setDefault(ItemReg.itemSAM.getId(), 10, 0);
        configList.setDefault(ItemReg.heldItemMissile.getId(), 10, 0);

        configList.setDefault(ItemReg.itemBombletExplosive.getId(), 2, 0);
        for(IExplosiveData data : ICBMClassicAPI.EXPLOSIVE_REGISTRY.getExplosives()) {
            int size = 2;
            switch(data.getTier()) {
                case TWO: size = 5; break;
                case THREE: size = 10; break;
                case FOUR: size = 20; break;
            }
            configList.setDefaultMeta(ExplosiveStacks.create(ItemReg.itemBombletExplosive.get(), data), size, 1);
        }


        configList.load("icbmclassic/missile/cluster/item_ban_allow/list/item_sizes", ConfigMissile.CLUSTER_MISSILE.ITEM_SIZES.ITEMS);
    });

    public static void setup() {
        loadFromConfig();
    }

    public static int sizeOf(ItemStack itemStack) {
        final Integer size = itemSizes.getValue(itemStack);
        return size != null ? size : ConfigMissile.CLUSTER_MISSILE.ITEM_SIZES.DEFAULT_SIZE;
    }

    public static boolean isAllowed(ItemStack itemStack) {
        return banAllowItems.isAllowed(itemStack, ConfigMissile.CLUSTER_MISSILE.BAN_ALLOW.BAN);
    }

    public static void loadFromConfig() {
        banAllowItems.reload();
        itemSizes.reload();
    }
}
// PORTED

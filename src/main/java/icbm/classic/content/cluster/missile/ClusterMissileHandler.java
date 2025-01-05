package icbm.classic.content.cluster.missile;

import icbm.classic.ICBMClassic;
import icbm.classic.api.ICBMClassicAPI;
import icbm.classic.api.reg.IExplosiveData;
import icbm.classic.config.missile.ConfigMissile;
import icbm.classic.config.util.ItemStackConfigList;
import icbm.classic.content.blocks.explosive.ItemBlockExplosive;
import icbm.classic.content.reg.BlockReg;
import icbm.classic.content.reg.ItemReg;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.RegistryObject;

@NoArgsConstructor(access = AccessLevel.NONE)
public final class ClusterMissileHandler {

    public static final ItemStackConfigList.ContainsCheck banAllowItems = new ItemStackConfigList.ContainsCheck("[Cluster Contents][Ban/Allow Config]", (configList) -> {
        configList.load("icbmclassic/missile/cluster/item_ban_allow/list", ConfigMissile.CLUSTER_MISSILE.BAN_ALLOW.ITEMS);
    });

    public static final ItemStackConfigList.IntOut itemSizes = new ItemStackConfigList.IntOut("[Cluster Contents][Item Sizes]", (configList) -> {

        configList.setDefault(ItemReg.BALLON.getId(), 2, 0);
        configList.setDefault(ItemReg.PARACHUTE.getId(), 2, 0);

        configList.setDefault(ItemReg.MISSILE_CLUSTER.getId(), 20, 0);
        configList.setDefault(ItemReg.MISSILE_SURFACE_TO_AIR.getId(), 10, 0);

        configList.setDefault(ItemReg.EXPLOSIVE_CONDENSED.getId(), 10, 0);
        configList.setDefault(ItemReg.EXPLOSIVE_SHRAPNEL.getId(), 10, 0);
        configList.setDefault(ItemReg.EXPLOSIVE_INCENDIARY.getId(), 10, 0);
        configList.setDefault(ItemReg.EXPLOSIVE_DEBILITATION.getId(), 10, 0);
        configList.setDefault(ItemReg.EXPLOSIVE_CHEMICAL.getId(), 10, 0);
        configList.setDefault(ItemReg.EXPLOSIVE_ANVIL.getId(), 10, 0);
        configList.setDefault(ItemReg.EXPLOSIVE_REPULSIVE.getId(), 10, 0);
        configList.setDefault(ItemReg.EXPLOSIVE_ATTRACTIVE.getId(), 10, 0);
        configList.setDefault(ItemReg.EXPLOSIVE_COLOR.getId(), 10, 0);
        configList.setDefault(ItemReg.EXPLOSIVE_SMOKE.getId(), 10, 0);

        configList.setDefault(ItemReg.EXPLOSIVE_FRAGMENTATION.getId(), 20, 0);
        configList.setDefault(ItemReg.EXPLOSIVE_CONTAGIOUS.getId(), 20, 0);
        configList.setDefault(ItemReg.EXPLOSIVE_SONIC.getId(), 20, 0);
        configList.setDefault(ItemReg.EXPLOSIVE_BREACHING.getId(), 20, 0);
        configList.setDefault(ItemReg.EXPLOSIVE_THERMOBARIC .getId(), 20, 0);

        configList.setDefault(ItemReg.EXPLOSIVE_NUCLEAR.getId(), 30, 0);
        configList.setDefault(ItemReg.EXPLOSIVE_EMP.getId(), 30, 0);
        configList.setDefault(ItemReg.EXPLOSIVE_EXOTHERMIC.getId(), 30, 0);
        configList.setDefault(ItemReg.EXPLOSIVE_ENDOTHERMIC.getId(), 30, 0);
        configList.setDefault(ItemReg.EXPLOSIVE_GRAVITY.getId(), 30, 0);
        configList.setDefault(ItemReg.EXPLOSIVE_ENDER.getId(), 30, 0);

        configList.setDefault(ItemReg.EXPLOSIVE_ANTIMATTER.getId(), 50, 0);
        configList.setDefault(ItemReg.EXPLOSIVE_REDMATTER.getId(), 50, 0);

        configList.setDefault(ItemReg.BOMBLET_CONDENSED.getId(), 2, 0);


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
        return ConfigMissile.CLUSTER_MISSILE.BAN_ALLOW.BAN == banAllowItems.isAllowed(itemStack);
    }

    public static void loadFromConfig() {
        banAllowItems.reload();
        itemSizes.reload();
    }
}

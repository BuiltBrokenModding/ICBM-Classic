package icbm.classic.content.missile.entity.itemstack.item;

import icbm.classic.config.missile.ConfigMissile;
import icbm.classic.config.util.ItemStackConfigList;
import icbm.classic.content.reg.ItemReg;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import net.minecraft.item.ItemStack;

@NoArgsConstructor(access = AccessLevel.NONE)
public final class HeldItemMissileHandler {
    public static final ItemStackConfigList.ContainsCheck banAllowItems = new ItemStackConfigList.ContainsCheck("[HeldItem][Ban/Allow Config]", (configList) -> {
        configList.setDefault(ItemReg.itemBalloon.getRegistryName(), false, 0);
        configList.setDefault(ItemReg.itemParachute.getRegistryName(), false, 0);

        configList.setDefault(ItemReg.itemClusterMissile.getRegistryName(), false, 0);
        configList.setDefault(ItemReg.itemExplosiveMissile.getRegistryName(), false, 0);
        configList.setDefault(ItemReg.itemSAM.getRegistryName(), false, 0);

        configList.load("icbmclassic/missile/held_item/item_ban_allow/list", ConfigMissile.HELD_ITEM_MISSILE.BAN_ALLOW.ITEMS);
    });

    public static void setup() {
        loadFromConfig();
    }

    public static boolean isAllowed(ItemStack itemStack) {
        return banAllowItems.isAllowed(itemStack, ConfigMissile.HELD_ITEM_MISSILE.BAN_ALLOW.BAN);
    }

    public static void loadFromConfig() {
        banAllowItems.reload();
    }
}

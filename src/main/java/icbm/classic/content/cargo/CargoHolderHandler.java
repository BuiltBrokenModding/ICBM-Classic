package icbm.classic.content.cargo;

import icbm.classic.config.missile.ConfigMissile;
import icbm.classic.config.util.ItemStackConfigList;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import net.minecraft.item.ItemStack;

@NoArgsConstructor(access = AccessLevel.NONE)
public final class CargoHolderHandler {
    public static final ItemStackConfigList.ContainsCheck banAllowItems = new ItemStackConfigList.ContainsCheck("[CargoHolder][Ban/Allow Config]", (configList) -> {
        configList.load("icbmclassic/missile/cargo_holder/item_ban_allow/list", ConfigMissile.CARGO_HOLDERS.BAN_ALLOW.ITEMS);
    });

    public static void setup() {
        loadFromConfig();
    }

    public static boolean isAllowed(ItemStack itemStack) {
        return banAllowItems.isAllowed(itemStack, ConfigMissile.CARGO_HOLDERS.BAN_ALLOW.BAN);
    }

    public static void loadFromConfig() {
        banAllowItems.reload();
    }
}

package icbm.classic.content.missile.entity.itemstack.item;

import icbm.classic.ICBMClassic;
import icbm.classic.config.missile.ConfigMissile;
import icbm.classic.config.util.ItemStackConfigList;
import icbm.classic.content.items.ItemMissile;
import icbm.classic.content.reg.EntityReg;
import icbm.classic.content.reg.ItemReg;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.RegistryObject;

@NoArgsConstructor(access = AccessLevel.NONE)
public final class HeldItemMissileHandler {
    public static final ItemStackConfigList.ContainsCheck banAllowItems = new ItemStackConfigList.ContainsCheck("[HeldItem][Ban/Allow Config]", (configList) -> {
        configList.setDefault(ItemReg.BALLON.getId(), false, 0);
        configList.setDefault(ItemReg.PARACHUTE.getId(), false, 0);

        configList.setDefault(ItemReg.MISSILE_CLUSTER.getId(), false, 0);
        configList.setDefault(ItemReg.MISSILE_HELD_ITEM.getId(), false, 0);
        configList.setDefault(ItemReg.MISSILE_SURFACE_TO_AIR.getId(), false, 0);

        configList.setDefault(ItemReg.MISSILE_CONDENSED.getId(), false, 0);
        configList.setDefault(ItemReg.MISSILE_SHRAPNEL.getId(), false, 0);
        configList.setDefault(ItemReg.MISSILE_INCENDIARY.getId(), false, 0);
        configList.setDefault(ItemReg.MISSILE_DEBILITATION.getId(), false, 0);
        configList.setDefault(ItemReg.MISSILE_CHEMICAL.getId(), false, 0);
        configList.setDefault(ItemReg.MISSILE_ANVIL.getId(), false, 0);
        configList.setDefault(ItemReg.MISSILE_REPULSIVE.getId(), false, 0);
        configList.setDefault(ItemReg.MISSILE_ATTRACTIVE.getId(), false, 0);
        configList.setDefault(ItemReg.MISSILE_COLOR.getId(), false, 0);
        configList.setDefault(ItemReg.MISSILE_SMOKE.getId(), false, 0);
        configList.setDefault(ItemReg.MISSILE_FRAGMENTATION.getId(), false, 0);
        configList.setDefault(ItemReg.MISSILE_CONTAGIOUS.getId(), false, 0);
        configList.setDefault(ItemReg.MISSILE_SONIC.getId(), false, 0);
        configList.setDefault(ItemReg.MISSILE_BREACHING.getId(), false, 0);
        configList.setDefault(ItemReg.MISSILE_THERMOBARIC.getId(), false, 0);
        configList.setDefault(ItemReg.MISSILE_NUCLEAR.getId(), false, 0);
        configList.setDefault(ItemReg.MISSILE_EMP.getId(), false, 0);
        configList.setDefault(ItemReg.MISSILE_EXOTHERMIC.getId(), false, 0);
        configList.setDefault(ItemReg.MISSILE_ENDOTHERMIC.getId(), false, 0);
        configList.setDefault(ItemReg.MISSILE_GRAVITY.getId(), false, 0);
        configList.setDefault(ItemReg.MISSILE_ENDER.getId(), false, 0);
        configList.setDefault(ItemReg.MISSILE_ANTIMATTER.getId(), false, 0);
        configList.setDefault(ItemReg.MISSILE_REDMATTER.getId(), false, 0);


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

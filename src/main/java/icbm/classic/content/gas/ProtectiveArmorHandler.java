package icbm.classic.content.gas;

import icbm.classic.config.ConfigMain;
import icbm.classic.config.util.ItemStackConfigList;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.resources.ResourceLocation;

@NoArgsConstructor(access = AccessLevel.NONE)
public class ProtectiveArmorHandler {

    public static final ItemStackConfigList.FloatOut protectionValues = new ItemStackConfigList.FloatOut("[CargoHolder][Ban/Allow Config]", (configList) -> {
        configList.setDefault(ResourceLocation.fromNamespaceAndPath("atomicscience", "hazmat_color_mask"), 0.75f, 1);
        configList.setDefault(ResourceLocation.fromNamespaceAndPath("atomicscience", "hazmat_color_body"), 0.15f, 1);
        configList.setDefault(ResourceLocation.fromNamespaceAndPath("atomicscience", "hazmat_color_leggings"), 0.05f, 1);
        configList.setDefault(ResourceLocation.fromNamespaceAndPath("atomicscience", "hazmat_color_boots"), 0.05f, 1);

        configList.setDefault(ResourceLocation.fromNamespaceAndPath("atomicscience", "hazmat_mask"), 0.75f, 1);
        configList.setDefault(ResourceLocation.fromNamespaceAndPath("atomicscience", "hazmat_body"), 0.15f, 1);
        configList.setDefault(ResourceLocation.fromNamespaceAndPath("atomicscience", "hazmat_leggings"), 0.05f, 1);
        configList.setDefault(ResourceLocation.fromNamespaceAndPath("atomicscience", "hazmat_boots"), 0.05f, 1);

        configList.load("icbmclassic/main/protective_armor/item_ratings", ConfigMain.protectiveArmor.ITEMS);
    });

    public static void setup() {
        loadFromConfig();
    }

    public static float getValue(ItemStack itemStack) {
        Float value = protectionValues.getValue(itemStack);
        return value != null ? Math.min(1, Math.max(0, value)) : 0;
    }

    public static void loadFromConfig() {
        protectionValues.reload();
    }

    public static float getProtectionRating(LivingEntity entityLivingBase) {

        if(ConfigMain.protectiveArmor.requireHelmet && entityLivingBase.getItemBySlot(EquipmentSlot.HEAD).isEmpty()) {
            return 0;
        }

        float value = 0;
        for(ItemStack armorStack: entityLivingBase.getArmorSlots()) {
            value += ProtectiveArmorHandler.getValue(armorStack);
        }
        return value;
    }
}
// PORTED

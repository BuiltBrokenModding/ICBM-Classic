package icbm.classic.content.cluster.missile;

import icbm.classic.api.ICBMClassicAPI;
import icbm.classic.api.missiles.projectile.IProjectileStack;
import icbm.classic.api.refs.ICBMExplosives;
import icbm.classic.content.cargo.parachute.ParachuteProjectileData;
import icbm.classic.content.reg.ItemReg;
import icbm.classic.lib.LanguageUtility;
import icbm.classic.lib.projectile.ProjectileStack;
import icbm.classic.prefab.item.ItemBase;
import icbm.classic.prefab.item.ItemStackCapProvider;
import net.minecraft.item.ItemGroup;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.NonNullList;
import net.minecraft.util.text.TranslationTextComponent;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ItemClusterMissile extends ItemBase {


    public ItemClusterMissile(Properties p_i48487_1_) {
        super(p_i48487_1_);
    }

    @Override
    @Nullable
    public net.minecraftforge.common.capabilities.ICapabilityProvider initCapabilities(ItemStack stack, @Nullable CompoundNBT nbt) {
        final ItemStackCapProvider provider = new ItemStackCapProvider(stack);
        provider.add("missile", ICBMClassicAPI.MISSILE_STACK_CAPABILITY, new CapabilityClusterMissileStack(stack));
        return provider;
    }

    @Override
    public int getMetadata(int damage) {
        return damage;
    }

    @Override
    public void getSubItems(ItemGroup tab, NonNullList<ItemStack> items) {
        if (tab == getCreativeTab() || tab == ItemGroup.SEARCH) {
            items.add(new ItemStack(this));
            items.add(createStack(new ItemStack(net.minecraft.item.Items.ARROW), 200));
            items.add(createStack(new ItemStack(ItemReg.itemBombletExplosive, 1, ICBMExplosives.CONDENSED.getRegistryID()), 100));

            final ItemStack parachute = new ItemStack(ItemReg.itemParachute);
            if(parachute.hasCapability(ICBMClassicAPI.PROJECTILE_STACK_CAPABILITY, null)) {
                final IProjectileStack projectileStack = parachute.getCapability(ICBMClassicAPI.PROJECTILE_STACK_CAPABILITY, null);
                if(projectileStack instanceof ProjectileStack) {
                    final ParachuteProjectileData projectileData = new ParachuteProjectileData();
                    projectileData.setHeldItem(new ItemStack(Items.COOKIE));
                    ((ProjectileStack) projectileStack).setProjectileData(projectileData);
                }
            }
            items.add(createStack(parachute, 50));
        }
    }

    private ItemStack createStack(ItemStack projectile, int count) {
        final ItemStack clusterStack = new ItemStack(this, 1);
        if (!clusterStack.hasCapability(ICBMClassicAPI.MISSILE_STACK_CAPABILITY, null)) {
            return clusterStack;
        }

        CapabilityClusterMissileStack cap = (CapabilityClusterMissileStack) clusterStack.getCapability(ICBMClassicAPI.MISSILE_STACK_CAPABILITY, null);

        for (int i = 0; i < count; i++) {
            cap.getActionDataCluster().getClusterSpawnEntries().add(projectile.copy());
        }

        return clusterStack;
    }

    @Override
    protected boolean hasDetailedInfo(ItemStack stack, PlayerEntity player) {
        return true;
    }

    @Override
    protected void getDetailedInfo(ItemStack stack, PlayerEntity player, List list) {
        StringBuilder contents = new StringBuilder("\n");

        CapabilityClusterMissileStack cap = (CapabilityClusterMissileStack) stack.getCapability(ICBMClassicAPI.MISSILE_STACK_CAPABILITY, null);

        if (cap.getActionDataCluster().getClusterSpawnEntries().isEmpty()) {
            contents.append("empty");
        }
        else {
            Map<String, Integer> contentMap = new HashMap<>();
            for (ItemStack itemStack : cap.getActionDataCluster().getClusterSpawnEntries()) {
                int count = contentMap.computeIfAbsent(itemStack.getDisplayName(), (k) -> 0);
                contentMap.put(itemStack.getDisplayName(), count + 1);
            }
            for (Map.Entry<String, Integer> entry : contentMap.entrySet()) {
                contents.append("\t").append(entry.getValue()).append(" x ").append(entry.getKey());
            }
        }


        final TranslationTextComponent translation = new TranslationTextComponent(getUnlocalizedName() + ".contents", contents.toString());
        LanguageUtility.outputLines(translation, list::add);
    }
}

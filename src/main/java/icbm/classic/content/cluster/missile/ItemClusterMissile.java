package icbm.classic.content.cluster.missile;

import icbm.classic.ICBMClassic;
import icbm.classic.api.ICBMClassicAPI;
import icbm.classic.api.missiles.projectile.IProjectileStack;
import icbm.classic.api.refs.ICBMExplosives;
import icbm.classic.content.cargo.parachute.ParachuteProjectileData;
import icbm.classic.content.reg.ItemReg;
import icbm.classic.lib.LanguageUtility;
import icbm.classic.lib.explosive.reg.ExplosiveRegistry;
import icbm.classic.lib.projectile.ProjectileStack;
import icbm.classic.prefab.item.ItemBase;
import icbm.classic.prefab.item.ItemStackCapProvider;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.NonNullList;
import net.minecraft.util.text.TextComponentTranslation;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ItemClusterMissile extends ItemBase {
    public ItemClusterMissile() {
        this.setMaxDamage(0);
        this.setMaxStackSize(1);
    }

    @Override
    @Nullable
    public net.minecraftforge.common.capabilities.ICapabilityProvider initCapabilities(ItemStack stack, @Nullable NBTTagCompound nbt) {
        final ItemStackCapProvider provider = new ItemStackCapProvider(stack);
        provider.add("missile", ICBMClassicAPI.MISSILE_STACK_CAPABILITY, new CapabilityClusterMissileStack(stack));
        return provider;
    }

    @Override
    public int getMetadata(int damage) {
        return damage;
    }

    @Override
    public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> items) {
        if (tab == getCreativeTab() || tab == CreativeTabs.SEARCH) {
            items.add(new ItemStack(this));
            items.add(createStack(new ItemStack(Items.ARROW), 200));
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
    protected boolean hasDetailedInfo(ItemStack stack, EntityPlayer player) {
        return true;
    }

    @Override
    protected void getDetailedInfo(ItemStack stack, EntityPlayer player, List list) {

        CapabilityClusterMissileStack cap = (CapabilityClusterMissileStack) stack.getCapability(ICBMClassicAPI.MISSILE_STACK_CAPABILITY, null);

        if (!cap.getActionDataCluster().getClusterSpawnEntries().isEmpty()) {
            list.add(LanguageUtility.getLocal("projectile.icbmclassic:holder.held.multiple"));

            Map<String, Integer> contentMap = new HashMap<>();
            boolean someDisabled = false;
            for (ItemStack itemStack : cap.getActionDataCluster().getClusterSpawnEntries()) {
                someDisabled = someDisabled || !ClusterMissileHandler.isAllowed(itemStack);

                String displayName = itemStack.getDisplayName();
                if(!ClusterMissileHandler.isAllowed(itemStack)) {
                    final TextComponentTranslation translation = new TextComponentTranslation("projectile.icbmclassic:holder.disabled.prefix", displayName);
                    displayName = translation.getFormattedText();
                }

                int count = contentMap.computeIfAbsent(displayName, (k) -> 0);
                contentMap.put(displayName, count + 1);
            }
            for (Map.Entry<String, Integer> entry : contentMap.entrySet()) {
                list.add("  " + entry.getValue() + " x " + entry.getKey());
                //TODO Find a way to provide more item details
            }

            if(someDisabled) {
                final TextComponentTranslation translation = new TextComponentTranslation("projectile.icbmclassic:holder.disabled.prefix",
                    LanguageUtility.getLocal("projectile.icbmclassic:holder.disabled.config")
                );
                list.add(translation.getFormattedText());
            }
        }

    }
}

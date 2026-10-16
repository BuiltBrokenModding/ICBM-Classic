package icbm.classic.content.cluster.missile;

import icbm.classic.api.ICBMClassicAPI;
import icbm.classic.api.missiles.ICapabilityMissileStack;
import icbm.classic.api.missiles.projectile.IProjectileStack;
import icbm.classic.api.refs.ICBMExplosives;
import icbm.classic.content.cargo.parachute.ParachuteProjectileData;
import icbm.classic.content.reg.ItemReg;
import icbm.classic.lib.LanguageUtility;
import icbm.classic.lib.capability.holder.ICapabilityHolderItem;
import icbm.classic.lib.explosive.ExplosiveStacks;
import icbm.classic.lib.projectile.ProjectileStack;
import icbm.classic.prefab.item.ISubItemProvider;
import icbm.classic.prefab.item.ItemBase;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.capabilities.ItemCapability;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ItemClusterMissile extends ItemBase implements ICapabilityHolderItem, ISubItemProvider {
    public ItemClusterMissile() {
        super(new Item.Properties().stacksTo(1));
    }

    @Nullable
    @Override
    public <T> T getCapability(ItemCapability<T, Void> capability, ItemStack stack) {
        if (capability == ICBMClassicAPI.MISSILE_STACK_CAPABILITY) {
            return (T) new CapabilityClusterMissileStack(stack);
        }
        return null;
    }

    @Override
    public void getSubItems(NonNullList<ItemStack> items) {
        items.add(new ItemStack(this));
        items.add(createStack(new ItemStack(Items.ARROW), 200));
        items.add(createStack(ExplosiveStacks.create(ItemReg.itemBombletExplosive.get(), ICBMExplosives.CONDENSED), 100));

        final ItemStack parachute = new ItemStack(ItemReg.itemParachute.get());
        final IProjectileStack projectileStack = parachute.getCapability(ICBMClassicAPI.PROJECTILE_STACK_CAPABILITY);
        if(projectileStack instanceof ProjectileStack) {
            final ParachuteProjectileData projectileData = new ParachuteProjectileData();
            projectileData.setHeldItem(new ItemStack(Items.COOKIE));
            ((ProjectileStack) projectileStack).setProjectileData(projectileData);
        }
        items.add(createStack(parachute, 50));
    }

    private ItemStack createStack(ItemStack projectile, int count) {
        final ItemStack clusterStack = new ItemStack(this, 1);
        final ICapabilityMissileStack missileStack = clusterStack.getCapability(ICBMClassicAPI.MISSILE_STACK_CAPABILITY);
        if (!(missileStack instanceof CapabilityClusterMissileStack cap)) {
            return clusterStack;
        }

        for (int i = 0; i < count; i++) {
            cap.getActionDataCluster().getClusterSpawnEntries().add(projectile.copy());
        }
        cap.saveToStack();

        return clusterStack;
    }

    @Override
    protected boolean hasDetailedInfo(ItemStack stack, @Nullable Player player) {
        return true;
    }

    @Override
    protected void getDetailedInfo(ItemStack stack, @Nullable Player player, List<Component> list) {

        final ICapabilityMissileStack missileStack = stack.getCapability(ICBMClassicAPI.MISSILE_STACK_CAPABILITY);
        if (!(missileStack instanceof CapabilityClusterMissileStack cap)) {
            return;
        }

        if (!cap.getActionDataCluster().getClusterSpawnEntries().isEmpty()) {
            list.add(Component.translatable("projectile.icbmclassic:holder.held.multiple"));

            Map<String, Integer> contentMap = new HashMap<>();
            boolean someDisabled = false;
            for (ItemStack itemStack : cap.getActionDataCluster().getClusterSpawnEntries()) {
                someDisabled = someDisabled || !ClusterMissileHandler.isAllowed(itemStack);

                String displayName = itemStack.getHoverName().getString();
                if(!ClusterMissileHandler.isAllowed(itemStack)) {
                    final Component translation = Component.translatable("projectile.icbmclassic:holder.disabled.prefix", displayName);
                    displayName = LanguageUtility.buildToolTipString(translation);
                }

                int count = contentMap.computeIfAbsent(displayName, (k) -> 0);
                contentMap.put(displayName, count + 1);
            }
            for (Map.Entry<String, Integer> entry : contentMap.entrySet()) {
                list.add(Component.literal("  " + entry.getValue() + " x " + entry.getKey()));
                //TODO Find a way to provide more item details
            }

            if(someDisabled) {
                list.add(Component.translatable("projectile.icbmclassic:holder.disabled.prefix",
                    Component.translatable("projectile.icbmclassic:holder.disabled.config")));
            }
        }

    }
}
// PORTED

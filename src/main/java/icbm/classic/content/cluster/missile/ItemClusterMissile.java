package icbm.classic.content.cluster.missile;

import icbm.classic.api.ICBMClassicAPI;
import icbm.classic.api.missiles.projectile.IProjectileStack;
import icbm.classic.api.refs.ICBMExplosives;
import icbm.classic.content.cargo.parachute.ParachuteProjectileData;
import icbm.classic.content.reg.EntityReg;
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
        return new ItemStackCapProvider(stack)
            .with(ICBMClassicAPI.MISSILE_STACK_CAPABILITY, () -> new CapabilityClusterMissileStack(EntityReg.MISSILE_CLUSTER::get));
    }

    @Override
    public void fillItemGroup(ItemGroup group, NonNullList<ItemStack> items) {
        if (this.isInGroup(group)) {
            items.add(new ItemStack(this));
            items.add(createStack(new ItemStack(net.minecraft.item.Items.ARROW), 200));
            items.add(createStack(new ItemStack(ItemReg.BOMBLET_CONDENSED.get()), 100));

            final ItemStack parachute = new ItemStack(ItemReg.PARACHUTE.get(), 1);
            if(parachute.getCapability(ICBMClassicAPI.PROJECTILE_STACK_CAPABILITY).isPresent()) {
                final IProjectileStack projectileStack = parachute.getCapability(ICBMClassicAPI.PROJECTILE_STACK_CAPABILITY).orElseThrow(IllegalStateException::new);
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
        if (!clusterStack.getCapability(ICBMClassicAPI.MISSILE_STACK_CAPABILITY).isPresent()) {
            return clusterStack;
        }

        CapabilityClusterMissileStack cap = (CapabilityClusterMissileStack) clusterStack.getCapability(ICBMClassicAPI.MISSILE_STACK_CAPABILITY).orElseThrow(IllegalStateException::new);

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

        CapabilityClusterMissileStack cap = (CapabilityClusterMissileStack) stack.getCapability(ICBMClassicAPI.MISSILE_STACK_CAPABILITY).orElseThrow(IllegalStateException::new);;

        if (cap.getActionDataCluster().getClusterSpawnEntries().isEmpty()) {
            contents.append("empty");
        }
        else {
            Map<String, Integer> contentMap = new HashMap<>();
            for (ItemStack itemStack : cap.getActionDataCluster().getClusterSpawnEntries()) {
                int count = contentMap.computeIfAbsent(itemStack.getTranslationKey(), (k) -> 0);
                contentMap.put(itemStack.getTranslationKey(), count + 1);
            }
            for (Map.Entry<String, Integer> entry : contentMap.entrySet()) {
                contents.append("\t").append(entry.getValue()).append(" x ").append(entry.getKey());
            }
        }


        final TranslationTextComponent translation = new TranslationTextComponent(getTranslationKey() + ".contents", contents.toString());
        LanguageUtility.outputLines(translation, list::add);
    }
}

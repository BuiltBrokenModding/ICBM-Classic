package icbm.classic.mods.jei;

import icbm.classic.ICBMConstants;
import icbm.classic.api.ICBMClassicAPI;
import icbm.classic.api.missiles.ICapabilityMissileStack;
import icbm.classic.api.missiles.projectile.IProjectileStack;
import icbm.classic.config.ConfigJEI;
import icbm.classic.content.cargo.CargoHolderHandler;
import icbm.classic.content.cargo.CargoProjectileData;
import icbm.classic.content.cluster.missile.CapabilityClusterMissileStack;
import icbm.classic.content.cluster.missile.ClusterMissileHandler;
import icbm.classic.content.missile.entity.itemstack.item.CapabilityHeldItemMissile;
import icbm.classic.content.missile.entity.itemstack.item.HeldItemMissileHandler;
import icbm.classic.content.reg.ItemReg;
import icbm.classic.lib.projectile.ProjectileStack;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.IModRegistry;
import mezz.jei.api.ISubtypeRegistry;
import mezz.jei.api.JEIPlugin;
import mezz.jei.api.recipe.VanillaRecipeCategoryUid;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@JEIPlugin
public class JEIProxy implements IModPlugin {
    // https://github.com/mezz/JustEnoughItems/releases/tag/v4.7.11
    // https://github.com/mezz/JustEnoughItems/tree/8a8c25448c126decefe8f12f65fc88cae94d4012
    // https://github.com/mekanism/Mekanism/tree/1.12/src/main/java/mekanism/client/jei
    // https://github.com/mezz/JustEnoughItems/wiki/Getting-Started

    private String cargoItemKey(ISubtypeRegistry subtypeRegistry, ItemStack itemStack) {
        String key = Integer.toString(itemStack.getMetadata());
        if(itemStack.hasCapability(ICBMClassicAPI.PROJECTILE_STACK_CAPABILITY, null)) {
            final IProjectileStack projectileStack = itemStack.getCapability(ICBMClassicAPI.PROJECTILE_STACK_CAPABILITY, null);
            if (projectileStack instanceof ProjectileStack && projectileStack.getProjectileData() instanceof CargoProjectileData) {
                final ItemStack stack = ((CargoProjectileData<?, ?>) projectileStack.getProjectileData()).getHeldItem();
                if(stack != null && !stack.isEmpty()) {
                    return key
                        + ":" + stack.getItem().getRegistryName()
                        + ":" + Optional.ofNullable(subtypeRegistry.getSubtypeInfo(stack)).orElse(Integer.toString(stack.getMetadata()));
                }
            }
        }
        return key;
    }

    private String clusterItemKey(ISubtypeRegistry subtypeRegistry, ItemStack itemStack) {
        String key = Integer.toString(itemStack.getMetadata());
        if(itemStack.hasCapability(ICBMClassicAPI.MISSILE_STACK_CAPABILITY, null)) {
            final ICapabilityMissileStack projectileStack = itemStack.getCapability(ICBMClassicAPI.MISSILE_STACK_CAPABILITY, null);
            if (projectileStack instanceof CapabilityClusterMissileStack) {
                final ItemStack stack = ((CapabilityClusterMissileStack) projectileStack).getActionDataCluster().getClusterSpawnEntries().stream().findFirst().orElse(null);
                if(stack != null && !stack.isEmpty()) {
                    return key
                        + ":" + stack.getItem().getRegistryName()
                        + ":" + Optional.ofNullable(subtypeRegistry.getSubtypeInfo(stack)).orElse(Integer.toString(stack.getMetadata()));
                }
            }
        }
        return key;
    }

    private String itemHolderKey(ISubtypeRegistry subtypeRegistry, ItemStack itemStack) {
        String key = Integer.toString(itemStack.getMetadata());
        if(itemStack.hasCapability(ICBMClassicAPI.MISSILE_STACK_CAPABILITY, null)) {
            final ICapabilityMissileStack projectileStack = itemStack.getCapability(ICBMClassicAPI.MISSILE_STACK_CAPABILITY, null);
            if (projectileStack instanceof CapabilityHeldItemMissile) {
                final ItemStack stack = ((CapabilityHeldItemMissile) projectileStack).getHeldItem();
                if(stack != null && !stack.isEmpty()) {
                    return key
                        + ":" + stack.getItem().getRegistryName()
                        + ":" + Optional.ofNullable(subtypeRegistry.getSubtypeInfo(stack)).orElse(Integer.toString(stack.getMetadata()));
                }
            }
        }
        return key;
    }

    @Override
    public void registerItemSubtypes(ISubtypeRegistry subtypeRegistry) {
        if(!ConfigJEI.DISABLED) {
            subtypeRegistry.registerSubtypeInterpreter(ItemReg.itemParachute, (i) -> this.cargoItemKey(subtypeRegistry, i));
            subtypeRegistry.registerSubtypeInterpreter(ItemReg.itemBalloon, (i) -> this.cargoItemKey(subtypeRegistry, i));
            subtypeRegistry.registerSubtypeInterpreter(ItemReg.itemClusterMissile, (i) -> this.clusterItemKey(subtypeRegistry, i));
            subtypeRegistry.registerSubtypeInterpreter(ItemReg.heldItemMissile, (i) -> this.itemHolderKey(subtypeRegistry, i));
        }
    }

    @Override
    public void register(IModRegistry registry) {

        if(!ConfigJEI.DISABLED && !ConfigJEI.DISABLE_PAYLOAD) {
            //https://github.com/ExtraCells/ExtraCells2/blob/b1d03907d911909aaa3b176a7cc90eac0c1467dc/src/main/scala/extracells/integration/jei/Plugin.scala

            final List recipes = new ArrayList();
            for (ItemStack stack : registry.getIngredientRegistry().getIngredients(ItemStack.class)) {
                if (CargoHolderHandler.isAllowed(stack)) {
                    recipes.add(new CargoItemWrapper(new ItemStack(ItemReg.itemBalloon), stack, new ResourceLocation(ICBMConstants.DOMAIN, "balloon_cargo")));
                    recipes.add(new CargoItemWrapper(new ItemStack(ItemReg.itemParachute), stack, new ResourceLocation(ICBMConstants.DOMAIN, "parachute_cargo")));

                    recipes.add(new DecraftCargoItemWrapper(new ItemStack(ItemReg.itemBalloon), stack, new ResourceLocation(ICBMConstants.DOMAIN, "balloon_cargo_decraft")));
                    recipes.add(new DecraftCargoItemWrapper(new ItemStack(ItemReg.itemParachute), stack, new ResourceLocation(ICBMConstants.DOMAIN, "parachute_cargo_decraft")));
                }
                if(HeldItemMissileHandler.isAllowed(stack)) {
                    recipes.add(new HeldItemMissileWrapper(new ItemStack(ItemReg.heldItemMissile), stack, new ResourceLocation(ICBMConstants.DOMAIN, "held_item_missile")));
                    recipes.add(new DecraftHeldItemMissileWrapper(new ItemStack(ItemReg.heldItemMissile), stack, new ResourceLocation(ICBMConstants.DOMAIN, "held_item_missile_decraft")));
                }
                if (ClusterMissileHandler.isAllowed(stack)) {
                    recipes.add(new ClusterItemWrapper(new ItemStack(ItemReg.itemClusterMissile), stack, new ResourceLocation(ICBMConstants.DOMAIN, "cluster_missile")));
                }
            }

            /*final List<ItemStack> stacks = registry.getIngredientRegistry().getIngredients(ItemStack.class)
                .stream().filter(i -> ClusterMissileHandler.isAllowed(i))
                .collect(Collectors.toList());
            recipes.add(new ClusterItemAllWrapper(new ItemStack(ItemReg.itemClusterMissile), stacks, false, new ResourceLocation(ICBMConstants.DOMAIN, "cluster_missile")));
            recipes.add(new ClusterItemAllWrapper(new ItemStack(ItemReg.itemClusterMissile), stacks, true, new ResourceLocation(ICBMConstants.DOMAIN, "cluster_missile")));
            */
            registry.addRecipes(recipes, VanillaRecipeCategoryUid.CRAFTING);
        }
    }
}

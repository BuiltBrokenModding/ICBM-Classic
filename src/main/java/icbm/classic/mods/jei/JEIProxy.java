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
import icbm.classic.content.reg.ComponentReg;
import icbm.classic.content.reg.ItemReg;
import icbm.classic.lib.projectile.ProjectileStack;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.RecipeTypes;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.ingredients.subtypes.ISubtypeInterpreter;
import mezz.jei.api.ingredients.subtypes.UidContext;
import mezz.jei.api.registration.IRecipeRegistration;
import mezz.jei.api.registration.ISubtypeRegistration;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * PORT: JEI 19 plugin. 1.12 metadata subtypes became the {@link ComponentReg#EXPLOSIVE} component, the
 * cargo/cluster/held item subtypes are built from the stack capabilities. Payload recipes are display only
 * shapeless recipes added to the vanilla crafting category.
 */
@JeiPlugin
public class JEIProxy implements IModPlugin {
    // https://github.com/mezz/JustEnoughItems/wiki/Getting-Started

    private static final ResourceLocation UID = ResourceLocation.fromNamespaceAndPath(ICBMConstants.DOMAIN, "jei_plugin");

    @Override
    public ResourceLocation getPluginUid() {
        return UID;
    }

    // ------------------------------------------------------------------
    // Subtypes
    // ------------------------------------------------------------------

    /** Subtype of an item held inside another item, replaces 1.12 registry name + meta/subtype info */
    private static String nestedKey(ItemStack stack) {
        String key = BuiltInRegistries.ITEM.getKey(stack.getItem()).toString();
        final ResourceLocation explosive = stack.get(ComponentReg.EXPLOSIVE.get());
        if (explosive != null) {
            key += ":" + explosive;
        }
        final CustomData customData = stack.get(DataComponents.CUSTOM_DATA);
        if (customData != null && !customData.isEmpty()) {
            key += ":" + customData.copyTag().hashCode(); // PORT-CHECK: replaces nested JEI subtype info lookup
        }
        return key;
    }

    @Nullable
    private static String explosiveKey(ItemStack itemStack) {
        final ResourceLocation explosive = itemStack.get(ComponentReg.EXPLOSIVE.get());
        return explosive != null ? explosive.toString() : null;
    }

    @Nullable
    private static String cargoItemKey(ItemStack itemStack) {
        final IProjectileStack projectileStack = itemStack.getCapability(ICBMClassicAPI.PROJECTILE_STACK_CAPABILITY);
        if (projectileStack instanceof ProjectileStack && projectileStack.getProjectileData() instanceof CargoProjectileData) {
            final ItemStack stack = ((CargoProjectileData<?, ?>) projectileStack.getProjectileData()).getHeldItem();
            if(stack != null && !stack.isEmpty()) {
                return nestedKey(stack);
            }
        }
        return null;
    }

    @Nullable
    private static String clusterItemKey(ItemStack itemStack) {
        final ICapabilityMissileStack projectileStack = itemStack.getCapability(ICBMClassicAPI.MISSILE_STACK_CAPABILITY);
        if (projectileStack instanceof CapabilityClusterMissileStack) {
            final ItemStack stack = ((CapabilityClusterMissileStack) projectileStack).getActionDataCluster().getClusterSpawnEntries().stream().findFirst().orElse(null);
            if(stack != null && !stack.isEmpty()) {
                return nestedKey(stack);
            }
        }
        return null;
    }

    @Nullable
    private static String itemHolderKey(ItemStack itemStack) {
        final ICapabilityMissileStack projectileStack = itemStack.getCapability(ICBMClassicAPI.MISSILE_STACK_CAPABILITY);
        if (projectileStack instanceof CapabilityHeldItemMissile) {
            final ItemStack stack = ((CapabilityHeldItemMissile) projectileStack).getHeldItem();
            if(stack != null && !stack.isEmpty()) {
                return nestedKey(stack);
            }
        }
        return null;
    }

    /** PORT-CHECK: JEI 19.21 ISubtypeInterpreter methods (getSubtypeData + deprecated getLegacyStringSubtypeInfo) */
    private static ISubtypeInterpreter<ItemStack> interpreter(Function<ItemStack, String> keyFunction) {
        return new ISubtypeInterpreter<>() {
            @Override
            @Nullable
            public Object getSubtypeData(ItemStack ingredient, UidContext context) {
                return keyFunction.apply(ingredient);
            }

            @Override
            @SuppressWarnings("removal")
            public String getLegacyStringSubtypeInfo(ItemStack ingredient, UidContext context) {
                final String key = keyFunction.apply(ingredient);
                return key != null ? key : "";
            }
        };
    }

    private static void register(ISubtypeRegistration registration, Item item, Function<ItemStack, String> keyFunction) {
        registration.registerSubtypeInterpreter(VanillaTypes.ITEM_STACK, item, interpreter(keyFunction));
    }

    @Override
    public void registerItemSubtypes(ISubtypeRegistration registration) {
        if(!ConfigJEI.DISABLED) {
            // PORT: 1.12 handled these through metadata (has subtypes), now the explosive component
            register(registration, ItemReg.itemGrenade.get(), JEIProxy::explosiveKey);
            register(registration, ItemReg.itemBombCart.get(), JEIProxy::explosiveKey);
            register(registration, ItemReg.itemExplosiveMissile.get(), JEIProxy::explosiveKey);
            register(registration, ItemReg.itemBlockExplosive.get(), JEIProxy::explosiveKey);
            register(registration, ItemReg.itemBombletExplosive.get(), JEIProxy::explosiveKey);

            register(registration, ItemReg.itemParachute.get(), JEIProxy::cargoItemKey);
            register(registration, ItemReg.itemBalloon.get(), JEIProxy::cargoItemKey);
            register(registration, ItemReg.itemClusterMissile.get(), JEIProxy::clusterItemKey);
            register(registration, ItemReg.heldItemMissile.get(), JEIProxy::itemHolderKey);
        }
    }

    // ------------------------------------------------------------------
    // Recipes
    // ------------------------------------------------------------------

    /** 1.12 names were shared by every recipe of a kind, JEI 19 needs unique ids so the cargo is appended */
    private static ResourceLocation recipeId(String name, ItemStack stack, int index) {
        final ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(stack.getItem());
        return ResourceLocation.fromNamespaceAndPath(ICBMConstants.DOMAIN,
            name + "/" + itemId.getNamespace() + "/" + itemId.getPath() + "/" + index);
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {

        if(!ConfigJEI.DISABLED && !ConfigJEI.DISABLE_PAYLOAD) {
            final List<RecipeHolder<CraftingRecipe>> recipes = new ArrayList<>();
            int index = 0;
            for (ItemStack stack : registration.getIngredientManager().getAllIngredients(VanillaTypes.ITEM_STACK)) {
                index++;
                if (CargoHolderHandler.isAllowed(stack)) {
                    recipes.add(new CargoItemWrapper(new ItemStack(ItemReg.itemBalloon.get()), stack, recipeId("balloon_cargo", stack, index)).build());
                    recipes.add(new CargoItemWrapper(new ItemStack(ItemReg.itemParachute.get()), stack, recipeId("parachute_cargo", stack, index)).build());

                    recipes.add(new DecraftCargoItemWrapper(new ItemStack(ItemReg.itemBalloon.get()), stack, recipeId("balloon_cargo_decraft", stack, index)).build());
                    recipes.add(new DecraftCargoItemWrapper(new ItemStack(ItemReg.itemParachute.get()), stack, recipeId("parachute_cargo_decraft", stack, index)).build());
                }
                if(HeldItemMissileHandler.isAllowed(stack)) {
                    recipes.add(new HeldItemMissileWrapper(new ItemStack(ItemReg.heldItemMissile.get()), stack, recipeId("held_item_missile", stack, index)).build());
                    recipes.add(new DecraftHeldItemMissileWrapper(new ItemStack(ItemReg.heldItemMissile.get()), stack, recipeId("held_item_missile_decraft", stack, index)).build());
                }
                if (ClusterMissileHandler.isAllowed(stack)) {
                    recipes.add(new ClusterItemWrapper(new ItemStack(ItemReg.itemClusterMissile.get()), stack, recipeId("cluster_missile", stack, index)).build());
                }
            }

            /*final List<ItemStack> stacks = registration.getIngredientManager().getAllIngredients(VanillaTypes.ITEM_STACK)
                .stream().filter(i -> ClusterMissileHandler.isAllowed(i))
                .collect(Collectors.toList());
            recipes.add(new ClusterItemAllWrapper(new ItemStack(ItemReg.itemClusterMissile.get()), stacks, false, ResourceLocation.fromNamespaceAndPath(ICBMConstants.DOMAIN, "cluster_missile")).build());
            recipes.add(new ClusterItemAllWrapper(new ItemStack(ItemReg.itemClusterMissile.get()), stacks, true, ResourceLocation.fromNamespaceAndPath(ICBMConstants.DOMAIN, "cluster_missile")).build());
            */
            registration.addRecipes(RecipeTypes.CRAFTING, recipes);
        }
    }
}
// PORTED

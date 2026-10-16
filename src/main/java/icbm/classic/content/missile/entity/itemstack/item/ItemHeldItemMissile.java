package icbm.classic.content.missile.entity.itemstack.item;

import icbm.classic.api.ICBMClassicAPI;
import icbm.classic.api.missiles.ICapabilityMissileStack;
import icbm.classic.lib.LanguageUtility;
import icbm.classic.lib.capability.holder.ICapabilityHolderItem;
import icbm.classic.prefab.item.ISubItemProvider;
import icbm.classic.prefab.item.ItemBase;
import icbm.classic.prefab.item.ItemICBMBase;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.TooltipFlag;
import net.neoforged.neoforge.capabilities.ItemCapability;

import javax.annotation.Nullable;
import java.util.List;

public class ItemHeldItemMissile extends ItemICBMBase implements ICapabilityHolderItem, ISubItemProvider {
    public ItemHeldItemMissile() {
        super("held_item_missile", new Item.Properties().stacksTo(1));
    }

    @Nullable
    private static CapabilityHeldItemMissile getCap(ItemStack stack) {
        final ICapabilityMissileStack cap = stack.getCapability(ICBMClassicAPI.MISSILE_STACK_CAPABILITY);
        return cap instanceof CapabilityHeldItemMissile ? (CapabilityHeldItemMissile) cap : null;
    }

    /** 1.12 showDurabilityBar */
    @Override
    public boolean isBarVisible(ItemStack stack)
    {
        final CapabilityHeldItemMissile cap = getCap(stack);
        if (cap != null) {
            final ItemStack heldItem = cap.getHeldItem();
            return !heldItem.isEmpty() && heldItem.isBarVisible();
        }
        return stack.isDamaged();
    }

    /** 1.12 getDurabilityForDisplay */
    @Override
    public int getBarWidth(ItemStack stack)
    {
        final CapabilityHeldItemMissile cap = getCap(stack);
        if (cap != null && !cap.getHeldItem().isEmpty()) {
            return cap.getHeldItem().getBarWidth();
        }
        return super.getBarWidth(stack);
    }

    @Override
    public int getBarColor(ItemStack stack)
    {
        final CapabilityHeldItemMissile cap = getCap(stack);
        if (cap != null && !cap.getHeldItem().isEmpty()) {
            return cap.getHeldItem().getBarColor();
        }
        return super.getBarColor(stack);
    }

    @Nullable
    @Override
    public <T> T getCapability(ItemCapability<T, Void> capability, ItemStack stack) {
        if (capability == ICBMClassicAPI.MISSILE_STACK_CAPABILITY) {
            return (T) new CapabilityHeldItemMissile(stack);
        }
        return null;
    }

    @Override
    public String getDescriptionId(ItemStack stack) {
        final CapabilityHeldItemMissile cap = getCap(stack);
        if (cap != null && cap.getHeldItem().getItem() instanceof SwordItem) {
            return super.getDescriptionId(stack) + ".sword";
            //TODO attempt to localize all items to allow customized naming
        }
        return super.getDescriptionId(stack);
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> list, TooltipFlag flag) {
        final CapabilityHeldItemMissile cap = getCap(stack);

        // Only show basic info if we have no projectile data
        if (cap == null || cap.getHeldItem().isEmpty()) {
            LanguageUtility.outputLines(Component.translatable(getDescriptionId() + ".info"), line -> list.add(Component.literal(line)));
        }

        // Show projectile information
        if (cap != null && !cap.getHeldItem().isEmpty()) {
            final ItemStack heldItem = cap.getHeldItem();

            LanguageUtility.outputLines(
                Component.translatable(
                    getDescriptionId() + ".held_item." + cap.getActionMode().name().toLowerCase()
                ), line -> list.add(Component.literal(line)));

            list.add(Component.empty());
            list.add(Component.translatable("projectile.icbmclassic:holder.held"));
            list.addAll(heldItem.getTooltipLines(context, ItemBase.getClientPlayer(), flag));
            list.add(Component.empty());

            if(!HeldItemMissileHandler.isAllowed(heldItem)) {
                list.add(Component.translatable("projectile.icbmclassic:holder.disabled.config"));
            }
        }
    }

    @Override
    public void getSubItems(NonNullList<ItemStack> items) {
        items.add(new ItemStack(this));
        items.add(createStack(new ItemStack(Items.DIAMOND_SWORD)));
        items.add(createStack(new ItemStack(Items.SHEARS)));
    }

    private ItemStack createStack(ItemStack data) {
        final ItemStack stack = new ItemStack(this);
        final CapabilityHeldItemMissile cap = getCap(stack);
        if (cap != null) {
            cap.setHeldItem(data);
        }
        return stack;
    }
}
// PORTED

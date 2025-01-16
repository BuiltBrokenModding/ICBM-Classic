package icbm.classic.content.missile.entity.itemstack.item;

import icbm.classic.api.ICBMClassicAPI;
import icbm.classic.api.missiles.ICapabilityMissileStack;
import icbm.classic.lib.LanguageUtility;
import icbm.classic.prefab.item.ItemStackCapProvider;
import net.minecraft.client.Minecraft;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.*;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.NonNullList;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.util.LazyOptional;

import javax.annotation.Nullable;
import java.util.List;

public class ItemHeldItemMissile extends Item {

    public ItemHeldItemMissile(Properties properties) {
        super(properties);
    }

    @Override
    public boolean showDurabilityBar(ItemStack stack)
    {
        final LazyOptional<ICapabilityMissileStack> cap = stack.getCapability(ICBMClassicAPI.MISSILE_STACK_CAPABILITY, null);
        if (cap.isPresent() && cap.orElseThrow(IllegalStateException::new) instanceof CapabilityHeldItemMissile) {
            final ItemStack heldItem = ((CapabilityHeldItemMissile) cap.orElseThrow(IllegalStateException::new)).getHeldItem();
            return heldItem.getItem().showDurabilityBar(heldItem);
        }
        return false;
    }

    @Override
    public double getDurabilityForDisplay(ItemStack stack)
    {
        final LazyOptional<ICapabilityMissileStack> cap = stack.getCapability(ICBMClassicAPI.MISSILE_STACK_CAPABILITY, null);
        if (cap.isPresent() && cap.orElseThrow(IllegalStateException::new) instanceof CapabilityHeldItemMissile) {
            final ItemStack heldItem = ((CapabilityHeldItemMissile) cap.orElseThrow(IllegalStateException::new)).getHeldItem();
            return heldItem.getItem().getDurabilityForDisplay(heldItem);
        }
        return super.getDurabilityForDisplay(stack);
    }

    @Override
    @Nullable
    public net.minecraftforge.common.capabilities.ICapabilityProvider initCapabilities(ItemStack stack, @Nullable CompoundNBT nbt) {
        return new ItemStackCapProvider(stack).with(ICBMClassicAPI.MISSILE_STACK_CAPABILITY, CapabilityHeldItemMissile::new);
    }

    @Override
    public String getTranslationKey(ItemStack stack) {
        final LazyOptional<ICapabilityMissileStack> cap = stack.getCapability(ICBMClassicAPI.MISSILE_STACK_CAPABILITY, null);
        if (cap.isPresent() && cap.orElseThrow(IllegalStateException::new) instanceof CapabilityHeldItemMissile) {
            final ItemStack heldItem = ((CapabilityHeldItemMissile) cap.orElseThrow(IllegalStateException::new)).getHeldItem();
            if(heldItem.getItem() instanceof SwordItem) {
                return super.getTranslationKey(stack) + ".sword";
            }
            //TODO attempt to localize all items to allow customized naming
        }
        return super.getTranslationKey(stack);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<ITextComponent> list, ITooltipFlag flagIn) {
        final LazyOptional<ICapabilityMissileStack> lazyOptional = stack.getCapability(ICBMClassicAPI.MISSILE_STACK_CAPABILITY, null);
        lazyOptional.ifPresent((cap) -> {
            // Only show basic info if we have no projectile data
            if (!(cap instanceof CapabilityHeldItemMissile) || ((CapabilityHeldItemMissile) cap).getHeldItem().isEmpty()) {
                LanguageUtility.outputComponents(new TranslationTextComponent(getTranslationKey() + ".info"), list::add);
            }

            // Show projectile information
            if (cap instanceof CapabilityHeldItemMissile && !((CapabilityHeldItemMissile) cap).getHeldItem().isEmpty()) {
                final ItemStack heldItem = ((CapabilityHeldItemMissile) cap).getHeldItem();


                LanguageUtility.outputComponents(
                    new TranslationTextComponent(
                        getTranslationKey() + ".held_item." + ((CapabilityHeldItemMissile) cap).getActionMode().name().toLowerCase()
                    ), list::add);


                list.add(new StringTextComponent("")); //TODO fix so formatting is in the translation file
                LanguageUtility.outputComponents(new TranslationTextComponent("projectile.icbmclassic:holder.held"), list::add);
                list.addAll(heldItem.getTooltip(Minecraft.getInstance().player, flagIn));
                list.add(new StringTextComponent(""));

                if(!HeldItemMissileHandler.isAllowed(heldItem)) {
                    LanguageUtility.outputComponents(new TranslationTextComponent("projectile.icbmclassic:holder.disabled.config"), list::add);
                }
            }
        });
    }

    @Override
    public void fillItemGroup(ItemGroup group, NonNullList<ItemStack> items) {
        if (this.isInGroup(group)) {
            items.add(new ItemStack(this));
            items.add(createStack(new ItemStack(Items.DIAMOND_SWORD)));
            items.add(createStack(new ItemStack(Items.SHEARS)));
        }
    }

    private ItemStack createStack(ItemStack data) {
        final ItemStack stack = new ItemStack(this);
        final LazyOptional<ICapabilityMissileStack> cap = stack.getCapability(ICBMClassicAPI.MISSILE_STACK_CAPABILITY, null);
        if (cap.isPresent() && cap.orElseThrow(IllegalStateException::new) instanceof CapabilityHeldItemMissile) {
            ((CapabilityHeldItemMissile) cap.orElseThrow(IllegalStateException::new)).setHeldItem(data);
        }
        return stack;
    }
}

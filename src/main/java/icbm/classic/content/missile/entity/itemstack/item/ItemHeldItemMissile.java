package icbm.classic.content.missile.entity.itemstack.item;

import icbm.classic.api.ICBMClassicAPI;
import icbm.classic.api.missiles.ICapabilityMissileStack;
import icbm.classic.api.missiles.projectile.IProjectileData;
import icbm.classic.api.missiles.projectile.IProjectileStack;
import icbm.classic.content.cargo.ProjectileCargoMode;
import icbm.classic.content.cargo.balloon.BalloonProjectileData;
import icbm.classic.content.cargo.parachute.EntityParachute;
import icbm.classic.content.cargo.parachute.ParachuteProjectileData;
import icbm.classic.content.reg.ItemReg;
import icbm.classic.lib.LanguageUtility;
import icbm.classic.lib.projectile.ProjectileStack;
import icbm.classic.prefab.item.ItemICBMBase;
import icbm.classic.prefab.item.ItemStackCapProvider;
import net.minecraft.client.Minecraft;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.NonNullList;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.List;

public class ItemHeldItemMissile extends ItemICBMBase {
    public ItemHeldItemMissile() {
        super("held_item_missile");
        this.setMaxStackSize(1);
        this.setHasSubtypes(true);
    }

    @Override
    public boolean showDurabilityBar(ItemStack stack)
    {
        final ICapabilityMissileStack cap = stack.getCapability(ICBMClassicAPI.MISSILE_STACK_CAPABILITY, null);
        if (cap instanceof CapabilityHeldItemMissile) {
            final ItemStack heldItem = ((CapabilityHeldItemMissile) cap).getHeldItem();
            return heldItem.getItem().showDurabilityBar(heldItem);
        }
        return stack.isItemDamaged();
    }

    @Override
    public double getDurabilityForDisplay(ItemStack stack)
    {
        final ICapabilityMissileStack cap = stack.getCapability(ICBMClassicAPI.MISSILE_STACK_CAPABILITY, null);
        if (cap instanceof CapabilityHeldItemMissile) {
            final ItemStack heldItem = ((CapabilityHeldItemMissile) cap).getHeldItem();
            return heldItem.getItem().getDurabilityForDisplay(heldItem);
        }
        return super.getDurabilityForDisplay(stack);
    }

    @Override
    @Nullable
    public net.minecraftforge.common.capabilities.ICapabilityProvider initCapabilities(ItemStack stack, @Nullable NBTTagCompound nbt) {
        final ItemStackCapProvider provider = new ItemStackCapProvider(stack);
        provider.add("missile", ICBMClassicAPI.MISSILE_STACK_CAPABILITY, new CapabilityHeldItemMissile());
        return provider;
    }

    @Override
    public String getUnlocalizedName(ItemStack stack) {
        final ICapabilityMissileStack cap = stack.getCapability(ICBMClassicAPI.MISSILE_STACK_CAPABILITY, null);
        if (cap instanceof CapabilityHeldItemMissile && (((CapabilityHeldItemMissile) cap).getHeldItem().getItem() instanceof ItemSword)) {
            return super.getUnlocalizedName(stack) + ".sword";
            //TODO attempt to localize all items to allow customized naming
        }
        return super.getUnlocalizedName(stack);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, World world, List<String> list, ITooltipFlag flag) {
        final ICapabilityMissileStack cap = stack.getCapability(ICBMClassicAPI.MISSILE_STACK_CAPABILITY, null);

        // Only show basic info if we have no projectile data
        if (!(cap instanceof CapabilityHeldItemMissile) || ((CapabilityHeldItemMissile) cap).getHeldItem().isEmpty()) {
            LanguageUtility.outputLines(new TextComponentTranslation(getUnlocalizedName() + ".info"), list::add);
        }

        // Show projectile information
        if (cap instanceof CapabilityHeldItemMissile && !((CapabilityHeldItemMissile) cap).getHeldItem().isEmpty()) {
            final ItemStack heldItem = ((CapabilityHeldItemMissile) cap).getHeldItem();


            LanguageUtility.outputLines(
                new TextComponentTranslation(
                    getUnlocalizedName() + ".held_item." + ((CapabilityHeldItemMissile) cap).getActionMode().name().toLowerCase()
                ), list::add);

            list.add("----");
            list.addAll(heldItem.getTooltip(Minecraft.getMinecraft().player, flag));
            list.add("----");
        }
    }

    @Override
    public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> items) {
        if (this.isInCreativeTab(tab)) {
            items.add(new ItemStack(this));
            items.add(createStack(new ItemStack(Items.DIAMOND_SWORD)));
            items.add(createStack(new ItemStack(Items.SHEARS)));
        }
    }

    private ItemStack createStack(ItemStack data) {
        final ItemStack stack = new ItemStack(this);
        final ICapabilityMissileStack cap = stack.getCapability(ICBMClassicAPI.MISSILE_STACK_CAPABILITY, null);
        if (cap instanceof CapabilityHeldItemMissile) {
            ((CapabilityHeldItemMissile) cap).setHeldItem(data);
        }
        return stack;
    }
}

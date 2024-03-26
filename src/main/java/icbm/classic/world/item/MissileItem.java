package icbm.classic.world.item;

import icbm.classic.api.ICBMClassicAPI;
import icbm.classic.api.caps.IExplosive;
import icbm.classic.api.reg.ExplosiveType;
import icbm.classic.lib.capability.ex.CapabilityExplosiveStack;
import icbm.classic.lib.capability.missile.CapabilityMissileStack;
import icbm.classic.prefab.item.ItemBase;
import icbm.classic.prefab.item.ItemStackCapProvider;
import icbm.classic.world.block.explosive.ItemBlockExplosive;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.NonNullList;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;
import java.util.List;

public class MissileItem extends ItemBase {
    public MissileItem(Properties properties) {
        super(properties);

        this.setMaxDamage(0);
        this.setHasSubtypes(true);
        this.setMaxStackSize(1);
    }

    @Override
    @Nullable
    public net.neoforged.common.capabilities.ICapabilityProvider initCapabilities(ItemStack stack, @Nullable CompoundTag nbt) {
        final ItemStackCapProvider provider = new ItemStackCapProvider(stack);
        provider.add("explosive", ICBMClassicAPI.EXPLOSIVE_CAPABILITY, new CapabilityExplosiveStack(stack));
        provider.add("missile", ICBMClassicAPI.MISSILE_STACK_CAPABILITY, new CapabilityMissileStack(stack));
        return provider;
    }

    @Override
    public int getMetadata(int damage) {
        return damage;
    }

    @Override
    public String getUnlocalizedName(ItemStack itemstack) {
        if (itemstack.hasCapability(ICBMClassicAPI.EXPLOSIVE_CAPABILITY, null)) {
            final IExplosive explosive = itemstack.getCapability(ICBMClassicAPI.EXPLOSIVE_CAPABILITY, null);
            if (explosive != null) {
                final ExplosiveType data = explosive.getExplosiveData();
                if (data != null) {
                    return "missile." + data.getRegistryName();
                }
            }
        }
        return "missile";
    }

    @Override
    public String getUnlocalizedName() {
        return "missile";
    }

    @Override
    public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> items) {
        if (tab == getCreativeTab() || tab == CreativeTabs.SEARCH) {
            for (int id : ICBMClassicAPI.EX_MISSILE_REGISTRY.getExplosivesIDs()) {
                items.add(new ItemStack(this, 1, id));
            }
            items.add(new ItemStack(this, 1, 24)); //TODO fix work around for missile module not counting as a missile
        }
    }

    @Override
    protected boolean hasDetailedInfo(ItemStack stack, Player player) {
        return true;
    }

    @Override
    protected void getDetailedInfo(ItemStack stack, Player player, List list) {
        //TODO add hook
        ((ItemBlockExplosive) Item.getItemFromBlock(BlockReg.blockExplosive)).getDetailedInfo(stack, player, list);
    }
}

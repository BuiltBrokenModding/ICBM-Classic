package icbm.classic.content.cluster.bomblet;

import icbm.classic.api.ICBMClassicAPI;
import icbm.classic.api.ICBMClassicHelpers;
import icbm.classic.api.caps.IExplosive;
import icbm.classic.content.blocks.explosive.ItemBlockExplosive;
import icbm.classic.content.items.ExplosiveItemHelper;
import icbm.classic.content.reg.ItemReg;
import icbm.classic.lib.capability.ex.CapabilityExplosiveStack;
import icbm.classic.lib.capability.holder.ICapabilityHolderItem;
import icbm.classic.prefab.item.ISubItemProvider;
import icbm.classic.prefab.item.ItemBase;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.capabilities.ItemCapability;

import javax.annotation.Nullable;
import java.util.List;

public class ItemBombDroplet extends ItemBase implements ICapabilityHolderItem, ISubItemProvider {

    public ItemBombDroplet()
    {
        super(new Item.Properties().stacksTo(16));
    }

    @Nullable
    @Override
    public <T> T getCapability(ItemCapability<T, Void> capability, ItemStack stack) {
        if (capability == ICBMClassicAPI.EXPLOSIVE_ITEM_CAPABILITY) {
            return (T) new CapabilityExplosiveStack(stack);
        }
        if (capability == ICBMClassicAPI.PROJECTILE_STACK_CAPABILITY) {
            return (T) new BombletProjectileStack(stack::copy);
        }
        return null;
    }

    @Override
    public String getDescriptionId(ItemStack itemstack)
    {
        final IExplosive explosive = itemstack.getCapability(ICBMClassicAPI.EXPLOSIVE_ITEM_CAPABILITY);
        if (explosive != null && explosive.getExplosiveData() != null)
        {
            return "bomblet." + explosive.getExplosiveData().getRegistryKey();
        }
        return super.getDescriptionId();
    }

    @Override
    public void getSubItems(NonNullList<ItemStack> items)
    {
        ExplosiveItemHelper.addSubItems(this, ICBMClassicAPI.EX_GRENADE_REGISTRY, items);
    }

    @Override
    protected boolean hasDetailedInfo(ItemStack stack, @Nullable Player player)
    {
        return true;
    }

    @Override
    protected void getDetailedInfo(ItemStack stack, @Nullable Player player, List<Component> list)
    {
        //TODO add hook
        ItemReg.itemBlockExplosive.get().getDetailedInfo(stack, player, list);
        final IExplosive explosive = ICBMClassicHelpers.getExplosive(stack);
        if(explosive != null) { //TODO make shift-key display?
            explosive.collectInformation(line -> list.add(Component.literal(line)));
        }
    }
}
// PORTED

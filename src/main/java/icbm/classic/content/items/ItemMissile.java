package icbm.classic.content.items;

import icbm.classic.api.ICBMClassicAPI;
import icbm.classic.api.ICBMClassicHelpers;
import icbm.classic.api.caps.IExplosive;
import icbm.classic.content.reg.ComponentReg;
import icbm.classic.content.reg.ItemReg;
import icbm.classic.lib.capability.ex.CapabilityExplosiveStack;
import icbm.classic.lib.capability.holder.ICapabilityHolderItem;
import icbm.classic.lib.capability.missile.CapabilityMissileStack;
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

public class ItemMissile extends ItemBase implements ICapabilityHolderItem, ISubItemProvider
{
    public ItemMissile()
    {
        super(new Item.Properties().stacksTo(1));
    }

    @Nullable
    @Override
    @SuppressWarnings("unchecked")
    public <T> T getCapability(ItemCapability<T, Void> capability, ItemStack stack)
    {
        if (capability == ICBMClassicAPI.EXPLOSIVE_ITEM_CAPABILITY)
        {
            return (T) new CapabilityExplosiveStack(stack);
        }
        else if (capability == ICBMClassicAPI.MISSILE_STACK_CAPABILITY)
        {
            return (T) new CapabilityMissileStack(stack);
        }
        return null;
    }

    @Override
    public String getDescriptionId(ItemStack itemstack)
    {
        return ExplosiveItemHelper.getVariantKey("missile", itemstack);
    }

    @Override
    public String getDescriptionId()
    {
        return "missile";
    }

    @Override
    public void getSubItems(NonNullList<ItemStack> items)
    {
        ExplosiveItemHelper.addSubItems(this, ICBMClassicAPI.EX_MISSILE_REGISTRY, items);

        //TODO fix work around for missile module not counting as a missile
        final ItemStack module = ExplosiveItemHelper.create(this, 24);
        if (module.has(ComponentReg.EXPLOSIVE.get()) && items.stream().noneMatch(s -> ItemStack.isSameItemSameComponents(s, module)))
        {
            items.add(module);
        }
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

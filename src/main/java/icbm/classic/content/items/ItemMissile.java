package icbm.classic.content.items;

import icbm.classic.api.ICBMClassicAPI;
import icbm.classic.api.ICBMClassicHelpers;
import icbm.classic.api.caps.IExplosive;
import icbm.classic.api.refs.ICBMExplosives;
import icbm.classic.api.reg.IExplosiveData;
import icbm.classic.content.blast.cluster.ClusterCustomization;
import icbm.classic.content.blast.cluster.bomblet.BombletProjectileData;
import icbm.classic.content.blocks.explosive.ItemBlockExplosive;
import icbm.classic.content.cargo.parachute.ParachuteProjectileData;
import icbm.classic.content.reg.BlockReg;
import icbm.classic.content.reg.ItemReg;
import icbm.classic.lib.capability.ex.CapabilityExplosiveStack;
import icbm.classic.lib.capability.missile.CapabilityMissileStack;
import icbm.classic.lib.projectile.vanilla.ArrowProjectileData;
import icbm.classic.prefab.item.ItemBase;
import icbm.classic.prefab.item.ItemStackCapProvider;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.NonNullList;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;

public class ItemMissile extends ItemBase
{
    public ItemMissile()
    {
        this.setMaxDamage(0);
        this.setHasSubtypes(true);
        this.setMaxStackSize(1);
    }

    @Override
    @Nullable
    public net.minecraftforge.common.capabilities.ICapabilityProvider initCapabilities(ItemStack stack, @Nullable NBTTagCompound nbt)
    {
        final ItemStackCapProvider provider = new ItemStackCapProvider(stack);
        provider.add("explosive", ICBMClassicAPI.EXPLOSIVE_CAPABILITY, new CapabilityExplosiveStack(stack));
        provider.add("missile", ICBMClassicAPI.MISSILE_STACK_CAPABILITY, new CapabilityMissileStack(stack));
        return provider;
    }

    @Override
    public int getMetadata(int damage)
    {
        return damage;
    }

    @Override
    public String getUnlocalizedName(ItemStack itemstack)
    {
        if (itemstack.hasCapability(ICBMClassicAPI.EXPLOSIVE_CAPABILITY, null))
        {
            final IExplosive explosive = itemstack.getCapability(ICBMClassicAPI.EXPLOSIVE_CAPABILITY, null);
            if (explosive != null)
            {
                final IExplosiveData data = explosive.getExplosiveData();
                if (data != null)
                {
                    return "missile." + data.getRegistryName();
                }
            }
        }
        return "missile";
    }

    @Override
    public String getUnlocalizedName()
    {
        return "missile";
    }

    @Override
    public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> items)
    {
        if (tab == getCreativeTab() || tab == CreativeTabs.SEARCH)
        {
            for (int id : ICBMClassicAPI.EX_MISSILE_REGISTRY.getExplosivesIDs())
            {
                items.add(new ItemStack(this, 1, id));
            }
            items.add(new ItemStack(this, 1, 24)); //TODO fix work around for missile module not counting as a missile

            // Customized cluster type TODO remove after testing
            final ItemStack clusterArrows = new ItemStack(this, 1, ICBMExplosives.CLUSTER.getRegistryID());
            Optional.ofNullable(ICBMClassicHelpers.getExplosive(clusterArrows)).ifPresent(e -> {
                e.addCustomization(new ClusterCustomization()
                    .setProjectilesToSpawn(200)
                    .setProjectilesPerLayer(20)
                    .setProjectileData(ICBMClassicAPI.PROJECTILE_DATA_REGISTRY.build(ArrowProjectileData.NAME))
                    .setAllowPickupItems(false)
                );
            });
            items.add(clusterArrows);

            final ItemStack clusterBomblets = new ItemStack(this, 1, ICBMExplosives.CLUSTER.getRegistryID());
            Optional.ofNullable(ICBMClassicHelpers.getExplosive(clusterBomblets)).ifPresent(e -> {
                e.addCustomization(new ClusterCustomization()
                    .setProjectilesToSpawn(100)
                    .setProjectilesPerLayer(10)
                    .setProjectileData(new BombletProjectileData().setExplosiveStack(new ItemStack(ItemReg.itemBomblet)))
                    .setAllowPickupItems(false)
                );
            });
            items.add(clusterBomblets);

            final ItemStack parachute = new ItemStack(this, 1, ICBMExplosives.CLUSTER.getRegistryID());
            Optional.ofNullable(ICBMClassicHelpers.getExplosive(parachute)).ifPresent(e -> {
                e.addCustomization(new ClusterCustomization()
                    .setProjectilesToSpawn(9)
                    .setProjectilesPerLayer(3)
                    .setProjectileData(new ParachuteProjectileData().setHeldItem(new ItemStack(ItemReg.itemBomblet)))
                    .setAllowPickupItems(false)
                );
            });
            items.add(parachute);
        }
    }

    @Override
    protected boolean hasDetailedInfo(ItemStack stack, EntityPlayer player)
    {
        return true;
    }

    @Override
    protected void getDetailedInfo(ItemStack stack, EntityPlayer player, List list)
    {
        //TODO add hook
        ((ItemBlockExplosive) Item.getItemFromBlock(BlockReg.blockExplosive)).getDetailedInfo(stack, player, list);
        final IExplosive explosive = ICBMClassicHelpers.getExplosive(stack);
        if(explosive != null) { //TODO make shift-key display?
            explosive.collectInformation(list::add);
        }
    }
}

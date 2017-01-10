package icbm.classic.content.items;

import com.builtbroken.mc.lib.helper.LanguageUtility;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import icbm.classic.ICBMClassic;
import icbm.classic.content.entity.EntityGrenade;
import icbm.classic.content.explosive.Explosive;
import icbm.classic.content.explosive.ExplosiveRegistry;
import icbm.classic.content.explosive.Explosives;
import icbm.classic.prefab.item.ItemICBMBase;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumAction;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.IIcon;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import resonant.api.explosion.ExplosionEvent.ExplosivePreDetonationEvent;
import resonant.api.explosion.ExplosiveType;

import java.util.List;

public class ItemGrenade extends ItemICBMBase
{
    @SideOnly(Side.CLIENT)
    public static IIcon[] ICONS;

    public ItemGrenade()
    {
        super("grenade");
        this.setMaxStackSize(16);
        this.setMaxDamage(0);
        this.setHasSubtypes(true);
    }

    @Override
    public ItemStack onEaten(ItemStack par1ItemStack, World par2World, EntityPlayer par3EntityPlayer)
    {
        return par1ItemStack;
    }

    @Override
    public EnumAction getItemUseAction(ItemStack par1ItemStack)
    {
        return EnumAction.bow;
    }

    @Override
    public int getMaxItemUseDuration(ItemStack par1ItemStack)
    {
        return 3 * 20;
    }

    @Override
    public ItemStack onItemRightClick(ItemStack itemStack, World world, EntityPlayer entityPlayer)
    {
        if (itemStack != null)
        {
            Explosive zhaPin = ExplosiveRegistry.get(itemStack.getItemDamage());
            ExplosivePreDetonationEvent evt = new ExplosivePreDetonationEvent(world, entityPlayer, ExplosiveType.ITEM, zhaPin);
            MinecraftForge.EVENT_BUS.post(evt);

            if (!evt.isCanceled())
            {
                entityPlayer.setItemInUse(itemStack, this.getMaxItemUseDuration(itemStack));
            }
            else
            {
                entityPlayer.addChatMessage(new ChatComponentText("Grenades are banned in this region."));
            }
        }

        return itemStack;
    }

    @Override
    public void onPlayerStoppedUsing(ItemStack itemStack, World world, EntityPlayer entityPlayer, int nengLiang)
    {
        if (!world.isRemote)
        {
            Explosives zhaPin = Explosives.get(itemStack.getItemDamage());
            ExplosivePreDetonationEvent evt = new ExplosivePreDetonationEvent(world, entityPlayer, ExplosiveType.ITEM, zhaPin.handler);
            MinecraftForge.EVENT_BUS.post(evt);

            if (!evt.isCanceled())
            {
                if (!entityPlayer.capabilities.isCreativeMode)
                {
                    itemStack.stackSize--;

                    if (itemStack.stackSize <= 0)
                    {
                        entityPlayer.inventory.setInventorySlotContents(entityPlayer.inventory.currentItem, null);
                    }
                }

                world.playSoundAtEntity(entityPlayer, "random.fuse", 0.5F, 0.4F / (itemRand.nextFloat() * 0.4F + 0.8F));
                world.spawnEntityInWorld(new EntityGrenade(world, entityPlayer, zhaPin, (float) (this.getMaxItemUseDuration(itemStack) - nengLiang) / (float) this.getMaxItemUseDuration(itemStack)));
            }
            else
            {
                entityPlayer.addChatMessage(new ChatComponentText("Grenades are banned in this region."));
            }
        }
    }

    @Override
    public int getMetadata(int damage)
    {
        return damage;
    }

    @Override
    public String getUnlocalizedName(ItemStack itemstack)
    {
        return this.getUnlocalizedName() + "." + ExplosiveRegistry.get(itemstack.getItemDamage()).getUnlocalizedName();
    }

    @Override
    public String getUnlocalizedName()
    {
        return "icbm.grenade";
    }

    @Override
    public void addInformation(ItemStack par1ItemStack, EntityPlayer par2EntityPlayer, List par3List, boolean par4)
    {
        int explosiveTier = ExplosiveRegistry.get(par1ItemStack.getItemDamage()).getTier();
        par3List.add(LanguageUtility.getLocal("info.misc.tier") + ": " + explosiveTier);
		super.addInformation(par1ItemStack, par2EntityPlayer, par3List, par4);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void registerIcons(IIconRegister iconRegister)
    {
        ICONS = new IIcon[Explosives.values().length];
        for (Explosives ex : Explosives.values())
        {
            ICONS[ex.ordinal()] = iconRegister.registerIcon(ICBMClassic.PREFIX + "grenade_" + ex.handler.getUnlocalizedName());
        }
    }

    @Override
    public IIcon getIconFromDamage(int i)
    {
        return ICONS[i];
    }

    @Override
    public void getSubItems(Item par1, CreativeTabs par2CreativeTabs, List par3List)
    {
        for (Explosives ex : Explosives.values())
        {
            if (ex.handler.hasGrenadeForm())
            {
                par3List.add(new ItemStack(par1, 1, ex.ordinal()));
            }
        }
    }
}

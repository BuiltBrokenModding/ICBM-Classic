package icbm.classic.content.items;

import icbm.classic.lib.NBTConstants;
import icbm.classic.lib.LanguageUtility;
import icbm.classic.prefab.item.ItemICBMElectrical;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.List;

public class ItemTracker extends ItemICBMElectrical
{
    //private static final long ENERGY_PER_TICK = 1;

    public ItemTracker()
    {
        super("tracker");
        this.setMaxStackSize(1);
        //TODO use ItemCompass render to aim icon towards target
    }

    @SideOnly(Side.CLIENT)
    @Override
    protected void getDetailedInfo(ItemStack stack, EntityPlayer player, List lines)
    {
        Entity trackingEntity = getTrackingEntity(FMLClientHandler.instance().getClient().world, stack);

        if (trackingEntity != null)
        {
            lines.add(LanguageUtility.getLocal("info.tracker.tracking") + " " + trackingEntity.getName());
        }

        lines.add(LanguageUtility.getLocal("info.tracker.tooltip"));
    }


    public void setTrackingEntity(ItemStack itemStack, Entity entity)
    {
        if (itemStack.getTagCompound() == null)
        {
            itemStack.setTagCompound(new NBTTagCompound());
        }

        if (entity != null)
        {
            itemStack.getTagCompound().setInteger(NBTConstants.TRACKING_ENTITY, entity.getEntityId());
        }
    }


    public Entity getTrackingEntity(World worldObj, ItemStack itemStack)
    {
        if (worldObj != null)
        {
            if (itemStack.getTagCompound() != null)
            {
                int trackingID = itemStack.getTagCompound().getInteger(NBTConstants.TRACKING_ENTITY);
                return worldObj.getEntityByID(trackingID);
            }
        }
        return null;
    }

    @Override
    public void onCreated(ItemStack par1ItemStack, World par2World, EntityPlayer par3EntityPlayer)
    {
        super.onCreated(par1ItemStack, par2World, par3EntityPlayer);
        setTrackingEntity(par1ItemStack, par3EntityPlayer);
    }

    @Override
    public void onUpdate(ItemStack itemStack, World par2World, Entity par3Entity, int par4, boolean par5)
    {
        super.onUpdate(itemStack, par2World, par3Entity, par4, par5);

        if (par3Entity instanceof EntityPlayer)
        {
            EntityPlayer player = (EntityPlayer) par3Entity;

            if (player.inventory.getCurrentItem() != null)
            {
                if (player.inventory.getCurrentItem().getItem() == this && par2World.getWorldTime() % 20 == 0)
                {
                    Entity trackingEntity = this.getTrackingEntity(par2World, itemStack);

                    if (trackingEntity != null)
                    {
                        //if (this.discharge(itemStack, ENERGY_PER_TICK, true) < ENERGY_PER_TICK)
                        //{
                        this.setTrackingEntity(itemStack, null);
                        //}
                    }
                }
            }
        }
    }

    /**
     * Called when the player Left Clicks (attacks) an entity. Processed before damage is done, if
     * return value is true further processing is canceled and the entity is not attacked.
     *
     * @param itemStack The Item being used
     * @param player    The player that is attacking
     * @param entity    The entity being attacked
     * @return True to cancel the rest of the interaction.
     */
    @Override
    public boolean onLeftClickEntity(ItemStack itemStack, EntityPlayer player, Entity entity)
    {
        if (!player.world.isRemote)
        {
            boolean flag_ban = false;//FlagRegistry.getModFlag().getFlagWorld(player.worldObj).containsValue("ban_Tracker", "true", new Pos(entity));
            if (!flag_ban)
            {
                //if (this.getEnergy(itemStack) > ENERGY_PER_TICK)
                //{
                setTrackingEntity(itemStack, entity);
                player.sendMessage(new TextComponentString(LanguageUtility.getLocal("message.tracker.nowtrack") + " " + entity.getName())); //TODO use injection point for name
                return true;
                //}
                //else
                //{
                //    player.addChatMessage(LanguageUtility.getLocal("message.tracker.nopower"));
                //}
            }
            else
            {
                player.sendMessage(new TextComponentString(LanguageUtility.getLocal("message.tracker.banned")));
            }
        }

        return false;
    }
}

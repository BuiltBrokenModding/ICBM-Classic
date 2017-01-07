package icbm.classic.content.entity;

import com.builtbroken.mc.api.event.TriggerCause;
import com.builtbroken.mc.api.explosive.IExplosiveContainer;
import com.builtbroken.mc.api.explosive.IExplosiveHandler;
import com.builtbroken.mc.api.items.explosives.IExplosiveContainerItem;
import com.builtbroken.mc.api.items.explosives.IExplosiveItem;
import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.common.registry.IEntityAdditionalSpawnData;
import icbm.explosion.ICBMExplosion;
import io.netty.buffer.ByteBuf;
import net.minecraft.block.Block;
import net.minecraft.entity.item.EntityMinecartTNT;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;

public class EntityBombCart extends EntityMinecartTNT implements IEntityAdditionalSpawnData, IExplosiveContainer
{
    public ItemStack explosiveStack;

    public EntityBombCart(World par1World)
    {
        super(par1World);
    }

    public EntityBombCart(World par1World, double x, double y, double z, ItemStack explosiveCartStack)
    {
        super(par1World, x, y, z);
        if (explosiveCartStack.getItem() instanceof IExplosiveContainerItem)
        {
            this.explosiveStack = ((IExplosiveContainerItem) explosiveCartStack.getItem()).getExplosiveStack(explosiveCartStack);
        }
        else
        {
            this.explosiveStack = explosiveCartStack;
        }
    }

    @Override
    public void writeSpawnData(ByteBuf data)
    {
        data.writeBoolean(explosiveStack != null);
        if (explosiveStack != null)
        {
            ByteBufUtils.writeItemStack(data, explosiveStack);
        }
    }

    @Override
    public void readSpawnData(ByteBuf data)
    {
        if (data.readBoolean())
        {
            explosiveStack = ByteBufUtils.readItemStack(data);
        }
        else
        {
            explosiveStack = null;
        }
    }

    @Override
    protected void explodeCart(double par1)
    {
        // TODO add event
        this.worldObj.spawnParticle("hugeexplosion", this.posX, this.posY, this.posZ, 0.0D, 0.0D, 0.0D);
        IExplosiveHandler handler = this.getExplosiveType();
        if (handler != null)
        {
            handler.createBlastForTrigger(this.worldObj, this.posX, this.posY, this.posZ, new TriggerCause.TriggerCauseEntity(this), getSize(), getExplosiveNBT());
            this.setDead();
        }
    }

    public boolean interact(EntityPlayer player)
    {
        if (player.getCurrentEquippedItem() != null)
        {
            if (player.getCurrentEquippedItem().getItem() == Items.flint_and_steel)
            {
                this.ignite();
                return true;
            }
        }
        return false;
    }

    @Override
    public void killMinecart(DamageSource par1DamageSource)
    {
        this.setDead();
        ItemStack itemstack = new ItemStack(Items.minecart, 1);

        this.entityDropItem(itemstack, 0.0F);

        double d0 = this.motionX * this.motionX + this.motionZ * this.motionZ;

        if (!par1DamageSource.isExplosion())
        {
            this.entityDropItem(getCartItem(), 0.0F);
        }

        if (par1DamageSource.isFireDamage() || par1DamageSource.isExplosion() || d0 >= 0.009999999776482582D)
        {
            this.explodeCart(d0);
        }
    }

    @Override
    public ItemStack getCartItem()
    {
        ItemStack cartStack = new ItemStack(ICBMExplosion.itemBombCart);
        cartStack.getTagCompound().setTag("explosive", explosiveStack.writeToNBT(new NBTTagCompound()));
        return cartStack;
    }

    @Override
    protected void writeEntityToNBT(NBTTagCompound nbt)
    {
        super.writeEntityToNBT(nbt);
        if (explosiveStack != null)
        {
            nbt.setTag("explosive", explosiveStack.writeToNBT(new NBTTagCompound()));
        }
    }

    @Override
    protected void readEntityFromNBT(NBTTagCompound nbt)
    {
        super.readEntityFromNBT(nbt);
        if (nbt.hasKey("explosive"))
        {
            explosiveStack = ItemStack.loadItemStackFromNBT(nbt.getCompoundTag("explosive"));
        }
    }

    public IExplosiveHandler getExplosiveType()
    {
        if (explosiveStack != null && explosiveStack.getItem() instanceof IExplosiveItem)
        {
            return ((IExplosiveItem) explosiveStack.getItem()).getExplosive(explosiveStack);
        }
        return null;
    }

    public double getSize()
    {
        if (explosiveStack != null && explosiveStack.getItem() instanceof IExplosiveItem)
        {
            return ((IExplosiveItem) explosiveStack.getItem()).getExplosiveSize(explosiveStack);
        }
        return 1;
    }

    public NBTTagCompound getExplosiveNBT()
    {
        if (explosiveStack != null && explosiveStack.getItem() instanceof IExplosiveItem)
        {
            return ((IExplosiveItem) explosiveStack.getItem()).getAdditionalExplosiveData(explosiveStack);
        }
        return new NBTTagCompound();
    }

    @Override
    public Block func_145817_o()
    {
        return ICBMExplosion.blockExplosive;
    }

    @Override
    public ItemStack getExplosiveStack()
    {
        return explosiveStack;
    }

    @Override
    public boolean setExplosiveStack(ItemStack stack)
    {
        explosiveStack = stack;
        return true;
    }
}

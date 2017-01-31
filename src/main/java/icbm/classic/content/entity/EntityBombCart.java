package icbm.classic.content.entity;

import cpw.mods.fml.common.registry.IEntityAdditionalSpawnData;
import icbm.classic.ICBMClassic;
import icbm.classic.content.explosive.Explosives;
import io.netty.buffer.ByteBuf;
import net.minecraft.block.Block;
import net.minecraft.entity.item.EntityMinecartTNT;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;

public class EntityBombCart extends EntityMinecartTNT implements IEntityAdditionalSpawnData
{
    public Explosives explosive;

    public EntityBombCart(World par1World)
    {
        super(par1World);
    }

    public EntityBombCart(World par1World, double x, double y, double z, Explosives explosive)
    {
        super(par1World, x, y, z);
        this.explosive = explosive;
    }

    @Override
    public void writeSpawnData(ByteBuf data)
    {
        data.writeInt(explosive.ordinal());
    }

    @Override
    public void readSpawnData(ByteBuf data)
    {
        explosive = Explosives.get(data.readInt());
    }

    @Override
    protected void explodeCart(double par1)
    {
        // TODO add event
        this.worldObj.spawnParticle("hugeexplosion", this.posX, this.posY, this.posZ, 0.0D, 0.0D, 0.0D);
        explosive.handler.createExplosion(worldObj, posX, posY, posZ, this);
        this.setDead();
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
        if(!worldObj.isRemote)
        {
            this.setDead();
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
    }

    @Override
    public ItemStack getCartItem()
    {
        return new ItemStack(ICBMClassic.itemBombCart, 1, explosive.ordinal());
    }

    @Override
    protected void writeEntityToNBT(NBTTagCompound nbt)
    {
        super.writeEntityToNBT(nbt);
        nbt.setInteger("explosive", explosive.ordinal());
    }

    @Override
    protected void readEntityFromNBT(NBTTagCompound nbt)
    {
        super.readEntityFromNBT(nbt);
        explosive = Explosives.get(nbt.getInteger("explosive"));
    }

    @Override
    public Block func_145817_o()
    {
        return ICBMClassic.blockExplosive;
    }
}

package icbm.classic.content.entity;

import icbm.classic.ICBMClassic;
import icbm.classic.api.ICBMClassicHelpers;
import icbm.classic.lib.explosive.ExplosiveHandler;
import icbm.classic.content.blocks.explosive.BlockExplosive;
import icbm.classic.prefab.tile.BlockICBM;
import io.netty.buffer.ByteBuf;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.item.EntityMinecartTNT;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.IEntityAdditionalSpawnData;

public class EntityBombCart extends EntityMinecartTNT implements IEntityAdditionalSpawnData
{
    public int explosive = -1;
    public NBTTagCompound data;

    public EntityBombCart(World par1World)
    {
        super(par1World);
    }

    public EntityBombCart(World par1World, double x, double y, double z, int explosive)
    {
        super(par1World, x, y, z);
        this.explosive = explosive;
    }

    @Override
    public void writeSpawnData(ByteBuf data)
    {
        data.writeInt(explosive);
    }

    @Override
    public void readSpawnData(ByteBuf data)
    {
        explosive = data.readInt();
    }

    @Override
    protected void explodeCart(double par1)
    {
        // TODO add event
        this.world.spawnParticle(EnumParticleTypes.EXPLOSION_HUGE, this.posX, this.posY, this.posZ, 0.0D, 0.0D, 0.0D);
        ExplosiveHandler.createExplosion(this, world, posX, posY, posZ, explosive, 1, data);
        this.setDead();
    }

    @Override
    public void killMinecart(DamageSource par1DamageSource)
    {
        if(!world.isRemote)
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
    public EntityItem entityDropItem(ItemStack stack, float offsetY)
    {
        if(stack.getItem() == Item.getItemFromBlock(Blocks.TNT))
        {
            return super.entityDropItem(getCartItem(), offsetY);
        }
        return entityDropItem(stack, offsetY);
    }

    @Override
    public ItemStack getCartItem()
    {
        return new ItemStack(ICBMClassic.itemBombCart, 1, explosive);
    }

    @Override
    protected void writeEntityToNBT(NBTTagCompound nbt)
    {
        super.writeEntityToNBT(nbt);
        nbt.setInteger("explosive", explosive);
    }

    @Override
    protected void readEntityFromNBT(NBTTagCompound nbt)
    {
        super.readEntityFromNBT(nbt);
        explosive = nbt.getInteger("explosive");
    }

    @Override
    public IBlockState getDefaultDisplayTile()
    {
        return ICBMClassic.blockExplosive.getDefaultState()
                .withProperty(BlockExplosive.EX_PROP, ICBMClassicHelpers.getExplosive(explosive, false))
                .withProperty(BlockICBM.ROTATION_PROP, EnumFacing.UP);
    }
}

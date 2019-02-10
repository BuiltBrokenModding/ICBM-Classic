package icbm.classic.content.entity;

import icbm.classic.ICBMClassic;
import icbm.classic.content.explosive.Explosives;
import icbm.classic.content.explosive.tile.BlockExplosive;
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
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.IEntityAdditionalSpawnData;

public class EntityBombCart extends EntityMinecartTNT implements IEntityAdditionalSpawnData
{
    public Explosives explosive = Explosives.CONDENSED;

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
        this.world.spawnParticle(EnumParticleTypes.EXPLOSION_HUGE, this.posX, this.posY, this.posZ, 0.0D, 0.0D, 0.0D);
        explosive.handler.createExplosion(world, new BlockPos(posX, posY, posZ), this, 1);
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
        return super.entityDropItem(stack, offsetY);
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
    public IBlockState getDefaultDisplayTile()
    {
        return ICBMClassic.blockExplosive.getDefaultState().withProperty(BlockExplosive.EX_PROP, explosive).withProperty(BlockICBM.ROTATION_PROP, EnumFacing.UP);
    }
}

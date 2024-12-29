package icbm.classic.content.entity;

import icbm.classic.api.actions.IActionData;
import icbm.classic.content.missile.logic.source.cause.EntityCause;
import icbm.classic.lib.actions.PotentialAction;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.item.minecart.TNTMinecartEntity;
import net.minecraft.util.*;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

import java.util.function.Supplier;

public class EntityBombCart extends TNTMinecartEntity
{
    private final PotentialAction explodeAction = new PotentialAction();  //TODO expose through capability
    private final BlockState mimicBlock;
    private final Supplier<ItemStack> cartStack;

    //TODO add custom fuse timers

    public EntityBombCart(World par1World, IActionData triggerAction, BlockState mimicBlock, Supplier<ItemStack> cartStack)
    {
        super(par1World);
        explodeAction.setActionData(triggerAction);
        this.mimicBlock = mimicBlock;
        this.cartStack = cartStack;
    }

    @Override
    protected void explodeCart(double par1)
    {
        explodeAction.doAction(world, this.posX, this.posY, this.posZ, new EntityCause(this)); //TODO handle output and include trigger source & player of the cart
        this.remove();
    }

    @Override
    public void killMinecart(DamageSource par1DamageSource)
    {
        if (!world.isRemote)
        {
            this.remove();
            double d0 = this.getMotion().x * this.getMotion().x + this.getMotion().z * this.getMotion().z;

            if (par1DamageSource.isFireDamage() || par1DamageSource.isExplosion() || d0 >= 0.009999999776482582D)
            {
                this.explodeCart(d0);
            }
            else {
                this.entityDropItem(getCartItem(), 0.0F);
            }
        }
    }

    @Override
    public ItemEntity entityDropItem(ItemStack stack, float offsetY)
    {
        if (stack.getItem() == Item.getItemFromBlock(Blocks.TNT))
        {
            return super.entityDropItem(getCartItem(), offsetY);
        }
        return super.entityDropItem(stack, offsetY);
    }

    @Override
    public ItemStack getCartItem()
    {
        return cartStack.get();
    }

    @Override
    public BlockState getDefaultDisplayTile()
    {
        return mimicBlock;
    }
}

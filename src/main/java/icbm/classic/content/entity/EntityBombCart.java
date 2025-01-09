package icbm.classic.content.entity;

import icbm.classic.api.actions.IActionData;
import icbm.classic.content.missile.logic.source.cause.EntityCause;
import icbm.classic.lib.actions.PotentialAction;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.item.minecart.TNTMinecartEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.common.util.NonNullSupplier;

public class EntityBombCart extends TNTMinecartEntity
{
    private final PotentialAction explodeAction = new PotentialAction();  //TODO expose through capability
    private final LazyOptional<BlockState> mimicBlock;
    private final LazyOptional<ItemStack> cartStack;

    //TODO add custom fuse timers

    public EntityBombCart(EntityType<EntityBombCart> type, World par1World, IActionData triggerAction, NonNullSupplier<BlockState> mimicBlock, NonNullSupplier<ItemStack> cartStack)
    {
        super(type, par1World);
        explodeAction.setActionData(triggerAction);
        this.mimicBlock = LazyOptional.of(mimicBlock);
        this.cartStack = LazyOptional.of(cartStack);
    }

    @Override
    protected void explodeCart(double par1)
    {
        explodeAction.doAction(world, this.posX, this.posY, this.posZ, new EntityCause(this)); //TODO handle output and include trigger source & player of the cart
        this.remove();
    }

    @Override
    public void killMinecart(DamageSource damageSource)
    {
        if (!world.isRemote)
        {
            this.remove();
            double railVelocitySq = this.getMotion().x * this.getMotion().x + this.getMotion().z * this.getMotion().z;

            if (damageSource.isFireDamage() || damageSource.isExplosion() || railVelocitySq >= 0.009999999776482582D)
            {
                this.explodeCart(railVelocitySq);
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
        return cartStack.orElse(ItemStack.EMPTY);
    }

    @Override
    public BlockState getDefaultDisplayTile()
    {
        return mimicBlock.orElseThrow(IllegalStateException::new);
    }
}

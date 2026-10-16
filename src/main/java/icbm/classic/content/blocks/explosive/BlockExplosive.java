package icbm.classic.content.blocks.explosive;

import com.mojang.serialization.MapCodec;
import icbm.classic.ICBMClassic;
import icbm.classic.api.reg.IExplosiveData;
import icbm.classic.prefab.tile.BlockICBM;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.BlockHitResult;

import javax.annotation.Nullable;

/**
 * Explosive block. Rotation is a blockstate property, the explosive type lives in {@link TileEntityExplosive}.
 * <p>
 * PORT: the 1.12 EX_PROP blockstate property is removed (1.21 properties can't be registry driven). Models
 * read {@link TileEntityExplosive#EXPLOSIVE_PROPERTY} from model data instead.
 */
public class BlockExplosive extends BlockICBM
{
    public static final MapCodec<BlockExplosive> CODEC = simpleCodec(p -> new BlockExplosive());

    public BlockExplosive()
    {
        // 1.12 Material.TNT, hardness 2, resistance field 10 (= 2), cloth sound
        super("explosives", Properties.of().mapColor(MapColor.FIRE).strength(2f, 2f).sound(SoundType.WOOL));
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec()
    {
        return CODEC;
    }

    @Override
    public ItemStack getCloneItemStack(LevelReader world, BlockPos pos, BlockState state)
    {
        final BlockEntity tile = world.getBlockEntity(pos);
        if (tile instanceof TileEntityExplosive && ((TileEntityExplosive) tile).capabilityExplosive != null)
        {
            final ItemStack stack = ((TileEntityExplosive) tile).capabilityExplosive.toStack();
            if (stack != null && !stack.isEmpty())
            {
                return stack;
            }
        }
        return super.getCloneItemStack(world, pos, state);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context)
    {
        // PORT: 1.12 fell back to explosive id 0 and rewrote the stack when the id was invalid,
        //  the explosive is now read from the stack's component by the tile
        return defaultBlockState().setValue(ROTATION_PROP, context.getClickedFace());
    }

    /**
     * Called when the block is placed in the world.
     */
    @Override
    public void setPlacedBy(Level world, BlockPos pos, BlockState state, @Nullable LivingEntity entityLiving, ItemStack itemStack)
    {
        final BlockEntity tile = world.getBlockEntity(pos);
        if (tile instanceof TileEntityExplosive)
        {
            TileEntityExplosive explosive = (TileEntityExplosive) tile;
            explosive.setExplosiveStack(itemStack);

            if (world.hasNeighborSignal(pos))
            {
                BlockExplosive.triggerExplosive(world, pos, false);
            }

            // Check to see if there is fire nearby.
            // If so, then detonate.
            for (Direction rotation : Direction.Plane.HORIZONTAL)
            {
                final BlockState neighbor = world.getBlockState(pos.relative(rotation));

                if (neighbor.is(Blocks.FIRE) || neighbor.is(Blocks.LAVA))
                {
                    BlockExplosive.triggerExplosive(world, pos, true);
                    break;
                }
            }

            if (entityLiving != null && !world.isClientSide())
            {
                final IExplosiveData data = explosive.getExplosiveData();
                //TODO turn into event and logger
                ICBMClassic.logger().info("ICBMClassic>>BlockExplosive#onBlockPlacedBy: " + entityLiving.getName().getString()
                        + " placed " + (data != null ? data.getRegistryKey() : null) + " in: " + pos);
            }
        }
    }

    /**
     * Lets the block know when one of its neighbor changes.
     */
    @Override
    protected void neighborChanged(BlockState thisBlock, Level world, BlockPos pos, Block blockIn, BlockPos fromPos, boolean isMoving)
    {
        if (world.hasNeighborSignal(pos))
        {
            BlockExplosive.triggerExplosive(world, pos, false);
        }
    }

    /*
     * Called to detonate the TNT. Args: world, x, y, z, metaData, CauseOfExplosion (0, intentional,
     * 1, exploded, 2 burned)
     */
    public static void triggerExplosive(Level world, BlockPos pos, boolean setFire)
    {
        if (!world.isClientSide())
        {
            final BlockEntity tileEntity = world.getBlockEntity(pos);

            if (tileEntity instanceof TileEntityExplosive)
            {
                ((TileEntityExplosive) tileEntity).trigger(setFire);
            }
        }
    }

    /**
     * Called upon the block being destroyed by an explosion
     */
    @Override
    public void onBlockExploded(BlockState state, Level world, BlockPos pos, Explosion explosion)
    {
        BlockExplosive.triggerExplosive(world, pos, false);
        super.onBlockExploded(state, world, pos, explosion);
    }

    @Override
    public boolean dropFromExplosion(Explosion explosionIn)
    {
        return false;
    }

    /**
     * Called upon block activation with an item
     */
    @Override
    protected ItemInteractionResult useItemOn(ItemStack itemstack, BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit)
    {
        if (!itemstack.isEmpty() && (itemstack.getItem() == Items.FLINT_AND_STEEL || itemstack.getItem() == Items.FIRE_CHARGE))
        {
            BlockExplosive.triggerExplosive(world, pos, false);

            if (itemstack.getItem() == Items.FLINT_AND_STEEL)
            {
                itemstack.hurtAndBreak(1, player, LivingEntity.getSlotForHand(hand));
            }
            else if (!player.getAbilities().instabuild)
            {
                itemstack.shrink(1);
            }

            return ItemInteractionResult.sidedSuccess(world.isClientSide());
        }
        return super.useItemOn(itemstack, state, world, pos, player, hand, hit);
    }

    /**
     * PORT: 1.12 checked burning arrows colliding with the block, 1.21 reports projectile hits directly
     */
    @Override
    protected void onProjectileHit(Level world, BlockState state, BlockHitResult hit, Projectile projectile)
    {
        if (!world.isClientSide() && projectile instanceof AbstractArrow && projectile.isOnFire())
        {
            BlockExplosive.triggerExplosive(world, hit.getBlockPos(), false);
        }
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state)
    {
        return new TileEntityExplosive(pos, state);
    }
}
// PORTED

package icbm.classic.content.blocks.explosive;

import icbm.classic.api.actions.IActionData;
import icbm.classic.api.actions.cause.IActionCause;
import icbm.classic.api.actions.data.ActionFields;
import icbm.classic.content.missile.logic.source.cause.EntityCause;
import icbm.classic.lib.actions.PotentialAction;
import icbm.classic.lib.actions.fields.ActionFieldProvider;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.AbstractArrowEntity;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.Explosion;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.List;

public class BlockExplosive extends Block
{
    public final IActionData action;

    public BlockExplosive(IActionData action, Block.Properties properties)
    {
        super(properties);
        this.action = action;
    }

    @Override
    public BlockState rotate(BlockState state, Rotation rot) {
        return state.with(BlockStateProperties.FACING, rot.rotate(state.get(BlockStateProperties.FACING)));
    }

    @Override
    public BlockState mirror(BlockState state, Mirror mirrorIn) {
        return state.rotate(mirrorIn.toRotation(state.get(BlockStateProperties.FACING)));
    }

    @Override
    protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
        builder.add(BlockStateProperties.FACING);
    }


    @Override
    public BlockState getStateForPlacement(BlockItemUseContext context) {
        return getDefaultState().with(BlockStateProperties.FACING, context.getFace());
    }

    @Override
    public void catchFire(BlockState state, World world, BlockPos pos, @Nullable net.minecraft.util.Direction face, @Nullable LivingEntity igniter) {
        this.doAction(world, pos, igniter != null ? new EntityCause(igniter): null); //TODO note was with fire as well
    }

    @Override
    public void onBlockAdded(BlockState state, World worldIn, BlockPos pos, BlockState oldState, boolean isMoving) {
        if (oldState.getBlock() != state.getBlock()) {
            if (worldIn.isBlockPowered(pos)) {
                catchFire(state, worldIn, pos, null, null);
                worldIn.removeBlock(pos, false);
            }
        }
    }

    @Override
    public void neighborChanged(BlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos, boolean isMoving) {
        if (worldIn.isBlockPowered(pos)) {
            catchFire(state, worldIn, pos, null, null);
            worldIn.removeBlock(pos, false);
        }
    }

    @Override
    public void onExplosionDestroy(World worldIn, BlockPos pos, Explosion explosionIn) {
        if (!worldIn.isRemote) {
           this.doAction(worldIn, pos, null); //TODO add cause by explosion
        }
    }

    public void doAction(World world, BlockPos pos, @Nullable IActionCause cause) {
        if (!world.isRemote) {
            final BlockState state = world.getBlockState(pos);
            final Direction direction = state.getBlock() instanceof BlockExplosive && state.has(BlockStateProperties.FACING) ?
                state.get(BlockStateProperties.FACING).getOpposite() : null; //TODO why opposite

            final PotentialAction potentialAction = new PotentialAction();
            potentialAction.withProvider(new ActionFieldProvider().field(ActionFields.HOST_DIRECTION, () -> direction));
            potentialAction.setActionData(action);
            potentialAction.doAction(world, pos, cause);
        }
    }

    @Override
    public boolean canDropFromExplosion(Explosion explosionIn)
    {
        return false;
    }

    @Override
    public boolean onBlockActivated(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand handIn, BlockRayTraceResult hit) {
        ItemStack itemstack = player.getHeldItem(handIn);
        Item item = itemstack.getItem();
        if (item != Items.FLINT_AND_STEEL && item != Items.FIRE_CHARGE) {
            return super.onBlockActivated(state, worldIn, pos, player, handIn, hit);
        } else {
            catchFire(state, worldIn, pos, hit.getFace(), player);
            worldIn.setBlockState(pos, Blocks.AIR.getDefaultState(), 11);
            if (item == Items.FLINT_AND_STEEL) {
                itemstack.damageItem(1, player, (p_220287_1_) -> {
                    p_220287_1_.sendBreakAnimation(handIn);
                });
            } else {
                itemstack.shrink(1);
            }

            return true;
        }
    }

    @Override
    public void onProjectileCollision(World worldIn, BlockState state, BlockRayTraceResult hit, Entity projectile) {
        if (!worldIn.isRemote && projectile instanceof AbstractArrowEntity) {
            AbstractArrowEntity abstractarrowentity = (AbstractArrowEntity)projectile;
            Entity entity = abstractarrowentity.getShooter();
            if (abstractarrowentity.isBurning()) {
                BlockPos blockpos = hit.getPos();
                catchFire(state, worldIn, blockpos, null, entity instanceof LivingEntity ? (LivingEntity)entity : null);
                worldIn.removeBlock(blockpos, false);
            }
        }
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable IBlockReader worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
        tooltip.add(new TranslationTextComponent(this.getTranslationKey() + ".info"));
    }
}

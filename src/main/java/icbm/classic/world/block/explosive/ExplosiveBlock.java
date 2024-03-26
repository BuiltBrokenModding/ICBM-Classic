package icbm.classic.world.block.explosive;

import icbm.classic.ICBMClassic;
import icbm.classic.api.ICBMClassicAPI;
import icbm.classic.api.reg.ExplosiveType;
import icbm.classic.lib.capability.ex.CapabilityExplosiveStack;
import icbm.classic.prefab.tile.IcbmBlock;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.Explosion;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityLivingBase;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.EntityArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

public class ExplosiveBlock extends IcbmBlock {
    public static final PropertyExplosive EX_PROP = new PropertyExplosive();

    public ExplosiveBlock(BlockBehaviour.Properties properties) {
        super("explosives", Material.TNT);
        setHardness(2);
        setSoundType(SoundType.CLOTH);
    }

    @Override
    public ItemStack getPickBlock(BlockState state, RayTraceResult target, Level level, BlockPos pos, Player player) {
        return getItem(world, pos, getActualState(state, world, pos));
    }

    @Override
    public int damageDropped(BlockState state) {
        return state.getValue(EX_PROP).getRegistryID();
    }

    @Override
    public BlockState getActualState(BlockState state, IBlockAccess worldIn, BlockPos pos) {
        ExplosiveType explosiveData = null;
        BlockEntity blockEntity = worldIn.getBlockEntity(pos);
        if (tile instanceof BlockEntityExplosive && ((BlockEntityExplosive) tile).capabilityExplosive != null) {
            explosiveData = ((BlockEntityExplosive) tile).capabilityExplosive.getExplosiveData();
        }

        if (explosiveData != null) {
            return state.withProperty(EX_PROP, explosiveData);
        }
        return state;
    }

    @Override
    public boolean isNormalCube(BlockState state, IBlockAccess world, BlockPos pos) {
        return true;
    }

    @Override
    public boolean isSideSolid(BlockState base_state, IBlockAccess world, BlockPos pos, Direction side) {
        return isNormalCube(base_state, world, pos);
    }

    @Override
    public boolean isTopSolid(BlockState state) {
        return true;
    }

    @Override
    public boolean isOpaqueCube(BlockState state) {
        return true;
    }

    @Override
    public EnumBlockRenderType getRenderType(BlockState state) {
        return EnumBlockRenderType.MODEL;
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, ROTATION_PROP, EX_PROP);
    }

    @Override
    public BlockState getStateForPlacement(Level level, BlockPos pos, Direction facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer, InteractionHand hand) {
        ItemStack stack = placer.getHeldItem(hand);
        BlockState state = getDefaultState().withProperty(ROTATION_PROP, facing);
        ExplosiveType prop = ICBMClassicAPI.EXPLOSIVE_REGISTRY.getExplosiveData(stack.getItemDamage());
        if (prop != null) {
            return state.withProperty(EX_PROP, prop);
        } else { // if the explosives id doesnt exist, then fallback to the one with the id 0
            prop = ICBMClassicAPI.EXPLOSIVE_REGISTRY.getExplosiveData(0);
            ICBMClassic.logger().log(Level.ERROR, "Unable to get explosives kind, choosing " + prop.getRegistryName().toString() + " as a fallback.");
            stack.setItemDamage(0);
            return state.withProperty(EX_PROP, prop);
        }
    }

    @Override
    public void onBlockAdded(Level level, BlockPos pos, BlockState state) {
        // Can't be implemented as we lack blockState for explosive at this point
        //super.onBlockAdded(world, pos, state);

        //if (!world.isClientSide() && world.isBlockPowered(pos))
        //{
        //    BlockExplosive.triggerExplosive(world, pos, false);
        //}
    }

    /**
     * Called when the block is placed in the world.
     */
    @Override
    public void onBlockPlacedBy(Level level, BlockPos pos, BlockState state, EntityLivingBase entityLiving, ItemStack itemStack) {
        final BlockEntity blockEntity = world.getBlockEntity(pos);
        if (tile instanceof BlockEntityExplosive) {
            BlockEntityExplosive explosive = (BlockEntityExplosive) tile;
            explosive.capabilityExplosive = new CapabilityExplosiveStack(itemStack.copy());

            if (world.isBlockPowered(pos)) {
                ExplosiveBlock.triggerExplosive(world, pos, false);
            }

            // Check to see if there is fire nearby.
            // If so, then detonate.
            for (Direction rotation : Direction.HORIZONTALS) {
                Pos position = new Pos(pos).add(rotation);
                Block blockId = position.getBlock(world);

                if (blockId == Blocks.FIRE || blockId == Blocks.FLOWING_LAVA || blockId == Blocks.LAVA) {
                    ExplosiveBlock.triggerExplosive(world, pos, true);
                    break;
                }
            }

            if (entityLiving != null) {
                //TODO turn into event and logger
                ICBMClassic.logger().info("ICBMClassic>>BlockExplosive#onBlockPlacedBy: " + entityLiving.getName()
                    + " placed " + explosive.capabilityExplosive.getExplosiveData().getRegistryName() + " in: " + pos);
            }
        }
    }

    /**
     * Lets the block know when one of its neighbor changes. Doesn't know which neighbor changed
     * (coordinates passed are their own) Args: x, y, z, neighbor block
     */
    @Override
    public void neighborChanged(BlockState thisBlock, Level level, BlockPos pos, Block blockIn, BlockPos fromPos) {
        if (world.isBlockPowered(pos)) {
            ExplosiveBlock.triggerExplosive(world, pos, false);
        }
    }

    /*
     * Called to detonate the TNT. Args: world, x, y, z, metaData, CauseOfExplosion (0, intentional,
     * 1, exploded, 2 burned)
     */

    public static void triggerExplosive(Level level, BlockPos pos, boolean setFire) {
        if (!world.isClientSide()) {
            BlockEntity blockEntityEntity = world.getBlockEntity(pos);

            if (tileEntity instanceof BlockEntityExplosive) {
                ((BlockEntityExplosive) tileEntity).trigger(setFire);
            }
        }
    }

    /**
     * Called upon the block being destroyed by an explosion
     */
    @Override
    public void onBlockExploded(Level level, BlockPos pos, Explosion explosion) {
        ExplosiveBlock.triggerExplosive(world, pos, false);
        super.onBlockExploded(world, pos, explosion);
    }

    @Override
    public boolean canDropFromExplosion(Explosion explosionIn) {
        return false;
    }

    /**
     * Called upon block activation (left or right click on the block.). The three integers
     * represent x,y,z of the block.
     */
    @Override
    public boolean onBlockActivated(Level level, BlockPos pos, BlockState state, Player player, InteractionHand hand, Direction facing, float hitX, float hitY, float hitZ) {
        ItemStack itemstack = player.getHeldItem(hand);

        if (!itemstack.isEmpty() && (itemstack.getItem() == Items.FLINT_AND_STEEL || itemstack.getItem() == Items.FIRE_CHARGE)) {
            ExplosiveBlock.triggerExplosive(world, pos, false);

            if (itemstack.getItem() == Items.FLINT_AND_STEEL) {
                itemstack.damageItem(1, player);
            } else if (!player.capabilities.isCreativeMode) {
                itemstack.shrink(1);
            }

            return true;
        } else {
            return super.onBlockActivated(world, pos, state, player, hand, facing, hitX, hitY, hitZ);
        }
    }

    @Override
    public void onEntityCollidedWithBlock(Level level, BlockPos pos, BlockState state, Entity entityIn) {
        if (!world.isClientSide() && entityIn instanceof EntityArrow) {
            EntityArrow entityarrow = (EntityArrow) entityIn;

            if (entityarrow.isBurning()) {
                ExplosiveBlock.triggerExplosive(world, pos, false);
            }
        }
    }

    @Override
    public void getSubBlocks(CreativeTabs tab, NonNullList<ItemStack> items) {
        if (tab == this.getCreativeTabToDisplayOn()) {
            for (int id : ICBMClassicAPI.EX_BLOCK_REGISTRY.getExplosivesIDs()) {
                items.add(new ItemStack(this, 1, id));
            }
        }
    }

    @Override
    public BlockEntity createNewBlockEntity(Level level, int meta) {
        return new BlockEntityExplosive();
    }
}

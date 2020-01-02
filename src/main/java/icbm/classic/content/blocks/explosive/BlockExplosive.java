package icbm.classic.content.blocks.explosive;

import icbm.classic.ICBMClassic;
import icbm.classic.api.ICBMClassicAPI;
import icbm.classic.api.reg.IExplosiveData;
import icbm.classic.lib.capability.ex.CapabilityExplosiveStack;
import icbm.classic.lib.transform.vector.Pos;
import icbm.classic.prefab.tile.BlockICBM;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.Explosion;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import org.apache.logging.log4j.Level;

public class BlockExplosive extends BlockICBM
{
    public static final PropertyExplosive EX_PROP = new PropertyExplosive();

    public BlockExplosive()
    {
        super("explosives", Material.TNT);
        setHardness(2);
        setSoundType(SoundType.CLOTH);
    }

    @Override
    public ItemStack getPickBlock(IBlockState state, RayTraceResult target, World world, BlockPos pos, EntityPlayer player)
    {
        return getItem(world, pos, getActualState(state, world, pos));
    }

    @Override
    public int damageDropped(IBlockState state)
    {
        return state.getValue(EX_PROP).getRegistryID();
    }

    @Override
    public IBlockState getActualState(IBlockState state, IBlockAccess worldIn, BlockPos pos)
    {
        IExplosiveData explosiveData = null;
        TileEntity tile = worldIn.getTileEntity(pos);
        if (tile instanceof TileEntityExplosive && ((TileEntityExplosive) tile).capabilityExplosive != null)
        {
            explosiveData = ((TileEntityExplosive) tile).capabilityExplosive.getExplosiveData();
        }

        if (explosiveData != null)
        {
            return state.withProperty(EX_PROP, explosiveData);
        }
        return state;
    }

    @Override
    public boolean isNormalCube(IBlockState state, IBlockAccess world, BlockPos pos)
    {
        return true;
    }

    @Override
    public boolean isSideSolid(IBlockState base_state, IBlockAccess world, BlockPos pos, EnumFacing side)
    {
        return isNormalCube(base_state, world, pos);
    }

    @Override
    public boolean isTopSolid(IBlockState state)
    {
        return true;
    }

    @Override
    public boolean isOpaqueCube(IBlockState state)
    {
        return true;
    }

    @Override
    public EnumBlockRenderType getRenderType(IBlockState state)
    {
        return EnumBlockRenderType.MODEL;
    }

    @Override
    protected BlockStateContainer createBlockState()
    {
        return new BlockStateContainer(this, ROTATION_PROP, EX_PROP);
    }

    @Override
    public IBlockState getStateForPlacement(World world, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer, EnumHand hand)
    {
        ItemStack stack = placer.getHeldItem(hand);
        IBlockState state = getDefaultState().withProperty(ROTATION_PROP, facing);
        IExplosiveData prop = ICBMClassicAPI.EXPLOSIVE_REGISTRY.getExplosiveData(stack.getItemDamage());
        if(prop != null) {
            return state.withProperty(EX_PROP, prop);
        }
        else { // if the explosives id doesnt exist, then fallback to the one with the id 0
            prop = ICBMClassicAPI.EXPLOSIVE_REGISTRY.getExplosiveData(0);
            ICBMClassic.logger().log(Level.ERROR, "Unable to get explosives kind, choosing "+prop.getRegistryName().toString()+" as a fallback.");
            stack.setItemDamage(0);
            return state.withProperty(EX_PROP, prop);
        }
    }

    /**
     * Called when the block is placed in the world.
     */
    @Override
    public void onBlockPlacedBy(World world, BlockPos pos, IBlockState state, EntityLivingBase entityLiving, ItemStack itemStack)
    {
        final TileEntity tile = world.getTileEntity(pos);
        if (tile instanceof TileEntityExplosive)
        {
            TileEntityExplosive explosive = (TileEntityExplosive) tile;
            explosive.capabilityExplosive = new CapabilityExplosiveStack(itemStack.copy());

            if (world.getRedstonePowerFromNeighbors(pos) > 0)
            {
                BlockExplosive.triggerExplosive(world, pos, false);
            }

            // Check to see if there is fire nearby.
            // If so, then detonate.
            for (EnumFacing rotation : EnumFacing.HORIZONTALS)
            {
                Pos position = new Pos(pos).add(rotation);
                Block blockId = position.getBlock(world);

                if (blockId == Blocks.FIRE || blockId == Blocks.FLOWING_LAVA || blockId == Blocks.LAVA)
                {
                    BlockExplosive.triggerExplosive(world, pos, true);
                    break;
                }
            }

            if (entityLiving != null)
            {
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
    public void neighborChanged(IBlockState thisBlock, World world, BlockPos pos, Block blockIn, BlockPos fromPos)
    {
        int power = world.getRedstonePowerFromNeighbors(pos);
        if (power > 0)
        {
            BlockExplosive.triggerExplosive(world, pos, false);
            return;
        }
        else
        {
            for (EnumFacing facing : EnumFacing.VALUES) //TODO recode
            {
                IBlockState state = world.getBlockState(pos.add(facing.getDirectionVec()));
                Block block = state.getBlock();
                if (block == Blocks.FIRE || block == Blocks.FLOWING_LAVA || block == Blocks.LAVA)
                {
                    BlockExplosive.triggerExplosive(world, pos, false);
                    return;
                }
            }
        }
    }

    /*
     * Called to detonate the TNT. Args: world, x, y, z, metaData, CauseOfExplosion (0, intentional,
     * 1, exploded, 2 burned)
     */

    public static void triggerExplosive(World world, BlockPos pos, boolean setFire)
    {
        if (!world.isRemote)
        {
            TileEntity tileEntity = world.getTileEntity(pos);

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
    public void onBlockExploded(World world, BlockPos pos, Explosion explosion)
    {
        BlockExplosive.triggerExplosive(world, pos, false);
        super.onBlockExploded(world, pos, explosion);
    }

    @Override
    public boolean canDropFromExplosion(Explosion explosionIn)
    {
        return false;
    }

    /**
     * Called upon block activation (left or right click on the block.). The three integers
     * represent x,y,z of the block.
     */
    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer entityPlayer, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ)
    {
        TileEntity tileEntity = world.getTileEntity(pos);

        if (entityPlayer.getHeldItem(hand) != null)
        {
            if (entityPlayer.getHeldItem(hand).getItem() == Items.FLINT_AND_STEEL)
            {
                BlockExplosive.triggerExplosive(world, pos, true);
                return true;
            }
        }

        if (tileEntity instanceof TileEntityExplosive)
        {
            //return ((TileEntityExplosive) tileEntity).explosive.handler.onBlockActivated(world, pos, entityPlayer, hand, facing, hitX, hitY, hitZ); TODO fix
        }

        return false;
    }

    @Override
    public void getSubBlocks(CreativeTabs tab, NonNullList<ItemStack> items)
    {
        if (tab == getCreativeTab())
        {
            for (int id : ICBMClassicAPI.EX_BLOCK_REGISTRY.getExplosivesIDs())
            {
                items.add(new ItemStack(this, 1, id));
            }
        }
    }

    @Override
    public TileEntity createNewTileEntity(World world, int meta)
    {
        return new TileEntityExplosive();
    }
}

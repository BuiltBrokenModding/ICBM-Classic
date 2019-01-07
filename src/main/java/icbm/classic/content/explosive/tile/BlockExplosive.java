package icbm.classic.content.explosive.tile;

import icbm.classic.ICBMClassic;
import icbm.classic.api.ICBMClassicAPI;
import icbm.classic.api.reg.IExplosiveData;
import icbm.classic.content.entity.EntityExplosive;
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
import net.minecraft.world.Explosion;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class BlockExplosive extends BlockICBM
{
    public static final PropertyExplosive EX_PROP = new PropertyExplosive(); //TODO filter to block versions only

    public BlockExplosive()
    {
        super("explosives", Material.TNT);
        setHardness(2);
        setSoundType(SoundType.CLOTH);
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
        if (tile instanceof TileEntityExplosive)
        {
            explosiveData = ((TileEntityExplosive) tile).explosive;
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
        return getDefaultState().withProperty(ROTATION_PROP, facing).withProperty(EX_PROP, ICBMClassicAPI.EXPLOSIVE_REGISTRY.getExplosiveData(stack.getItemDamage()));
    }

    /** Called when the block is placed in the world. */
    @Override
    public void onBlockPlacedBy(World world, BlockPos pos, IBlockState state, EntityLivingBase entityLiving, ItemStack itemStack)
    {
        TileEntity tile = world.getTileEntity(pos);
        if (tile instanceof TileEntityExplosive)
        {
            int explosiveID = itemStack.getItemDamage();
            ((TileEntityExplosive) tile).explosive = ICBMClassicAPI.EXPLOSIVE_REGISTRY.getExplosiveData(explosiveID);


            if (world.getRedstonePowerFromNeighbors(pos) > 0)
            {
                BlockExplosive.triggerExplosive(world, pos, explosiveID, 0);
            }

            // Check to see if there is fire nearby.
            // If so, then detonate.
            for (EnumFacing rotation : EnumFacing.HORIZONTALS)
            {
                Pos position = new Pos(pos).add(rotation);
                Block blockId = position.getBlock(world);

                if (blockId == Blocks.FIRE || blockId == Blocks.FLOWING_LAVA || blockId == Blocks.LAVA)
                {
                    BlockExplosive.triggerExplosive(world, pos, explosiveID, 2);
                    break;
                }
            }

            if (entityLiving != null)
            {
                //TODO turn into event and logger
                ICBMClassic.logger().info("ICBMClassic>>BlockExplosive#onBlockPlacedBy: " + entityLiving.getName()
                        + " placed " + ICBMClassicAPI.EXPLOSIVE_REGISTRY.getExplosiveData(explosiveID).getRegistryID() + " in: " + pos);
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
        TileEntity tile = world.getTileEntity(pos);
        if (tile instanceof TileEntityExplosive)
        {
            int explosiveID = ((TileEntityExplosive) tile).explosive.getRegistryID();

            int power = world.getRedstonePowerFromNeighbors(pos);
            if (power > 0)
            {
                BlockExplosive.triggerExplosive(world, pos, explosiveID, 0);
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
                        BlockExplosive.triggerExplosive(world, pos, explosiveID, 0);
                        return;
                    }
                }
            }
        }
    }

    /*
     * Called to detonate the TNT. Args: world, x, y, z, metaData, CauseOfExplosion (0, intentional,
     * 1, exploded, 2 burned)
     */

    public static void triggerExplosive(World world, BlockPos pos, int explosiveID, int causeOfExplosion)
    {
        if (!world.isRemote)
        {
            TileEntity tileEntity = world.getTileEntity(pos);

            if (tileEntity instanceof TileEntityExplosive)
            {
                ((TileEntityExplosive) tileEntity).exploding = true;
                EntityExplosive entityExplosive = new EntityExplosive(world, new Pos(pos).add(0.5), explosiveID, (byte) ((TileEntityExplosive) tileEntity).getDirection().ordinal(), ((TileEntityExplosive) tileEntity).nbtData);

                switch (causeOfExplosion)
                {
                    case 2:
                        entityExplosive.setFire(100);
                        break;
                }

                world.spawnEntity(entityExplosive);
                world.setBlockToAir(pos);
            }
        }
    }

    /** Called upon the block being destroyed by an explosion */
    @Override
    public void onBlockExploded(World world, BlockPos pos, Explosion explosion)
    {
        if (world.getTileEntity(pos) instanceof TileEntityExplosive)
        {
            BlockExplosive.triggerExplosive(world, pos, ((TileEntityExplosive) world.getTileEntity(pos)).explosive.getRegistryID(), 1);
        }

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
                BlockExplosive.triggerExplosive(world, pos, ((TileEntityExplosive) tileEntity).explosive.getRegistryID(), 0);
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
            for (Explosives zhaPin : Explosives.values())
            {
                if (zhaPin.handler.hasBlockForm())
                {
                    items.add(new ItemStack(this, 1, zhaPin.ordinal()));
                }
            }
        }
    }

    @Override
    public TileEntity createNewTileEntity(World world, int meta)
    {
        return new TileEntityExplosive();
    }
}

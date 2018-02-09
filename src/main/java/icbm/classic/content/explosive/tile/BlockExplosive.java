package icbm.classic.content.explosive.tile;

import com.builtbroken.mc.imp.transform.vector.Pos;
import com.builtbroken.mc.prefab.inventory.InventoryUtility;
import icbm.classic.ICBMClassic;
import icbm.classic.content.entity.EntityExplosive;
import icbm.classic.content.explosive.Explosives;
import icbm.classic.prefab.BlockICBM;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyEnum;
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
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.Explosion;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import java.util.Random;

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
    public IBlockState getActualState(IBlockState state, IBlockAccess worldIn, BlockPos pos)
    {
        Explosives ex = Explosives.CONDENSED;
        TileEntity tile = worldIn.getTileEntity(pos);
        if (tile instanceof TileEntityExplosive)
        {
            ex = ((TileEntityExplosive) tile).explosive;
        }
        return state.withProperty(EX_PROP, ex);
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

    public static final class PropertyExplosive extends PropertyEnum<Explosives>
    {
        public PropertyExplosive()
        {
            super("explosive", Explosives.class, Explosives.getBlocksOnly());
        }
    }

    /**
     * Returns a bounding box from the pool of bounding boxes (this means this box can change after
     * the pool has been cleared to be reused)
     */
    @Override
    public AxisAlignedBB getCollisionBoundingBox(IBlockState blockState, IBlockAccess world, BlockPos pos)
    {
        TileEntity tileEntity = world.getTileEntity(pos);

        if (tileEntity != null)
        {
            if (tileEntity instanceof TileEntityExplosive)
            {
                if (((TileEntityExplosive) tileEntity).explosive == Explosives.SMINE)
                {
                    return new AxisAlignedBB(pos.getX(), pos.getY(), pos.getZ(), pos.getX() + 1, pos.getY() + 0.2, pos.getZ() + 1);
                }
            }
        }

        return super.getCollisionBoundingBox(blockState, world, pos);
    }

    @Override
    public IBlockState getStateForPlacement(World world, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer, EnumHand hand)
    {
        ItemStack stack = placer.getHeldItem(hand);
        return getDefaultState().withProperty(ROTATION_PROP, facing).withProperty(EX_PROP, Explosives.get(stack.getItemDamage()));
    }

    /** Called when the block is placed in the world. */
    @Override
    public void onBlockPlacedBy(World world, BlockPos pos, IBlockState state, EntityLivingBase entityLiving, ItemStack itemStack)
    {
        TileEntity tile = world.getTileEntity(pos);
        if (tile instanceof TileEntityExplosive)
        {
            Explosives ex = Explosives.get(itemStack.getItemDamage());
            ((TileEntityExplosive) tile).explosive = ex;


            if (world.isBlockIndirectlyGettingPowered(pos) > 0)
            {
                BlockExplosive.triggerExplosive(world, pos, ex, 0);
            }

            // Check to see if there is fire nearby.
            // If so, then detonate.
            for (EnumFacing rotation : EnumFacing.HORIZONTALS)
            {
                Pos position = new Pos(pos).add(rotation);
                Block blockId = position.getBlock(world);

                if (blockId == Blocks.FIRE || blockId == Blocks.FLOWING_LAVA || blockId == Blocks.LAVA)
                {
                    BlockExplosive.triggerExplosive(world, pos, ex, 2);
                    break;
                }
            }

            if (entityLiving != null)
            {
                ICBMClassic.INSTANCE.logger().info("ICBMClassic>>BlockExplosive#onBlockPlacedBy: " + entityLiving.getName() + " placed " + ex.handler.getExplosiveName() + " in: " + pos);
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
            Explosives explosiveID = ((TileEntityExplosive) tile).explosive;

            int power = world.isBlockIndirectlyGettingPowered(pos);
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

    public static void triggerExplosive(World world, BlockPos pos, Explosives explosiveID, int causeOfExplosion)
    {
        if (!world.isRemote)
        {
            TileEntity tileEntity = world.getTileEntity(pos);

            if (tileEntity != null)
            {
                if (tileEntity instanceof TileEntityExplosive)
                {
                    ((TileEntityExplosive) tileEntity).exploding = true;
                    EntityExplosive eZhaDan = new EntityExplosive(world, new Pos(pos).add(0.5), ((TileEntityExplosive) tileEntity).explosive, (byte) ((TileEntityExplosive) tileEntity).getDirection().ordinal(), ((TileEntityExplosive) tileEntity).nbtData);

                    switch (causeOfExplosion)
                    {
                        case 2:
                            eZhaDan.setFire(100);
                            break;
                    }

                    world.spawnEntity(eZhaDan);
                    world.setBlockToAir(pos);
                }
            }
        }
    }

    /** Called upon the block being destroyed by an explosion */
    @Override
    public void onBlockExploded(World world, BlockPos pos, Explosion explosion)
    {
        if (world.getTileEntity(pos) instanceof TileEntityExplosive)
        {
            BlockExplosive.triggerExplosive(world, pos, ((TileEntityExplosive) world.getTileEntity(pos)).explosive, 1);
        }

        super.onBlockExploded(world, pos, explosion);
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
                Explosives explosiveID = ((TileEntityExplosive) tileEntity).explosive;
                BlockExplosive.triggerExplosive(world, pos, explosiveID, 0);
                return true;
            }
        }

        if (tileEntity instanceof TileEntityExplosive)
        {
            return ((TileEntityExplosive) tileEntity).explosive.handler.onBlockActivated(world, pos, entityPlayer, hand, facing, hitX, hitY, hitZ);
        }

        return false;
    }

    @Override
    public ItemStack getPickBlock(IBlockState state, RayTraceResult target, World world, BlockPos pos, EntityPlayer player)
    {
        TileEntity tile = world.getTileEntity(pos);
        if (tile instanceof TileEntityExplosive && ((TileEntityExplosive) tile).explosive != null)
        {
            return new ItemStack(this, 1, ((TileEntityExplosive) tile).explosive.ordinal());
        }
        return new ItemStack(this);
    }

    @Override
    public boolean removedByPlayer(IBlockState state, World world, BlockPos pos, EntityPlayer player, boolean willHarvest)
    {
        if (willHarvest)
        {
            InventoryUtility.dropBlockAsItem(world, pos, false);
        }
        return world.setBlockToAir(pos);
    }

    @Override
    public void getDrops(NonNullList<ItemStack> drops, IBlockAccess world, BlockPos pos, IBlockState state, int fortune)
    {
        TileEntity tileEntity = world.getTileEntity(pos);
        if (tileEntity instanceof TileEntityExplosive)
        {
            if (!((TileEntityExplosive) tileEntity).exploding)
            {
                int explosiveID = ((TileEntityExplosive) tileEntity).explosive.ordinal();
                drops.add(new ItemStack(ICBMClassic.blockExplosive, 1, explosiveID));
            }
        }
    }

    @Override
    public int quantityDropped(Random par1Random)
    {
        return 0;
    }

    @Override
    public void getSubBlocks(CreativeTabs tab, NonNullList<ItemStack> items)
    {
        if (tab == getCreativeTabToDisplayOn())
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

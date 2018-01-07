package icbm.classic.content.explosive.tile;

import com.builtbroken.mc.core.registry.implement.IPostInit;
import com.builtbroken.mc.data.Direction;
import com.builtbroken.mc.imp.transform.vector.Pos;
import com.builtbroken.mc.prefab.inventory.InventoryUtility;
import icbm.classic.ICBMClassic;
import icbm.classic.content.entity.EntityExplosive;
import icbm.classic.content.explosive.Explosives;
import icbm.classic.prefab.BlockICBM;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.Explosion;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.GameRegistry;

import java.util.Random;

public class BlockExplosive extends BlockICBM implements IPostInit
{

    public BlockExplosive()
    {
        super("explosives", Material.TNT);
        this.setUnlocalizedName(ICBMClassic.PREFIX + "explosives");
        setHardness(2);
        setSoundType(SoundType.CLOTH);
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
            for (byte i = 0; i < 6; i++)
            {
                Pos position = new Pos(pos).add(Direction.getOrientation(i));
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
    public void onNeighborChange(IBlockAccess world, BlockPos pos, BlockPos neighbor)
    {
        if (world instanceof World)
        {
            Explosives explosiveID = ((TileEntityExplosive) world.getTileEntity(pos)).explosive;

            for (EnumFacing facing : EnumFacing.VALUES)
            {
                if (world.getStrongPower(pos, facing) > 0)
                {
                    BlockExplosive.triggerExplosive((World) world, pos, explosiveID, 0);
                    return;
                }
                else
                {
                    IBlockState state = world.getBlockState(pos.add(facing.getDirectionVec()));
                    Block block = state.getBlock();
                    if (block == Blocks.FIRE || block == Blocks.FLOWING_LAVA || block == Blocks.LAVA)
                    {
                        BlockExplosive.triggerExplosive((World) world, pos, explosiveID, 0);
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

    @Override
    public void onPostInit()
    {
        GameRegistry.registerTileEntity(TileEntityExplosive.class, "icbmCTileExplosive");
    }
}

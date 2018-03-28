package icbm.classic.content.multiblock;

import icbm.classic.ICBMClassic;
import icbm.classic.api.tile.multiblock.IMultiTile;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.ModelBakery;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
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
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;

import java.util.Collections;
import java.util.Random;

/**
 * Created by Dark on 7/4/2015.
 */
public class BlockMultiblock extends BlockContainer
{
    public BlockMultiblock()
    {
        super(Material.ROCK);
        this.setRegistryName(ICBMClassic.DOMAIN, "multiblock");
        this.setUnlocalizedName(ICBMClassic.PREFIX + "multiblock");
        this.setHardness(2f);
        needsRandomTick = true;
    }

    @Override
    public void randomTick(World worldIn, BlockPos pos, IBlockState state, Random random)
    {
        if (!worldIn.isRemote)
        {
            TileEntity tile = worldIn.getTileEntity(pos);
            if (tile instanceof TileMulti)
            {
                if (!((TileMulti) tile).hasHost())
                {
                    worldIn.setBlockToAir(pos);
                }
                else if (((TileMulti) tile).isHostLoaded() && ((TileMulti) tile).getHost() == null)
                {
                    worldIn.setBlockToAir(pos);
                }
            }
        }
    }

    @Override
    public boolean isNormalCube(IBlockState state, IBlockAccess world, BlockPos pos)
    {
        return false;
    }

    @Override
    public boolean isOpaqueCube(IBlockState state)
    {
        return false;
    }

    @Override
    public EnumBlockRenderType getRenderType(IBlockState state)
    {
        return EnumBlockRenderType.INVISIBLE;
    }

    @Override
    public ItemStack getPickBlock(IBlockState state, RayTraceResult target, World world, BlockPos pos, EntityPlayer player)
    {
        IMultiTile multiblock = getTile(world, pos);
        if (multiblock != null && multiblock.getHost() instanceof TileEntity)
        {
            TileEntity tileEntity = ((TileEntity) multiblock.getHost());
            IBlockState block = tileEntity.getWorld().getBlockState(tileEntity.getPos());
            ItemStack stack = block.getBlock().getPickBlock(block, target, tileEntity.getWorld(), tileEntity.getPos(), player);
            return stack;
        }
        return null;
    }

    @Override
    public int quantityDropped(Random p_149745_1_)
    {
        return 0;
    }

    @Override
    public Item getItemDropped(IBlockState state, Random rand, int fortune)
    {
        return null;
    }


    @Override
    public void getDrops(NonNullList<ItemStack> drops, IBlockAccess world, BlockPos pos, IBlockState state, int fortune)
    {
        //Nothing
    }

    @Override
    protected boolean canSilkHarvest()
    {
        return false;
    }

    @Override
    public void onBlockAdded(World world, BlockPos pos, IBlockState state)
    {
        IMultiTile tile = getTile(world, pos);
        if (tile != null && tile.getHost() != null)
        {
            tile.getHost().onMultiTileAdded(tile);
        }
        super.onBlockAdded(world, pos, state);
    }

    @Override
    public void breakBlock(World world, BlockPos pos, IBlockState state)
    {
        IMultiTile tile = getTile(world, pos);
        if (tile != null && tile.getHost() != null)
        {
            tile.getHost().onMultiTileBroken(tile, null, true);
        }
        super.breakBlock(world, pos, state);
    }

    @Override
    public boolean removedByPlayer(IBlockState state, World world, BlockPos pos, EntityPlayer player, boolean willHarvest)
    {
        IMultiTile tile = getTile(world, pos);
        if (tile != null && tile.getHost() != null)
        {
            tile.getHost().onMultiTileBroken(tile, player, willHarvest);
        }
        return super.removedByPlayer(state, world, pos, player, willHarvest);
    }

    @Override
    public void onBlockDestroyedByExplosion(World world, BlockPos pos, Explosion ex)
    {
        IMultiTile tile = getTile(world, pos);
        if (tile != null && tile.getHost() != null)
        {
            tile.getHost().onMultiTileBroken(tile, ex, true);
        }
    }

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ)
    {
        TileMulti tile = getTile(world, pos);
        if (tile != null)
        {
            //Kill check
            if (!world.isRemote)
            {
                if (!tile.hasHost() || tile.isHostLoaded() && tile.getHost() == null)
                {
                    world.setBlockToAir(pos);
                    return true;
                }
            }

            //Normal click
            return tile.getHost() != null && tile.getHost().onMultiTileActivated(tile, player, hand, side, hitX, hitY, hitZ);
        }
        return true;
    }

    @Override
    public void onBlockClicked(World world, BlockPos pos, EntityPlayer player)
    {
        TileMulti tile = getTile(world, pos);
        if (tile != null)
        {
            //Kill check
            if (!world.isRemote)
            {
                if (!tile.hasHost() || tile.isHostLoaded() && tile.getHost() == null)
                {
                    world.setBlockToAir(pos);
                    return;
                }
            }

            //Normal click
            if (tile.getHost() != null)
            {
                tile.getHost().onMultiTileClicked(tile, player);
            }
        }
    }

    @Override
    public TileEntity createNewTileEntity(World world, int meta)
    {
        return new TileMulti();
    }

    protected TileMulti getTile(World world, BlockPos pos)
    {
        TileEntity tile = world.getTileEntity(pos);
        if (tile instanceof TileMulti)
        {
            return (TileMulti) tile;
        }
        return null;
    }

    //Fix for multi-block having no model but still wanting a JSON file
    @Mod.EventBusSubscriber(value = Side.CLIENT, modid = ICBMClassic.DOMAIN)
    public static class ClientLoader
    {
        @SubscribeEvent
        public static void registerModels(ModelRegistryEvent event)
        {
            ModelLoader.setCustomStateMapper(ICBMClassic.multiBlock, block -> Collections.emptyMap());
            ModelBakery.registerItemVariants(Item.getItemFromBlock(ICBMClassic.multiBlock));
        }
    }
}

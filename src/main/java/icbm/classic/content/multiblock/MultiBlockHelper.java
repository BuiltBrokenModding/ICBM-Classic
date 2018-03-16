package icbm.classic.content.multiblock;

import icbm.classic.api.IWorldPosition;
import icbm.classic.api.tile.multiblock.IMultiTile;
import icbm.classic.api.tile.multiblock.IMultiTileHost;
import icbm.classic.ICBMClassic;
import icbm.classic.prefab.inventory.InventoryUtility;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Collection;

/**
 * Created by Dark on 8/15/2015.
 */
public class MultiBlockHelper
{
    private static final Logger logger = LogManager.getLogger("VE-MultiBlockHelper");

    /**
     * Builds a multi block structure using data from the provided hostTile
     *
     * @param world    - world
     * @param hostTile - multiblock host
     * @param validate - if true will check if a hostTile already exists at location rather than placing a new one
     * @param offset   - offset the map position by the hostTile center
     */
    public static void buildMultiBlock(World world, IMultiTileHost hostTile, boolean validate, boolean offset)
    {
        //Rare edge case, should never happen
        if (world == null)
        {
            logger.error("MultiBlockHelper: buildMultiBlock was called with a null world by " + hostTile, new RuntimeException());
            return;
        }
        //Rare edge case, should never happen
        if (hostTile == null)
        {
            logger.error("MultiBlockHelper: buildMultiBlock was called with a null hostTile ", new RuntimeException());
            return;
        }
        //Multi-block should be registered but just in case a dev forgot
        if (ICBMClassic.multiBlock != null)
        {
            //Get layout of multi-block for it's current state
            Collection<BlockPos> placementList = hostTile.getLayoutOfMultiBlock();
            //Ensure the map is not null or empty in case there is no structure to generate
            if (placementList != null && !placementList.isEmpty())
            {
                //Keep track of position just for traceability
                int i = 0;
                //Loop all blocks and start placement
                for (BlockPos location : placementList)
                {
                    if (location == null)
                    {
                        logger.error("MultiBlockHelper: location[" + i + "] is null, this is most likely in error in " + hostTile);
                        i++;
                        continue;
                    }
                    //Moves the position based on the location of the host
                    if (offset)
                    {
                        location = location.add(hostTile.xi(), hostTile.yi(), hostTile.zi());
                    }


                    TileEntity tile = world.getTileEntity(location);
                    if (!validate || tile == null)
                    {
                        if (!world.setBlockState(location, ICBMClassic.multiBlock.getDefaultState(), 3))
                        {
                            logger.error("MultiBlockHelper:  error block was not placed ");
                        }
                        tile = world.getTileEntity(location);
                    }

                    if (tile instanceof IMultiTile)
                    {
                        ((IMultiTile) tile).setHost(hostTile);
                    }
                    else
                    {
                        logger.error("MultiBlockHelper: hostTile at location is not IMultiTile, " + tile);
                    }
                    i++;
                }
            }
            else
            {
                logger.error("Tile[" + hostTile + "] didn't return a structure map");
            }
        }
        else
        {
            logger.error("MultiBlock was never registered, this is a critical error and can have negative effects on gameplay. " +
                    "Make sure the block was not disabled in the configs and contact support to ensure nothing is broken", new RuntimeException());
        }
    }

    public static boolean canBuild(World world, IMultiTileHost tile, boolean offset)
    {
        if (world != null && tile != null && ICBMClassic.multiBlock != null)
        {
            return canBuild(world, ((TileEntity)tile).getPos(), tile.getLayoutOfMultiBlock(), offset);
        }
        return false;
    }

    public static boolean canBuild(World world, BlockPos pos, Collection<BlockPos> map, boolean offset)
    {
        if (world != null && ICBMClassic.multiBlock != null)
        {
            //Ensure the map is not null or empty in case there is no structure to generate
            if (map != null && !map.isEmpty())
            {
                //Loop all blocks and start placement
                for (BlockPos location : map)
                {
                    //Validate data
                    if (location == null || !world.isBlockLoaded(pos))
                    {
                        return false;
                    }

                    //Moves the position based on the location of the host
                    if (offset)
                    {
                        location = location.add(pos);
                    }

                    //Get block
                    IBlockState block = world.getBlockState(location);

                    //If not replaceable, do not place
                    if (!block.getBlock().isReplaceable(world, location))
                    {
                        return false;
                    }
                    else if (block.getBlock() == ICBMClassic.multiBlock)
                    {
                        TileEntity tileEntity = world.getTileEntity(location);
                        if (tileEntity instanceof IMultiTile && ((IMultiTile) tileEntity).getHost() != null)
                        {
                            return false;
                        }
                    }
                }
                return true;
            }
        }
        return false;
    }

    @Deprecated
    public static void destroyMultiBlockStructure(IMultiTileHost host)
    {
        destroyMultiBlockStructure(host, true);
    }

    /**
     * Breaks down the multiblock structure linked to the host
     *
     * @param host    - host providing the layout of the structure
     * @param doDrops - attempt to drop blocks?
     */
    public static void destroyMultiBlockStructure(IMultiTileHost host, boolean doDrops)
    {
        destroyMultiBlockStructure(host, doDrops, false);
    }

    /**
     * Breaks down the multiblock stucture linked to the host
     *
     * @param host    - host providing the layour of the structure
     * @param doDrops - attempt to drop blocks?
     * @param offset  - offset the layout by the location of the host?
     */
    public static void destroyMultiBlockStructure(IMultiTileHost host, boolean doDrops, boolean offset)
    {
        destroyMultiBlockStructure(host, doDrops, offset, true);
    }

    /**
     * Breaks down the multiblock stucture linked to the host
     *
     * @param host     - host providing the layour of the structure
     * @param doDrops  - attempt to drop blocks?
     * @param offset   - offset the layout by the location of the host?
     * @param killHost - destroy the host block as well?
     */
    public static void destroyMultiBlockStructure(IMultiTileHost host, boolean doDrops, boolean offset, boolean killHost)
    {
        if (host != null)
        {
            Collection<BlockPos> map = host.getLayoutOfMultiBlock();
            if (map != null && !map.isEmpty())
            {
                BlockPos center;

                if (host instanceof TileEntity)
                {
                    center = ((TileEntity) host).getPos();
                }
                else if (host instanceof IWorldPosition)
                {
                    center = new BlockPos(host.xi(), host.yi(), host.zi());
                }
                else
                {
                    logger.error("MultiBlockHelper >> Tile[" + host + "]'s is not a TileEntity or IWorldPosition instance, thus we can not get a position to break down the structure.");
                    return;
                }

                for (BlockPos pos : map)
                {
                    if (offset)
                    {
                        pos = pos.add(center);
                    }
                    TileEntity tile = host.world().getTileEntity(pos);
                    if (tile instanceof IMultiTile)
                    {
                        ((IMultiTile) tile).setHost(null);
                        host.world().setBlockToAir(pos);
                    }
                }
                if (doDrops)
                {
                    InventoryUtility.dropBlockAsItem(((TileEntity) host).getWorld(), center, killHost);
                }
                else if (killHost)
                {
                    ((TileEntity) host).getWorld().setBlockToAir(center);
                }
            }
            else
            {
                logger.error("MultiBlockHelper >> Tile[" + host + "]'s structure map is empty");
            }
        }
    }
}

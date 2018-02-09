package icbm.classic.content.machines.launcher.frame;

import icbm.classic.api.tile.multiblock.IMultiTile;
import icbm.classic.api.tile.multiblock.IMultiTileHost;
import icbm.classic.lib.network.IPacketIDReceiver;
import icbm.classic.lib.multiblock.MultiBlockHelper;
import icbm.classic.lib.transform.region.Cube;
import icbm.classic.prefab.tile.TileMachine;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;

/**
 * This tile entity is for the screen of the missile launcher
 *
 * @author Calclavia
 */
public class TileLauncherFrame extends TileMachine implements IPacketIDReceiver, IMultiTileHost
{
    public static List<BlockPos> tileMapCache = new ArrayList();

    static
    {
        tileMapCache.add(new BlockPos(0, 1, 0));
        tileMapCache.add(new BlockPos(0, 2, 0));
    }

    private boolean _destroyingStructure = false;

    @Override
    public boolean shouldRefresh(World world, BlockPos pos, IBlockState oldState, IBlockState newSate)
    {
        return false;
    }

    /** Gets the inaccuracy of the missile based on the launcher support frame's tier */
    public int getInaccuracy()
    {
        switch (getTier())
        {
            default:
                return 15;
            case TWO:
                return 7;
            case THREE:
                return 1;
        }
    }

    //==========================================
    //==== Multi-Block code
    //=========================================

    @Override
    public void onLoad()
    {
        super.onLoad();
        MultiBlockHelper.buildMultiBlock(world, this, true, true);
    }

    @Override
    public void onMultiTileAdded(IMultiTile tileMulti)
    {
        if (tileMulti instanceof TileEntity)
        {
            if (getLayoutOfMultiBlock().contains(getPos().subtract(((TileEntity) tileMulti).getPos())))
            {
                tileMulti.setHost(this);
            }
        }
    }

    @Override
    public boolean onMultiTileBroken(IMultiTile tileMulti, Object source, boolean harvest)
    {
        if (!_destroyingStructure && tileMulti instanceof TileEntity)
        {
            if (getLayoutOfMultiBlock().contains(getPos().subtract(((TileEntity) tileMulti).getPos())))
            {
                MultiBlockHelper.destroyMultiBlockStructure(this, harvest, true, true);
                return true;
            }
        }
        return false;
    }

    @Override
    public void onTileInvalidate(IMultiTile tileMulti)
    {

    }

    @Override
    public boolean onMultiTileActivated(IMultiTile tile, EntityPlayer player, EnumHand hand, EnumFacing side, float xHit, float yHit, float zHit)
    {
        //TODO return this.onPlayerRightClick(player, side, new Pos(xHit, yHit, zHit));
        return true;
    }

    @Override
    public void onMultiTileClicked(IMultiTile tile, EntityPlayer player)
    {

    }

    @Override
    public List<BlockPos> getLayoutOfMultiBlock()
    {
        return tileMapCache;
    }


    @Override
    public AxisAlignedBB getRenderBoundingBox()
    {
        return new Cube(-1, 0, -1, 1, 3, 1).add(toPos()).toAABB();
    }
}


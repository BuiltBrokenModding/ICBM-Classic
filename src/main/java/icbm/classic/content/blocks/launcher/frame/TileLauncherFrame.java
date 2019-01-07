package icbm.classic.content.blocks.launcher.frame;

import icbm.classic.api.tile.multiblock.IMultiTile;
import icbm.classic.api.tile.multiblock.IMultiTileHost;
import icbm.classic.content.blocks.launcher.base.TileLauncherBase;
import icbm.classic.content.blocks.multiblock.MultiBlockHelper;
import icbm.classic.lib.network.IPacketIDReceiver;
import icbm.classic.lib.transform.region.Cube;
import icbm.classic.prefab.tile.TileMachine;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.capabilities.Capability;

import javax.annotation.Nullable;
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

    public TileLauncherBase launcherBase;

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

    @Override
    public boolean hasCapability(Capability<?> capability, @Nullable EnumFacing facing)
    {
        if (launcherBase != null)
        {
            return launcherBase.hasCapability(capability, facing);
        }
        return super.hasCapability(capability, facing);
    }

    @Override
    @Nullable
    public <T> T getCapability(Capability<T> capability, @Nullable EnumFacing facing)
    {
        if (launcherBase != null)
        {
            return launcherBase.getCapability(capability, facing);
        }
        return super.getCapability(capability, facing);
    }

    //==========================================
    //==== Multi-Block code
    //=========================================

    @Override
    public void onMultiTileAdded(IMultiTile tileMulti)
    {
        if (tileMulti instanceof TileEntity)
        {
            BlockPos pos = ((TileEntity) tileMulti).getPos().subtract(getPos());
            if (getLayoutOfMultiBlock().contains(pos))
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
            BlockPos pos = ((TileEntity) tileMulti).getPos().subtract(getPos());
            if (getLayoutOfMultiBlock().contains(pos))
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
        return this.onPlayerRightClick(player, hand, player.getHeldItem(hand));
    }

    protected boolean onPlayerRightClick(EntityPlayer player, EnumHand hand, ItemStack heldItem)
    {
        if(launcherBase != null)
        {
            return launcherBase.onPlayerRightClick(player, hand, heldItem);
        }
        return false;
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
        return new Cube(-1, 0, -1, 1, 3, 1).add(this).toAABB();
    }
}


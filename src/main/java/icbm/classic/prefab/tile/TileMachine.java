package icbm.classic.prefab.tile;

import icbm.classic.ICBMClassic;
import icbm.classic.api.data.IWorldPosition;
import icbm.classic.lib.tile.ITick;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Dark(DarkGuardsman, Robert) on 1/9/2017.
 */
public class TileMachine extends TileEntity implements IWorldPosition, ITickable, IGuiTile
{
    protected int ticks = -1;

    protected final List<ITick> tickActions = new ArrayList();

    @Override
    public void update()
    {
        //Increase tick
        ticks++;
        if (ticks >= Integer.MAX_VALUE - 1)
        {
            ticks = 0;
        }

        tickActions.forEach(action -> {
            action.update(ticks, isServer());
        });
    }

    @Override
    public boolean shouldRefresh(World world, BlockPos pos, IBlockState oldState, IBlockState newSate)
    {
        return oldState.getBlock() != newSate.getBlock();
    }

    @Override
    public SPacketUpdateTileEntity getUpdatePacket()
    {
        return new SPacketUpdateTileEntity(pos, 0, getUpdateTag());
    }

    @Override
    public NBTTagCompound getUpdateTag()
    {
        return writeToNBT(new NBTTagCompound());
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt)
    {
        readFromNBT(pkt.getNbtCompound());
    }

    public boolean isServer()
    {
        return world != null && !world.isRemote;
    }

    public boolean isClient()
    {
        return world != null && world.isRemote;
    }

    public EnumFacing getRotation()
    {
        IBlockState state = getBlockState();
        if (state.getProperties().containsKey(BlockICBM.ROTATION_PROP))
        {
            return state.getValue(BlockICBM.ROTATION_PROP);
        }
        return EnumFacing.NORTH;
    }

    public void setRotation(EnumFacing facingDirection)
    {
        //Only update if state has changed
        if (facingDirection != getRotation())
        {
            //Update block state
            IBlockState state = getBlockState();
            if (state.getProperties().containsKey(BlockICBM.ROTATION_PROP))
            {
                world.setBlockState(pos, getBlockState().withProperty(BlockICBM.ROTATION_PROP, facingDirection));
            }
        }
    }

    public IBlockState getBlockState()
    {
        return world.getBlockState(getPos());
    }

    @Override
    public World world()
    {
        return getWorld();
    }

    @Override
    public double z()
    {
        return getPos().getZ();
    }

    @Override
    public double x()
    {
        return getPos().getX();
    }

    @Override
    public double y()
    {
        return getPos().getY();
    }

    @Override
    public boolean openGui(EntityPlayer player, int requestedID)
    {
        player.openGui(ICBMClassic.INSTANCE, requestedID, world, xi(), yi(), zi());
        return true;
    }
}

package icbm.classic.api.events;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.eventhandler.Event;

public class BlastBlockModifyEvent extends Event
{
    private World world = null;
    private BlockPos position = null;
    private IBlockState newState = null;
    private int flags = 0;
    private Runnable callback = null;
    private boolean placedBackDown = false;

    public enum BlockBreakType
    {
        SET_TO_AIR, SET_STATE, SET_STATE_WITH_FLAGS, USE_CALLBACK
    }

    private BlockBreakType modificationType;

    public BlastBlockModifyEvent(World _world, BlockPos _position)
    {
        world = _world;
        position = _position;
        newState = null;
        flags = 0;
        modificationType = BlockBreakType.SET_TO_AIR;
    }

    public BlastBlockModifyEvent(World _world, BlockPos _position, IBlockState _newState)
    {
        world = _world;
        position = _position;
        newState = _newState;
        modificationType = BlockBreakType.SET_STATE;
    }

    public BlastBlockModifyEvent(World _world, BlockPos _position, IBlockState _newState, int _flags)
    {
        world = _world;
        position = _position;
        newState = _newState;
        flags = _flags;
        modificationType = BlockBreakType.SET_STATE_WITH_FLAGS;
    }

    public BlastBlockModifyEvent(World _world, BlockPos _position, IBlockState _newState, int _flags, boolean _placedBackDown)
    {
        world = _world;
        position = _position;
        newState = _newState;
        flags = _flags;
        modificationType = BlockBreakType.SET_STATE_WITH_FLAGS;
        placedBackDown = _placedBackDown; // We are being placed back down if this constructor is called
    }

    public BlastBlockModifyEvent(World _world, BlockPos _position, Runnable _callback)
    {
        modificationType = BlockBreakType.USE_CALLBACK;
        callback = _callback;
        world = _world;
        position = _position;
    }

    public BlockBreakType getModificationType() {
        return modificationType;
    }

    public BlockPos getPosition() {
        return position;
    }

    public IBlockState getNewState() {
        return newState;
    }

    public int getFlags() {
        return flags;
    }

    public World getWorld() {
        return world;
    }

    public Runnable getCallback() {
        return callback;
    }

    public boolean getPlacedBackDown() { return placedBackDown; }
}
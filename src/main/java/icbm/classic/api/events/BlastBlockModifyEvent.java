package icbm.classic.api.events;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.eventhandler.Event;

import javax.annotation.Nullable;

/**
 * Fired when a Blast tries to modify a block in the world to allow interrupting block destruction or modification before it can occur.
 *
 * Created by AFlyingCar on 5/5/20
 */
public class BlastBlockModifyEvent extends Event
{
    private World world = null;
    private BlockPos position = null;
    private IBlockState newState = null;
    private int flags = 0;
    private Runnable callback = null;

    private BlastBlockModifyEventType modificationType;

    /**
     * Creates a BlastBlockModifyEvent that will set a block to air.
     *
     * @param world The world the modification takes place in.
     * @param position The position of the block to modify.
     */
    public BlastBlockModifyEvent(World world, BlockPos position)
    {
        this.world = world;
        this.position = position;
        this.newState = null;
        this.flags = 0;
        this.modificationType = BlastBlockModifyEventType.SET_TO_AIR;
    }

    /**
     * Creates a BlastBlockModifyEvent that will set a new block state.
     *
     * @param world The world the modification takes place in.
     * @param position The position of the block to modify.
     * @param newState The new state of the modified block.
     */
    public BlastBlockModifyEvent(World world, BlockPos position, IBlockState newState)
    {
        this.world = world;
        this.position = position;
        this.newState = newState;
        this.modificationType = BlastBlockModifyEventType.SET_STATE;
    }

    /**
     * Creates a BlastBlockModifyEvent that will set a new block state with flags.
     *
     * @param world The world the modification takes place in.
     * @param position The position of the block to modify.
     * @param newState The new state of the modified block.
     * @param flags The flags to pass to setBlockState().
     */
    public BlastBlockModifyEvent(World world, BlockPos position, IBlockState newState, int flags)
    {
        this.world = world;
        this.position = position;
        this.newState = newState;
        this.flags = flags;
        this.modificationType = BlastBlockModifyEventType.SET_STATE_WITH_FLAGS;
    }

    /**
     * Creates a BlastBlockModifyEvent that will use a callback for modifying the world.
     *
     * @param world The world the modification takes place in.
     * @param position The position of the block to modify.
     * @param callback The callback to run on modification.
     */
    public BlastBlockModifyEvent(World world, BlockPos position, Runnable callback)
    {
        this.modificationType = BlastBlockModifyEventType.USE_CALLBACK;
        this.callback = callback;
        this.world = world;
        this.position = position;
    }

    /**
     * Gets the type of modification that is to occur.
     * @return The modification type.
     */
    public BlastBlockModifyEventType getModificationType() {
        return modificationType;
    }

    /**
     * Gets the position of the block to be modified.
     *
     * @return The position of the block to modify.
     */
    public BlockPos getPosition() {
        return position;
    }

    /**
     * Gets the new IBlockState that will be used to modify the block. If no block state was given, then this may be null.
     *
     * @return The new IBlockState if one exists, or null.
     */
    @Nullable
    public IBlockState getNewState() {
        return newState;
    }

    /**
     * Gets the flags to pass to setBlockState(). If no flags exist, then this will return 0.
     *
     * @return The flags to pass to setBlockState() or 0
     */
    public int getFlags() {
        return flags;
    }

    /**
     * Gets the world this event applies to.
     *
     * @return The world.
     */
    public World getWorld() {
        return world;
    }

    /**
     * Gets the callback of this event. If no callback was given, this may be null.
     *
     * @return The callback if one exists, or null.
     */
    @Nullable
    public Runnable getCallback() {
        return callback;
    }
}
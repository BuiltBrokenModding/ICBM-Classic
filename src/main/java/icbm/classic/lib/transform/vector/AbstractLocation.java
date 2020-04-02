package icbm.classic.lib.transform.vector;

import com.builtbroken.jlib.data.vector.IPos3D;
import icbm.classic.api.data.IWorldPosition;
import icbm.classic.lib.NBTConstants;
import io.netty.buffer.ByteBuf;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.dispenser.ILocation;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.common.DimensionManager;

/**
 * Prefab for location data that doesn't implement IWorldPosition
 * Created by robert on 1/13/2015.
 */
public abstract class AbstractLocation<R extends AbstractLocation> extends AbstractPos<R> implements ILocation
{
    /** Minecraft world for this location */
    public World world;

    public AbstractLocation(World world, double x, double y, double z)
    {
        super(x, y, z);
        this.world = world;
    }

    public AbstractLocation(World world, BlockPos pos)
    {
        super(pos.getX(), pos.getY(), pos.getZ());
        this.world = world;
    }

    /**
     * Creates a location from NBT data
     *
     * @param nbt - valid data, can't be null
     */
    public AbstractLocation(NBTTagCompound nbt)
    {
        this(DimensionManager.getWorld(nbt.getInteger(NBTConstants.DIMENSION)), nbt.getDouble(NBTConstants.X), nbt.getDouble(NBTConstants.Y), nbt.getDouble(NBTConstants.Z));
    }

    /**
     * Creates a location from a ByteBuf
     *
     * @param data - data, should contain int, double, double, double
     */
    public AbstractLocation(ByteBuf data)
    {
        this(DimensionManager.getWorld(data.readInt()), data.readDouble(), data.readDouble(), data.readDouble());
    }

    /**
     * Create a location from an entity's location data
     *
     * @param entity - entity in the world, should be valid
     */
    public AbstractLocation(Entity entity)
    {
        this(entity.world, entity.posX, entity.posY, entity.posZ);
    }

    /**
     * Creates a location from a tile
     *
     * @param tile - valid tile with a world
     */
    public AbstractLocation(TileEntity tile)
    {
        this(tile.getWorld(), tile.getPos());
    }

    /**
     * Creates a location from an {@link IWorldPosition}, basically clones it
     *
     * @param vec - valid location
     */
    public AbstractLocation(IWorldPosition vec)
    {
        this(vec.world(), vec.x(), vec.y(), vec.z());
    }

    /**
     * Creates a location from a world and {@link IPos3D} combo
     *
     * @param world  - valid world, can be null but not recommended
     * @param vector - location data, should be valid
     */
    public AbstractLocation(World world, IPos3D vector)
    {
        this(world, vector.x(), vector.y(), vector.z());
    }

    /**
     * Creates a location from a world and {@link Vec3d} combo
     *
     * @param world - valid world, can be null but not recommended
     * @param vec   - minecraft vector
     */
    public AbstractLocation(World world, Vec3d vec)
    {
        this(world, vec.x, vec.y, vec.z);
    }

    /**
     * Creates a location from a world and {@link RayTraceResult} combo
     *
     * @param world  - valid world, can be null but not recommended
     * @param target - miencraft moving object position
     */
    public AbstractLocation(World world, RayTraceResult target)
    {
        this(world, target.hitVec);
    }

    /**
     * Gets the world instance
     *
     * @return a world
     */
    public World world()
    {
        return world;
    }

    /**
     * Gets the world instance
     *
     * @return a world
     */
    public World getWorld()
    {
        return world;
    }

    /**
     * Conversions
     */
    @Override
    public NBTTagCompound writeNBT(NBTTagCompound nbt)
    {
        nbt.setInteger(NBTConstants.DIMENSION, world != null && world.provider != null ? world.provider.getDimension() : 0);
        nbt.setDouble(NBTConstants.X, x());
        nbt.setDouble(NBTConstants.Y, y());
        nbt.setDouble(NBTConstants.Z, z());
        return nbt;
    }

    @Override
    public ByteBuf writeByteBuf(ByteBuf data)
    {
        data.writeInt(world != null && world.provider != null ? world.provider.getDimension() : 0);
        data.writeDouble(x());
        data.writeDouble(y());
        data.writeDouble(z());
        return data;
    }

    /**
     * @Depricated use {@link #toPos()}
     */
    @Deprecated
    public Pos toVector3()
    {
        return new Pos(x(), y(), z());
    }

    /**
     * Converts the location to a position
     *
     * @return new position from the location data
     */
    public Pos toPos()
    {
        return new Pos(x(), y(), z());
    }

    /**
     * Called to get the block at the position.
     *
     * @return Block or null if the chunk is not loaded
     */
    @Deprecated //Use getBlockState()
    public Block getBlock()
    {
        if (world != null && world.getChunkProvider().isChunkGeneratedAt(xi() / 16, zi() / 16))
        {
            return super.getBlock(world);
        }
        else
        {
            return null;
        }
    }

    public IBlockState getBlockState()
    {
        if (world != null && world.getChunkProvider().isChunkGeneratedAt(xi() / 16, zi() / 16))
        {
            return super.getBlockState(world);
        }
        else
        {
            return null;
        }
    }

    /**
     * Gets the tile entity at the location. Will return null if the world is null or the tile is invalid.
     *
     * @return tile entity, can be null
     */
    public TileEntity getTileEntity()
    {
        if (world != null)
        {
            TileEntity tile = world.getTileEntity(toBlockPos());
            return tile == null || tile.isInvalid() ? null : tile;
        }
        return null;
    }

    /**
     * Gets the block's resistance to being mined
     *
     * @return value of resistance
     */
    public float getHardness()
    {
        return super.getHardness(world);
    }

    /**
     * Gets the resistance value of the block to explosives
     *
     * @param cause - sources of the explosive
     * @param xx    - location of the explosion
     * @param yy    - location of the explosion
     * @param zz    - location of the explosion
     * @return value of resistance to the explosion
     */
    public float getResistance(Entity cause, double xx, double yy, double zz)
    {
        return super.getResistance(world, cause, xx, yy, zz);
    }

    /**
     * Replaces the block at the location with a new block
     *
     * @param block    - block to place
     * @param notify   - notification level to use when placing the block
     * @return true if it was repalced
     */
    public boolean setBlock(IBlockState block, int notify)
    {
        return super.setBlock(world, block, notify);
    }

    /**
     * Replaces the block at the location with a new block
     *
     * @return true if it was repalced
     */
    public boolean setBlock(IBlockState state)
    {
        return super.setBlock(world, state, 3);
    }

    /**
     * Replaces the block at the location with a new block
     *
     * @param block - block to place
     * @return true if it was repalced
     */
    public boolean setBlock(Block block)
    {
        return super.setBlock(world, block);
    }

    /**
     * Removes the block at the location and replaces it with an air block
     *
     * @return true if the block was replaced
     */
    public boolean setBlockToAir()
    {
        return super.setBlockToAir(world);
    }

    /**
     * Is the block an air block
     *
     * @return true if the block is an air block
     */
    public boolean isAirBlock()
    {
        return super.isAirBlock(world);
    }

    /**
     * Is the block passed in equal to the block at the location
     *
     * @param block - block to check
     * @return true if they match
     */
    public boolean isBlockEqual(Block block)
    {
        return super.isBlockEqual(world, block);
    }

    /**
     * Checks if the block at the locate is freezable
     *
     * @return true if the block can be frozen
     */
    public boolean isBlockFreezable()
    {
        return super.isBlockFreezable(world);
    }

    /**
     * Checks if the block is replaceable
     *
     * @return true if it can be replaced
     */
    public boolean isReplaceable()
    {
        return super.isReplaceable(world);
    }

    /**
     * Checks to see if the tile can see the sky
     *
     * @return true if it can see sky, false if not or world is null
     */
    public boolean canSeeSky()
    {
        return super.canSeeSky(world);
    }

    /**
     * Checks if the chunk is loaded at the location
     *
     * @return true if the chunk is loaded
     */
    public boolean isChunkLoaded()
    {
        //For some reason the server has it's own chunk provider that actually checks if the chunk exists
        if (world instanceof WorldServer)
        {
            return ((WorldServer) world).getChunkProvider().chunkExists(xi() >> 4, zi() >> 4) && getChunk().isLoaded();
        }
        return world.getChunkProvider().isChunkGeneratedAt(xi() >> 4, zi() >> 4) && getChunk().isLoaded();
    }

    /**
     * Gets the chunk from the location data
     *
     * @return chunk the location is in
     */
    public Chunk getChunk()
    {
        return world.getChunk(toBlockPos());
    }

    /** Marks a block for update */
    public void markForUpdate()
    {
        super.markForUpdate(world);
    }

    @Override
    public boolean equals(Object o)
    {
        return o instanceof AbstractLocation && this.world == ((AbstractLocation) o).world() && ((AbstractLocation) o).x() == x() && ((AbstractLocation) o).y() == y() && ((AbstractLocation) o).z() == z();
    }

    @Override
    public String toString()
    {
        return "WorldLocation [" + this.x() + "x," + this.y() + "y," + this.z() + "z," + (this.world == null ? "n" : this.world.provider == null ? "p" : this.world.provider.getDimension()) + "d]";
    }
}

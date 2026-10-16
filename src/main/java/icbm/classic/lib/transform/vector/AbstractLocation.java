package icbm.classic.lib.transform.vector;

import com.builtbroken.jlib.data.vector.IPos3D;
import icbm.classic.api.data.IWorldPosition;
import icbm.classic.lib.NBTConstants;
import io.netty.buffer.ByteBuf;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.entity.Entity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.Level;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

/**
 * Prefab for location data that doesn't implement IWorldPosition
 * Created by Robin on 1/13/2015.
 */
// PORT: 1.12 implemented net.minecraft.dispenser.ILocation, which has no 1.21 equivalent
public abstract class AbstractLocation<R extends AbstractLocation> extends AbstractPos<R>
{
    /** Minecraft world for this location */
    public Level world;

    public AbstractLocation(Level world, double x, double y, double z)
    {
        super(x, y, z);
        this.world = world;
    }

    public AbstractLocation(Level world, BlockPos pos)
    {
        super(pos.getX(), pos.getY(), pos.getZ());
        this.world = world;
    }

    /**
     * Creates a location from NBT data
     *
     * PORT: dimension is stored as the dimension's resource location string instead of an int id
     *
     * @param nbt - valid data, can't be null
     */
    public AbstractLocation(CompoundTag nbt)
    {
        this(lookupWorld(readDimension(nbt)), nbt.getDouble(NBTConstants.X), nbt.getDouble(NBTConstants.Y), nbt.getDouble(NBTConstants.Z));
    }

    /**
     * Creates a location from a ByteBuf
     *
     * PORT: dimension is sent as a resource key instead of an int id
     *
     * @param data - data, should contain dimension key, double, double, double
     */
    public AbstractLocation(ByteBuf data)
    {
        this(lookupWorld(new FriendlyByteBuf(data).readResourceKey(Registries.DIMENSION)), data.readDouble(), data.readDouble(), data.readDouble());
    }

    /**
     * Replacement for 1.12 DimensionManager#getWorld. Only works when a server is running (dedicated or integrated)
     *
     * @param key of the dimension
     * @return level or null if not found
     */
    public static Level lookupWorld(ResourceKey<Level> key)
    {
        final MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (key != null && server != null)
        {
            return server.getLevel(key);
        }
        return null;
    }

    /**
     * Reads the dimension key saved by {@link #writeNBT(CompoundTag)}
     *
     * @param nbt to read
     * @return key or null if missing/invalid
     */
    public static ResourceKey<Level> readDimension(CompoundTag nbt)
    {
        final ResourceLocation location = ResourceLocation.tryParse(nbt.getString(NBTConstants.DIMENSION));
        if (location != null && nbt.contains(NBTConstants.DIMENSION))
        {
            return ResourceKey.create(Registries.DIMENSION, location);
        }
        return null;
    }

    private ResourceKey<Level> dimensionOrDefault()
    {
        return world != null ? world.dimension() : Level.OVERWORLD;
    }

    /**
     * Create a location from an entity's location data
     *
     * @param entity - entity in the world, should be valid
     */
    public AbstractLocation(Entity entity)
    {
        this(entity.level(), entity.getX(), entity.getY(), entity.getZ());
    }

    /**
     * Creates a location from a tile
     *
     * @param tile - valid tile with a world
     */
    public AbstractLocation(BlockEntity tile)
    {
        this(tile.getLevel(), tile.getBlockPos());
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
    public AbstractLocation(Level world, IPos3D vector)
    {
        this(world, vector.x(), vector.y(), vector.z());
    }

    /**
     * Creates a location from a world and {@link Vec3} combo
     *
     * @param world - valid world, can be null but not recommended
     * @param vec   - minecraft vector
     */
    public AbstractLocation(Level world, Vec3 vec)
    {
        this(world, vec.x, vec.y, vec.z);
    }

    /**
     * Creates a location from a world and {@link HitResult} combo
     *
     * @param world  - valid world, can be null but not recommended
     * @param target - miencraft moving object position
     */
    public AbstractLocation(Level world, HitResult target)
    {
        this(world, target.getLocation());
    }

    /**
     * Gets the world instance
     *
     * @return a world
     */
    public Level world()
    {
        return world;
    }

    /**
     * Gets the world instance
     *
     * @return a world
     */
    public Level getWorld()
    {
        return world;
    }

    /**
     * Conversions
     */
    @Override
    public CompoundTag writeNBT(CompoundTag nbt)
    {
        nbt.putString(NBTConstants.DIMENSION, dimensionOrDefault().location().toString());
        nbt.putDouble(NBTConstants.X, x());
        nbt.putDouble(NBTConstants.Y, y());
        nbt.putDouble(NBTConstants.Z, z());
        return nbt;
    }

    @Override
    public ByteBuf writeByteBuf(ByteBuf data)
    {
        new FriendlyByteBuf(data).writeResourceKey(dimensionOrDefault());
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
        if (world != null && world.hasChunk(xi() / 16, zi() / 16))
        {
            return super.getBlock(world);
        }
        else
        {
            return null;
        }
    }

    public BlockState getBlockState()
    {
        if (world != null && world.hasChunk(xi() / 16, zi() / 16))
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
    public BlockEntity getTileEntity()
    {
        if (world != null)
        {
            BlockEntity tile = world.getBlockEntity(toBlockPos());
            return tile == null || tile.isRemoved() ? null : tile;
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
    public boolean setBlock(BlockState block, int notify)
    {
        return super.setBlock(world, block, notify);
    }

    /**
     * Replaces the block at the location with a new block
     *
     * @return true if it was repalced
     */
    public boolean setBlock(BlockState state)
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
        // PORT: 1.21 hasChunk only reports chunks that are loaded, which covers both 1.12 checks
        return world.hasChunk(xi() >> 4, zi() >> 4);
    }

    /**
     * Gets the chunk from the location data
     *
     * @return chunk the location is in
     */
    public LevelChunk getChunk()
    {
        return world.getChunkAt(toBlockPos());
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
        return "WorldLocation [" + this.x() + "x," + this.y() + "y," + this.z() + "z," + (this.world == null ? "n" : this.world.dimension().location()) + "d]";
    }
}
// PORTED

package icbm.classic.lib.transform.region;

import com.builtbroken.jlib.data.network.IByteBufWriter;
import com.builtbroken.jlib.data.vector.IPos3D;
import icbm.classic.ICBMClassic;
import icbm.classic.lib.NBTConstants;
import icbm.classic.lib.transform.vector.Point;
import icbm.classic.lib.transform.vector.Pos;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.Chunk;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by robert on 2/16/2015.
 */
public class Cube extends Shape3D implements Cloneable, IByteBufWriter
{
    public static final Cube EMPTY = new Cube().disableEdits();
    public static final Cube FULL = new Cube(0, 0, 0, 1, 1, 1).disableEdits();

    private IPos3D pointOne;
    private IPos3D pointTwo;
    private Pos lowerPoint;
    private Pos higherPoint;

    private boolean canEdit = true;

    public Cube()
    {
        this(new Pos(0, -1, 0), new Pos(0, -1, 0));
    }

    public Cube(IPos3D min, IPos3D max)
    {
        super((Pos) null);
        set(min, max);
    }

    public Cube(Cube cube)
    {
        this(cube.pointOne(), cube.pointTwo());
    }

    public Cube(double x, double y, double z, double i, double j, double k)
    {
        this(new Pos(x, y, z), new Pos(i, j, k));
    }

    public Cube(AxisAlignedBB bb)
    {
        this(new Pos(bb.minX, bb.minY, bb.minZ), new Pos(bb.maxX, bb.maxY, bb.maxZ));
    }

    public Cube(NBTTagCompound nbt)
    {
        super(nbt);
        if (nbt.hasKey(NBTConstants.POINT_ONE))
        {
            pointOne = new Pos(nbt.getCompoundTag(NBTConstants.POINT_ONE));
        }
        if (nbt.hasKey(NBTConstants.POINT_TWO))
        {
            pointTwo = new Pos(nbt.getCompoundTag(NBTConstants.POINT_TWO));
        }
        recalc();
    }

    public Cube(ByteBuf buf)
    {
        this(new Pos(buf), new Pos(buf));
    }

    public Cube enableEdits()
    {
        canEdit = true;
        return this;
    }

    public Cube disableEdits()
    {
        canEdit = false;
        return this;
    }

    @Override
    public ByteBuf writeBytes(ByteBuf buf)
    {
        buf.writeDouble(pointOne != null ? pointOne.x() : 0);
        buf.writeDouble(pointOne != null ? pointOne.y() : -1);
        buf.writeDouble(pointOne != null ? pointOne.z() : 0);

        buf.writeDouble(pointTwo != null ? pointTwo.x() : 0);
        buf.writeDouble(pointTwo != null ? pointTwo.y() : -1);
        buf.writeDouble(pointTwo != null ? pointTwo.z() : 0);
        return buf;
    }

    //////////////////////
    ///Conversion methods
    ///////////////////////

    public AxisAlignedBB toAABB()
    {
        return isValid() ? new AxisAlignedBB(min().x(), min().y(), min().z(), max().x(), max().y(), max().z()) : new AxisAlignedBB(0, 0, 0, 0, 0, 0);
    }

    public AxisAlignedBB getAABB()
    {
        return new AxisAlignedBB(min().x(), min().y(), min().z(), max().x(), max().y(), max().z());
    }

    public Rectangle toRectangle()
    {
        return isValid() ? new Rectangle(new Point(min()), new Point(max())) : null;
    }

    public NBTTagCompound toNBT()
    {
        return save(new NBTTagCompound());
    }

    public NBTTagCompound save(NBTTagCompound tag)
    {
        if (pointOne != null)
        {
            tag.setTag(NBTConstants.POINT_ONE, new Pos(pointOne).writeNBT(new NBTTagCompound()));
        }
        if (pointTwo != null)
        {
            tag.setTag(NBTConstants.POINT_TWO, new Pos(pointTwo).writeNBT(new NBTTagCompound()));
        }
        return tag;
    }


    //////////////////////
    ///Math methods
    /////////////////////

    public Cube add(IPos3D vec)
    {
        return add(vec.x(), vec.y(), vec.z());
    }

    public Cube add(double x, double y, double z)
    {
        if (isValid() && canEdit)
        {
            pointOne = new Pos(pointOne.x() + x, pointOne.y() + y, pointOne.z() + z);
            pointTwo = new Pos(pointTwo.x() + x, pointTwo.y() + y, pointTwo.z() + z);
            recalc();
        }
        return this;
    }

    public Cube subtract(IPos3D vec)
    {
        return subtract(vec.x(), vec.y(), vec.z());
    }

    public Cube subtract(double x, double y, double z)
    {
        if (isValid() && canEdit)
        {
            pointOne = new Pos(pointOne.x() - x, pointOne.y() - y, pointOne.z() - z);
            pointTwo = new Pos(pointTwo.x() - x, pointTwo.y() - y, pointTwo.z() - z);
            recalc();
        }
        return this;
    }

    //////////////////////
    /// Collision Detection
    /////////////////////

    public boolean intersects(Vec3d v)
    {
        return intersects(v.x, v.y, v.z);
    }

    public boolean intersects(IPos3D v)
    {
        return intersects(v.x(), v.y(), v.z());
    }

    public boolean intersects(double x, double y, double z)
    {
        return isWithinX(x) && isWithinY(y) && isWithinZ(z);
    }

    public boolean doesOverlap(AxisAlignedBB box)
    {
        return doesOverlap(box.minX, box.minY, box.minZ, box.maxX, box.maxY, box.maxZ);
    }

    public boolean doesOverlap(Cube box)
    {
        return box.isValid() && doesOverlap(box.min().x(), box.min().y(), box.min().z(), box.max().x(), box.max().y(), box.max().z());
    }

    public boolean doesOverlap(double x, double y, double z, double i, double j, double k)
    {
        return !isOutSideX(x, i) || !isOutSideY(y, j) || !isOutSideZ(z, k);
    }

    public boolean isOutSideX(double min, double max)
    {
        return min().x() > min || max > max().x();
    }

    public boolean isOutSideY(double max, double min)
    {
        return min().y() > max || min > max().y();
    }

    public boolean isOutSideZ(double max, double min)
    {
        return min().z() > max || min > max().z();
    }

    public boolean isInsideBounds(double x, double y, double z, double i, double j, double k)
    {
        return isWithin(min().x(), max().x(), x, i) && isWithin(min().y(), max().y(), y, j) && isWithin(min().z(), max().z(), z, k);
    }

    public boolean isInsideBounds(Cube other)
    {
        return isInsideBounds(other.min().x(), other.min().y(), other.min().z(), other.max().x(), other.max().y(), other.max().z());
    }

    public boolean isInsideBounds(AxisAlignedBB other)
    {
        return isInsideBounds(other.minX, other.minY, other.minZ, other.maxX, other.maxY, other.maxZ);
    }

    public boolean isVecInYZ(Vec3d v)
    {
        return isWithinY(v) && isWithinZ(v);
    }

    public boolean isVecInYZ(IPos3D v)
    {
        return isWithinY(v) && isWithinZ(v);
    }

    public boolean isWithinXZ(Vec3d v)
    {
        return isWithinX(v) && isWithinZ(v);
    }

    public boolean isWithinXZ(IPos3D v)
    {
        return isWithinX(v) && isWithinZ(v);
    }

    public boolean isWithinX(double v)
    {
        return isWithinRange(min().x(), max().x(), v);
    }

    public boolean isWithinX(Vec3d v)
    {
        return isWithinX(v.x);
    }

    public boolean isWithinX(IPos3D v)
    {
        return isWithinX(v.x());
    }

    public boolean isWithinY(double v)
    {
        return isWithinRange(min().y(), max().y(), v);
    }

    public boolean isWithinY(Vec3d v)
    {
        return isWithinY(v.y);
    }

    public boolean isWithinY(IPos3D v)
    {
        return isWithinY(v.y());
    }

    public boolean isWithinZ(double v)
    {
        return isWithinRange(min().z(), max().z(), v);
    }

    public boolean isWithinZ(Vec3d v)
    {
        return isWithinZ(v.z);
    }

    public boolean isWithinZ(IPos3D v)
    {
        return isWithinZ(v.z());
    }

    public boolean isWithinRange(double min, double max, double v)
    {
        return v >= min + 1E-5 && v <= max - 1E-5;
    }

    @Override
    public boolean isWithin(double x, double y, double z)
    {
        return isWithinX(x) && isWithinY(y) && isWithinZ(z);
    }

    public boolean isWithin(IPos3D pos)
    {
        return isWithinX(pos.x()) && isWithinY(pos.y()) && isWithinZ(pos.z());
    }

    public boolean isWithinInt(IPos3D pos)
    {
        return isWithinX(pos.xi()) && isWithinY(pos.yi()) && isWithinZ(pos.zi());
    }


    /**
     * Checks to see if a line segment is within the defined line. Assume the lines overlap each other.
     *
     * @param min - pointOne point
     * @param max - pointTwo point
     * @param a   - pointOne line point
     * @param b   - pointTwo line point
     * @return true if the line segment is within the bounds
     */
    public boolean isWithin(double min, double max, double a, double b)
    {
        return a + 1E-5 >= min && b - 1E-5 <= max;
    }

    public static IPos3D[] getCorners(Cube box)
    {
        IPos3D[] array = new IPos3D[8];
        if (box.isValid())
        {
            double l = box.pointTwo.x() - box.pointOne.x();
            double w = box.pointTwo.z() - box.pointOne.z();
            double h = box.pointTwo.y() - box.pointOne.y();
            array[0] = new Pos(box.pointOne.x(), box.pointOne.y(), box.pointOne.z());
            array[1] = new Pos(box.pointOne.x(), box.pointOne.y() + h, box.pointOne.z());
            array[2] = new Pos(box.pointOne.x(), box.pointOne.y() + h, box.pointOne.z() + w);
            array[3] = new Pos(box.pointOne.x(), box.pointOne.y(), box.pointOne.z() + w);
            array[4] = new Pos(box.pointOne.x() + l, box.pointOne.y(), box.pointOne.z());
            array[5] = new Pos(box.pointOne.x() + l, box.pointOne.y() + h, box.pointOne.z());
            array[6] = new Pos(box.pointOne.x() + l, box.pointOne.y() + h, box.pointOne.z() + w);
            array[7] = new Pos(box.pointOne.x() + l, box.pointOne.y(), box.pointOne.z() + w);
        }
        return array;
    }

    public static IPos3D[] getCorners(AxisAlignedBB box)
    {
        return getCorners(new Cube(box));
    }


    //////////////////////
    ///Accessors
    /////////////////////

    public double radius()
    {
        double m = 0;
        if (getSizeX() > m)
        {
            m = getSizeX();
        }
        if (getSizeY() > m)
        {
            m = getSizeY();
        }
        if (getSizeZ() > m)
        {
            m = getSizeZ();
        }
        return m;
    }

    @Override
    public double getVolume()
    {
        return getSizeX() * getSizeY() * getSizeZ();
    }

    @Override
    public double getArea()
    {
        double area = 0;
        area += getSizeX() * getSizeZ() * 2;
        area += getSizeY() * getSizeZ() * 2;
        area += getSizeX() * getSizeY() * 2;
        return area;
    }

    @Override
    public double getSizeX()
    {
        if (isValid())
        {
            return max().x() - min().x() + 1;
        }
        return 0;
    }

    @Override
    public double getSizeY()
    {
        if (isValid())
        {
            return max().y() - min().y() + 1;
        }
        return 0;
    }

    @Override
    public double getSizeZ()
    {
        if (isValid())
        {
            return max().z() - min().z() + 1;
        }
        return 0;
    }

    public IPos3D pointOne()
    {
        return pointOne;
    }

    public IPos3D pointTwo()
    {
        return pointTwo;
    }

    public Pos min()
    {
        return lowerPoint;
    }

    public Pos max()
    {
        return higherPoint;
    }

    public boolean isSquared()
    {
        return getSizeX() == getSizeY() && getSizeY() == getSizeZ();
    }

    public boolean isSquaredInt()
    {
        return (int) getSizeX() == (int) getSizeY() && (int) getSizeY() == (int) getSizeZ();
    }

    public double distance(Vec3d v)
    {
        if (!isValid())
        {
            if (min() != null)
            {
                return new Pos(min()).distance(v);
            }
            else if (max() != null)
            {
                return new Pos(max()).distance(v);
            }
            else
            {
                return Double.MIN_VALUE;
            }
        }
        return center.distance(v);
    }

    public double distance(IPos3D v)
    {
        if (!isValid())
        {
            if (min() != null)
            {
                return new Pos(min()).distance(v);
            }
            else if (max() != null)
            {
                return new Pos(max()).distance(v);
            }
            else
            {
                return Double.MIN_VALUE;
            }
        }
        return center.distance(v);
    }

    public double distance(Cube box)
    {
        return distance(box.center);
    }

    public double distance(AxisAlignedBB box)
    {
        return distance(new Cube(box));
    }

    public double distance(double xx, double yy, double zz)
    {
        if (this.isValid())
        {
            IPos3D center = this.center;
            double x = center.x() - xx;
            double y = center.y() - yy;
            double z = center.z() - zz;
            return Math.sqrt(x * x + y * y + z * z);
        }
        return 0;
    }

    /**
     * Generates a list of all entities in the cube
     *
     * @param world - world to search
     * @return list
     */
    public List<Entity> getEntities(World world)
    {
        return getEntities(world, Entity.class);
    }

    /**
     * Generates a list of all entities in the cube that extend the class
     *
     * @param world       - world to search
     * @param entityClass - class to search for
     * @return list
     */
    public List getEntities(World world, Class<? extends Entity> entityClass)
    {
        return world.getEntitiesWithinAABB(entityClass, toAABB());
    }

    /**
     * Scans threw the loaded chunks grab all tiles
     *
     * @param world - world to scan
     * @return list
     */
    public List<TileEntity> getTilesInArea(World world)
    {
        List<TileEntity> tilesInArea = new ArrayList();
        for (Chunk chunk : getChunks(world))
        {
            for (Object object : chunk.getTileEntityMap().values())
            {
                if (object instanceof TileEntity && ((TileEntity) object).isInvalid() && ((TileEntity) object).getWorld() != null && isWithin(((TileEntity) object).getPos()))
                {
                    tilesInArea.add((TileEntity) object);
                }
            }
        }
        return tilesInArea;
    }

    /**
     * Grabs chunks loaded in the region
     *
     * @param world - world to search
     * @return
     */
    public List<Chunk> getChunks(World world)
    {
        return getChunks(world, true);
    }

    /**
     * Grabs chunks in the cube
     *
     * @param world  - world to search
     * @param loaded - only grab loaded chunks
     * @return list
     */
    public List<Chunk> getChunks(World world, boolean loaded)
    {
        List<Chunk> chunks = new ArrayList();
        for (int chunkX = (min().xi() >> 4) - 1; chunkX <= (max().xi() >> 4) + 1; chunkX++)
        {
            for (int chunkZ = (min().zi() >> 4) - 1; chunkZ <= (max().zi() >> 4) + 1; chunkZ++)
            {
                if (loaded || (!(world instanceof WorldServer) || ((WorldServer) world).getChunkProvider().chunkExists(chunkX, chunkZ)))
                {
                    Chunk chunk = world.getChunk(chunkX, chunkZ);
                    if (chunk != null)
                    {
                        chunks.add(chunk);
                    }
                }
            }
        }
        return chunks;
    }

    /**
     * Gets a lit of chunk coords, used when a list of chunks is not actually need.
     *
     * @return list of coords using cube bounds
     * for an example of usage.
     */
    public List<ChunkPos> getChunkCoords()
    {
        List<ChunkPos> chunks = new ArrayList();
        for (int chunkX = (min().xi() >> 4) - 1; chunkX <= (max().xi() >> 4) + 1; chunkX++)
        {
            for (int chunkZ = (min().zi() >> 4) - 1; chunkZ <= (max().zi() >> 4) + 1; chunkZ++)
            {
                chunks.add(new ChunkPos(chunkX, chunkZ));
            }
        }
        return chunks;
    }


    //////////////////////
    ///Setters
    /////////////////////

    public void setPointOne(IPos3D pos)
    {
        if (canEdit)
        {
            this.pointOne = pos;
            recalc();
        }
    }

    public void setPointTwo(IPos3D pos)
    {
        if (canEdit)
        {
            this.pointTwo = pos;
            recalc();
        }
    }

    public void set(IPos3D min, IPos3D max)
    {
        if (canEdit)
        {
            this.pointOne = min;
            this.pointTwo = max;
            recalc();
        }
    }

    public Cube set(Cube cube)
    {
        this.set(cube.min() != null ? new Pos(cube.min()) : null, cube.max() != null ? new Pos(cube.max()) : null);
        return this;
    }

    /**
     * Called after the cube's data has changed in order
     * to update any internal data.
     */
    protected void recalc()
    {
        if (canEdit)
        {
            if (pointOne != null && pointTwo != null)
            {
                lowerPoint = new Pos(Math.min(pointOne.x(), pointTwo.x()), Math.min(pointOne.y(), pointTwo.y()), Math.min(pointOne.z(), pointTwo.z()));
                higherPoint = new Pos(Math.max(pointOne.x(), pointTwo.x()), Math.max(pointOne.y(), pointTwo.y()), Math.max(pointOne.z(), pointTwo.z()));
                this.center = new Pos(min().x() + (getSizeX() / 2), min().y() + (getSizeY() / 2), min().z() + (getSizeZ() / 2));
            }
            else
            {
                this.center = null;
            }
        }
    }

    /**
     * Used to check if the cube is valid. Amounts to a few null checks
     */
    public boolean isValid()
    {
        return min() != null && max() != null && !max().equals(min()) && min().y() > -1 && max().y() > -1;
    }

    @Override
    public Cube clone()
    {
        return new Cube(pointOne, pointTwo);
    }

    @Override
    public String toString()
    {
        return "Cube[" + pointOne + "  " + pointTwo + "]";
    }

    @Override
    public boolean equals(Object other)
    {
        return other instanceof Cube && ((Cube) other).pointOne == pointOne && ((Cube) other).pointTwo == pointTwo;
    }

    /**
     * Mainly used by the selection render to see if camera is close enough to
     * the bounds to render. Mainly checks for distance to corner, center, and is
     * in side bounds.
     *
     * @param pos      - location to check from
     * @param distance - distance to check
     * @return true if any conditions are meet
     */
    public boolean isCloseToAnyCorner(IPos3D pos, int distance)
    {
        if (pos != null)
        {
            //If we are near the center return true even if the corners are too far away
            if (center != null && center.distance(pos) <= distance)
            {
                return true;
            }

            //If we are inside then we should be able to render the bounds
            if (isWithin(pos))
            {
                return true;
            }

            if (lowerPoint != null && higherPoint != null)
            {
                if (pos.x() <= lowerPoint.x() && lowerPoint.x() - pos.x() >= distance)
                {
                    return false;
                }

                if (pos.y() <= lowerPoint.y() && lowerPoint.y() - pos.y() >= distance)
                {
                    return false;
                }

                if (pos.z() <= lowerPoint.z() && lowerPoint.z() - pos.z() >= distance)
                {
                    return true;
                }

                if (pos.x() >= higherPoint.x() && pos.x() - higherPoint.x() >= distance)
                {
                    return false;
                }

                if (pos.y() >= higherPoint.y() && pos.y() - higherPoint.y() >= distance)
                {
                    return false;
                }

                return !(pos.z() >= higherPoint.z() && pos.z() - higherPoint.z() >= distance);

            }
            else if (lowerPoint != null)
            {
                return lowerPoint.distance(pos) <= distance;
            }
            else if (higherPoint != null)
            {
                return higherPoint.distance(pos) <= distance;
            }
        }
        return false;
    }

    /**
     * Limits the cube to the world bounds
     *
     * @return this
     */
    public Cube cropToWorld()
    {
        Pos one = min();
        Pos two = max();
        if (min().y() < 0)
        {
            one = new Pos(min().x(), 0, min().y());
        }
        if (max().y() > ICBMClassic.MAP_HEIGHT)
        {
            two = new Pos(min().x(), ICBMClassic.MAP_HEIGHT, min().y());
        }
        set(one, two);
        return this;
    }

    public Cube expand(int range)
    {
        Pos start = min().sub(range);
        Pos end = max().add(range);
        set(start, end);
        return this;
    }
}

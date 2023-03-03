package icbm.classic.lib.transform.vector;

import com.builtbroken.jlib.data.vector.IPos3D;
import com.builtbroken.jlib.data.vector.ITransform;
import com.builtbroken.jlib.data.vector.Pos3D;
import icbm.classic.ICBMClassic;
import icbm.classic.lib.NBTConstants;
import icbm.classic.lib.transform.rotation.EulerAngle;
import io.netty.buffer.ByteBuf;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.dispenser.IPosition;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.*;
import net.minecraft.world.Explosion;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import java.util.List;

/**
 * Abstract version of Pos3D for interaction with the minecraft world
 * Created by robert on 1/13/2015.
 */
public abstract class AbstractPos<R extends AbstractPos> extends Pos3D<R> implements IPosition
{
    public AbstractPos()
    {
        this(0, 0, 0);
    }

    public AbstractPos(double a)
    {
        this(a, a, a);
    }

    public AbstractPos(double x, double y, double z)
    {
        super(x, y, z);
    }

    public AbstractPos(double yaw, double pitch)
    {
        this(-Math.sin(Math.toRadians(yaw)), Math.sin(Math.toRadians(pitch)), -Math.cos(Math.toRadians(yaw)) * Math.cos(Math.toRadians(pitch)));
    }

    public AbstractPos(TileEntity tile)
    {
        this(tile.getPos());
    }

    public AbstractPos(Entity entity)
    {
        this(entity.posX, entity.posY, entity.posZ);
    }

    public AbstractPos(IPos3D vec)
    {
        this(vec.x(), vec.y(), vec.z());
    }

    public AbstractPos(NBTTagCompound nbt)
    {
        this(nbt.getDouble(NBTConstants.X), nbt.getDouble(NBTConstants.Y), nbt.getDouble(NBTConstants.Z));
    }

    public AbstractPos(ByteBuf data)
    {
        this(data.readDouble(), data.readDouble(), data.readDouble());
    }

    public AbstractPos(BlockPos par1)
    {
        this(par1.getX(), par1.getY(), par1.getZ());
    }

    public AbstractPos(EnumFacing dir)
    {
        this(dir.getFrontOffsetX(), dir.getFrontOffsetY(), dir.getFrontOffsetZ());
    }

    public AbstractPos(Vec3d vec)
    {
        this(vec.x, vec.y, vec.z);
    }

    public double angle(IPos3D other)
    {
        return Math.acos((this.cross(other)).magnitude() / (new Pos(other).magnitude() * magnitude()));
    }

    public double anglePreNorm(IPos3D other)
    {
        return Math.acos(this.cross(other).magnitude());
    }

    //=========================
    //========Converters=======
    //=========================

    public Vec3d toVec3d()
    {
        return new Vec3d(x(), y(), z());
    }

    public Point toVector2()
    {
        return new Point(x(), z());
    }

    public EnumFacing toDirection()
    {
        //TODO maybe add a way to convert convert any vector into a direction from origin
        for (EnumFacing dir : EnumFacing.values())
        {
            if (xi() == dir.getFrontOffsetX() && yi() == dir.getFrontOffsetY() && zi() == dir.getFrontOffsetZ())
            {
                return dir;
            }
        }
        return null;
    }

    public EulerAngle toEulerAngle(IPos3D target)
    {
        return sub(target).toEulerAngle();
    }

    public EulerAngle toEulerAngle(Vec3d target)
    {
        return sub(target).toEulerAngle();
    }

    public EulerAngle toEulerAngle()
    {
        return new EulerAngle(Math.toDegrees(Math.atan2(x(), z())), Math.toDegrees(-Math.atan2(y(), Math.hypot(z(), x()))));
    }

    public IPos3D transform(ITransform transformer)
    {
        if (this instanceof IPos3D)
        {
            return transformer.transform((IPos3D) this);
        }
        return null;
    }

    /**
     * Calls {@link Math#abs(double)} on each term of the pos data
     *
     * @return abs
     */
    public R absolute()
    {
        return newPos(Math.abs(x()), Math.abs(y()), Math.abs(z()));
    }

    //=========================
    //======Math Operators=====
    //=========================

    public R add(BlockPos other)
    {
        return add(other.getX(), other.getY(), other.getZ());
    }

    public R add(EnumFacing face)
    {
        return add(face.getFrontOffsetX(), face.getFrontOffsetY(), face.getFrontOffsetZ());
    }

    public R add(Vec3d vec)
    {
        return add(vec.x, vec.y, vec.z);
    }

    public R sub(EnumFacing face)
    {
        return sub(face.getFrontOffsetX(), face.getFrontOffsetY(), face.getFrontOffsetZ());
    }

    public R sub(Vec3d vec)
    {
        return sub(vec.x, vec.y, vec.z);
    }

    public double distance(Vec3i vec)
    {
        return distance(vec.getX() + 0.5, vec.getY() + 0.5, vec.getZ() + 0.5);
    }

    public double distance(Vec3d vec)
    {
        return distance(vec.x, vec.y, vec.z);
    }

    public double distance(Entity entity)
    {
        return distance(entity.posX, entity.posY, entity.posZ);
    }

    public R multiply(EnumFacing face)
    {
        return multiply(face.getFrontOffsetX(), face.getFrontOffsetY(), face.getFrontOffsetZ());
    }

    public R multiply(Vec3d vec)
    {
        return multiply(vec.x, vec.y, vec.z);
    }

    public R divide(EnumFacing face)
    {
        return divide(face.getFrontOffsetX(), face.getFrontOffsetY(), face.getFrontOffsetZ());
    }

    public R divide(Vec3d vec)
    {
        return divide(vec.x, vec.y, vec.z);
    }

    @Override
    public R floor()
    {
        return newPos(Math.floor(x()), Math.floor(y()), Math.floor(z()));
    }

    //=========================
    //========NBT==============
    //=========================

    public NBTTagCompound toNBT()
    {
        return writeNBT(new NBTTagCompound());
    }

    public NBTTagCompound toIntNBT()
    {
        return writeIntNBT(new NBTTagCompound());
    }

    public NBTTagCompound writeNBT(NBTTagCompound nbt)
    {
        nbt.setDouble(NBTConstants.X, x());
        nbt.setDouble(NBTConstants.Y, y());
        nbt.setDouble(NBTConstants.Z, z());
        return nbt;
    }


    public NBTTagCompound writeIntNBT(NBTTagCompound nbt)
    {
        nbt.setInteger(NBTConstants.X, xi());
        nbt.setInteger(NBTConstants.Y, yi());
        nbt.setInteger(NBTConstants.Z, zi());
        return nbt;
    }

    public ByteBuf writeByteBuf(ByteBuf data)
    {
        data.writeDouble(x());
        data.writeDouble(y());
        data.writeDouble(z());
        return data;
    }

    public RayTraceResult rayTrace(World world, IPos3D dir, double dist)
    {
        return rayTrace(world, new Pos(x() + dir.x() * dist, y() + dir.y() * dist, z() + dir.z() * dist));
    }


    public RayTraceResult rayTrace(World world, IPos3D end)
    {
        return rayTrace(world, end, false, false, false);
    }

    public RayTraceResult rayTrace(World world, IPos3D end, boolean rightClickWithBoat, boolean doColliderCheck, boolean doMiss)
    {
        RayTraceResult block = rayTraceBlocks(world, end, rightClickWithBoat, doColliderCheck, doMiss);
        RayTraceResult entity = rayTraceEntities(world, end);

        if (block == null)
        {
            return entity;
        }
        if (entity == null)
        {
            return block;
        }

        if (distance(new Pos(block.hitVec)) < distance(new Pos(entity.hitVec)))
        {
            return block;
        }

        return entity;
    }


    public RayTraceResult rayTraceBlocks(World world, IPos3D end)
    {
        return rayTraceBlocks(world, end, false, false, false);
    }

    public RayTraceResult rayTraceBlocks(World world, IPos3D end, boolean b1, boolean b2, boolean b3)
    {
        return world.rayTraceBlocks(toVec3d(), new Vec3d(end.x(), end.y(), end.z()), b1, b2, b3);
    }

    public RayTraceResult rayTraceEntities(World world, IPos3D end)
    {
        RayTraceResult closestEntityMOP = null;
        double closetDistance = 0D;

        double checkDistance = distance(end);
        AxisAlignedBB scanRegion = new AxisAlignedBB(-checkDistance, -checkDistance, -checkDistance, checkDistance, checkDistance, checkDistance).offset(x(), y(), z());

        List checkEntities = world.getEntitiesWithinAABB(Entity.class, scanRegion);

        for (Object obj : checkEntities)
        {
            Entity entity = (Entity) obj;
            if (entity != null && entity.canBeCollidedWith() && entity.getEntityBoundingBox() != null)
            {
                float border = entity.getCollisionBorderSize();
                AxisAlignedBB bounds = entity.getEntityBoundingBox().expand(border, border, border);
                RayTraceResult hit = bounds.calculateIntercept(toVec3d(), new Vec3d(end.x(), end.y(), end.z()));

                if (hit != null)
                {
                    if (bounds.contains(toVec3d()))
                    {
                        if (0 < closetDistance || closetDistance == 0)
                        {
                            closestEntityMOP = new RayTraceResult(entity);

                            closestEntityMOP.hitVec = hit.hitVec;
                            closetDistance = 0;
                        }
                    }
                    else
                    {
                        double dist = distance(new Pos(hit.hitVec));

                        if (dist < closetDistance || closetDistance == 0)
                        {
                            closestEntityMOP = new RayTraceResult(entity);
                            closestEntityMOP.hitVec = hit.hitVec;

                            closetDistance = dist;
                        }
                    }
                }
            }
        }

        return closestEntityMOP;
    }

    //===================
    //===World Setters===
    //===================
    public boolean setBlock(World world, Block block)
    {
        return setBlock(world, block.getDefaultState());
    }

    public boolean setBlock(World world, IBlockState state)
    {
        return setBlock(world, state, 3);
    }

    public boolean setBlock(World world, IBlockState block, int notify)
    {
        if (world != null && block != null)
        {
            return world.setBlockState(toBlockPos(), block, notify);
        }
        else
        {
            return false;
        }
    }

    public boolean setBlockToAir(World world)
    {
        return world.setBlockToAir(toBlockPos());
    }

    public BlockPos toBlockPos()
    {
        return new BlockPos(xi(), yi(), zi());
    }

    //===================
    //==World Accessors==
    //===================
    public boolean isAirBlock(World world)
    {
        return world.isAirBlock(toBlockPos());
    }

    @Deprecated
    public boolean isBlockFreezable(World world)
    {
        return false;
    }

    /**
     * Checks if the block is replaceable
     *
     * @return true if it can be replaced
     */
    public boolean isReplaceable(World world)
    {
        BlockPos pos = toBlockPos();
        IBlockState block = world.getBlockState(pos);
        return block == null || block.getBlock().isAir(block, world, pos) || block.getBlock().isAir(block, world, toBlockPos()) || block.getBlock().isReplaceable(world, toBlockPos());
    }

    /**
     * Checks to see if the tile can see the sky
     *
     * @return true if it can see sky, false if not or world is null
     */
    public boolean canSeeSky(World world)
    {
        return world.canSeeSky(toBlockPos());
    }

    public boolean isBlockEqual(World world, Block block)
    {
        Block b = getBlock(world);
        return b != null && b == block;
    }

    public Block getBlock(IBlockAccess world)
    {
        IBlockState state = getBlockState(world);
        if (world != null && state != null) //TODO check if chunk is loaded
        {
            return state.getBlock();
        }
        else
        {
            return null;
        }
    }

    public IBlockState getBlockState(IBlockAccess world)
    {
        if (world != null) //TODO check if chunk is loaded
        {
            return world.getBlockState(toBlockPos());
        }
        else
        {
            return null;
        }
    }

    public TileEntity getTileEntity(IBlockAccess world)
    {
        if (world != null) //TODO check if chunk is loaded
        {
            return world.getTileEntity(toBlockPos());
        }
        return null;
    }

    public float getHardness(World world)
    {
        IBlockState state = getBlockState(world);
        if (state != null && !state.getBlock().isAir(state, world, toBlockPos()))
        {
            return state.getBlock().getBlockHardness(state, world, toBlockPos());
        }
        else
        {
            return 0;
        }
    }

    /**
     * Gets the resistance of a block using block.getResistance method
     *
     * @param cause - entity that triggered/is the explosion
     */
    public float getResistance(Entity cause)
    {
        return getResistance(cause.world, cause, x(), y(), z());
    }

    /**
     * Gets the resistance of a block using block.getResistance method
     *
     * @param cause - entity that triggered/is the explosion
     */
    public float getResistanceToEntity(Entity cause)
    {
        return getBlock(cause.world).getExplosionResistance(cause);
    }

    /**
     * Gets the resistance of a block using block.getResistance method
     *
     * @param cause - entity that triggered/is the explosion
     */
    public float getResistanceToEntity(World world, Entity cause)
    {
        return getBlock(world).getExplosionResistance(cause);
    }

    /**
     * Gets the resistance of a block using block.getResistance method
     *
     * @param world - world to check in
     * @param cause - entity that triggered/is the explosion
     */
    public float getResistance(World world, Entity cause)
    {
        return getResistance(world, cause, cause.posX, cause.posY, cause.posZ);
    }

    /**
     * Gets the resistance of a block using block.getResistance method
     *
     * @param world - world to check in
     * @param cause - entity that triggered/is the explosion
     * @param xx    - xPos location of the explosion
     * @param yy    - xPos location of the explosion
     * @param zz    - xPos location of the explosion
     */
    public float getResistance(World world, Entity cause, double xx, double yy, double zz)
    {
        return getBlock(world).getExplosionResistance(world, toBlockPos(), cause, new Explosion(world, cause, xx, yy, zz, 1, false, false));
    }

    public boolean isAboveBedrock()
    {
        return y() > 0;
    }

    public boolean isInsideMap()
    {
        return isAboveBedrock() && y() < ICBMClassic.MAP_HEIGHT;
    }

    /**
     * Marks a block for update
     *
     * @param world - world to update the location in
     */
    public void markForUpdate(World world)
    {
        BlockPos pos = toBlockPos();
        IBlockState state = world.getBlockState(pos);
        if (state != null && !state.getBlock().isAir(state, world, toBlockPos()))
        {
            world.notifyBlockUpdate(pos, state, state, 3);
        }
    }

    //===================
    //==ILocation Accessors==
    //===================
    @Override
    public double getX()
    {
        return x();
    }

    @Override
    public double getY()
    {
        return y();
    }

    @Override
    public double getZ()
    {
        return z();
    }
}

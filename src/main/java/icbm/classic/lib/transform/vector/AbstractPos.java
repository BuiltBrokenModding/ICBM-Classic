package icbm.classic.lib.transform.vector;

import com.builtbroken.jlib.data.vector.IPos3D;
import com.builtbroken.jlib.data.vector.ITransform;
import com.builtbroken.jlib.data.vector.Pos3D;
import icbm.classic.ICBMClassic;
import icbm.classic.lib.NBTConstants;
import icbm.classic.lib.transform.rotation.EulerAngle;
import io.netty.buffer.ByteBuf;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.core.Position;
import net.minecraft.world.entity.Entity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;

import java.util.List;
import java.util.Optional;

/**
 * Abstract version of Pos3D for interaction with the minecraft world
 * Created by Robin on 1/13/2015.
 */
public abstract class AbstractPos<R extends AbstractPos> extends Pos3D<R> implements Position
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

    public AbstractPos(BlockEntity tile)
    {
        this(tile.getBlockPos());
    }

    public AbstractPos(Entity entity)
    {
        this(entity.getX(), entity.getY(), entity.getZ());
    }

    public AbstractPos(IPos3D vec)
    {
        this(vec.x(), vec.y(), vec.z());
    }

    public AbstractPos(CompoundTag nbt)
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

    public AbstractPos(Direction dir)
    {
        this(dir.getStepX(), dir.getStepY(), dir.getStepZ());
    }

    public AbstractPos(Vec3 vec)
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

    public Vec3 toVec3d()
    {
        return new Vec3(x(), y(), z());
    }

    public Point toVector2()
    {
        return new Point(x(), z());
    }

    public Direction toDirection()
    {
        //TODO maybe add a way to convert convert any vector into a direction from origin
        for (Direction dir : Direction.values())
        {
            if (xi() == dir.getStepX() && yi() == dir.getStepY() && zi() == dir.getStepZ())
            {
                return dir;
            }
        }
        return null;
    }

    @Deprecated
    public EulerAngle toEulerAngle(IPos3D target)
    {
        return sub(target).toEulerAngle();
    }

    public EulerAngle toEulerAngle(Vec3 target)
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

    public R add(Direction face)
    {
        return add(face.getStepX(), face.getStepY(), face.getStepZ());
    }

    public R add(Vec3 vec)
    {
        return add(vec.x, vec.y, vec.z);
    }

    public R sub(Direction face)
    {
        return sub(face.getStepX(), face.getStepY(), face.getStepZ());
    }

    public R sub(Vec3 vec)
    {
        return sub(vec.x, vec.y, vec.z);
    }

    public double distance(Vec3i vec)
    {
        return distance(vec.getX() + 0.5, vec.getY() + 0.5, vec.getZ() + 0.5);
    }

    public double distance(Vec3 vec)
    {
        return distance(vec.x, vec.y, vec.z);
    }

    public double distance(Entity entity)
    {
        return distance(entity.getX(), entity.getY(), entity.getZ());
    }

    public R multiply(Direction face)
    {
        return multiply(face.getStepX(), face.getStepY(), face.getStepZ());
    }

    public R multiply(Vec3 vec)
    {
        return multiply(vec.x, vec.y, vec.z);
    }

    public R divide(Direction face)
    {
        return divide(face.getStepX(), face.getStepY(), face.getStepZ());
    }

    public R divide(Vec3 vec)
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

    public CompoundTag toNBT()
    {
        return writeNBT(new CompoundTag());
    }

    public CompoundTag toIntNBT()
    {
        return writeIntNBT(new CompoundTag());
    }

    public CompoundTag writeNBT(CompoundTag nbt)
    {
        nbt.putDouble(NBTConstants.X, x());
        nbt.putDouble(NBTConstants.Y, y());
        nbt.putDouble(NBTConstants.Z, z());
        return nbt;
    }


    public CompoundTag writeIntNBT(CompoundTag nbt)
    {
        nbt.putInt(NBTConstants.X, xi());
        nbt.putInt(NBTConstants.Y, yi());
        nbt.putInt(NBTConstants.Z, zi());
        return nbt;
    }

    public ByteBuf writeByteBuf(ByteBuf data)
    {
        data.writeDouble(x());
        data.writeDouble(y());
        data.writeDouble(z());
        return data;
    }

    public HitResult rayTrace(Level world, IPos3D dir, double dist)
    {
        return rayTrace(world, new Pos(x() + dir.x() * dist, y() + dir.y() * dist, z() + dir.z() * dist));
    }


    public HitResult rayTrace(Level world, IPos3D end)
    {
        return rayTrace(world, end, false, false, false);
    }

    public HitResult rayTrace(Level world, IPos3D end, boolean rightClickWithBoat, boolean doColliderCheck, boolean doMiss)
    {
        HitResult block = rayTraceBlocks(world, end, rightClickWithBoat, doColliderCheck, doMiss);
        HitResult entity = rayTraceEntities(world, end);

        if (block == null)
        {
            return entity;
        }
        if (entity == null)
        {
            return block;
        }

        if (distance(new Pos(block.getLocation())) < distance(new Pos(entity.getLocation())))
        {
            return block;
        }

        return entity;
    }


    public HitResult rayTraceBlocks(Level world, IPos3D end)
    {
        return rayTraceBlocks(world, end, false, false, false);
    }

    /**
     * PORT: 1.12 World#rayTraceBlocks(start, end, stopOnLiquid, ignoreBlockWithoutBoundingBox, returnLastUncollidableBlock)
     *
     * @param b1 - stop on liquid source blocks (1.12 BlockLiquid#canCollideCheck only hit LEVEL == 0)
     * @param b2 - ignore blocks without collision box
     * @param b3 - return miss result instead of null
     */
    public HitResult rayTraceBlocks(Level world, IPos3D end, boolean b1, boolean b2, boolean b3)
    {
        final BlockHitResult hit = world.clip(new ClipContext(toVec3d(), new Vec3(end.x(), end.y(), end.z()),
            b2 ? ClipContext.Block.COLLIDER : ClipContext.Block.OUTLINE,
            b1 ? ClipContext.Fluid.SOURCE_ONLY : ClipContext.Fluid.NONE,
            CollisionContext.empty())); // PORT-CHECK: ClipContext(Vec3, Vec3, Block, Fluid, CollisionContext) constructor in 1.21.1
        if (hit.getType() == HitResult.Type.MISS && !b3)
        {
            return null;
        }
        return hit;
    }

    public HitResult rayTraceEntities(Level world, IPos3D end)
    {
        HitResult closestEntityMOP = null;
        double closetDistance = 0D;

        double checkDistance = distance(end);
        AABB scanRegion = new AABB(-checkDistance, -checkDistance, -checkDistance, checkDistance, checkDistance, checkDistance).move(x(), y(), z());

        List checkEntities = world.getEntitiesOfClass(Entity.class, scanRegion);

        for (Object obj : checkEntities)
        {
            Entity entity = (Entity) obj;
            if (entity != null && entity.isPickable() && entity.getBoundingBox() != null)
            {
                float border = entity.getPickRadius();
                AABB bounds = entity.getBoundingBox().inflate(border, border, border);
                Optional<Vec3> hit = bounds.clip(toVec3d(), new Vec3(end.x(), end.y(), end.z()));

                if (hit.isPresent())
                {
                    if (bounds.contains(toVec3d()))
                    {
                        if (0 < closetDistance || closetDistance == 0)
                        {
                            closestEntityMOP = new EntityHitResult(entity, hit.get());
                            closetDistance = 0;
                        }
                    }
                    else
                    {
                        double dist = distance(new Pos(hit.get()));

                        if (dist < closetDistance || closetDistance == 0)
                        {
                            closestEntityMOP = new EntityHitResult(entity, hit.get());
                            closetDistance = dist;
                        }
                    }
                }
            }
        }

        return closestEntityMOP;
    }

    //===================
    //===Level Setters===
    //===================
    public boolean setBlock(Level world, Block block)
    {
        return setBlock(world, block.defaultBlockState());
    }

    public boolean setBlock(Level world, BlockState state)
    {
        return setBlock(world, state, 3);
    }

    public boolean setBlock(Level world, BlockState block, int notify)
    {
        if (world != null && block != null)
        {
            return world.setBlock(toBlockPos(), block, notify);
        }
        else
        {
            return false;
        }
    }

    public boolean setBlockToAir(Level world)
    {
        return world.removeBlock(toBlockPos(), false);
    }

    public BlockPos toBlockPos()
    {
        return new BlockPos(xi(), yi(), zi());
    }

    //===================
    //==Level Accessors==
    //===================
    public boolean isAirBlock(Level world)
    {
        return world.isEmptyBlock(toBlockPos());
    }

    @Deprecated
    public boolean isBlockFreezable(Level world)
    {
        return false;
    }

    /**
     * Checks if the block is replaceable
     *
     * @return true if it can be replaced
     */
    public boolean isReplaceable(Level world)
    {
        BlockPos pos = toBlockPos();
        BlockState block = world.getBlockState(pos);
        return block == null || block.isAir() || block.canBeReplaced();
    }

    /**
     * Checks to see if the tile can see the sky
     *
     * @return true if it can see sky, false if not or world is null
     */
    public boolean canSeeSky(Level world)
    {
        return world.canSeeSky(toBlockPos());
    }

    public boolean isBlockEqual(Level world, Block block)
    {
        Block b = getBlock(world);
        return b != null && b == block;
    }

    public Block getBlock(BlockGetter world)
    {
        BlockState state = getBlockState(world);
        if (world != null && state != null) //TODO check if chunk is loaded
        {
            return state.getBlock();
        }
        else
        {
            return null;
        }
    }

    public BlockState getBlockState(BlockGetter world)
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

    public BlockEntity getTileEntity(BlockGetter world)
    {
        if (world != null) //TODO check if chunk is loaded
        {
            return world.getBlockEntity(toBlockPos());
        }
        return null;
    }

    public float getHardness(Level world)
    {
        BlockState state = getBlockState(world);
        if (state != null && !state.isAir())
        {
            return state.getDestroySpeed(world, toBlockPos());
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
        return getResistance(cause.level(), cause, x(), y(), z());
    }

    /**
     * Gets the resistance of a block using block.getResistance method
     *
     * @param cause - entity that triggered/is the explosion
     */
    public float getResistanceToEntity(Entity cause)
    {
        // PORT: 1.12 Block#getExplosionResistance(Entity) had no world context, 1.21 resistance is a plain block property
        return getBlock(cause.level()).getExplosionResistance();
    }

    /**
     * Gets the resistance of a block using block.getResistance method
     *
     * @param cause - entity that triggered/is the explosion
     */
    public float getResistanceToEntity(Level world, Entity cause)
    {
        return getBlock(world).getExplosionResistance();
    }

    /**
     * Gets the resistance of a block using block.getResistance method
     *
     * @param world - world to check in
     * @param cause - entity that triggered/is the explosion
     */
    public float getResistance(Level world, Entity cause)
    {
        return getResistance(world, cause, cause.getX(), cause.getY(), cause.getZ());
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
    public float getResistance(Level world, Entity cause, double xx, double yy, double zz)
    {
        final Explosion explosion = new Explosion(world, cause, xx, yy, zz, 1, false, Explosion.BlockInteraction.KEEP); // PORT-CHECK: Explosion constructor overload in 1.21.1
        return getBlockState(world).getExplosionResistance(world, toBlockPos(), explosion);
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
    public void markForUpdate(Level world)
    {
        BlockPos pos = toBlockPos();
        BlockState state = world.getBlockState(pos);
        if (state != null && !state.isAir())
        {
            world.sendBlockUpdated(pos, state, state, 3);
        }
    }

    //===================
    //==ILocation Accessors==
    //===================
    // PORT: 1.21 Position uses x()/y()/z() which Pos3D already provides, these are kept for existing callers
    public double getX()
    {
        return x();
    }

    public double getY()
    {
        return y();
    }

    public double getZ()
    {
        return z();
    }
}
// PORTED

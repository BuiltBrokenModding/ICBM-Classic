package icbm.classic.lib.transform.vector;

import com.builtbroken.jlib.data.vector.ITransform;
import com.builtbroken.jlib.data.vector.Pos3D;
import com.builtbroken.jlib.data.vector.Vec3;
import icbm.classic.ICBMClassic;
import icbm.classic.lib.NBTConstants;
import icbm.classic.lib.transform.rotation.EulerAngle;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.Direction;
import net.minecraft.dispenser.IPosition;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.math.*;
import net.minecraft.world.Explosion;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

/**
 * Abstract version of Pos3D for interaction with the minecraft world
 * Created by robert on 1/13/2015.
 */
public abstract class AbstractPos<R extends AbstractPos> extends Pos3D<R> implements IPosition {
    public AbstractPos() {
        this(0, 0, 0);
    }

    public AbstractPos(double a) {
        this(a, a, a);
    }

    public AbstractPos(double x, double y, double z) {
        super(x, y, z);
    }

    public AbstractPos(double yaw, double pitch) {
        this(-Math.sin(Math.toRadians(yaw)), Math.sin(Math.toRadians(pitch)), -Math.cos(Math.toRadians(yaw)) * Math.cos(Math.toRadians(pitch)));
    }

    public AbstractPos(BlockEntity blockEntity) {
        this(tile.getPos());
    }

    public AbstractPos(Entity entity) {
        this(entity.getX(), entity.getY(), entity.getZ());
    }

    public AbstractPos(Vec3 vec) {
        this(vec.x(), vec.y(), vec.z());
    }

    public AbstractPos(CompoundTag nbt) {
        this(nbt.getDouble(NBTConstants.X), nbt.getDouble(NBTConstants.Y), nbt.getDouble(NBTConstants.Z));
    }

    public AbstractPos(ByteBuf data) {
        this(data.readDouble(), data.readDouble(), data.readDouble());
    }

    public AbstractPos(BlockPos par1) {
        this(par1.getX(), par1.getY(), par1.getZ());
    }

    public AbstractPos(Direction dir) {
        this(dir.getFrontOffsetX(), dir.getFrontOffsetY(), dir.getFrontOffsetZ());
    }

    public AbstractPos(Vec3 vec) {
        this(vec.x, vec.y, vec.z);
    }

    public double angle(Vec3 other) {
        return Math.acos((this.cross(other)).magnitude() / (new Pos(other).magnitude() * magnitude()));
    }

    public double anglePreNorm(Vec3 other) {
        return Math.acos(this.cross(other).magnitude());
    }

    //=========================
    //========Converters=======
    //=========================

    public Vec3 toVec3() {
        return new Vec3(x(), y(), z());
    }

    public Point toVector2() {
        return new Point(x(), z());
    }

    public Direction toDirection() {
        //TODO maybe add a way to convert convert any vector into a direction from origin
        for (Direction dir : Direction.values()) {
            if (xi() == dir.getFrontOffsetX() && yi() == dir.getFrontOffsetY() && zi() == dir.getFrontOffsetZ()) {
                return dir;
            }
        }
        return null;
    }

    @Deprecated
    public EulerAngle toEulerAngle(Vec3 target) {
        return sub(target).toEulerAngle();
    }

    public EulerAngle toEulerAngle(Vec3 target) {
        return sub(target).toEulerAngle();
    }

    public EulerAngle toEulerAngle() {
        return new EulerAngle(Math.toDegrees(Math.atan2(x(), z())), Math.toDegrees(-Math.atan2(y(), Math.hypot(z(), x()))));
    }

    public Vec3 transform(ITransform transformer) {
        if (this instanceof Vec3) {
            return transformer.transform((Vec3) this);
        }
        return null;
    }

    /**
     * Calls {@link Math#abs(double)} on each term of the pos data
     *
     * @return abs
     */
    public R absolute() {
        return newPos(Math.abs(x()), Math.abs(y()), Math.abs(z()));
    }

    //=========================
    //======Math Operators=====
    //=========================

    public R add(BlockPos other) {
        return add(other.getX(), other.getY(), other.getZ());
    }

    public R add(Direction face) {
        return add(face.getFrontOffsetX(), face.getFrontOffsetY(), face.getFrontOffsetZ());
    }

    public R add(Vec3 vec) {
        return add(vec.x, vec.y, vec.z);
    }

    public R sub(Direction face) {
        return sub(face.getFrontOffsetX(), face.getFrontOffsetY(), face.getFrontOffsetZ());
    }

    public R sub(Vec3 vec) {
        return sub(vec.x, vec.y, vec.z);
    }

    public double distance(Vec3i vec) {
        return distance(vec.getX() + 0.5, vec.getY() + 0.5, vec.getZ() + 0.5);
    }

    public double distance(Vec3 vec) {
        return distance(vec.x, vec.y, vec.z);
    }

    public double distance(Entity entity) {
        return distance(entity.getX(), entity.getY(), entity.getZ());
    }

    public R multiply(Direction face) {
        return multiply(face.getFrontOffsetX(), face.getFrontOffsetY(), face.getFrontOffsetZ());
    }

    public R multiply(Vec3 vec) {
        return multiply(vec.x, vec.y, vec.z);
    }

    public R divide(Direction face) {
        return divide(face.getFrontOffsetX(), face.getFrontOffsetY(), face.getFrontOffsetZ());
    }

    public R divide(Vec3 vec) {
        return divide(vec.x, vec.y, vec.z);
    }

    @Override
    public R floor() {
        return newPos(Math.floor(x()), Math.floor(y()), Math.floor(z()));
    }

    //=========================
    //========NBT==============
    //=========================

    public CompoundTag toNBT() {
        return writeNBT(new CompoundTag());
    }

    public CompoundTag toIntNBT() {
        return writeIntNBT(new CompoundTag());
    }

    public CompoundTag writeNBT(CompoundTag nbt) {
        nbt.setDouble(NBTConstants.X, x());
        nbt.setDouble(NBTConstants.Y, y());
        nbt.setDouble(NBTConstants.Z, z());
        return nbt;
    }


    public CompoundTag writeIntNBT(CompoundTag nbt) {
        nbt.setInteger(NBTConstants.X, xi());
        nbt.setInteger(NBTConstants.Y, yi());
        nbt.setInteger(NBTConstants.Z, zi());
        return nbt;
    }

    public ByteBuf writeByteBuf(ByteBuf data) {
        data.writeDouble(x());
        data.writeDouble(y());
        data.writeDouble(z());
        return data;
    }

    public RayTraceResult rayTrace(Level level, Vec3 dir, double dist) {
        return rayTrace(world, new Pos(x() + dir.x() * dist, y() + dir.y() * dist, z() + dir.z() * dist));
    }


    public RayTraceResult rayTrace(Level level, Vec3 end) {
        return rayTrace(world, end, false, false, false);
    }

    public RayTraceResult rayTrace(Level level, Vec3 end, boolean rightClickWithBoat, boolean doColliderCheck, boolean doMiss) {
        RayTraceResult block = rayTraceBlocks(world, end, rightClickWithBoat, doColliderCheck, doMiss);
        RayTraceResult entity = rayTraceEntities(world, end);

        if (block == null) {
            return entity;
        }
        if (entity == null) {
            return block;
        }

        if (distance(new Pos(block.hitVec)) < distance(new Pos(entity.hitVec))) {
            return block;
        }

        return entity;
    }


    public RayTraceResult rayTraceBlocks(Level level, Vec3 end) {
        return rayTraceBlocks(world, end, false, false, false);
    }

    public RayTraceResult rayTraceBlocks(Level level, Vec3 end, boolean b1, boolean b2, boolean b3) {
        return world.rayTraceBlocks(toVec3(), new Vec3(end.x(), end.y(), end.z()), b1, b2, b3);
    }

    public RayTraceResult rayTraceEntities(Level level, Vec3 end) {
        RayTraceResult closestEntityMOP = null;
        double closetDistance = 0D;

        double checkDistance = distance(end);
        AxisAlignedBB scanRegion = new AxisAlignedBB(-checkDistance, -checkDistance, -checkDistance, checkDistance, checkDistance, checkDistance).offset(x(), y(), z());

        List checkEntities = world.getEntitiesWithinAABB(Entity.class, scanRegion);

        for (Object obj : checkEntities) {
            Entity entity = (Entity) obj;
            if (entity != null && entity.canBeCollidedWith() && entity.getEntityBoundingBox() != null) {
                float border = entity.getCollisionBorderSize();
                AxisAlignedBB bounds = entity.getEntityBoundingBox().expand(border, border, border);
                RayTraceResult hit = bounds.calculateIntercept(toVec3(), new Vec3(end.x(), end.y(), end.z()));

                if (hit != null) {
                    if (bounds.contains(toVec3())) {
                        if (0 < closetDistance || closetDistance == 0) {
                            closestEntityMOP = new RayTraceResult(entity);

                            closestEntityMOP.hitVec = hit.hitVec;
                            closetDistance = 0;
                        }
                    } else {
                        double dist = distance(new Pos(hit.hitVec));

                        if (dist < closetDistance || closetDistance == 0) {
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
    //===Level Setters===
    //===================
    public boolean setBlock(Level level, Block block) {
        return setBlock(world, block.getDefaultState());
    }

    public boolean setBlock(Level level, BlockState state) {
        return setBlock(world, state, 3);
    }

    public boolean setBlock(Level level, BlockState block, int notify) {
        if (world != null && block != null) {
            return world.setBlockState(toBlockPos(), block, notify);
        } else {
            return false;
        }
    }

    public boolean setBlockToAir(Level level) {
        return world.setBlockToAir(toBlockPos());
    }

    public BlockPos toBlockPos() {
        return new BlockPos(xi(), yi(), zi());
    }

    //===================
    //==Level Accessors==
    //===================
    public boolean isAirBlock(Level level) {
        return world.isAirBlock(toBlockPos());
    }

    @Deprecated
    public boolean isBlockFreezable(Level level) {
        return false;
    }

    /**
     * Checks if the block is replaceable
     *
     * @return true if it can be replaced
     */
    public boolean isReplaceable(Level level) {
        BlockPos pos = toBlockPos();
        BlockState block = world.getBlockState(pos);
        return block == null || block.getBlock().isAir(block, world, pos) || block.getBlock().isAir(block, world, toBlockPos()) || block.getBlock().isReplaceable(world, toBlockPos());
    }

    /**
     * Checks to see if the tile can see the sky
     *
     * @return true if it can see sky, false if not or world is null
     */
    public boolean canSeeSky(Level level) {
        return world.canSeeSky(toBlockPos());
    }

    public boolean isBlockEqual(Level level, Block block) {
        Block b = getBlock(world);
        return b != null && b == block;
    }

    public Block getBlock(IBlockAccess world) {
        BlockState state = getBlockState(world);
        if (world != null && state != null) //TODO check if chunk is loaded
        {
            return state.getBlock();
        } else {
            return null;
        }
    }

    public BlockState getBlockState(IBlockAccess world) {
        if (world != null) //TODO check if chunk is loaded
        {
            return world.getBlockState(toBlockPos());
        } else {
            return null;
        }
    }

    public BlockEntity getBlockEntity(IBlockAccess world) {
        if (world != null) //TODO check if chunk is loaded
        {
            return world.getBlockEntity(toBlockPos());
        }
        return null;
    }

    public float getHardness(Level level) {
        BlockState state = getBlockState(world);
        if (state != null && !state.getBlock().isAir(state, world, toBlockPos())) {
            return state.getBlock().getBlockHardness(state, world, toBlockPos());
        } else {
            return 0;
        }
    }

    /**
     * Gets the resistance of a block using block.getResistance method
     *
     * @param cause - entity that triggered/is the explosion
     */
    public float getResistance(Entity cause) {
        return getResistance(cause.world, cause, x(), y(), z());
    }

    /**
     * Gets the resistance of a block using block.getResistance method
     *
     * @param cause - entity that triggered/is the explosion
     */
    public float getResistanceToEntity(Entity cause) {
        return getBlock(cause.world).getExplosionResistance(cause);
    }

    /**
     * Gets the resistance of a block using block.getResistance method
     *
     * @param cause - entity that triggered/is the explosion
     */
    public float getResistanceToEntity(Level level, Entity cause) {
        return getBlock(world).getExplosionResistance(cause);
    }

    /**
     * Gets the resistance of a block using block.getResistance method
     *
     * @param world - world to check in
     * @param cause - entity that triggered/is the explosion
     */
    public float getResistance(Level level, Entity cause) {
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
    public float getResistance(Level level, Entity cause, double xx, double yy, double zz) {
        return getBlock(world).getExplosionResistance(world, toBlockPos(), cause, new Explosion(world, cause, xx, yy, zz, 1, false, false));
    }

    public boolean isAboveBedrock() {
        return y() > 0;
    }

    public boolean isInsideMap() {
        return isAboveBedrock() && y() < ICBMClassic.MAP_HEIGHT;
    }

    /**
     * Marks a block for update
     *
     * @param world - world to update the location in
     */
    public void markForUpdate(Level level) {
        BlockPos pos = toBlockPos();
        BlockState state = world.getBlockState(pos);
        if (state != null && !state.getBlock().isAir(state, world, toBlockPos())) {
            world.notifyBlockUpdate(pos, state, state, 3);
        }
    }

    //===================
    //==ILocation Accessors==
    //===================
    @Override
    public double getX() {
        return x();
    }

    @Override
    public double getY() {
        return y();
    }

    @Override
    public double getZ() {
        return z();
    }
}

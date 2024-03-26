package icbm.classic.client.particle;

import icbm.classic.core.particles.IcbmParticleOptions;
import icbm.classic.world.IcbmBlocks;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SmokeParticle;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

@OnlyIn(Dist.CLIENT)
public class LauncherSmokeParticle extends SmokeParticle {

    public static final Set<Block> BLOCKS_TO_IGNORE_COLLISIONS = new HashSet<>();

    public LauncherSmokeParticle(ClientLevel level, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed, float scale, SpriteSet sprites) {
        super(level, x, y, z, xSpeed, ySpeed, zSpeed, scale, sprites);
    }

    public void setColor(float r, float g, float b, boolean addColorVariant) {
        this.rCol = r;
        this.gCol = g;
        this.bCol = b;

        if (addColorVariant) {
            float colorVariant = (float) (Math.random() * 0.9);
            this.rCol *= colorVariant;
            this.bCol *= colorVariant;
            this.gCol *= colorVariant;
        }
    }

    //Re-implementing to allow customizing what we can collide with
    @Override
    public void move(double x, double y, double z) {
        double d0 = y;
        double origX = x;
        double origZ = z;

        if (this.hasPhysics) {
            List<VoxelShape> list = getCollisionBoxes(this.level, this.getBoundingBox().inflate(x, y, z), LauncherSmokeParticle::shouldAllowCollision);

            for (VoxelShape shape : list) {
                y = shape.collide(Direction.Axis.Y, this.getBoundingBox(), y);
            }

            this.setBoundingBox(this.getBoundingBox().move(0.0D, y, 0.0D));

            for (VoxelShape shape : list) {
                x = shape.collide(Direction.Axis.X, this.getBoundingBox(), x);
            }

            this.setBoundingBox(this.getBoundingBox().move(x, 0.0D, 0.0D));

            for (VoxelShape shape : list) {
                z = shape.collide(Direction.Axis.Z, this.getBoundingBox(), z);
            }

            this.setBoundingBox(this.getBoundingBox().move(0.0D, 0.0D, z));
        } else {
            this.setBoundingBox(this.getBoundingBox().move(x, y, z));
        }

        this.setLocationFromBoundingbox();
        this.onGround = d0 != y && d0 < 0.0D;

        if (origX != x) {
            this.xd = 0.0D;
        }

        if (origZ != z) {
            this.zd = 0.0D;
        }
    }

    public static boolean shouldAllowCollision(BlockState blockState) {
        Block block = blockState.getBlock();
        return block != IcbmBlocks.LAUNCHER_BASE.get()
            && block != IcbmBlocks.LAUNCH_FRAME.get()
            && block != IcbmBlocks.LAUNCH_SCREEN.get()
            && !BLOCKS_TO_IGNORE_COLLISIONS.contains(block);
    }

    public static List<VoxelShape> getCollisionBoxes(Level level, AABB aabb, Function<BlockState, Boolean> allowCollision) {
        final List<VoxelShape> outList = new ArrayList<>();

        int startX = (int) Math.floor(aabb.minX) - 1;
        int endX = (int) Math.ceil(aabb.maxX) + 1;
        int startY = (int) Math.floor(aabb.minY) - 1;
        int endY = (int) Math.ceil(aabb.maxY) + 1;
        int startZ = (int) Math.floor(aabb.minZ) - 1;
        int endZ = (int) Math.ceil(aabb.maxZ) + 1;

        final BlockPos.MutableBlockPos blockPos = new BlockPos.MutableBlockPos();

        for (int posX = startX; posX < endX; ++posX) {
            for (int posZ = startZ; posZ < endZ; ++posZ) {
                boolean flag2 = posX == startX || posX == endX - 1;
                boolean flag3 = posZ == startZ || posZ == endZ - 1;

                if ((!flag2 || !flag3) && level.isLoaded(blockPos.set(posX, 64, posZ))) {
                    for (int posY = startY; posY < endY; ++posY) {
                        if (!flag2 && !flag3 || posY != endY - 1) {
                            blockPos.set(posX, posY, posZ);
                            BlockState blockState = level.getBlockState(blockPos);
                            if (allowCollision.apply(blockState)) {
                                outList.add(blockState.getCollisionShape(level, blockPos));
                            }
                        }
                    }
                }
            }
        }

        return outList;
    }

    @OnlyIn(Dist.CLIENT)
    public static class Provider implements ParticleProvider<IcbmParticleOptions> {
        private final SpriteSet sprites;

        public Provider(SpriteSet pSprites) {
            this.sprites = pSprites;
        }

        public Particle createParticle(
            @NotNull IcbmParticleOptions options,
            @NotNull ClientLevel level,
            double x,
            double y,
            double z,
            double xSpeed,
            double ySpeed,
            double zSpeed
        ) {
            LauncherSmokeParticle particle = new LauncherSmokeParticle(level, x, y, z, xSpeed, ySpeed, zSpeed, options.getScale(), this.sprites);
            particle.setColor(options.getColor().x(), options.getColor().y(), options.getColor().z(), true);
            particle.setLifetime(options.getLifetime());
            return particle;
        }
    }
}
package icbm.classic.world.blast;

import icbm.classic.ICBMClassic;
import icbm.classic.api.explosion.IBlastTickable;
import icbm.classic.lib.transform.PosDistanceSorter;
import icbm.classic.world.blast.thread.ThreadSmallExplosion;
import icbm.classic.world.blast.threaded.BlastThreaded;
import icbm.classic.world.entity.flyingblock.FlyingBlock;
import icbm.classic.world.entity.flyingblock.FlyingBlockEntity;
import net.minecraft.block.BlockLiquid;
import net.minecraft.core.BlockPos;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.fluids.IFluidBlock;

import java.util.*;
import java.util.function.Consumer;

public class BlastAntiGravitational extends BlastThreaded implements IBlastTickable {
    protected ThreadSmallExplosion thread;
    protected Set<FlyingBlockEntity> flyingBlocks = new HashSet<FlyingBlockEntity>();

    @Override
    public boolean setupBlast() {
        if (!this.level().isClientSide()) {
            this.thread = new ThreadSmallExplosion(this, (int) this.getBlastRadius(), this.exploder);
            this.thread.start();
        }

        //this.oldLevel().playSoundEffect(position.x(), position.y(), position.z(), References.PREFIX + "antigravity", 6.0F, (1.0F + (oldLevel().rand.nextFloat() - oldLevel().rand.nextFloat()) * 0.2F) * 0.7F);
        return true;
    }

    @Override
    public boolean doRun(int loops, Consumer<BlockPos> edits) {
        int ymin = -this.getBlockPos().getY();
        int ymax = 255 - this.getBlockPos().getY();
        BlastHelpers.forEachPosInRadius(this.getBlastRadius(), (x, y, z) -> {

            if (y >= ymin && y < ymax) {
                edits.accept(new BlockPos(xi() + x, yi() + y, zi() + z));
            }
        });
        return false;
    }

    @Override
    public boolean doExplode(int callCount) //TODO rewrite entire method
    {
        int r = this.callCount;

        if (level() != null && !this.level().isClientSide()) {
            try {
                if (this.thread != null) //TODO replace thread check with callback triggered by thread and delayed into main thread
                {
                    if (this.thread.isComplete) {
                        //Copy as concurrent list is not fast to sort
                        List<BlockPos> results = new ArrayList(getThreadResults()); //TODO fix

                        if (r == 0) {
                            Collections.sort(results, new PosDistanceSorter(location, true, PosDistanceSorter.Sort.MANHATTEN));
                        }
                        int blocksToTake = 20;

                        for (BlockPos targetPosition : results) {
                            final BlockState blockState = world.getBlockState(targetPosition);
                            if (!blockState.getBlock().isAir(blockState, world, targetPosition) //don't pick up air
                                && !blockState.getBlock().isReplaceable(world, targetPosition) //don't pick up replacable blocks like fire, grass, or snow (this does not include crops)
                                && !(blockState.getBlock() instanceof IFluidBlock) && !(blockState.getBlock() instanceof BlockLiquid)) //don't pick up liquids
                            {
                                float hardness = blockState.getBlockHardness(world, targetPosition);
                                if (hardness >= 0 && hardness < 1000) {
                                    if (level().rand.nextInt(3) > 0) {
                                        //Remove block
                                        world.setBlockToAir(targetPosition);

                                        //Mark blocks taken
                                        blocksToTake--;
                                        if (blocksToTake <= 0) {
                                            break;
                                        }

                                        //Create flying block
                                        FlyingBlock.spawnFlyingBlock(world, targetPosition, blockState, (entity) -> {
                                            entity.yawChange = 50 * level().rand.nextFloat();
                                            entity.pitchChange = 100 * level().rand.nextFloat();
                                            entity.motionY += Math.max(0.15 * level().rand.nextFloat(), 0.1);
                                            entity.noClip = true;
                                            entity.gravity = 0;
                                        }, entityFlyingBlock -> {
                                            flyingBlocks.add(entityFlyingBlock);
                                            ICBMClassic.logger().info("Spawned flying block" + entityFlyingBlock);
                                        });
                                    }
                                }
                            }
                        }
                    }
                } else {
                    String msg = String.format("BlastAntiGravitational#doPostExplode() -> Failed to run due to null thread" +
                            "\nLevel = %s " +
                            "\nThread = %s" +
                            "\nSize = %s" +
                            "\nPos = ",
                        world, thread, size, location);
                    ICBMClassic.logger().error(msg);
                }
            } catch (Exception e) {
                String msg = String.format("BlastAntiGravitational#doPostExplode() ->  Unexpected error while running post detonation code " +
                        "\nLevel = %s " +
                        "\nThread = %s" +
                        "\nSize = %s" +
                        "\nPos = ",
                    world, thread, size, location);
                ICBMClassic.logger().error(msg, e);
            }
        }

        int radius = (int) this.getBlastRadius();
        AxisAlignedBB bounds = new AxisAlignedBB(location.x() - radius, location.y() - radius, location.z() - radius, location.y() + radius, 100, location.z() + radius);
        List<Entity> allEntities = level().getEntitiesWithinAABB(Entity.class, bounds);

        for (Entity entity : allEntities) {
            if (!(entity instanceof FlyingBlockEntity) && entity.getY() < 100 + location.y()) {
                if (entity.motionY < 0.4) {
                    entity.motionY += 0.15;
                }
            }
        }

        return this.callCount > 20 * 120;
    }

    @Override
    protected void onBlastCompleted() {
        flyingBlocks.forEach(FlyingBlockEntity::restoreGravity);
    }
}

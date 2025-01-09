package icbm.classic.content.blast;

import icbm.classic.ICBMClassic;
import icbm.classic.ICBMConstants;
import icbm.classic.api.explosion.IBlastTickable;
import icbm.classic.content.blast.thread.ThreadSmallExplosion;
import icbm.classic.content.blast.threaded.BlastThreaded;
import icbm.classic.content.entity.flyingblock.EntityFlyingBlock;
import icbm.classic.content.entity.flyingblock.FlyingBlock;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;

import java.util.*;
import java.util.function.Consumer;

public class BlastAntiGravitational extends BlastThreaded implements IBlastTickable {

    public static final int RUNNING_TICKS = ICBMConstants.TICKS_MIN * 2;
    protected ThreadSmallExplosion thread;
    protected Set<EntityFlyingBlock> flyingBlocks = new HashSet<EntityFlyingBlock>();
    List<BlockPos> results;

    int searchIndex = 0;

    @Override
    public boolean setupBlast() {
        if (!this.getWorld().isRemote) {
            this.thread = new ThreadSmallExplosion(this, (int) this.getBlastRadius(), this.exploder);
            this.thread.start();
        }

        //this.oldWorld().playSoundEffect(position.x(), position.y(), position.z(), References.PREFIX + "antigravity", 6.0F, (1.0F + (oldWorld().rand.nextFloat() - oldWorld().rand.nextFloat()) * 0.2F) * 0.7F);
        return true;
    }

    @Override
    public boolean doRun(int loops, Consumer<BlockPos> edits) {
        BlastHelpers.forEachPosInRadius(this.getBlastRadius(), (x, y, z) -> {
            edits.accept(new BlockPos(getPosition().x + x, getPosition().y + y, getPosition().z + z));
        });
        return false;
    }

    @Override
    public boolean doExplode(int callCount)
    {
        if (getWorld() != null && !getWorld().isRemote) {
            if (this.thread != null)
            {
                if (this.thread.isComplete) {
                    if (results == null) {
                        results = new ArrayList(getThreadResults());
                        Collections.shuffle(results);
                        results.sort(Comparator.comparingInt(r -> -r.getY()));
                        this.threadResults.clear();
                    }

                    if(searchIndex >= results.size()) {
                        searchIndex = 0;
                    }

                    // Search until we find a single block
                    for (; searchIndex < results.size(); searchIndex++) {
                        final BlockPos targetPosition = results.get(searchIndex);

                        if (FlyingBlock.spawnFlyingBlock(world, targetPosition, (entity) -> {
                            entity.yawChange = 50 * getWorld().rand.nextFloat();
                            entity.pitchChange = 100 * getWorld().rand.nextFloat();


                            double deltaX = targetPosition.getX() - getPosition().x;
                            double deltaZ = targetPosition.getZ() - getPosition().z;
                            double mag = Math.sqrt(deltaX * deltaX + deltaZ * deltaZ);

                            deltaX /= mag;
                            deltaZ /= mag;

                            entity.addVelocity(
                                deltaX * (1 - getWorld().rand.nextFloat()),
                                Math.max(1 * getWorld().rand.nextFloat(), 1),
                                deltaZ * (1 - getWorld().rand.nextFloat())
                            );


                            entity.setGravity(0);
                            entity.setInAirKillTime(RUNNING_TICKS * 2);
                        }, entityFlyingBlock -> {
                            flyingBlocks.add(entityFlyingBlock);
                        })) {
                           break;
                        }
                    }
                }
            } else {
                String msg = String.format("BlastAntiGravitational#doPostExplode() -> Failed to run due to null thread" +
                        "\nWorld = %s " +
                        "\nThread = %s" +
                        "\nSize = %s" +
                        "\nPos = ",
                    world, thread, size, getPosition());
                ICBMClassic.logger().error(msg);
            }
        }

        int radius = (int) this.getBlastRadius();
        final int affectHeight = Math.max(radius, 100); //TODO config affect height
        AxisAlignedBB bounds = new AxisAlignedBB(
            getPosition().x - radius, getPosition().y - radius, getPosition().z - radius,
            getPosition().x + radius, getPosition().y + affectHeight, getPosition().z + radius);
        List<Entity> allEntities = getWorld().getEntitiesWithinAABB(Entity.class, bounds);

        for (Entity entity : allEntities) {
            if (entity.posY < affectHeight + getPosition().y) {
                if (entity.getMotion().y < 0.4) {
                    entity.addVelocity(0, 0.1f, 0);
                }
            }
        }

        return this.callCount > RUNNING_TICKS;
    }

    @Override
    protected void onBlastCompleted() {
        flyingBlocks.forEach(EntityFlyingBlock::restoreGravity);
    }
}

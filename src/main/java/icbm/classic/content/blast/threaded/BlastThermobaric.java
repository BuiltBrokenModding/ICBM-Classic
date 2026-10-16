package icbm.classic.content.blast.threaded;

import icbm.classic.ICBMClassic;
import icbm.classic.api.refs.ICBMExplosives;
import icbm.classic.client.ICBMSounds;
import icbm.classic.config.blast.ConfigBlast;
import icbm.classic.lib.transform.vector.Location;
import icbm.classic.lib.transform.vector.Pos;
import net.minecraft.world.level.block.LiquidBlock;
import java.util.concurrent.ThreadLocalRandom;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.function.Consumer;

import static java.lang.Math.cos;
import static java.lang.Math.sin;

public class BlastThermobaric extends BlastThreaded {
    /** Players further away than this do not get the smoke cloud particles */
    private static final double CLOUD_VIEW_DISTANCE = 256;

    private double energy;

    public BlastThermobaric() {
    }

    public BlastThermobaric setEnergy(double energy) {
        this.energy = energy;
        return this;
    }

    @Override
    public boolean doRun(int loops, Consumer<BlockPos> edits) {
        //How many steps to go per rotation
        final int steps = (int) Math.ceil(Math.PI * this.getBlastRadius());

        double x;
        double y;
        double z;

        double dx;
        double dy;
        double dz;

        double power;

        double yaw;
        double pitch;

        final int lineDensityScale = 2;
        for (int yawSlices = 0; yawSlices < lineDensityScale * steps; yawSlices++) {
            for (int pitchSlice = 0; pitchSlice < steps; pitchSlice++) {
                //Calculate power
                power = this.energy - (this.energy * ThreadLocalRandom.current().nextFloat() /* PORT: worker thread, 1.21 level random throws when used from several threads */ / 2);

                //Get angles for rotation steps
                yaw = (Math.PI / steps) * yawSlices;
                pitch = (Math.PI / steps) * pitchSlice;

                //Figure out vector to move for trace (cut in half to improve trace skipping blocks)
                dx = sin(pitch) * cos(yaw) * 0.5;
                dy = cos(pitch) * 0.5;
                dz = sin(pitch) * sin(yaw) * 0.5;

                //Reset position to current
                x = this.x();
                y = this.y();
                z = this.z();

                BlockPos prevPos = null;

                //Trace from start to end
                while (location.distance(x, y, z) <= this.getBlastRadius() && power > 0) //TODO replace distance check with SQ version
                {
                    //Consume power per loop
                    power -= 0.3F * 0.75F * 5; //TODO why the magic numbers?

                    //Convert double position to int position as block pos
                    final BlockPos blockPos = BlockPos.containing(x, y, z);

                    //Only do action one time per block (not a perfect solution, but solves double hit on the same block in the same line)
                    if (!Objects.equals(prevPos, blockPos)) {
                        // PORT: worker thread, reads the chunks captured on the server thread
                        if (!isLoadedThreaded(blockPos)) //TODO: find better fix for non main thread loading
                            continue;

                        //Get block state and block from position
                        final BlockState state = getBlockStateThreaded(blockPos);

                        //Ignore air blocks
                        if (!state.isAir()) {
                            //Consume power based on block
                            power -= getResistance(blockPos, state);

                            //Only break block that can be broken && If we still have power. Then break the block
                            if (state.getDestroySpeed(world, blockPos) >= 0 && power > 0f) {
                                edits.accept(blockPos);
                            }
                        }
                    }

                    //Note previous block
                    prevPos = blockPos;

                    //Move forward
                    x += dx;
                    y += dy;
                    z += dz;
                }
            }
        }
        return false;
    }

    public float getResistance(BlockPos pos, BlockState state) {
        if (state.getBlock() instanceof LiquidBlock) { // PORT: 1.12 Material#isLiquid
            return 0.25f;
        } else {
            return getBlockResistance(pos, state);
        }
    }


    @Override
    public boolean setupBlast() {
        super.setupBlast();
        if (this.world() != null) {
            // Players close enough to see the cloud, collected once as the cloud is a few thousand particles
            final List<ServerPlayer> viewers = getCloudViewers();

            // Spawn nuclear cloud.
            for (int y = 0; y < 26; y++) {
                int r = 4;

                if (y < 8) {
                    r = Math.max(Math.min((8 - y) * 2, 10), 4);
                } else if (y > 15) {
                    r = Math.max(Math.min((y - 15) * 2, 15), 5);
                }

                for (int x = -r; x < r; x++) {
                    for (int z = -r; z < r; z++) {
                        double distance = Math.sqrt(x * x + z * z);

                        if (r > distance && r - 3 < distance) {
                            Location spawnPosition = location.add(new Pos(x * 2, (y - 2) * 2, z * 2));
                            float xDiff = (float) (spawnPosition.x() - location.x());
                            float zDiff = (float) (spawnPosition.z() - location.z());
                            // PORT: 1.12 called World#spawnParticle here, which does nothing on the server, so the cloud
                            //  was never seen. Sent to clients instead, count 0 makes the offsets the particle velocity.
                            sendCloudParticle(viewers, spawnPosition.x(), spawnPosition.y(), spawnPosition.z(),
                                xDiff * 0.3 * world().random.nextFloat(), -world().random.nextFloat(), zDiff * 0.3 * world().random.nextFloat()); //(float) (distance / this.getRadius()) * oldWorld().rand.nextFloat(), 0, //0, 8F, 1.2F);
                        }
                    }
                }
            }

            this.doDamageEntities((float)ConfigBlast.thermobaric.entityDamageScale, (float) (this.energy * ConfigBlast.thermobaric.entityDamageMultiplier));

            ICBMSounds.EXPLOSION.play(world, this.location.x(), this.location.y(), this.location.z(), 7.0F, (1.0F + (this.world().random.nextFloat() - this.world().random.nextFloat()) * 0.2F) * 0.7F, true);
        }

        return true;
    }

    private List<ServerPlayer> getCloudViewers()
    {
        if (world instanceof ServerLevel serverLevel)
        {
            return serverLevel.players().stream()
                .filter(player -> player.distanceToSqr(location.x(), location.y(), location.z()) <= CLOUD_VIEW_DISTANCE * CLOUD_VIEW_DISTANCE)
                .collect(Collectors.toList());
        }
        return Collections.emptyList();
    }

    /**
     * Sends one smoke particle of the cloud to the viewers. Count 0 makes the client use the offsets as velocity.
     */
    private void sendCloudParticle(List<ServerPlayer> viewers, double x, double y, double z, double velX, double velY, double velZ)
    {
        for (ServerPlayer player : viewers)
        {
            // Long distance, the cloud is meant to be watched from far away
            ((ServerLevel) world).sendParticles(player, ParticleTypes.SMOKE, true, x, y, z, 0, velX, velY, velZ, 1.0);
        }
    }

    @Override
    public boolean doExplode(int callCount) {
        super.doExplode(callCount);
        int r = this.callCount;

        if (this.world().isClientSide()) {
            for (int x = -r; x < r; x++) {
                for (int z = -r; z < r; z++) {
                    double distance = Math.sqrt(x * x + z * z);

                    if (distance < r && distance > r - 1) {
                        Location targetPosition = this.location.add(new Pos(x, 0, z));

                        if (this.world().random.nextFloat() < Math.max(0.001 * r, 0.05)) {
                            world.addParticle(ParticleTypes.SMOKE, targetPosition.x(), targetPosition.y(), targetPosition.z(), 0, 0, 0); //5F, 1F);
                        }
                    }
                }
            }
        }
        return false;
    }

    @Override
    public void onBlastCompleted() {
        super.onBlastCompleted();
        if (world() != null && !world().isClientSide()) {
            try {
                //Attack entities with concussion wave
                this.doDamageEntities((float)ConfigBlast.thermobaric.entityDamageScale, (float) (this.energy * ConfigBlast.thermobaric.entityDamageMultiplier));

                //Play audio
                ICBMSounds.EXPLOSION.play(world, this.location.x(), this.location.y(), this.location.z(), 10.0F, (1.0F + (this.world().random.nextFloat() - this.world().random.nextFloat()) * 0.2F) * 0.7F, true);

            } catch (Exception e) {
                String msg = String.format("BlastThermobaric#doPostExplode() ->  Unexpected error while running post detonation code " +
                        "\nWorld = %s " +
                        "\nThread = %s" +
                        "\nSize = %s" +
                        "\nPos = %s",
                    world, getThread(), size, location);
                ICBMClassic.logger().error(msg, e);
            }
        }
    }
}
// PORTED

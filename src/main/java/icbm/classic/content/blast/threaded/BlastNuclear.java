package icbm.classic.content.blast.threaded;

import icbm.classic.ICBMClassic;
import icbm.classic.api.refs.ICBMExplosives;
import icbm.classic.client.ICBMSounds;
import icbm.classic.config.blast.ConfigBlast;
import icbm.classic.content.blast.BlastMutation;
import icbm.classic.content.blast.BlastRadioactiveBlockSwaps;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;

import java.util.Objects;
import java.util.function.Consumer;

import static java.lang.Math.cos;
import static java.lang.Math.sin;

public class BlastNuclear extends BlastThreaded {
    private double energy;

    public BlastNuclear() {
    }

    public BlastNuclear setEnergy(double energy) {
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
                power = this.energy - (this.energy * world.rand.nextFloat() / 2);

                //Get angles for rotation steps
                yaw = (Math.PI / steps) * yawSlices;
                pitch = (Math.PI / steps) * pitchSlice;

                //Figure out vector to move for trace (cut in half to improve trace skipping blocks)
                dx = sin(pitch) * cos(yaw) * 0.5;
                dy = cos(pitch) * 0.5;
                dz = sin(pitch) * sin(yaw) * 0.5;

                //Reset position to current
                x = this.getPosition().x;
                y = this.getPosition().y;
                z = this.getPosition().z;

                BlockPos prevPos = null;

                //Trace from start to end
                while (Math.sqrt(getPosition().squareDistanceTo(x, y, z)) <= this.getBlastRadius() && power > 0) //TODO replace distance check with SQ version
                {
                    //Consume power per loop
                    power -= 0.3F * 0.75F * 5; //TODO why the magic numbers?

                    //Convert double position to int position as block pos
                    final BlockPos blockPos = new BlockPos(Math.floor(x), Math.floor(y), Math.floor(z));

                    //Only do action one time per block (not a perfect solution, but solves double hit on the same block in the same line)
                    if (!Objects.equals(prevPos, blockPos)) {
                        if (!world.isBlockLoaded(blockPos)) //TODO: find better fix for non main thread loading
                            continue;

                        //Get block state and block from position
                        final BlockState state = world.getBlockState(blockPos);
                        final Block block = state.getBlock();

                        //Ignore air blocks
                        if (!block.isAir(state, world, blockPos)) {
                            //Consume power based on block
                            power -= getResistance(blockPos, state);

                            //Only break block that can be broken && If we still have power. Then break the block
                            if (state.getBlockHardness(world, blockPos) >= 0 && power > 0f) {
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
        final Block block = state.getBlock();
        if (state.getMaterial().isLiquid()) {
            return 0.25f;
        } else {
            return block.getExplosionResistance(state, world, pos, getExplosivePlacedBy(), this);
        }
    }


    @Override
    public boolean setupBlast() {
        super.setupBlast();
        /*if (this.world() != null) {
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
                        double distance = MathHelper.sqrt(x * x + z * z);

                        if (r > distance && r - 3 < distance) {
                            Location spawnPosition = location.add(new Pos(x * 2, (y - 2) * 2, z * 2));
                            float xDiff = (float) (spawnPosition.x() - location.x());
                            float zDiff = (float) (spawnPosition.z() - location.z());
                            world.spawnParticle(EnumParticleTypes.SMOKE_NORMAL, spawnPosition.x(), spawnPosition.y(), spawnPosition.z(),
                                xDiff * 0.3 * world().rand.nextFloat(), -world().rand.nextFloat(), zDiff * 0.3 * world().rand.nextFloat()); //(float) (distance / this.getRadius()) * oldWorld().rand.nextFloat(), 0, //0, 8F, 1.2F);
                        }
                    }
                }
            }

            this.doDamageEntities((float)ConfigBlast.nuclear.entityDamageScale, (float) (this.energy * ConfigBlast.nuclear.entityDamageMultiplier));

            ICBMSounds.EXPLOSION.play(world, this.location.x(), this.location.y(), this.location.z(), 7.0F, (1.0F + (this.world().rand.nextFloat() - this.world().rand.nextFloat()) * 0.2F) * 0.7F, true);
        }*/

        return true;
    }

    @Override
    public boolean doExplode(int callCount) {
        super.doExplode(callCount);
        int r = this.callCount;

        /*if (this.world().isRemote) {
            for (int x = -r; x < r; x++) {
                for (int z = -r; z < r; z++) {
                    double distance = MathHelper.sqrt(x * x + z * z);

                    if (distance < r && distance > r - 1) {
                        Location targetPosition = this.location.add(new Pos(x, 0, z));

                        if (this.world().rand.nextFloat() < Math.max(0.001 * r, 0.05)) {
                            world.spawnParticle(EnumParticleTypes.SMOKE_NORMAL, targetPosition.x(), targetPosition.y(), targetPosition.z(), 0, 0, 0); //5F, 1F);
                        }
                    }
                }
            }
        }*/
        return false;
    }

    @Override
    public void onBlastCompleted() {
        super.onBlastCompleted();
        if (getWorld() != null && !getWorld().isRemote) {
            try {
                //Attack entities with concussion wave
                this.doDamageEntities((float)ConfigBlast.nuclear.entityDamageScale, (float) (this.energy * ConfigBlast.nuclear.entityDamageMultiplier));

                //Place radioactive blocks
                new BlastRadioactiveBlockSwaps()
                    .setBlastWorld(getWorld())
                    .setBlastSource(this.exploder)
                    .setBlastPosition(x, y, z)
                    .setBlastSize((float)ConfigBlast.nuclear.rotScale)
                    .setExplosiveData(ICBMExplosives.ROT)
                    .buildBlast().doAction();

                new BlastMutation()
                    .setBlastWorld(getWorld())
                    .setBlastSource(this.exploder)
                    .setBlastPosition(x, y, z)
                    .setBlastSize((float)ConfigBlast.nuclear.mutationScale)
                    .setExplosiveData(ICBMExplosives.MUTATION)
                    .buildBlast().doAction();

                // TODO trigger blast wave that hits all entities with radiation damage not behind protection (basically line of sight)
                // TODO spawn a radioactive gas cloud, can recycle the gas blast and set it to expand quickly
                // TODO throw projectiles containing radioactive material. On impact have the projectiles place radioactive dust in a nearby area shotgun pattern
                // TODO have radioactive dust fall from sky in a radius around the blast

                //Play audio
                ICBMSounds.EXPLOSION.play(world, x, y, z, 10.0F, (1.0F + (this.getWorld().rand.nextFloat() - this.getWorld().rand.nextFloat()) * 0.2F) * 0.7F, true);

            } catch (Exception e) {
                String msg = String.format("BlastNuclear#doPostExplode() ->  Unexpected error while running post detonation code " +
                        "\nWorld = %s " +
                        "\nThread = %s" +
                        "\nSize = %s" +
                        "\nPos = %s",
                    world, getThread(), size, getPosition());
                ICBMClassic.logger().error(msg, e);
            }
        }
    }
}

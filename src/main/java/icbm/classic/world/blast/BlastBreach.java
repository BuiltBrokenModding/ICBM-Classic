package icbm.classic.world.blast;

import icbm.classic.api.tile.IRotatable;
import icbm.classic.world.missile.entity.EntityMissile;
import net.minecraft.core.Direction;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.SoundCategory;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class BlastBreach extends BlastTNT {
    private int depth;

    public BlastBreach(int depth) {
        this.damageToEntities = 13;
        this.depth = depth;
    }

    @Override
    protected void calculateDamage() {
        //Turn into normal TNT if invalid
        if (depth <= 0) {
            super.calculateDamage();
        }
        //TODO add some smoke and block particles for wow effect of a breaching a building
        else if (!this.level().isClientSide()) {
            //Get direction to push blast
            Direction direction = Direction.DOWN; //TODO replace with angle for entities (blocks should stay as axis aligned)
            if (this.exploder instanceof IRotatable) {
                direction = ((IRotatable) this.exploder).getDirection().getOpposite();
            }
            //Guess direction from entity rotation
            else if (this.exploder != null) {
                if (this.exploder.getXRot() > 45) {
                    direction = this.exploder instanceof EntityMissile ? Direction.UP : Direction.DOWN;
                } else if (this.exploder.getXRot() < -45) {
                    direction = this.exploder instanceof EntityMissile ? Direction.DOWN : Direction.UP;
                } else {
                    direction = this.exploder.getAdjustedHorizontalFacing();

                    // fixes explosion going backwards when the missile flies east or west.
                    if (direction == Direction.EAST || direction == Direction.WEST) {
                        direction = direction.getOpposite();
                    }
                }
            }

            //Loop with and height in direction
            for (int h = -1; h < 2; h++) //TODO scale with size
            {
                for (int w = -1; w < 2; w++) //TODO scale with size
                {
                    //Reset energy per line
                    float energy = 4 * size + depth * 3;

                    //reduce power from center outwards
                    double centerDst = Math.sqrt(h * h + w * w);
                    energy *= 1 - centerDst / 4;

                    //TODO convert magic numbers into defined logic

                    //Loop depth
                    for (int i = 0; i < this.depth; i++) {
                        Pos dir = new Pos(direction).multiply(i);  //TODO replace xyz ints
                        Pos p;
                        if (direction == Direction.DOWN || direction == Direction.UP) {
                            p = dir.add(h, 0, w);
                        } else if (direction == Direction.EAST || direction == Direction.WEST) {
                            p = dir.add(0, h, w);
                        } else if (direction == Direction.NORTH || direction == Direction.SOUTH) {
                            p = dir.add(w, h, 0);
                        } else {
                            return;
                        }

                        //Translate by center
                        p = new Pos(this).add(p); //TODO replace with BlockPos

                        //Get block
                        BlockState state = p.getBlockState(level());
                        Block block = state.getBlock();
                        if (!block.isAir(state, level(), p.toBlockPos())) {
                            // get explosion resistance, take the square root of it and then half that to make it weaker
                            double e = Math.sqrt(block.getExplosionResistance(level(), p.toBlockPos(), this.exploder, this)) / 4;

                            if (e <= energy) // if there is energy (force) left to break it
                            {
                                energy -= e; // reduce the remaining energy
                                getAffectedBlockPositions().add(p.toBlockPos()); // mark block for destroying
                            } else {
                                break; // block blast from continuing
                            }
                        }
                        energy *= 0.65; // reduce the blast power by a percentage for every block it travelled
                        energy--;

                        if (energy <= 0) {
                            break;
                        }
                    }
                }
            }

            //Play some audio
            this.level().playSound(null, location.x(), location.y(), location.z(), SoundEvents.ENTITY_GENERIC_EXPLODE, SoundCategory.BLOCKS, 5.0F, (1.0F + (level().rand.nextFloat() - level().rand.nextFloat()) * 0.2F) * 0.7F);
        }
    }
}

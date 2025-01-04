package icbm.classic.content.blast;

import com.google.common.collect.ImmutableList;
import icbm.classic.api.actions.data.ActionField;
import icbm.classic.api.actions.data.ActionFields;
import icbm.classic.content.missile.entity.EntityMissile;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.nbt.INBT;
import net.minecraft.util.Direction;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;

import javax.annotation.Nonnull;
import java.util.Collection;

public class BlastBreach extends BlastTNT
{
    public static final ImmutableList<ActionField> SUPPORTED_FIELDS = ImmutableList.of(ActionFields.HOST_DIRECTION);
    @Setter @Accessors(chain = true)
    private int depth;

    @Setter @Accessors(chain = true)
    private int width;

    @Setter @Accessors(chain = true)
    private float energy;

    @Setter @Accessors(chain = true)
    private float energyDistanceScale;

    @Setter @Accessors(chain = true)
    private float energyCostDistance;

    @Setter @Accessors(chain = true)
    private Direction direction; //TODO recode to be angle based

    @Override
    public <VALUE, TAG extends INBT> void setValue(ActionField<VALUE, TAG> key, VALUE value) {
        if(key == ActionFields.HOST_DIRECTION) {
            direction = ActionFields.HOST_DIRECTION.cast(value);
        }
    }

    @Nonnull
    public Collection<ActionField> getFields() {
        return SUPPORTED_FIELDS;
    }

    @Override
    protected void calculateDamage()
    {
        //Turn into normal TNT if invalid
        if (depth <= 0)
        {
            super.calculateDamage();
        }
        //TODO add some smoke and block particles for wow effect of a breaching a building
        else if (!this.world().isRemote)
        {
            if(direction == null) {
                //Guess direction from entity rotation
                if (this.exploder != null) {
                    // TODO move this logic to projectile and provide via ActionField.HOST_DIRECTION
                    if (this.exploder.rotationPitch > 45) {
                        direction = this.exploder instanceof EntityMissile ? Direction.UP : Direction.DOWN;
                    } else if (this.exploder.rotationPitch < -45) {
                        direction = this.exploder instanceof EntityMissile ? Direction.DOWN : Direction.UP;
                    } else {
                        direction = this.exploder.getAdjustedHorizontalFacing();

                        // fixes explosion going backwards when the missile flies east or west.
                        if (direction == Direction.EAST || direction == Direction.WEST) {
                            direction = direction.getOpposite();
                        }
                    }
                }
                else {
                    direction = Direction.DOWN;
                }
            }

            //Loop with and height in direction
            for (int h = -width; h <= width; h++)
            {
                for (int w = -width; w <= width; w++)
                {
                    //Reset energy per line
                    float energyRemaining = this.energy - (this.energy * (h + w) * energyDistanceScale);

                    //TODO convert magic numbers into defined logic

                    //Loop depth
                    for (int depthIndex = 0; depthIndex < this.depth && energyRemaining > 0; depthIndex++)
                    {
                        int x = this.xi() + direction.getXOffset() * depthIndex;
                        int y = this.yi() + direction.getYOffset() * depthIndex;
                        int z = this.zi() + direction.getZOffset() * depthIndex;

                        if (direction == Direction.DOWN || direction == Direction.UP)
                        {
                            x += h;
                            z += w;
                        }
                        else if (direction == Direction.EAST || direction == Direction.WEST)
                        {
                            y += h;
                            z += w;
                        }
                        else if (direction == Direction.NORTH || direction == Direction.SOUTH)
                        {
                            y += h;
                            x += w;
                        }
                        else
                        {
                            return;
                        }

                        //Get block
                        final BlockPos pos = new BlockPos(x, y, z);
                        final BlockState state = world.getBlockState(pos);
                        final Block block = state.getBlock();

                        if (!block.isAir(state, world(), pos))
                        {
                            // Stop at unbreakable
                            if(block.getBlockHardness(state, world, pos) < 0) {
                                break;
                            }

                            //blockResistance=R*3.... aka hardness
                            //explosiveResistance=blockResistance / 5
                            //R = planks(5) -> 15 -> 3
                            //R = stone(10) -> 30 -> 10
                            //ICBM concrete -> 28
                            //ICBM compact concrete -> 380
                            //ICBM reinforced concrete -> 2800
                            //R = obby(2000) -> 6000 -> 1200
                            //R = unbreakable(6M) -> fuck that


                            final float cost = block.getExplosionResistance(state, world(), pos, this.exploder, this);
                            if (cost < energyRemaining)
                            {
                                energyRemaining -= cost;
                                getAffectedBlockPositions().add(pos);
                            }


                        }
                        else {
                            energyRemaining *= (1 - energyCostDistance);
                        }
                    }
                }
            }

            //Play some audio
            this.world().playSound(null, x(), y(), z(), SoundEvents.ENTITY_GENERIC_EXPLODE, SoundCategory.BLOCKS, 5.0F, (1.0F + (world().rand.nextFloat() - world().rand.nextFloat()) * 0.2F) * 0.7F);
        }
    }
}

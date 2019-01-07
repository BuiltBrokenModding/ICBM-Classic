package icbm.classic.content.blast;

import com.builtbroken.jlib.data.vector.IPos3D;
import icbm.classic.api.tile.IRotatable;
import icbm.classic.content.entity.missile.EntityMissile;
import icbm.classic.lib.transform.vector.Pos;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.SoundCategory;

public class BlastBreech extends BlastTNT
{
    private int depth;

    public BlastBreech(int depth)
    {
        this.damageToEntities = 13;
        this.depth = depth;
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
            //Get direction to push blast
            EnumFacing direction = EnumFacing.DOWN; //TODO replace with angle for entities (blocks should stay as axis aligned)
            if (this.exploder instanceof IRotatable)
            {
                direction = ((IRotatable) this.exploder).getDirection().getOpposite();
            }
            //Guess direction from entity rotation
            else if (this.exploder != null)
            {
                if (this.exploder.rotationPitch > 45)
                {
                    direction = this.exploder instanceof EntityMissile ? EnumFacing.UP : EnumFacing.DOWN;
                }
                else if (this.exploder.rotationPitch < -45)
                {
                    direction = this.exploder instanceof EntityMissile ? EnumFacing.DOWN : EnumFacing.UP;
                }
                else
                {
                    direction = this.exploder.getAdjustedHorizontalFacing();
                }
            }

            //Loop with and height in direction
            for (int h = -1; h < 2; h++) //TODO scale with size
            {
                for (int w = -1; w < 2; w++) //TODO scale with size
                {
                    //Reset energy per line
                    float energy = 4 * size + depth * 3;
                    //TODO convert magic numbers into defined logic
                    //TODO reduce by distance from center

                    //Loop depth
                    for (int i = 0; i < this.depth; i++)
                    {
                        Pos dir = new Pos(direction).multiply(i);  //TODO replace xyz ints
                        Pos p;
                        if (direction == EnumFacing.DOWN || direction == EnumFacing.UP)
                        {
                            p = dir.add(h, 0, w);
                        }
                        else if (direction == EnumFacing.EAST || direction == EnumFacing.WEST)
                        {
                            p = dir.add(0, h, w);
                        }
                        else if (direction == EnumFacing.NORTH || direction == EnumFacing.SOUTH)
                        {
                            p = dir.add(w, h, 0);
                        }
                        else
                        {
                            return;
                        }

                        //Translate by center
                        p = new Pos((IPos3D) this).add(p); //TODO replace with BlockPos

                        //Get block
                        IBlockState state = p.getBlockState(world());
                        Block block = state.getBlock();
                        if (!block.isAir(state, world(), p.toBlockPos()))
                        {
                            float e = block.getExplosionResistance(world(), p.toBlockPos(), this.exploder, this);
                            if (e < 40)
                            {
                                energy -= e;
                                getAffectedBlockPositions().add(p.toBlockPos());
                            }
                        }
                        if (energy <= 0)
                        {
                            break;
                        }
                    }
                }
            }

            //Play some audio
            this.world().playSound(null, location.x(), location.y(), location.z(), SoundEvents.ENTITY_GENERIC_EXPLODE, SoundCategory.BLOCKS, 5.0F, (1.0F + (world().rand.nextFloat() - world().rand.nextFloat()) * 0.2F) * 0.7F);
        }
    }
}

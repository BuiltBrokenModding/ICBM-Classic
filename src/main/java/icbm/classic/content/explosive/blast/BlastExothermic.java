package icbm.classic.content.explosive.blast;

import com.builtbroken.mc.imp.transform.vector.Location;
import icbm.classic.Settings;
import icbm.classic.client.ICBMSounds;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class BlastExothermic extends BlastBeam
{
    public BlastExothermic(World world, Entity entity, double x, double y, double z, float size)
    {
        super(world, entity, x, y, z, size);
        this.red = 0.7f;
        this.green = 0.3f;
        this.blue = 0;
    }

    @Override
    public void doExplode()
    {
        super.doExplode();
        ICBMSounds.BEAM_CHARGING.play(world, position.x(), position.y(), position.z(), 4.0F, 0.8F, true);
    }

    @Override
    public void doPostExplode()
    {
        super.doPostExplode();

        if (!this.oldWorld().isRemote)
        {
            ICBMSounds.POWER_DOWN.play(world, position.x(), position.y(), position.z(), 4.0F, 0.8F, true);

            if (this.canFocusBeam(this.oldWorld(), position) && this.thread.isComplete)
            {
                for (BlockPos targetPosition : this.thread.results)
                {
                    double distanceFromCenter = position.distance(targetPosition);

                    if (distanceFromCenter > this.getRadius())
                    {
                        continue;
                    }

                    /*
                     * Reduce the chance of setting blocks on fire based on distance from center.
                     */
                    double chance = this.getRadius() - (Math.random() * distanceFromCenter);

                    if (chance > distanceFromCenter * 0.55)
                    {
                        /*
                         * Check to see if the block is an air block and there is a block below it
                         * to support the fire.
                         */
                        IBlockState blockState = world.getBlockState(targetPosition);
                        Block block = blockState.getBlock();

                        if (blockState.getMaterial() == Material.WATER || block == Blocks.ICE)
                        {
                            this.oldWorld().setBlockToAir(targetPosition);
                        }

                        if (blockState.getMaterial() == Material.ROCK && this.oldWorld().rand.nextFloat() > 0.8)
                        {
                            this.oldWorld().setBlockState(targetPosition, Blocks.FLOWING_LAVA.getDefaultState(), 3);
                        }

                        if ((block.isReplaceable(oldWorld(), targetPosition))
                                && Blocks.FIRE.canPlaceBlockAt(oldWorld(), targetPosition))
                        {
                            if (this.oldWorld().rand.nextFloat() > 0.99)
                            {
                                this.oldWorld().setBlockState(targetPosition, Blocks.FLOWING_LAVA.getDefaultState(), 3);
                            }
                            else
                            {
                                this.oldWorld().setBlockState(targetPosition, Blocks.FIRE.getDefaultState(), 3);

                                blockState = this.oldWorld().getBlockState(targetPosition.down());
                                block = blockState.getBlock();

                                if (Settings.EXOTHERMIC_CREATE_NETHER_RACK && (block == Blocks.STONE || block == Blocks.GRASS || block == Blocks.DIRT) && this.oldWorld().rand.nextFloat() > 0.75)
                                {
                                    this.oldWorld().setBlockState(targetPosition.down(), Blocks.NETHERRACK.getDefaultState(), 3);
                                }
                            }
                        }
                    }
                }

                ICBMSounds.EXPLOSION_FIRE.play(world,position.x() + 0.5D, position.y() + 0.5D, position.z() + 0.5D, 6.0F, (1.0F + (oldWorld().rand.nextFloat() - oldWorld().rand.nextFloat()) * 0.2F) * 1F, true);
            }

            if(!oldWorld().getGameRules().getBoolean("doDaylightCycle")) //TODO add config
            {
                this.oldWorld().setWorldTime(18000);
            }
        }
    }

    @Override
    public boolean canFocusBeam(World worldObj, Location position)
    {
        return true;
    }
}

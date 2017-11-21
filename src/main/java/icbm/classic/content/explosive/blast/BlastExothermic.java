package icbm.classic.content.explosive.blast;

import com.builtbroken.mc.imp.transform.vector.Location;
import com.builtbroken.mc.imp.transform.vector.Pos;
import icbm.classic.ICBMClassic;
import icbm.classic.content.explosive.Explosives;
import icbm.classic.content.explosive.ex.ExExothermic;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
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
        this.oldWorld().playSoundEffect(position.x(), position.y(), position.z(), ICBMClassic.PREFIX + "beamcharging", 4.0F, 0.8F);
    }

    @Override
    public void doPostExplode()
    {
        super.doPostExplode();

        if (!this.oldWorld().isRemote)
        {
            this.oldWorld().playSoundEffect(position.x(), position.y(), position.z(), ICBMClassic.PREFIX + "powerdown", 4.0F, 0.8F);

            if (this.canFocusBeam(this.oldWorld(), position) && this.thread.isComplete)
            {
                for (Pos targetPosition : this.thread.results)
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
                        Block blockID = this.oldWorld().getBlock(targetPosition.xi(), targetPosition.yi(), targetPosition.zi());

                        if (blockID.getMaterial() == Material.water || blockID == Blocks.ice)
                        {
                            this.oldWorld().setBlockToAir(targetPosition.xi(), targetPosition.yi(), targetPosition.zi());
                        }

                        if (blockID.blockMaterial == Material.rock && this.oldWorld().rand.nextFloat() > 0.8)
                        {
                            this.oldWorld().setBlock(targetPosition.xi(), targetPosition.yi(), targetPosition.zi(), Blocks.flowing_lava, 0, 2);
                        }

                        if ((blockID.isReplaceable(oldWorld(), targetPosition.xi(), targetPosition.yi(), targetPosition.zi()))
                                && Blocks.fire.canPlaceBlockAt(oldWorld(), targetPosition.xi(), targetPosition.yi(), targetPosition.zi()))
                        {
                            if (this.oldWorld().rand.nextFloat() > 0.99)
                            {
                                this.oldWorld().setBlock(targetPosition.xi(), targetPosition.yi(), targetPosition.zi(), Blocks.flowing_lava, 0, 2);
                            }
                            else
                            {
                                this.oldWorld().setBlock(targetPosition.xi(), targetPosition.yi(), targetPosition.zi(), Blocks.fire, 0, 2);

                                blockID = this.oldWorld().getBlock(targetPosition.xi(), targetPosition.yi() - 1, targetPosition.zi());

                                if (((ExExothermic) Explosives.EXOTHERMIC.handler).createNetherrack && (blockID == Blocks.stone || blockID == Blocks.grass || blockID == Blocks.dirt) && this.oldWorld().rand.nextFloat() > 0.75)
                                {
                                    this.oldWorld().setBlock(targetPosition.xi(), targetPosition.yi() - 1, targetPosition.zi(), Blocks.netherrack, 0, 2);
                                }
                            }
                        }
                    }
                }

                this.oldWorld().playSoundEffect(position.x() + 0.5D, position.y() + 0.5D, position.z() + 0.5D, ICBMClassic.PREFIX + "explosionfire", 6.0F, (1.0F + (oldWorld().rand.nextFloat() - oldWorld().rand.nextFloat()) * 0.2F) * 1F);
            }

            if(!oldWorld().getGameRules().getGameRuleBooleanValue("doDaylightCycle"))
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

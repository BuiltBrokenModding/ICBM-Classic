package icbm.classic.content.explosive.blast;

import com.builtbroken.mc.lib.transform.vector.Location;
import com.builtbroken.mc.lib.transform.vector.Pos;
import icbm.classic.ICBMClassic;
import icbm.classic.content.explosive.Explosives;
import icbm.classic.content.explosive.ex.ExExothermic;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

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
        this.world().playSoundEffect(position.x(), position.y(), position.z(), ICBMClassic.PREFIX + "beamcharging", 4.0F, 0.8F);
    }

    @Override
    public void doPostExplode()
    {
        super.doPostExplode();

        if (!this.world().isRemote)
        {
            this.world().playSoundEffect(position.x(), position.y(), position.z(), ICBMClassic.PREFIX + "powerdown", 4.0F, 0.8F);

            if (this.canFocusBeam(this.world(), position) && this.thread.isComplete)
            {
                for (Pos targetPosition : this.thread.results)
                {
                    double distance = targetPosition.distance(position);
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
                        Block blockID = this.world().getBlock(targetPosition.xi(), targetPosition.yi(), targetPosition.zi());

                        if (blockID.getMaterial() == Material.water || blockID == Blocks.ice)
                        {
                            this.world().setBlockToAir(targetPosition.xi(), targetPosition.yi(), targetPosition.zi());
                        }

                        if(blockID.blockMaterial == Material.rock && this.world().rand.nextFloat() > 0.8)
                        {
                            this.world().setBlock(targetPosition.xi(), targetPosition.yi(), targetPosition.zi(), Blocks.flowing_lava, 0, 2);
                        }

                        Block blockBellow = world().getBlock(targetPosition.xi(), targetPosition.yi() - 1, targetPosition.zi());

                        if ((blockID.isReplaceable(world(), targetPosition.xi(), targetPosition.yi(), targetPosition.zi())) && blockBellow.getMaterial().isSolid() && blockBellow.isSideSolid(world(), targetPosition.xi(), targetPosition.yi() - 1, targetPosition.zi(), ForgeDirection.UP))
                        {
                            if (this.world().rand.nextFloat() > 0.99)
                            {
                                this.world().setBlock(targetPosition.xi(), targetPosition.yi(), targetPosition.zi(), Blocks.flowing_lava, 0, 2);
                            }
                            else
                            {
                                this.world().setBlock(targetPosition.xi(), targetPosition.yi(), targetPosition.zi(), Blocks.fire, 0, 2);

                                blockID = this.world().getBlock(targetPosition.xi(), targetPosition.yi() - 1, targetPosition.zi());

                                if (((ExExothermic) Explosives.EXOTHERMIC.handler).createNetherrack && (blockID == Blocks.stone || blockID == Blocks.grass || blockID == Blocks.dirt) && this.world().rand.nextFloat() > 0.75)
                                {
                                    this.world().setBlock(targetPosition.xi(), targetPosition.yi() - 1, targetPosition.zi(), Blocks.netherrack, 0, 2);
                                }
                            }
                        }
                    }
                }

                this.world().playSoundEffect(position.x() + 0.5D, position.y() + 0.5D, position.z() + 0.5D, ICBMClassic.PREFIX + "explosionfire", 6.0F, (1.0F + (world().rand.nextFloat() - world().rand.nextFloat()) * 0.2F) * 1F);
            }

            this.world().setWorldTime(18000);
        }
    }

    @Override
    public boolean canFocusBeam(World worldObj, Location position)
    {
        long worldTime = worldObj.getWorldTime();

        while (worldTime > 23999)
        {
            worldTime -= 23999;
        }

        return worldTime < 12000 && !worldObj.isRaining() && super.canFocusBeam(worldObj, position);
    }

}

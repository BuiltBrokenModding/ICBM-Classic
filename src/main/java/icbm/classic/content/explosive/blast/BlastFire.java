package icbm.classic.content.explosive.blast;

import com.builtbroken.mc.imp.transform.vector.Pos;
import icbm.classic.ICBMClassic;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.world.World;

public class BlastFire extends Blast
{
    public BlastFire(World world, Entity entity, double x, double y, double z, float size)
    {
        super(world, entity, x, y, z, size);
    }

    @Override
    public void doExplode()
    {
        if (!this.oldWorld().isRemote)
        {
            int radius = (int) this.getRadius();

            for (int x = 0; x < radius; ++x)
            {
                for (int y = 0; y < radius; ++y)
                {
                    for (int z = 0; z < radius; ++z)
                    {
                        if (x == 0 || x == radius - 1 || y == 0 || y == radius - 1 || z == 0 || z == radius - 1)
                        {
                            double xStep = x / (radius - 1.0F) * 2.0F - 1.0F;
                            double yStep = y / (radius - 1.0F) * 2.0F - 1.0F;
                            double zStep = z / (radius - 1.0F) * 2.0F - 1.0F;
                            double diagonalDistance = Math.sqrt(xStep * xStep + yStep * yStep + zStep * zStep);
                            xStep /= diagonalDistance;
                            yStep /= diagonalDistance;
                            zStep /= diagonalDistance;
                            float var14 = radius * (0.7F + oldWorld().rand.nextFloat() * 0.6F);
                            double var15 = position.x();
                            double var17 = position.y();
                            double var19 = position.z();

                            for (float var21 = 0.3F; var14 > 0.0F; var14 -= var21 * 0.75F)
                            {
                                Pos targetPosition = new Pos(var15, var17, var19);
                                double distanceFromCenter = position.distance(targetPosition);
                                Block var25 = oldWorld().getBlock(targetPosition.xi(), targetPosition.yi(), targetPosition.zi());

                                if (var25 != Blocks.air)
                                {
                                    var14 -= (var25.getExplosionResistance(this.exploder, oldWorld(), targetPosition.xi(), targetPosition.yi(), targetPosition.zi(), position.xi(), position.yi(), position.zi()) + 0.3F) * var21;
                                }

                                if (var14 > 0.0F)
                                {
                                    // Set fire by chance and distance
                                    double chance = radius - (Math.random() * distanceFromCenter);

                                    if (chance > distanceFromCenter * 0.55)
                                    {
                                        /*
                                         * Check to see if the block is an air block and there is a
                                         * block below it to support the fire.
                                         */
                                        Block block = oldWorld().getBlock((int) targetPosition.x(), (int) targetPosition.y(), (int) targetPosition.z());

                                        boolean canReplace = block.isReplaceable(oldWorld(), (int) targetPosition.x(), (int) targetPosition.y(), (int) targetPosition.z())
                                                || block.isAir(oldWorld(), (int) targetPosition.x(), (int) targetPosition.y(), (int) targetPosition.z());

                                        if (canReplace && Blocks.fire.canPlaceBlockAt(oldWorld(), (int) targetPosition.x(), (int) targetPosition.y(), (int) targetPosition.z()))
                                        {
                                            targetPosition.setBlock(oldWorld(), Blocks.fire);
                                        }
                                        else if (block == Blocks.ice)
                                        {
                                            targetPosition.setBlockToAir(oldWorld());
                                        }
                                    }
                                }

                                var15 += xStep * var21;
                                var17 += yStep * var21;
                                var19 += zStep * var21;
                            }
                        }
                    }
                }
            }
        }

        oldWorld().playSoundEffect(position.x() + 0.5D, position.y() + 0.5D, position.z() + 0.5D, ICBMClassic.PREFIX + "explosionfire", 4.0F, (1.0F + (oldWorld().rand.nextFloat() - oldWorld().rand.nextFloat()) * 0.2F) * 1F);
    }

    @Override
    public long getEnergy()
    {
        return 3000;
    }
}

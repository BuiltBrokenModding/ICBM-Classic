package icbm.classic.content.explosive.blast;

import com.builtbroken.mc.imp.transform.region.Cube;
import com.builtbroken.mc.imp.transform.vector.Pos;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;

public class BlastTNT extends Blast
{
    public static final int rays = 16;
    public static float power = 10F;

    protected List<Pos> blownBlocks = new ArrayList<Pos>();

    /** 0- No push, 1 - Attract, 2 - Repel */
    private int pushType = 0;
    private boolean destroyItem = false;

    public BlastTNT(World world, Entity entity, double x, double y, double z, float size)
    {
        super(world, entity, x, y, z, size);
    }

    public BlastTNT setPushType(int type)
    {
        this.pushType = type;
        return this;
    }

    public BlastTNT setDestroyItems()
    {
        this.destroyItem = true;
        return this;
    }

    @Override
    public void doExplode()
    {
        calculateDamage();
        this.oldWorld().playSoundEffect(this.position.x(), this.position.y(), this.position.z(), "random.explode", 4.0F, (1.0F + (this.oldWorld().rand.nextFloat() - this.oldWorld().rand.nextFloat()) * 0.2F) * 0.7F);

        switch (this.pushType)
        {
            case 0:
                this.doDamageEntities(this.getRadius(), power, this.destroyItem);
                break;
            default:
                this.pushEntities(12, this.getRadius() * 4, this.pushType);
                break;
        }

        doDestroyBlocks();
    }

    protected void calculateDamage()
    {
        if (!this.oldWorld().isRemote)
        {
            for (int x = 0; x < this.rays; ++x)
            {
                for (int y = 0; y < this.rays; ++y)
                {
                    for (int z = 0; z < this.rays; ++z)
                    {
                        if (x == 0 || x == this.rays - 1 || y == 0 || y == this.rays - 1 || z == 0 || z == this.rays - 1)
                        {
                            //Delta distance
                            double xStep = x / (this.rays - 1.0F) * 2.0F - 1.0F;
                            double yStep = y / (this.rays - 1.0F) * 2.0F - 1.0F;
                            double zStep = z / (this.rays - 1.0F) * 2.0F - 1.0F;

                            //Distance
                            double diagonalDistance = Math.sqrt(xStep * xStep + yStep * yStep + zStep * zStep);

                            //normalize
                            xStep /= diagonalDistance;
                            yStep /= diagonalDistance;
                            zStep /= diagonalDistance;


                            float radialEnergy = this.getRadius() * (0.7F + this.oldWorld().rand.nextFloat() * 0.6F);

                            double var15 = this.position.x();
                            double var17 = this.position.y();
                            double var19 = this.position.z();

                            for (float var21 = 0.3F; radialEnergy > 0.0F; radialEnergy -= var21 * 0.75F)
                            {
                                //Get block
                                int var22 = MathHelper.floor_double(var15);
                                int var23 = MathHelper.floor_double(var17);
                                int var24 = MathHelper.floor_double(var19);
                                Block var25 = this.oldWorld().getBlock(var22, var23,  var24);

                                //Get resistance
                                if (var25 != Blocks.air)
                                {
                                    radialEnergy -= (var25.getExplosionResistance(this.exploder, this.oldWorld(), var22, var23, var24, this.position.xi(), this.position.yi(), this.position.zi()) + 0.3F) * var21;
                                }

                                if (radialEnergy > 0.0F)
                                {
                                    Pos pos = new Pos(var22, var23, var24);
                                    if(!blownBlocks.contains(pos))
                                    {
                                        blownBlocks.add(pos);
                                    }
                                }

                                //Iterate location
                                var15 += xStep * var21;
                                var17 += yStep * var21;
                                var19 += zStep * var21;
                            }
                        }
                    }
                }
            }

        }
    }

    protected void doDestroyBlocks()
    {
        if (!this.oldWorld().isRemote)
        {
            int var3;
            Pos blownPosition;
            int var5;
            int var6;
            int var7;
            Block block;
            int metadata;

            for (var3 = blownBlocks.size() - 1; var3 >= 0; --var3)
            {
                blownPosition = blownBlocks.get(var3);
                var5 = blownPosition.xi();
                var6 = blownPosition.yi();
                var7 = blownPosition.zi();
                block = this.oldWorld().getBlock(var5, var6, var7);
                metadata = this.oldWorld().getBlockMetadata(var5, var6, var7);

                double var9 = (var5 + this.oldWorld().rand.nextFloat());
                double var11 = (var6 + this.oldWorld().rand.nextFloat());
                double var13 = (var7 + this.oldWorld().rand.nextFloat());
                double var151 = var9 - this.position.y();
                double var171 = var11 - this.position.y();
                double var191 = var13 - this.position.z();
                double var211 = MathHelper.sqrt_double(var151 * var151 + var171 * var171 + var191 * var191);
                var151 /= var211;
                var171 /= var211;
                var191 /= var211;
                double var23 = 0.5D / (var211 / this.getRadius() + 0.1D);
                var23 *= (this.oldWorld().rand.nextFloat() * this.oldWorld().rand.nextFloat() + 0.3F);
                var151 *= var23;
                var171 *= var23;
                var191 *= var23;
                this.oldWorld().spawnParticle("explode", (var9 + this.position.x() * 1.0D) / 2.0D, (var11 + this.position.y() * 1.0D) / 2.0D, (var13 + this.position.z() * 1.0D) / 2.0D, var151, var171, var191);
                this.oldWorld().spawnParticle("smoke", var9, var11, var13, var151, var171, var191);

                if (block != Blocks.air)
                {
                    try
                    {

                        if (block.canDropFromExplosion(null))
                        {
                            block.dropBlockAsItemWithChance(this.oldWorld(), var5, var6, var7, this.oldWorld().getBlockMetadata(var5, var6, var7), 1F, 0);
                        }

                        block.onBlockExploded(this.oldWorld(), var5, var6, var7, this);
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    public void pushEntities(float radius, float force, int type)
    {
        // Step 2: Damage all entities
        Pos minCoord = position.toPos();
        minCoord = minCoord.add(-radius - 1);
        Pos maxCoord = position.toPos();
        maxCoord = maxCoord.add(radius + 1);

        Cube region = new Cube(minCoord, maxCoord);
        List<Entity> entities = region.getEntities(this.oldWorld(), Entity.class);

        for (Entity entity : entities)
        {
            double var13 = entity.getDistance(position.x(), position.y(), position.z()) / radius;

            if (var13 <= 1.0D)
            {
                double xDifference = entity.posX - position.x();
                double yDifference = entity.posY - position.y();
                double zDifference = entity.posZ - position.z();
                double distance = MathHelper.sqrt_double(xDifference * xDifference + yDifference * yDifference + zDifference * zDifference);
                xDifference /= distance;
                yDifference /= distance;
                zDifference /= distance;

                if (type == 1)
                {
                    double modifier = var13 * force * (entity instanceof EntityPlayer ? 0.5 : 1);
                    entity.addVelocity(-xDifference * modifier, -yDifference * modifier, -zDifference * modifier);
                }
                else if (type == 2)
                {
                    double modifier = (1.0D - var13) * force * (entity instanceof EntityPlayer ? 0.5 : 1);
                    entity.addVelocity(xDifference * modifier, yDifference * modifier, zDifference * modifier);
                }
            }
        }
    }

    @Override
    public long getEnergy()
    {
        return 418000;
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt)
    {
        super.readFromNBT(nbt);
        this.pushType = nbt.getInteger("pushType");
        this.destroyItem = nbt.getBoolean("destroyItem");
    }

    @Override
    public void writeToNBT(NBTTagCompound nbt)
    {
        super.writeToNBT(nbt);
        nbt.setInteger("pushType", this.pushType);
        nbt.setBoolean("destroyItem", this.destroyItem);
    }
}

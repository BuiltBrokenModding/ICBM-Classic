package icbm.classic.content.explosive.blast;

import com.builtbroken.mc.imp.transform.region.Cube;
import com.builtbroken.mc.imp.transform.rotation.EulerAngle;
import com.builtbroken.mc.imp.transform.vector.Location;
import com.builtbroken.mc.imp.transform.vector.Pos;
import icbm.classic.ICBMClassic;
import icbm.classic.content.entity.EntityExplosion;
import icbm.classic.content.entity.EntityExplosive;
import icbm.classic.content.entity.EntityFlyingBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockLiquid;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;
import net.minecraftforge.fluids.IFluidBlock;
import resonant.api.explosion.IExplosiveIgnore;

import java.util.List;

public class BlastRedmatter extends Blast
{
    public static final float NORMAL_RADIUS = 35;
    public static final float ENTITY_DESTROY_RADIUS = 6;

    private static int MAX_BLOCKS_REMOVED_PER_TICK = 10;
    public static int MAX_LIFESPAN = 36000; // 30 minutes
    public static boolean DO_DESPAWN = true;

    public static boolean doAudio = true;
    public static boolean doFlyingBlocks = true;

    private AxisAlignedBB bounds;
    private float entityRadius;

    public BlastRedmatter(World world, Entity entity, double x, double y, double z, float size)
    {
        super(world, entity, x, y, z, size);
    }

    @Override
    public void doPreExplode()
    {
        if (!this.oldWorld().isRemote)
        {
            this.oldWorld().createExplosion(this.exploder, position.x(), position.y(), position.z(), 15.0F, true);
        }
    }

    @Override
    protected void doPostExplode()
    {
        AxisAlignedBB bounds = AxisAlignedBB.getBoundingBox(this.explosionX - this.explosionSize, this.explosionY - this.explosionSize, this.explosionZ - this.explosionSize, this.explosionX + this.explosionSize, this.explosionY + this.explosionSize, this.explosionZ + this.explosionSize);
        List<?> list = this.oldWorld().getEntitiesWithinAABB(EntityExplosion.class, bounds);

        for (Object obj : list)
        {
            if (obj instanceof EntityExplosion)
            {
                EntityExplosion explosion = (EntityExplosion) obj;

                if (explosion.getBlast() == this)
                {
                    explosion.setDead();
                }
            }
        }
    }

    @Override
    public void doExplode()
    {
        if (!this.oldWorld().isRemote)
        {
            //Limit life span of the blast
            if (DO_DESPAWN && callCount >= MAX_LIFESPAN)
            {
                this.postExplode();
            }
            doDestroyBlocks();
            doEntityMovement();
            if (doAudio)
            {
                if (this.oldWorld().rand.nextInt(8) == 0)
                {
                    this.oldWorld().playSoundEffect(position.x() + (Math.random() - 0.5) * getRadius(), position.y() + (Math.random() - 0.5) * getRadius(), position.z() + (Math.random() - 0.5) * getRadius(), ICBMClassic.PREFIX + "collapse", 6.0F - this.oldWorld().rand.nextFloat(), 1.0F - this.oldWorld().rand.nextFloat() * 0.4F);
                }
                this.oldWorld().playSoundEffect(position.x(), position.y(), position.z(), ICBMClassic.PREFIX + "redmatter", 3.0F, (1.0F + (this.oldWorld().rand.nextFloat() - this.oldWorld().rand.nextFloat()) * 0.2F) * 1F);
            }
        }
    }

    protected void doDestroyBlocks()
    {
        // Try to find and grab some blocks to orbit
        int blocksDestroyed = 0;
        for (int currentRadius = 1; currentRadius < getRadius(); currentRadius++) //TODO recode as it can stall the main thread
        {
            for (int xr = -currentRadius; xr < currentRadius; xr++)
            {
                for (int yr = -currentRadius; yr < currentRadius; yr++)
                {
                    for (int zr = -currentRadius; zr < currentRadius; zr++)
                    {
                        final Location currentPos = position.add(xr, yr, zr);
                        final double dist = position.distance(currentPos);

                        //We are looping in a shell orbit around the center
                        if (dist < currentRadius && dist > currentRadius - 2)
                        {
                            final Block block = currentPos.getBlock();
                            //Null if call was made on an unloaded chunk
                            if (block != null)
                            {
                                final boolean isFluid = block instanceof BlockLiquid || block instanceof IFluidBlock;
                                int meta = currentPos.getBlockMetadata();
                                //Ignore air blocks and unbreakable blocks
                                if (!block.isAir(oldWorld(), currentPos.xi(), currentPos.yi(), currentPos.zi()) && (isFluid || block.getBlockHardness(this.oldWorld(), currentPos.xi(), currentPos.yi(), currentPos.zi()) >= 0))
                                {
                                    //TODO handle multi-blocks

                                    currentPos.setBlock(Blocks.air, 0, isFluid ? 2 : 3);
                                    //TODO: render fluid streams moving into hole
                                    if (!isFluid && doFlyingBlocks)
                                    {
                                        //Convert a random amount of destroyed blocks into flying blocks for visuals
                                        if (this.oldWorld().rand.nextFloat() > 0.8)
                                        {
                                            EntityFlyingBlock entity = new EntityFlyingBlock(this.oldWorld(), currentPos.add(0.5D), block, meta);
                                            entity.yawChange = 50 * this.oldWorld().rand.nextFloat();
                                            entity.pitchChange = 50 * this.oldWorld().rand.nextFloat();
                                            this.oldWorld().spawnEntityInWorld(entity);
                                        }
                                    }
                                    //Keep track of blocks removed to keep from lagging the game
                                    blocksDestroyed++;
                                }
                            }
                        }

                        //Exit conditions, make sure stays at bottom of loop
                        if (blocksDestroyed > this.MAX_BLOCKS_REMOVED_PER_TICK || !isAlive)
                        {
                            return;
                        }
                    }
                }
            }
        }
    }

    protected void doEntityMovement()
    {
        float entityRadius = this.getRadius() * 2;
        Cube cube = new Cube(position.add(0.5).sub(entityRadius), position.add(0.5).add(entityRadius));
        AxisAlignedBB bounds = AxisAlignedBB.getBoundingBox(cube.min().x(), cube.min().y(), cube.min().z(), cube.max().x(), cube.max().y(), cube.max().z());
        List<Entity> allEntities = this.oldWorld().getEntitiesWithinAABB(Entity.class, bounds);
        boolean doExplosion = true;
        for (Entity entity : allEntities)
        {
            doExplosion = !this.affectEntity(entityRadius, entity, doExplosion);
        }
    }

    /**
     * Makes an entity get affected by Red Matter.
     *
     * @Return True if explosion happened
     */
    public boolean affectEntity(float radius, Entity entity, boolean doExplosion)
    {
        //Ignore players that are in creative mode or can't be harmed
        if (entity instanceof EntityPlayer && (((EntityPlayer) entity).capabilities.isCreativeMode || ((EntityPlayer) entity).capabilities.disableDamage))
        {
            return false;
        }

        //Ignore self
        if (entity == this.controller)
        {
            return false;
        }

        //Ignore entities that mark themselves are ignorable
        if (entity instanceof IExplosiveIgnore)
        {
            if (((IExplosiveIgnore) entity).canIgnore(this))
            {
                return false;
            }
        }

        //Calculate different from center
        double xDifference = entity.posX - position.xi() + 0.5;
        double yDifference = entity.posY - position.yi() + 0.5;
        double zDifference = entity.posZ - position.zi() + 0.5;

        /** The percentage of the closeness of the entity. */
        double xPercentage = 1 - (xDifference / radius);
        double yPercentage = 1 - (yDifference / radius);
        double zPercentage = 1 - (zDifference / radius);
        double distancePercentage = this.position.distance(entity) / radius;

        Pos entityPosition = new Pos(entity);
        Pos centeredPosition = entityPosition.subtract(this.position);
        centeredPosition = (Pos) centeredPosition.transform(new EulerAngle(1.5 * distancePercentage * Math.random(), 1.5 * distancePercentage * Math.random(), 1.5 * distancePercentage * Math.random()));

        Location newPosition = this.position.add(centeredPosition);
        // Orbit Velocity
        entity.addVelocity(newPosition.x() - entityPosition.x(), 0, newPosition.z() - entityPosition.z());
        // Gravity Velocity (0.015 is barely enough to overcome y gravity so do not lower)
        entity.addVelocity(-xDifference * 0.015 * xPercentage, -yDifference * 0.015 * yPercentage, -zDifference * 0.015 * zPercentage);

        boolean explosionCreated = false;

        if (new Pos(entity.posX, entity.posY, entity.posZ).distance(position) < (ENTITY_DESTROY_RADIUS * (getRadius() / NORMAL_RADIUS)))
        {
            if (entity instanceof EntityExplosion)
            {
                if (((EntityExplosion) entity).getBlast() instanceof BlastAntimatter)
                {
                    if (doAudio)
                    {
                        this.oldWorld().playSoundEffect(position.x(), position.y(), position.z(), ICBMClassic.PREFIX + "explosion", 7.0F, (1.0F + (this.oldWorld().rand.nextFloat() - this.oldWorld().rand.nextFloat()) * 0.2F) * 0.7F);
                    }
                    if (this.oldWorld().rand.nextFloat() > 0.85 && !this.oldWorld().isRemote)
                    {
                        entity.setDead();
                        return explosionCreated;
                    }
                }
                else if (((EntityExplosion) entity).getBlast() instanceof BlastRedmatter)
                {
                    //https://www.wolframalpha.com/input/?i=(4%2F3)pi+*+r%5E3+%3D+(4%2F3)pi+*+a%5E3+%2B+(4%2F3)pi+*+b%5E3

                    //We are going to merge both blasts together
                    double sizeA = this.getRadius();
                    sizeA = sizeA * sizeA * sizeA;
                    double sizeB = ((EntityExplosion) entity).getBlast().getRadius();
                    sizeB = sizeB * sizeB * sizeB;
                    float radiusNew = (float) Math.cbrt(sizeA + sizeB);

                    //Average out timer
                    this.callCount = (callCount + ((EntityExplosion) entity).getBlast().callCount) / 2;

                    //Destroy current instance
                    this.isAlive = false;
                    this.controller.setDead();

                    //Create new to avoid doing packet syncing
                    new BlastRedmatter(oldWorld(), entity, position.x(), position.y(), position.z(), radiusNew).explode();
                }
                //Kill explosion entity
                ((EntityExplosion) entity).getBlast().isAlive = false;
                //Kill entity in the center of the ball
                entity.setDead();
            }
            else if (entity instanceof EntityExplosive)
            {
                ((EntityExplosive) entity).explode();
            }
            else if(entity instanceof EntityLiving)
            {
                entity.attackEntityFrom(new DamageSourceRedmatter(this), 2000);
            }
            else
            {
                //Kill entity in the center of the ball
                entity.setDead();
            }
        }

        return explosionCreated;
    }

    public static class DamageSourceRedmatter extends DamageSource
    {
        public final BlastRedmatter blastRedmatter;

        public DamageSourceRedmatter(BlastRedmatter blastRedmatter)
        {
            super("icbm.redmatter");
            this.blastRedmatter = blastRedmatter;
        }
    }

    /**
     * The interval in ticks before the next procedural call of this explosive
     *
     * @return - Return -1 if this explosive does not need proceudral calls
     */
    @Override
    public int proceduralInterval()
    {
        return 1;
    }

    @Override
    public long getEnergy()
    {
        return -3000;
    }

    @Override
    public boolean isMovable()
    {
        return true;
    }
}

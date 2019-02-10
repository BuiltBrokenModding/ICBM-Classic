package icbm.classic.content.blast;

import icbm.classic.ICBMClassic;
import icbm.classic.client.ICBMSounds;
import icbm.classic.content.potion.CustomPotionEffect;
import icbm.classic.lib.transform.vector.Pos;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.MobEffects;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.AxisAlignedBB;

import java.util.List;

public class BlastChemical extends Blast //TODO recode to separate out sub types
{
    public static final int PARTICLES_TO_SPAWN = 200; //TODO maybe add a config?
    public static final int TICKS_BETWEEN_RUNS = 5;

    private int duration;
    /** Color of particles */
    private float red = 1, green = 1, blue = 1;
    private boolean playShortSoundFX;
    private boolean isContagious, isPoisonous, isConfuse, isMutate;

    public BlastChemical(int duration, boolean playShortSoundFX)
    {
        this.duration = duration / this.proceduralInterval();
        this.playShortSoundFX = playShortSoundFX;
    }

    public BlastChemical setRGB(float r, float g, float b)
    {
        this.red = r;
        this.green = g;
        this.blue = b;
        return this;
    }

    public BlastChemical setConfuse()
    {
        this.isConfuse = true;
        return this;
    }

    public BlastChemical setPoison()
    {
        this.isPoisonous = true;
        return this;
    }

    public BlastChemical setContagious()
    {
        this.isContagious = true;
        this.isMutate = true;
        return this;
    }

    @Override
    public void doPreExplode()
    {
        super.doPreExplode();
        if (!this.playShortSoundFX)
        {
            ICBMSounds.DEBILITATION.play(world, this.location.x(), this.location.y(), this.location.z(), 4.0F, (1.0F + (world().rand.nextFloat() - world().rand.nextFloat()) * 0.2F) * 0.7F, true);
        }
    }

    @Override
    public void doExplode()
    {
        final float radius = this.getBlastRadius();

        //Trigger effects for user feedback
        generateGraphicEffect();
        generateAudioEffect();

        //Only run potion effect application for the following types
        if (isContagious || isPoisonous || isConfuse)
        {
            //Get bounding box for effect
            AxisAlignedBB bounds = new AxisAlignedBB(
                    location.x() - radius, location.y() - radius, location.z() - radius,
                    location.x() + radius, location.y() + radius, location.z() + radius);
            //TODO scale affect area with time, the graphics do not match the logic
            //TODO cache box, we do not need to recreate it each tick

            //Get all living entities
            List<EntityLivingBase> allEntities = world().getEntitiesWithinAABB(EntityLivingBase.class, bounds);

            //Loop all entities
            for (EntityLivingBase entity : allEntities)
            {
                if (this.isContagious)
                {
                    ICBMClassic.contagios_potion.poisonEntity(location.toPos(), entity);
                }

                if (this.isPoisonous)
                {
                    ICBMClassic.poisonous_potion.poisonEntity(location.toPos(), entity);
                }

                if (this.isConfuse)
                {
                    entity.addPotionEffect(new CustomPotionEffect(MobEffects.POISON, 18 * 20, 0));
                    entity.addPotionEffect(new CustomPotionEffect(MobEffects.MINING_FATIGUE, 20 * 60, 0));
                    entity.addPotionEffect(new CustomPotionEffect(MobEffects.SLOWNESS, 20 * 60, 2));
                }
            }
        }

        //Trigger secondary blast
        if (this.isMutate) //TODO why?
        {
            new BlastMutation()
                    .setBlastWorld(world())
                    .setBlastSource(this.exploder)
                    .setBlastPosition(location.x(), location.y(), location.z())
                    .setBlastSize(radius)
                    .buildBlast().runBlast(); //TODO trigger from explosive handler
        }

        //End explosion when we hit life timer
        if (this.callCount > this.duration)
        {
            this.isAlive = false;
        }
    }

    protected void generateAudioEffect()
    {
        if (this.playShortSoundFX)
        {
            ICBMSounds.GAS_LEAK.play(world, location.x() + 0.5D, location.y() + 0.5D, location.z() + 0.5D,
                    4.0F, (1.0F + (world().rand.nextFloat() - world().rand.nextFloat()) * 0.2F) * 1F, true);
        }
    }

    protected void generateGraphicEffect()
    {
        if (this.world().isRemote)
        {
            final float radius = this.getBlastRadius();
            for (int i = 0; i < PARTICLES_TO_SPAWN; i++)
            {
                //Get random spawn point (generates a random point in a box area)
                Pos randomSpawnPoint = new Pos(
                        Math.random() * radius / 2 - radius / 4,
                        Math.random() * radius / 2 - radius / 4,
                        Math.random() * radius / 2 - radius / 4);

                //Scale random by time alive
                randomSpawnPoint = randomSpawnPoint.multiply(Math.min(radius, callCount) / 10);

                //Ensure point is inside radius
                if (randomSpawnPoint.magnitude() <= radius)
                {
                    //Offset by our center
                    randomSpawnPoint = randomSpawnPoint.add(this.location);

                    //Call to spawn TODO maybe build a list of points, then spawn all at once?
                    ICBMClassic.proxy.spawnSmoke(world, randomSpawnPoint,
                            (Math.random() - 0.5) / 2, (Math.random() - 0.5) / 2, (Math.random() - 0.5) / 2,
                            this.red, this.green, this.blue,
                            7.0F, 100);
                }
            }
        }
    }

    @Override
    public int proceduralInterval()
    {
        return TICKS_BETWEEN_RUNS;
    }

    @Override
    public void load(NBTTagCompound nbt)
    {
        super.load(nbt);
        this.duration = nbt.getInteger("duration");
        this.isContagious = nbt.getBoolean("isContagious");
        this.isPoisonous = nbt.getBoolean("isPoisonous");
        this.isConfuse = nbt.getBoolean("isConfuse");
        this.isMutate = nbt.getBoolean("isMutate");
        this.red = nbt.getFloat("red");
        this.green = nbt.getFloat("green");
        this.blue = nbt.getFloat("blue");
        this.playShortSoundFX = nbt.getBoolean("playShortSoundFX");
    }

    @Override
    public void save(NBTTagCompound nbt)
    {
        super.save(nbt);
        nbt.setInteger("duration", this.duration);
        nbt.setBoolean("isContagious", this.isContagious);
        nbt.setBoolean("isPoisonous", this.isPoisonous);
        nbt.setBoolean("isConfuse", this.isConfuse);
        nbt.setBoolean("isMutate", this.isMutate);
        nbt.setFloat("red", this.red);
        nbt.setFloat("green", this.green);
        nbt.setFloat("blue", this.blue);
        nbt.setBoolean("playShortSoundFX", this.playShortSoundFX);
    }
}

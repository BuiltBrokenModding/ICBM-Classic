package icbm.classic.content.blast;

import icbm.classic.ICBMClassic;
import icbm.classic.api.NBTConstants;
import icbm.classic.api.explosion.IBlastTickable;
import icbm.classic.client.ICBMSounds;
import icbm.classic.content.potion.CustomPotionEffect;
import icbm.classic.lib.transform.vector.Pos;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.MobEffects;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.AxisAlignedBB;

import java.util.List;

public class BlastChemical extends Blast implements IBlastTickable //TODO recode to separate out sub types
{
    public static final int PARTICLES_TO_SPAWN = 200; //TODO maybe add a config?
    public static final int TICKS_BETWEEN_RUNS = 5;

    private int duration;
    /** Color of particles */
    private float red = 1, green = 1, blue = 1;
    private boolean playShortSoundFX;
    private boolean isContagious, isPoisonous, isConfuse, isMutate;
    private AxisAlignedBB bounds;

    public BlastChemical(int duration, boolean playShortSoundFX)
    {
        this.duration = duration;
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
    public Blast setPosition(double posX, double posY, double posZ)
    {
        final Blast returnValue = super.setPosition(posX, posY, posZ);
        setEffectBounds(); //only change effect bounding box when position changes
        return returnValue;
    }

    @Override
    public void setupBlast()
    {
        super.setupBlast();
    }

    @Override
    public boolean doExplode(int callCount)
    {
        //Play start audio
        if (callCount == 0 && !this.playShortSoundFX)
        {
            ICBMSounds.DEBILITATION.play(world, this.location.x(), this.location.y(), this.location.z(), 4.0F, (1.0F + (world().rand.nextFloat() - world().rand.nextFloat()) * 0.2F) * 0.7F, true);
        }

        //Do gas effect
        if (callCount % TICKS_BETWEEN_RUNS == 0)
        {
            //Trigger effects for user feedback
            generateGraphicEffect();
            generateAudioEffect();

            //Only run potion effect application for the following types
            if (isContagious || isPoisonous || isConfuse)
            {
                //TODO scale affect area with time, the graphics do not match the logic

                if (bounds == null) //just to be sure
                {
                    setEffectBounds();
                }

                //Get all living entities
                List<EntityLivingBase> allEntities = world().getEntitiesWithinAABB(EntityLivingBase.class, bounds);

                //Loop all entities
                for (EntityLivingBase entity : allEntities)
                {
                    if (!(entity instanceof EntityPlayer) || !((EntityPlayer) entity).isCreative())
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
            }

            //Trigger secondary blast which mutates mobs similar to a lightning strike
            if (this.isMutate)
            {
                new BlastMutation()
                        .setBlastWorld(world())
                        .setBlastSource(this.exploder)
                        .setBlastPosition(location.x(), location.y(), location.z())
                        .setBlastSize(getBlastRadius())
                        .buildBlast().runBlast(); //TODO trigger from explosive handler
            }

            //End explosion when we hit life timer
            return this.callCount > this.duration;
        }

        return false;
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

    private void setEffectBounds()
    {
        final float radius = this.getBlastRadius();
        bounds = new AxisAlignedBB(
                location.x() - radius, location.y() - radius, location.z() - radius,
                location.x() + radius, location.y() + radius, location.z() + radius);
    }

    @Override
    public void load(NBTTagCompound nbt)
    {
        super.load(nbt);
        this.duration = nbt.getInteger(NBTConstants.DURATION);
        this.isContagious = nbt.getBoolean(NBTConstants.IS_CONTAGIOUS);
        this.isPoisonous = nbt.getBoolean(NBTConstants.IS_POISONOUS);
        this.isConfuse = nbt.getBoolean(NBTConstants.IS_CONFUSE);
        this.isMutate = nbt.getBoolean(NBTConstants.IS_MUTATE);
        this.red = nbt.getFloat(NBTConstants.RED);
        this.green = nbt.getFloat(NBTConstants.GREEN);
        this.blue = nbt.getFloat(NBTConstants.BLUE);
        this.playShortSoundFX = nbt.getBoolean(NBTConstants.PLAY_SHORT_SOUND_FX);
    }

    @Override
    public void save(NBTTagCompound nbt)
    {
        super.save(nbt);
        nbt.setInteger(NBTConstants.DURATION, this.duration);
        nbt.setBoolean(NBTConstants.IS_CONTAGIOUS, this.isContagious);
        nbt.setBoolean(NBTConstants.IS_POISONOUS, this.isPoisonous);
        nbt.setBoolean(NBTConstants.IS_CONFUSE, this.isConfuse);
        nbt.setBoolean(NBTConstants.IS_MUTATE, this.isMutate);
        nbt.setFloat(NBTConstants.RED, this.red);
        nbt.setFloat(NBTConstants.GREEN, this.green);
        nbt.setFloat(NBTConstants.BLUE, this.blue);
        nbt.setBoolean(NBTConstants.PLAY_SHORT_SOUND_FX, this.playShortSoundFX);
    }
}

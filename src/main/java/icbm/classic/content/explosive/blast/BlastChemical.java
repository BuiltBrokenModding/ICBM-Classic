package icbm.classic.content.explosive.blast;

import icbm.classic.lib.transform.vector.Pos;
import icbm.classic.ICBMClassic;
import icbm.classic.client.ICBMSounds;
import icbm.classic.content.potion.CustomPotionEffect;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.MobEffects;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.World;

import java.util.List;

public class BlastChemical extends Blast
{
    private static final int CHECK_BAN_JING = 16;
    private static final float NENG_LIANG = 10F;
    private int duration;
    /** Color of particles */
    private float red = 1, green = 1, blue = 1;
    private boolean playShortSoundFX;
    private boolean isContagious, isPoisonous, isConfuse, isMutate;

    public BlastChemical(World world, Entity entity, double x, double y, double z, float size)
    {
        super(world, entity, x, y, z, size);
    }

    public BlastChemical(World world, Entity entity, double x, double y, double z, float size, int duration, boolean playShortSoundFX)
    {
        this(world, entity, x, y, z, size);
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
            ICBMSounds.DEBILITATION.play(world, this.position.x(), this.position.y(), this.position.z(), 4.0F, (1.0F + (world().rand.nextFloat() - world().rand.nextFloat()) * 0.2F) * 0.7F, true);
        }
    }

    @Override
    public void doExplode()
    {
        float radius = this.getBlastRadius();

        if (this.world().isRemote)
        {
            for (int i = 0; i < 200; i++)
            {
                Pos diDian = new Pos(Math.random() * radius / 2 - radius / 4, Math.random() * radius / 2 - radius / 4, Math.random() * radius / 2 - radius / 4);
                diDian = diDian.multiply(Math.min(radius, callCount) / 10);

                if (diDian.magnitude() <= radius)
                {
                    diDian = diDian.add(this.position);
                    //ICBMClassic.proxy.spawnParticle("smoke", this.oldWorld(), diDian, (Math.random() - 0.5) / 2, (Math.random() - 0.5) / 2, (Math.random() - 0.5) / 2, this.red, this.green, this.blue, 7.0F, 8);
                }
            }
        }

        AxisAlignedBB bounds = new AxisAlignedBB(position.x() - radius, position.y() - radius, position.z() - radius, position.x() + radius, position.y() + radius, position.z() + radius);
        List<EntityLivingBase> allEntities = world().getEntitiesWithinAABB(EntityLivingBase.class, bounds);

        for (EntityLivingBase entity : allEntities)
        {
            if (this.isContagious)
            {
                ICBMClassic.contagios_potion.poisonEntity(position.toPos(), entity);
            }

            if (this.isPoisonous)
            {
                ICBMClassic.poisonous_potion.poisonEntity(position.toPos(), entity);
            }

            if (this.isConfuse)
            {
                entity.addPotionEffect(new CustomPotionEffect(MobEffects.POISON, 18 * 20, 0));
                entity.addPotionEffect(new CustomPotionEffect(MobEffects.MINING_FATIGUE, 20 * 60, 0));
                entity.addPotionEffect(new CustomPotionEffect(MobEffects.SLOWNESS, 20 * 60, 2));
            }
        }

        if (this.isMutate)
        {
            new BlastMutation(world(), this.exploder, position.x(), position.y(), position.z(), this.getBlastRadius()).explode();
        }

        if (this.playShortSoundFX)
        {
            ICBMSounds.GAS_LEAK.play(world, position.x() + 0.5D, position.y() + 0.5D, position.z() + 0.5D, 4.0F, (1.0F + (world().rand.nextFloat() - world().rand.nextFloat()) * 0.2F) * 1F, true);
        }

        if (this.callCount > this.duration)
        {
            this.controller.endExplosion();
        }
    }

    @Override
    public long getEnergy()
    {
        return 20;
    }

    /** The interval in ticks before the next procedural call of this explosive
     *
     * @return - Return -1 if this explosive does not need proceudral calls */
    @Override
    public int proceduralInterval()
    {
        return 5;
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt)
    {
        super.readFromNBT(nbt);
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
    public void writeToNBT(NBTTagCompound nbt)
    {
        super.writeToNBT(nbt);
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

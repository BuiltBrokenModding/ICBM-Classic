package icbm.classic.content.blast.gas;

import icbm.classic.content.potion.CustomPotionEffect;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.MobEffects;
import net.minecraft.util.math.Vec3i;

/**
 * Created by Robin Seifert on 4/1/2022.
 */
public class BlastConfusion extends BlastGasBase
{
    public static final int DURATION = 20 * 30; //TODO move to config

    public static final float red = 1f, green = 1f, blue = 1f;

    public BlastConfusion()
    {
        super(DURATION, false);
    }

    @Override
    protected boolean canEffectEntities()
    {
        return true;
    }

    @Override
    protected void applyEffect(final EntityLivingBase entity, final int hitCount)
    {
        entity.addPotionEffect(new CustomPotionEffect(MobEffects.POISON, 18 * 20, 0)); //TODO scale
        entity.addPotionEffect(new CustomPotionEffect(MobEffects.MINING_FATIGUE, 20 * 60, 0));
        entity.addPotionEffect(new CustomPotionEffect(MobEffects.SLOWNESS, 20 * 60, 2));
    }

    @Override
    protected float getParticleColorRed(final Vec3i pos)
    {
        return red;
    }

    @Override
    protected float getParticleColorGreen(final Vec3i pos)
    {
        return green;
    }

    @Override
    protected float getParticleColorBlue(final Vec3i pos)
    {
        return blue;
    }
}

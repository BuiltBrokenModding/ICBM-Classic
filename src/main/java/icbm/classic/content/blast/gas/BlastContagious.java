package icbm.classic.content.blast.gas;

import icbm.classic.ICBMClassic;
import icbm.classic.content.blast.BlastMutation;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.Vec3i;

/**
 * Created by Robin Seifert on 4/1/2022.
 */
public class BlastContagious extends BlastGasBase
{
    public static final DamageSource CONTAGIOUS_DAMAGE = new DamageSource("icbm.contagious");
    public static final int DURATION = 20 * 30; //TODO move to config

    public static final float red = 0.3f, green = 0.8f, blue = 0;

    public BlastContagious()
    {
        super(DURATION, false);
    }

    @Override
    protected boolean canEffectEntities()
    {
        return true;
    }

    @Override
    protected boolean canGasEffect(EntityLivingBase entity)
    {
        return super.canGasEffect(entity) && !entity.isEntityInvulnerable(CONTAGIOUS_DAMAGE);
    }

    @Override
    protected void applyEffect(final EntityLivingBase entity, final int hitCount)
    {
        ICBMClassic.contagiousPotion.poisonEntity(location.toPos(), entity, 3); //TODO scale

        //Apply damage to non-mutated entities if toxin level is high enough
        if (!BlastMutation.applyMutationEffect(entity) && hitCount > 10)
        {
            entity.attackEntityFrom(CONTAGIOUS_DAMAGE, (hitCount - 10f) / 5);
        }
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

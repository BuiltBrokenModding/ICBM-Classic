package icbm.classic.content.blast.gas;

import icbm.classic.ICBMClassic;
import icbm.classic.config.ConfigMain;
import icbm.classic.content.blast.BlastMutation;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.Vec3i;

/**
 * Created by Robin Seifert on 4/1/2022.
 */
public class BlastContagious extends BlastGasBase
{
    public static final DamageSource CONTAGIOUS_DAMAGE = new DamageSource("icbm.contagious");
    public static final float red = 0.3f, green = 0.8f, blue = 0;

    @Setter @Accessors(chain = true)
    private int toxicityBuildup = 10; // tick rate is 5
    @Setter @Accessors(chain = true)
    private float toxicityScale = 0.05f;
    @Setter @Accessors(chain = true)
    private float toxicityMinDamage = 1f;

    @Override
    protected boolean canEffectEntities()
    {
        return true;
    }

    @Override
    protected boolean canGasEffect(LivingEntity entity)
    {
        return super.canGasEffect(entity) && !entity.isInvulnerableTo(CONTAGIOUS_DAMAGE);
    }

    @Override
    protected float minGasProtection() {
        return ConfigMain.protectiveArmor.minProtectionViralGas;
    }

    @Override
    protected void applyEffect(final LivingEntity entity, final int hitCount)
    {
        ICBMClassic.contagiousPotion.poisonEntity(getPosition(), entity, 3);

        //Apply damage to non-mutated entities if toxin level is high enough
        if (!BlastMutation.applyMutationEffect(entity) && hitCount > toxicityBuildup)
        {
            entity.attackEntityFrom(CONTAGIOUS_DAMAGE,  Math.max(toxicityMinDamage, hitCount * toxicityScale));
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

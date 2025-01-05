package icbm.classic.content.blast.gas;

import icbm.classic.ICBMClassic;
import icbm.classic.config.ConfigMain;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.Vec3i;

public class BlastChemical extends BlastGasBase
{
    public static final DamageSource CHEMICAL_DAMAGE = new DamageSource("icbm.chemical");

    public static final float red = 0.8f, green = 0.8f, blue = 0;

    @Setter @Accessors(chain = true)
    private int toxicityBuildup = 20; // tick rate is 5
    @Setter @Accessors(chain = true)
    private float toxicityScale = 0.1f;
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
        return super.canGasEffect(entity) && !entity.isInvulnerableTo(CHEMICAL_DAMAGE);
    }

    @Override
    protected float minGasProtection() {
        return ConfigMain.protectiveArmor.minProtectionChemicalGas;
    }

    @Override
    protected void applyEffect(final LivingEntity entity, final int hitCount)
    {
        ICBMClassic.chemicalPotion.poisonEntity(getPosition(), entity);
        if (hitCount > toxicityBuildup)
        {
            // TODO https://builtbroken.codecks.io/decks/14-icbm-backlog/card/1aq-rework-gas-weapons
            entity.attackEntityFrom(CHEMICAL_DAMAGE, Math.max(toxicityMinDamage, hitCount * toxicityScale));
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

package icbm.classic.content.blast.gas;

import icbm.classic.ICBMClassic;
import icbm.classic.config.ConfigMain;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.minecraft.world.entity.LivingEntity;
import icbm.classic.content.blast.ICBMDamageTypes;
import net.minecraft.core.Vec3i;

public class BlastChemical extends BlastGasBase
{
    // PORT: 1.12 static CHEMICAL_DAMAGE source is now data driven, see ICBMDamageTypes.CHEMICAL

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
        return super.canGasEffect(entity) && !entity.isInvulnerableTo(ICBMDamageTypes.source(world, ICBMDamageTypes.CHEMICAL));
    }

    @Override
    protected float minGasProtection() {
        return ConfigMain.protectiveArmor.minProtectionChemicalGas;
    }

    @Override
    protected void applyEffect(final LivingEntity entity, final int hitCount)
    {
        ICBMClassic.chemicalPotion.get().poisonEntity(location.toPos(), entity);
        if (hitCount > toxicityBuildup)
        {
            // TODO https://builtbroken.codecks.io/decks/14-icbm-backlog/card/1aq-rework-gas-weapons
            entity.hurt(ICBMDamageTypes.source(world, ICBMDamageTypes.CHEMICAL), Math.max(toxicityMinDamage, hitCount * toxicityScale));
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
// PORTED
